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
package org.akraino.validation.ui.client.jenkins;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;

import javax.annotation.Nonnull;
import javax.ws.rs.core.MultivaluedMap;

import org.akraino.validation.ui.client.jenkins.resources.CrumbResponse;
import org.akraino.validation.ui.client.jenkins.resources.Parameter;
import org.akraino.validation.ui.client.jenkins.resources.Parameters;
import org.akraino.validation.ui.client.jenkins.resources.QueueJobItem;
import org.apache.commons.httpclient.HttpException;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.springframework.stereotype.Service;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.urlconnection.HttpURLConnectionFactory;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;

@Service
public final class JenkinsExecutorClient {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(JenkinsExecutorClient.class);

    private static final Object LOCK = new Object();
    private final Client client;

    private final String user;
    private final String password;
    private final String baseurl;

    public JenkinsExecutorClient() {
        this.baseurl = System.getenv("JENKINS_URL");
        this.user = System.getenv("JENKINS_USERNAME");
        this.password = System.getenv("JENKINS_USER_PASSWORD");
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        this.client = new Client(new URLConnectionClientHandler(new HttpURLConnectionFactory() {
            Proxy proxy = null;

            @Override
            public HttpURLConnection getHttpURLConnection(URL url) throws IOException {
                try {
                    String proxyIp = System.getenv("JENKINS_PROXY").substring(0,
                            System.getenv("JENKINS_PROXY").lastIndexOf(":"));
                    String proxyPort = System.getenv("JENKINS_PROXY")
                            .substring(System.getenv("JENKINS_PROXY").lastIndexOf(":") + 1);
                    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIp, Integer.parseInt(proxyPort)));
                    return (HttpURLConnection) url.openConnection(proxy);
                } catch (Exception ex) {
                    return (HttpURLConnection) url.openConnection();
                }
            }
        }), clientConfig);
        this.client.addFilter(new HTTPBasicAuthFilter(user, password));
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
            LOGGER.info(EELFLoggerDelegate.applicationLogger, "Trying to get a Jenkins resource");
            String crumb = this.getCrumb();
            LOGGER.debug(EELFLoggerDelegate.debugLogger, "Jenkins crumb is: " + crumb);
            WebResource webResource = this.client.resource(queueJobItemUrl + "/api/json");
            LOGGER.debug(EELFLoggerDelegate.debugLogger, "Request URI of get: " + webResource.getURI().toString());
            WebResource.Builder builder = webResource.getRequestBuilder();
            builder.header("Jenkins-Crumb", crumb);
            ClientResponse response = builder.accept("application/json").type("application/json")
                    .get(ClientResponse.class);
            if (response.getStatus() != 200) {
                throw new HttpException("Get on Jenkins failed. HTTP error code : " + response.getStatus()
                + " and message: " + response.getEntity(String.class));
            }
            LOGGER.info(EELFLoggerDelegate.applicationLogger, "Get of Jenkins resource succeeded");
            return response.getEntity(QueueJobItem.class);
        }
    }

    public URL postJobWithQueryParams(@Nonnull String jobName, @Nonnull Parameters parameters)
            throws HttpException, ClientHandlerException, UniformInterfaceException, MalformedURLException,
            KeyManagementException, NoSuchAlgorithmException {
        synchronized (LOCK) {
            LOGGER.info(EELFLoggerDelegate.applicationLogger, "Trying to trigger a job in Jenkins");
            String crumb = this.getCrumb();
            LOGGER.debug(EELFLoggerDelegate.debugLogger, "Jenkins crumb is: " + crumb);
            String queryParams = "?";
            for (Parameter parameter : parameters.getParameter()) {
                queryParams = queryParams + parameter.getName() + "=" + parameter.getValue() + "&";
            }
            queryParams = queryParams.substring(0, queryParams.length() - 1);
            WebResource webResource = this.client
                    .resource(this.getBaseUrl() + "/job/" + jobName + "/buildWithParameters" + queryParams);
            LOGGER.debug(EELFLoggerDelegate.debugLogger, "Request URI of post: " + webResource.getURI().toString());
            WebResource.Builder builder = webResource.getRequestBuilder();
            builder.header("Jenkins-Crumb", crumb);
            ClientResponse response = builder.type("application/json").post(ClientResponse.class, String.class);
            if (response.getStatus() != 200 && response.getStatus() != 201) {
                throw new HttpException("Post of Jenkins job failed. HTTP error code : " + response.getStatus()
                + " and message: " + response.getEntity(String.class));
            }
            LOGGER.info(EELFLoggerDelegate.applicationLogger, "Jenkins job has been successfully triggered");
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
        LOGGER.info(EELFLoggerDelegate.applicationLogger, "Attempting to get the crumb");
        String crumbUri = baseurl + "/crumbIssuer/api/json";
        WebResource webResource = this.client.resource(crumbUri);
        ClientResponse response = webResource.accept("application/json").type("application/json")
                .get(ClientResponse.class);
        if (response.getStatus() == 201 || response.getStatus() == 200) {
            CrumbResponse crumbResponse = response.getEntity(CrumbResponse.class);
            LOGGER.info(EELFLoggerDelegate.applicationLogger, "Successful crumb retrieval");
            return crumbResponse.getCrumb();
        }
        throw new HttpException("Get crumb attempt towards Jenkins failed. HTTP error code: " + response.getStatus()
        + " and message: " + response.getEntity(String.class));
    }

}
