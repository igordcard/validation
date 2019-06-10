/*
 * Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.akraino.validation.ui.client.jenkins;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.core.MultivaluedMap;

import org.akraino.validation.ui.client.jenkins.resources.CrumbResponse;
import org.akraino.validation.ui.client.jenkins.resources.Parameter;
import org.akraino.validation.ui.client.jenkins.resources.Parameters;
import org.akraino.validation.ui.client.jenkins.resources.QueueJobItem;
import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

public final class JenkinsExecutorClient {

    private static final Logger LOGGER = Logger.getLogger(JenkinsExecutorClient.class);

    private static final List<JenkinsExecutorClient> JENKINS_CLIENTS = new ArrayList<>();
    private static final Object LOCK = new Object();
    private final Client client;

    private final String user;
    private final String password;
    private final String baseurl;

    private final HostnameVerifier hostnameVerifier;
    private final TrustManager[] trustAll;

    private JenkinsExecutorClient(String newUser, String newPassword, String newBaseurl) {
        this.user = newUser;
        this.password = newPassword;
        this.baseurl = newBaseurl;
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        this.client = Client.create(clientConfig);
        this.client.addFilter(new HTTPBasicAuthFilter(user, password));
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

    public static synchronized JenkinsExecutorClient getInstance(@Nonnull String newUser, @Nonnull String newPassword,
            @Nonnull String newBaseurl) throws MalformedURLException {
        new URL(newBaseurl);
        for (JenkinsExecutorClient client : JENKINS_CLIENTS) {
            if (client.getBaseUrl().equals(newBaseurl) && client.getUser().equals(newUser)
                    && client.getPassword().equals(newPassword)) {
                return client;
            }
        }
        JenkinsExecutorClient client = new JenkinsExecutorClient(newUser, newPassword, newBaseurl);
        JENKINS_CLIENTS.add(client);
        return client;
    }

    public String getUser() {
        return this.user;
    }

    public String getPassword() {
        return this.password;
    }

    public String getBaseUrl() {
        return this.baseurl;
    }

    public QueueJobItem getQueueJobItem(URL queueJobItemUrl) throws HttpException, ClientHandlerException,
            UniformInterfaceException, KeyManagementException, NoSuchAlgorithmException {
        synchronized (LOCK) {
            LOGGER.info("Trying to get a Jenkins resource");
            String crumb = this.getCrumb();
            LOGGER.debug("Jenkins crumb is: " + crumb);
            WebResource webResource = this.client.resource(queueJobItemUrl + "/api/json");
            LOGGER.debug("Request URI of get: " + webResource.getURI().toString());
            WebResource.Builder builder = webResource.getRequestBuilder();
            builder.header("Jenkins-Crumb", crumb);
            ClientResponse response =
                    builder.accept("application/json").type("application/json").get(ClientResponse.class);
            if (response.getStatus() != 200) {
                throw new HttpException("Get on Jenkins failed. HTTP error code : " + response.getStatus()
                        + " and message: " + response.getEntity(String.class));
            }
            LOGGER.info("Get of Jenkins resource succeeded");
            return response.getEntity(QueueJobItem.class);
        }
    }

    /**
     *
     * @param jobName
     * @param parameters
     * @return The URL of the corresponding Jenkins queue job item
     * @throws UniformInterfaceException
     * @throws ClientHandlerException
     * @throws HttpException
     * @throws MalformedURLException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    public URL postJobWithQueryParams(@Nonnull String jobName, @Nonnull Parameters parameters)
            throws HttpException, ClientHandlerException, UniformInterfaceException, MalformedURLException,
            KeyManagementException, NoSuchAlgorithmException {
        synchronized (LOCK) {
            LOGGER.info("Trying to trigger a job to Jenkins");
            String crumb = this.getCrumb();
            LOGGER.debug("Jenkins crumb is: " + crumb);
            String queryParams = "?";
            for (Parameter parameter : parameters.getParameter()) {
                queryParams = queryParams + parameter.getName() + "=" + parameter.getValue() + "&";
            }
            queryParams = queryParams.substring(0, queryParams.length() - 1);
            WebResource webResource =
                    this.client.resource(this.getBaseUrl() + "/job/" + jobName + "/buildWithParameters" + queryParams);
            LOGGER.debug("Request URI of post: " + webResource.getURI().toString());
            WebResource.Builder builder = webResource.getRequestBuilder();
            builder.header("Jenkins-Crumb", crumb);
            ClientResponse response = builder.type("application/json").post(ClientResponse.class, String.class);
            if (response.getStatus() != 200 && response.getStatus() != 201) {
                throw new HttpException("Post of Jenkins job failed. HTTP error code : " + response.getStatus()
                        + " and message: " + response.getEntity(String.class));
            }
            LOGGER.info("Jenkins job has been successfully triggered");
            URL buildQueueUrl = null;
            MultivaluedMap<String, String> responseValues = response.getHeaders();
            Iterator<String> iter = responseValues.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                if (key.equals("Location")) {
                    buildQueueUrl = new URL(responseValues.getFirst(key));
                }
            }
            return buildQueueUrl;
        }
    }

    private String getCrumb() throws HttpException, ClientHandlerException, UniformInterfaceException,
            KeyManagementException, NoSuchAlgorithmException {
        LOGGER.info("Get crumb attempt");
        setProperties();
        String crumbUri = baseurl + "/crumbIssuer/api/json";
        WebResource webResource = this.client.resource(crumbUri);
        ClientResponse response =
                webResource.accept("application/json").type("application/json").get(ClientResponse.class);
        if (response.getStatus() == 201 || response.getStatus() == 200) {
            CrumbResponse crumbResponse = response.getEntity(CrumbResponse.class);
            LOGGER.info("Successful crumb retrieval.");
            return crumbResponse.getCrumb();
        }
        throw new HttpException("Get crumb attempt towards Jenkins failed. HTTP error code: " + response.getStatus()
                + " and message: " + response.getEntity(String.class));
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
