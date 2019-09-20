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
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

import javax.annotation.Nonnull;

import org.akraino.validation.ui.client.nexus.resources.RobotTestResult;
import org.akraino.validation.ui.client.nexus.resources.Status;
import org.akraino.validation.ui.client.nexus.resources.TestInfoYaml;
import org.akraino.validation.ui.client.nexus.resources.WRobotNexusTestResult;
import org.akraino.validation.ui.entity.Blueprint;
import org.akraino.validation.ui.entity.BlueprintInstance;
import org.akraino.validation.ui.entity.LabInfo;
import org.akraino.validation.ui.entity.ValidationDbTestResult;
import org.akraino.validation.ui.entity.WRobotDbTestResult;
import org.akraino.validation.ui.service.DbAdapter;
import org.apache.commons.httpclient.HttpException;
import org.json.JSONObject;
import org.json.XML;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.onap.portalsdk.core.onboarding.util.PortalApiProperties;
import org.onap.portalsdk.core.web.support.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.client.urlconnection.HttpURLConnectionFactory;
import com.sun.jersey.client.urlconnection.URLConnectionClientHandler;

@Service
public final class NexusExecutorClient {

    @Autowired
    DbAdapter dbAdapter;

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(NexusExecutorClient.class);

    private final Client client;
    private final String baseurl;

