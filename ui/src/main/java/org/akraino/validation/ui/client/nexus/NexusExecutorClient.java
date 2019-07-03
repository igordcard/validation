/*
 * Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.akraino.validation.ui.client.nexus;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.akraino.validation.ui.client.nexus.resources.RobotTestResult;
import org.akraino.validation.ui.client.nexus.resources.WrapperRobotTestResult;
import org.akraino.validation.ui.data.BlueprintLayer;
import org.apache.commons.httpclient.HttpException;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.urlconnection.HTTPSProperties;
import com.sun.jersey.client.urlconnection.HttpURLConnectionFactory;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;

public final class NexusExecutorClient {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(NexusExecutorClient.class);

    private final Client client;
    private final String baseurl;
    private final HostnameVerifier hostnameVerifier;
    private final TrustManager[] trustAll;

    public NexusExecutorClient(String newBaseurl) {
        this.baseurl = newBaseurl;
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        this.client = new Client(new URLConnectionClientHandler(new HttpURLConnectionFactory() {
            Proxy proxy = null;

            @Override
            public HttpURLConnection getHttpURLConnection(URL url) throws IOException {
                try {
                    String proxyIp =
                            System.getenv("NEXUS_PROXY").substring(0, System.getenv("NEXUS_PROXY").lastIndexOf(":"));
                    String proxyPort =
                            System.getenv("NEXUS_PROXY").substring(System.getenv("NEXUS_PROXY").lastIndexOf(":") + 1);
                    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIp, Integer.parseInt(proxyPort)));
                    return (HttpURLConnection) url.openConnection(proxy);
                } catch (Exception ex) {
                    return (HttpURLConnection) url.openConnection();
                }
            }
        }), clientConfig);
        // Create all-trusting host name verifier
        hostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
        // Create a trust manager that does not validate certificate chains
        trustAll = new TrustManager[] {new X509TrustManager() {
            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return null; // Not relevant.
            }

            @Override
            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                // Do nothing. Just allow them all.
            }

            @Override
            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                // Do nothing. Just allow them all.
            }
        }};
    }

    public String getBaseUrl() {
        return this.baseurl;
    }

    public List<WrapperRobotTestResult> getRobotTestResults() throws ClientHandlerException, UniformInterfaceException,
            JsonParseException, JsonMappingException, IOException, KeyManagementException, NoSuchAlgorithmException {
        List<WrapperRobotTestResult> listOfwrappers = new ArrayList<WrapperRobotTestResult>();
        LOGGER.info(EELFLoggerDelegate.applicationLogger, "Trying to get the blueprint layers");
        setProperties();
        WebResource webResource = this.client.resource(this.baseurl + "/");
        LOGGER.debug(EELFLoggerDelegate.debugLogger, "Request URI of get: " + webResource.getURI().toString());
        ClientResponse response = webResource.get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new HttpException("Could not retrieve blueprint layers from Nexus. HTTP error code : "
                    + response.getStatus() + " and message: " + response.getEntity(String.class));
        }
        Document document = Jsoup.parse(response.getEntity(String.class));
        List<Element> elements =
                document.getElementsByTag("body").get(0).getElementsByTag("table").get(0).getElementsByTag("tr");
        for (int i = 2; i < elements.size(); i++) {
            String layer = elements.get(i).getElementsByTag("td").get(0).getElementsByTag("a").get(0).text();
            layer = layer.substring(0, layer.length() - 1);
            List<RobotTestResult> robotTestResults = getResultsOfALayer(this.baseurl + "/" + layer);
            WrapperRobotTestResult wrapper = new WrapperRobotTestResult();
            wrapper.setBlueprintLayer(BlueprintLayer.valueOf(layer.substring(0, 1).toUpperCase() + layer.substring(1)));
            wrapper.setRobotTestResults(robotTestResults);
            listOfwrappers.add(wrapper);
        }
        return listOfwrappers;
    }

    private List<RobotTestResult> getResultsOfALayer(String resultsUrl)
            throws ClientHandlerException, UniformInterfaceException, JsonParseException, JsonMappingException,
            IOException, KeyManagementException, NoSuchAlgorithmException {
        List<RobotTestResult> robotTestResults = new ArrayList<RobotTestResult>();
        LOGGER.info(EELFLoggerDelegate.applicationLogger, "Trying to get Robot Test Results");
        setProperties();
        WebResource webResource = this.client.resource(resultsUrl + "/");
        LOGGER.debug(EELFLoggerDelegate.debugLogger, "Request URI of get: " + webResource.getURI().toString());
        ClientResponse response = webResource.get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new HttpException("Could not retrieve robot test results from Nexus. HTTP error code : "
                    + response.getStatus() + " and message: " + response.getEntity(String.class));
        }
        Document document = Jsoup.parse(response.getEntity(String.class));
        List<Element> elements = document.getElementsByTag("body").get(0).getElementsByTag("table").get(0)
                .getElementsByTag("tbody").get(0).getElementsByTag("tr");
        for (int i = 2; i < elements.size(); i++) {
            String testSuiteName = elements.get(i).getElementsByTag("td").get(0).getElementsByTag("a").get(0).text();
            testSuiteName = testSuiteName.substring(0, testSuiteName.length() - 1);
            webResource = this.client.resource(resultsUrl + "/" + testSuiteName + "/output.xml");
            LOGGER.debug(EELFLoggerDelegate.debugLogger, "Request URI of get: " + webResource.getURI().toString());
            response = webResource.get(ClientResponse.class);
            if (response.getStatus() != 200) {
                throw new HttpException("Could not retrieve robot test result from Nexus. HTTP error code : "
                        + response.getStatus() + " and message: " + response.getEntity(String.class));
            }
            String result = response.getEntity(String.class);
            JSONObject xmlJSONObj = XML.toJSONObject(result);
            ObjectMapper mapper = new ObjectMapper();
            mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
            mapper.setSerializationInclusion(Include.NON_NULL);
            RobotTestResult robotTestResult = mapper.readValue(xmlJSONObj.toString(), RobotTestResult.class);
            robotTestResult.setName(testSuiteName);
            robotTestResults.add(robotTestResult);
        }
        return robotTestResults;
    }

    private void setProperties() throws NoSuchAlgorithmException, KeyManagementException {
        SSLContext sslContext = SSLContext.getInstance("SSL");
        sslContext.init(null, this.trustAll, new java.security.SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        // Install the all-trusting host verifier
        HttpsURLConnection.setDefaultHostnameVerifier(this.hostnameVerifier);
        DefaultClientConfig config = new DefaultClientConfig();
        Map<String, Object> properties = config.getProperties();
        HTTPSProperties httpsProperties = new HTTPSProperties((str, sslSession) -> true, sslContext);
        properties.put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, httpsProperties);
    }

}