    public NexusExecutorClient() {
        this.baseurl = PortalApiProperties.getProperty("nexus_url");
        ClientConfig clientConfig = new DefaultClientConfig();
        clientConfig.getFeatures().put(JSONConfiguration.FEATURE_POJO_MAPPING, Boolean.TRUE);
        this.client = new Client(new URLConnectionClientHandler(new HttpURLConnectionFactory() {
            Proxy proxy = null;

            @Override
            public HttpURLConnection getHttpURLConnection(URL url) throws IOException {
                try {
                    String proxyIp = System.getenv("NEXUS_PROXY").substring(0,
                            System.getenv("NEXUS_PROXY").lastIndexOf(":"));
                    String proxyPort = System.getenv("NEXUS_PROXY")
                            .substring(System.getenv("NEXUS_PROXY").lastIndexOf(":") + 1);
                    proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyIp, Integer.parseInt(proxyPort)));
                    return (HttpURLConnection) url.openConnection(proxy);
                } catch (Exception ex) {
                    return (HttpURLConnection) url.openConnection();
                }
            }
        }), clientConfig);
    }

    public String getBaseUrl() {
        return this.baseurl;
    }

    public List<String> getResource(String endpoint)
            throws ClientHandlerException, UniformInterfaceException, JsonParseException, JsonMappingException,
            IOException, KeyManagementException, NoSuchAlgorithmException, ParseException {
        List<String> resources = new ArrayList<String>();
        String nexusUrl = this.baseurl;
        if (endpoint != null) {
            nexusUrl = this.baseurl + "/" + endpoint;
        }
        LOGGER.info(EELFLoggerDelegate.applicationLogger, "Trying to get nexus resource: " + nexusUrl);
        WebResource webResource = this.client.resource(nexusUrl + "/");
        LOGGER.debug(EELFLoggerDelegate.debugLogger, "Request URI of get: " + webResource.getURI().toString());
        ClientResponse response = webResource.get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new HttpException("Could not retrieve nexus resource " + nexusUrl + ". HTTP error code : "
                    + response.getStatus() + " and message: " + response.getEntity(String.class));
        }
        Document document = Jsoup.parse(response.getEntity(String.class));
        List<Element> elements = document.getElementsByTag("body").get(0).getElementsByTag("table").get(0)
                .getElementsByTag("tbody").get(0).getElementsByTag("tr");
        for (int i = 2; i < elements.size(); i++) {
            String resource = elements.get(i).getElementsByTag("td").get(0).getElementsByTag("a").get(0).text();
            resource = resource.substring(0, resource.length() - 1);
            resources.add(resource);
        }
        return resources;
    }

    public List<String> getResource(@Nonnull String endpoint1, @Nonnull String endpoint2)
            throws ClientHandlerException, UniformInterfaceException, JsonParseException, JsonMappingException,
            IOException, KeyManagementException, NoSuchAlgorithmException, ParseException {
        String endpoint = endpoint1 + "/" + endpoint2;
        return this.getResource(endpoint);
    }

    public List<String> getResource(@Nonnull String endpoint1, @Nonnull String endpoint2, @Nonnull String endpoint3)
            throws ClientHandlerException, UniformInterfaceException, JsonParseException, JsonMappingException,
            IOException, KeyManagementException, NoSuchAlgorithmException, ParseException {
        String endpoint = endpoint1 + "/" + endpoint2 + "/" + endpoint3;
        return this.getResource(endpoint);
    }

    public ValidationDbTestResult getResult(@Nonnull String name, @Nonnull String version, @Nonnull String siloText,
            @Nonnull String timestamp)
                    throws ClientHandlerException, UniformInterfaceException, JsonParseException, JsonMappingException,
                    IOException, KeyManagementException, NoSuchAlgorithmException, ParseException, NullPointerException {
        String nexusUrl = this.baseurl + "/" + siloText + "/" + "bluval_results/" + name + "/" + version;
        LOGGER.info(EELFLoggerDelegate.applicationLogger, "Trying to get validation nexus test result");
        WebResource webResource = this.client.resource(nexusUrl + "/");
        LOGGER.debug(EELFLoggerDelegate.debugLogger, "Request URI of get: " + webResource.getURI().toString());
        ClientResponse response = webResource.get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new HttpException("Could not retrieve validation nexus test result. HTTP error code : "
                    + response.getStatus() + " and message: " + response.getEntity(String.class));
        }
        Document document = Jsoup.parse(response.getEntity(String.class));
        List<Element> elements = document.getElementsByTag("body").get(0).getElementsByTag("table").get(0)
                .getElementsByTag("tbody").get(0).getElementsByTag("tr");
        Element element = findElementByTimestamp(elements.subList(2, elements.size()), timestamp);
        if (element == null) {
            return null;
        }
        ValidationDbTestResult vDbResult = new ValidationDbTestResult();
        Blueprint blueprint = new Blueprint();
        blueprint.setBlueprintName(name);
        BlueprintInstance blueInst = new BlueprintInstance();
        blueInst.setBlueprint(blueprint);
        blueInst.setVersion(version);
        vDbResult.setBlueprintInstance(blueInst);
        LabInfo lab = new LabInfo();
        lab.setSilo(siloText);
        vDbResult.setLab(lab);
        vDbResult.setTimestamp(timestamp);
        String lastModified = element.getElementsByTag("td").get(1).text();
        vDbResult.setDateStorage(lastModified);
        TestInfoYaml testInfo = getTestInfo(webResource.getURI().toString() + timestamp);
        if (testInfo != null) {
            if (testInfo.gettest_info().getLayer().equals("all")) {
                vDbResult.setAllLayers(true);
            } else {
                vDbResult.setAllLayers(false);
            }
            vDbResult.setOptional(testInfo.gettest_info().getOptional());
        }
        List<WRobotNexusTestResult> wTestResults = getWRobotTestResults(name, version, siloText, timestamp);
        if (wTestResults.size() < 1) {
            throw new RuntimeException("No robot test results could be obtained.");
        }
        vDbResult.setResult(determineResult(wTestResults));
        List<WRobotDbTestResult> wDbResults = new ArrayList<WRobotDbTestResult>();
        for (WRobotNexusTestResult wTestResult : wTestResults) {
            WRobotDbTestResult wDbResult = new WRobotDbTestResult();
            wDbResult.setLayer(wTestResult.getLayer());
            ObjectMapper mapper = new ObjectMapper();
            wDbResult.setRobotTestResults(mapper.writeValueAsString(wTestResult.getRobotNexusTestResults()));
            wDbResults.add(wDbResult);
        }
        vDbResult.setWRobotDbTestResults(new HashSet<WRobotDbTestResult>(wDbResults));
        return vDbResult;
    }

    public List<ValidationDbTestResult> getResults(@Nonnull String name, @Nonnull String version,
            @Nonnull String siloText, int noOfLastElements)
                    throws ClientHandlerException, UniformInterfaceException, JsonParseException, JsonMappingException,
                    IOException, KeyManagementException, NoSuchAlgorithmException, ParseException {
        String nexusUrl = this.baseurl + "/" + siloText + "/" + "bluval_results/" + name + "/" + version;
        LOGGER.info(EELFLoggerDelegate.applicationLogger, "Trying to get validation Nexus test results");
        WebResource webResource = this.client.resource(nexusUrl + "/");
        LOGGER.debug(EELFLoggerDelegate.debugLogger, "Request URI of get: " + webResource.getURI().toString());
        ClientResponse response = webResource.get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new HttpException("Could not retrieve validation Nexus test results. HTTP error code : "
                    + response.getStatus() + " and message: " + response.getEntity(String.class));
        }
        List<ValidationDbTestResult> vDbResults = new ArrayList<ValidationDbTestResult>();
        Document document = Jsoup.parse(response.getEntity(String.class));
        List<Element> elements = document.getElementsByTag("body").get(0).getElementsByTag("table").get(0)
                .getElementsByTag("tbody").get(0).getElementsByTag("tr");
        elements = findLastElementsByDate(elements.subList(2, elements.size()), noOfLastElements);
        for (int i = 0; i < elements.size(); i++) {
            String timestamp = null;
            try {
                timestamp = elements.get(i).getElementsByTag("td").get(0).getElementsByTag("a").get(0).text();
                timestamp = timestamp.substring(0, timestamp.length() - 1);
                ValidationDbTestResult vDbResult = dbAdapter.getValidationTestResult(siloText, timestamp);
                if (vDbResult == null || vDbResult.getDateStorage() == null) {
                    vDbResults.add(this.getResult(name, version, siloText, timestamp));
                } else {
                    // Just to avoid deletion of already received validation timestamp results
                    vDbResult = new ValidationDbTestResult();
                    Blueprint blueprint = new Blueprint();
                    blueprint.setBlueprintName(name);
                    BlueprintInstance blueInst = new BlueprintInstance();
                    blueInst.setBlueprint(blueprint);
                    blueInst.setVersion(version);
                    vDbResult.setBlueprintInstance(blueInst);
                    LabInfo lab = new LabInfo();
                    lab.setSilo(siloText);
                    vDbResult.setLab(lab);
                    vDbResult.setTimestamp(timestamp);
                    vDbResults.add(vDbResult);
                }
            } catch (IllegalArgumentException | HttpException | NullPointerException | NoSuchElementException ex) {
                LOGGER.warn(EELFLoggerDelegate.auditLogger, "Exception occured while retrieving timestamp : "
                        + timestamp + " result." + UserUtils.getStackTrace(ex));
                continue;
            }
        }
        return vDbResults;
    }

    public List<ValidationDbTestResult> getResults(@Nonnull String name, @Nonnull String version,
            @Nonnull String siloText, @Nonnull Date date)
                    throws ClientHandlerException, UniformInterfaceException, JsonParseException, JsonMappingException,
                    IOException, KeyManagementException, NoSuchAlgorithmException, ParseException, NullPointerException {
        String nexusUrl = this.baseurl + "/" + siloText + "/" + "bluval_results/" + name + "/" + version;
        LOGGER.debug(EELFLoggerDelegate.applicationLogger, "Trying to get validation Nexus results based on date");
        WebResource webResource = this.client.resource(nexusUrl + "/");
        LOGGER.debug(EELFLoggerDelegate.debugLogger, "Request URI of get: " + webResource.getURI().toString());
        ClientResponse response = webResource.get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new HttpException("Could not retrieve validation Nexus results based on date. HTTP error code : "
                    + response.getStatus() + " and message: " + response.getEntity(String.class));
        }
        List<ValidationDbTestResult> vDbResults = new ArrayList<ValidationDbTestResult>();
        Document document = Jsoup.parse(response.getEntity(String.class));
        List<Element> elements = document.getElementsByTag("body").get(0).getElementsByTag("table").get(0)
                .getElementsByTag("tbody").get(0).getElementsByTag("tr");
        elements = findElementsBasedOnDate(elements.subList(2, elements.size()), date);
        for (int i = 0; i < elements.size(); i++) {
            try {
                String timestamp = elements.get(i).getElementsByTag("td").get(0).getElementsByTag("a").get(0).text();
                timestamp = timestamp.substring(0, timestamp.length() - 1);
                ValidationDbTestResult vDbResult = this.getResult(name, version, siloText, timestamp);
                vDbResults.add(vDbResult);
            } catch (IllegalArgumentException | HttpException | NullPointerException ex) {
                LOGGER.warn(EELFLoggerDelegate.auditLogger,
                        "Exception occured while retrieving timestamp results. " + UserUtils.getStackTrace(ex));
                continue;
            }
        }
        return vDbResults;
    }

    public ValidationDbTestResult getLastResultBasedOnOutcome(@Nonnull String name, @Nonnull String version,
            @Nonnull String siloText, List<String> layers, Boolean optional, boolean outcome)
                    throws ClientHandlerException, UniformInterfaceException, JsonParseException, JsonMappingException,
                    IOException, KeyManagementException, NoSuchAlgorithmException, ParseException, NullPointerException {
        String nexusUrl = this.baseurl + "/" + siloText + "/" + "bluval_results/" + name + "/" + version;
        LOGGER.info(EELFLoggerDelegate.applicationLogger, "Trying to get last result based on outcome");
        WebResource webResource = this.client.resource(nexusUrl + "/");
        LOGGER.debug(EELFLoggerDelegate.debugLogger, "Request URI of get: " + webResource.getURI().toString());
        ClientResponse response = webResource.get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new HttpException("Could not retrieve last result based on outcome from Nexus. HTTP error code : "
                    + response.getStatus() + " and message: " + response.getEntity(String.class));
        }
        Document document = Jsoup.parse(response.getEntity(String.class));
        List<Element> elements = document.getElementsByTag("body").get(0).getElementsByTag("table").get(0)
                .getElementsByTag("tbody").get(0).getElementsByTag("tr");
        elements = elements.subList(2, elements.size());
        if (elements.size() < 1) {
            return null;
        }
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Collections.sort(elements, new Comparator<Element>() {
            @Override
            public int compare(Element element1, Element element2) {
                try {
                    return dateFormat.parse(element2.getElementsByTag("td").get(1).text())
                            .compareTo(dateFormat.parse(element1.getElementsByTag("td").get(1).text()));
                } catch (ParseException e) {
                    LOGGER.error(EELFLoggerDelegate.errorLogger,
                            "Error when parsing date. " + UserUtils.getStackTrace(e));
                    return 0;
                }
            }
        });
        for (Element element : elements) {
            try {
                String elementTimestamp = element.getElementsByTag("td").get(0).getElementsByTag("a").get(0).text();
                elementTimestamp = elementTimestamp.substring(0, elementTimestamp.length() - 1);
                ValidationDbTestResult vDbResult = this.getResult(name, version, siloText, elementTimestamp);
                if (vDbResult.getResult() != outcome) {
                    continue;
                }
                if (optional != null && vDbResult.getOptional() != optional) {
                    continue;
                }
                if (layers != null) {
                    List<String> storedLayers = new ArrayList<String>();
                    for (WRobotDbTestResult wRobot : vDbResult.getWRobotDbTestResults()) {
                        storedLayers.add(wRobot.getLayer());
                    }
                    if (!new HashSet<>(storedLayers).equals(new HashSet<>(layers))) {
                        continue;
                    }
                }
                return vDbResult;
            } catch (IllegalArgumentException | HttpException | NullPointerException ex) {
                LOGGER.warn(EELFLoggerDelegate.auditLogger,
                        "Error when trying to retrieve results. " + UserUtils.getStackTrace(ex));
                continue;
            }
        }
        return null;
    }

    public ValidationDbTestResult getLastResultBasedOnOutcome(@Nonnull String name, @Nonnull String version,
            @Nonnull String siloText, Boolean allLayers, Boolean optional, boolean outcome)
                    throws ClientHandlerException, UniformInterfaceException, JsonParseException, JsonMappingException,
                    IOException, KeyManagementException, NoSuchAlgorithmException, ParseException, NullPointerException {
        String nexusUrl = this.baseurl + "/" + siloText + "/" + "bluval_results/" + name + "/" + version;
        LOGGER.info(EELFLoggerDelegate.applicationLogger, "Trying to get last result based on outcome");
        WebResource webResource = this.client.resource(nexusUrl + "/");
        LOGGER.debug(EELFLoggerDelegate.debugLogger, "Request URI of get: " + webResource.getURI().toString());
        ClientResponse response = webResource.get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new HttpException("Could not retrieve last result based on outcome from Nexus. HTTP error code : "
                    + response.getStatus() + " and message: " + response.getEntity(String.class));
        }
        Document document = Jsoup.parse(response.getEntity(String.class));
        List<Element> elements = document.getElementsByTag("body").get(0).getElementsByTag("table").get(0)
                .getElementsByTag("tbody").get(0).getElementsByTag("tr");
        elements = elements.subList(2, elements.size());
        if (elements.size() < 1) {
            return null;
        }
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Collections.sort(elements, new Comparator<Element>() {
            @Override
            public int compare(Element element1, Element element2) {
                try {
                    return dateFormat.parse(element2.getElementsByTag("td").get(1).text())
                            .compareTo(dateFormat.parse(element1.getElementsByTag("td").get(1).text()));
                } catch (ParseException e) {
                    LOGGER.error(EELFLoggerDelegate.errorLogger,
                            "Error when parsing date. " + UserUtils.getStackTrace(e));
                    return 0;
                }
            }
        });
        for (Element element : elements) {
            try {
                String elementTimestamp = element.getElementsByTag("td").get(0).getElementsByTag("a").get(0).text();
                elementTimestamp = elementTimestamp.substring(0, elementTimestamp.length() - 1);
                ValidationDbTestResult vDbResult = this.getResult(name, version, siloText, elementTimestamp);
                if (vDbResult.getResult() != outcome) {
                    continue;
                }
                if (optional != null && vDbResult.getOptional() != optional) {
                    continue;
                }
                if (allLayers != null && vDbResult.getAllLayers() != allLayers) {
                    continue;
                }
                return vDbResult;
            } catch (IllegalArgumentException | HttpException | NullPointerException ex) {
                LOGGER.warn(EELFLoggerDelegate.auditLogger,
                        "Error when trying to retrieve results. " + UserUtils.getStackTrace(ex));
                continue;
            }
        }
        return null;
    }

    public List<WRobotNexusTestResult> getWRobotTestResults(@Nonnull String name, @Nonnull String version,
            @Nonnull String siloText, @Nonnull String timestamp)
                    throws ClientHandlerException, UniformInterfaceException, JsonParseException, JsonMappingException,
                    IOException, KeyManagementException, NoSuchAlgorithmException {
        String nexusUrl = this.baseurl + "/" + siloText + "/" + "bluval_results/" + name + "/" + version + "/"
                + timestamp + "/results";
        List<WRobotNexusTestResult> listOfwrappers = new ArrayList<WRobotNexusTestResult>();
        LOGGER.info(EELFLoggerDelegate.applicationLogger, "Trying to get the blueprint layers");
        WebResource webResource = this.client.resource(nexusUrl + "/");
        LOGGER.debug(EELFLoggerDelegate.debugLogger, "Request URI of get: " + webResource.getURI().toString());
        ClientResponse response = webResource.get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new HttpException("Could not retrieve blueprint layers from Nexus. HTTP error code : "
                    + response.getStatus() + " and message: " + response.getEntity(String.class));
        }
        Document document = Jsoup.parse(response.getEntity(String.class));
        List<Element> elements = document.getElementsByTag("body").get(0).getElementsByTag("table").get(0)
                .getElementsByTag("tr");
        for (int i = 2; i < elements.size(); i++) {
            try {
                String layer = elements.get(i).getElementsByTag("td").get(0).getElementsByTag("a").get(0).text();
                layer = layer.substring(0, layer.length() - 1);
                if (layer.contains("test")) {
                    continue;
                }
                List<RobotTestResult> robotTestResults = getRobotTestResults(nexusUrl + "/" + layer);
                WRobotNexusTestResult wrapper = new WRobotNexusTestResult();
                wrapper.setLayer(layer);
                wrapper.setRobotNexusTestResults(robotTestResults);
                listOfwrappers.add(wrapper);
            } catch (IllegalArgumentException | HttpException | NullPointerException ex) {
                LOGGER.warn(EELFLoggerDelegate.auditLogger,
                        "Exception occured while retrieving robot results. " + UserUtils.getStackTrace(ex));
                continue;
            }
        }
        return listOfwrappers;
    }

    private List<RobotTestResult> getRobotTestResults(String resultsUrl)
            throws ClientHandlerException, UniformInterfaceException, JsonParseException, JsonMappingException,
            IOException, KeyManagementException, NoSuchAlgorithmException {
        List<RobotTestResult> rTestResults = new ArrayList<RobotTestResult>();
        LOGGER.info(EELFLoggerDelegate.applicationLogger, "Trying to get test suites results");
        WebResource webResource = this.client.resource(resultsUrl + "/");
        LOGGER.debug(EELFLoggerDelegate.debugLogger, "Request URI of get: " + webResource.getURI().toString());
        ClientResponse response = webResource.get(ClientResponse.class);
        if (response.getStatus() != 200) {
            throw new HttpException("Could not retrieve test suites results from Nexus. HTTP error code : "
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
                throw new HttpException("Could not retrieve test suite result from Nexus. HTTP error code : "
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
            rTestResults.add(robotTestResult);
        }
        return rTestResults;
    }

    private boolean determineResult(List<WRobotNexusTestResult> wTestResults) {
        boolean result = true;
        for (WRobotNexusTestResult wTestResult : wTestResults) {
            for (RobotTestResult robotTestResult : wTestResult.getRobotNexusTestResults()) {
                for (Status status : robotTestResult.getRobot().getStatistics().getTotal().getStat()) {
                    if (status.getContent().trim().equals("All Tests") && status.getFail() > 0) {
                        result = false;
                    }
                }
            }
        }
        return result;
    }

    private List<Element> findLastElementsByDate(List<Element> elements, int noOfLastElements) {
        if (elements.size() <= noOfLastElements) {
            return elements;
        }
        DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        Collections.sort(elements, new Comparator<Element>() {
            @Override
            public int compare(Element element1, Element element2) {
                try {
                    return dateFormat.parse(element2.getElementsByTag("td").get(1).text())
                            .compareTo(dateFormat.parse(element1.getElementsByTag("td").get(1).text()));
                } catch (ParseException e) {
                    LOGGER.error(EELFLoggerDelegate.errorLogger,
                            "Error when parsing date. " + UserUtils.getStackTrace(e));
                    return 0;
                }
            }
        });
        return elements.subList(0, noOfLastElements);
    }

    private Element findElementByTimestamp(List<Element> elements, String timestamp) {
        for (Element element : elements) {
            String elementTimestamp = element.getElementsByTag("td").get(0).getElementsByTag("a").get(0).text();
            elementTimestamp = elementTimestamp.substring(0, elementTimestamp.length() - 1);
            if (elementTimestamp.equals(timestamp)) {
                return element;
            }
        }
        return null;
    }

    private List<Element> findElementsBasedOnDate(List<Element> elements, Date date) throws ParseException {
        DateFormat formatTime = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
        DateFormat formatNoTime = new SimpleDateFormat("EEE MMM dd zzz yyyy", Locale.US);
        List<Element> desiredElements = new ArrayList<Element>();
        for (Element element : elements) {
            String lastModified = element.getElementsByTag("td").get(1).text();
            if (formatNoTime.format(formatTime.parse(lastModified)).compareTo(formatNoTime.format(date)) == 0) {
                desiredElements.add(element);
            }
        }
        return desiredElements;
    }

    private TestInfoYaml getTestInfo(String timestampUrl) throws JsonParseException, JsonMappingException, IOException {
        LOGGER.info(EELFLoggerDelegate.applicationLogger, "Trying to get test info");
        WebResource webResource = this.client.resource(timestampUrl + "/results/test_info.yaml");
        LOGGER.debug(EELFLoggerDelegate.debugLogger, "Request URI of get: " + webResource.getURI().toString());
        ClientResponse response = webResource.get(ClientResponse.class);
        if (response.getStatus() != 200) {
            return null;
        }
        String testInfo = response.getEntity(String.class);
        ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
        Object obj = yamlReader.readValue(testInfo, Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mapper.setSerializationInclusion(Include.NON_NULL);
        ObjectMapper jsonWriter = new ObjectMapper();
        return mapper.readValue(jsonWriter.writeValueAsString(obj), TestInfoYaml.class);
    }
}
