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
package org.akraino.validation.ui.service;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.akraino.validation.ui.client.jenkins.JenkinsExecutorClient;
import org.akraino.validation.ui.client.jenkins.resources.QueueJobItem;
import org.akraino.validation.ui.client.jenkins.resources.QueueJobItem.Executable;
import org.akraino.validation.ui.client.nexus.NexusExecutorClient;
import org.akraino.validation.ui.client.nexus.resources.WrapperRobotTestResult;
import org.akraino.validation.ui.conf.UiUtils;
import org.akraino.validation.ui.data.BlueprintLayer;
import org.akraino.validation.ui.entity.LabSilo;
import org.akraino.validation.ui.entity.Submission;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

@Service
public class ResultService {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(ResultService.class);

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private SiloService siloService;

    @Deprecated
    public URL getNexusResultUrl(Submission submission) throws Exception {

        String url = System.getenv("JENKINS_URL");
        String userName = System.getenv("JENKINS_USERNAME");
        String password = System.getenv("JENKINS_USER_PASSWORD");

        Executable executable = null;
        while (executable == null) {
            JenkinsExecutorClient client;
            client = JenkinsExecutorClient.getInstance(userName, password, url);
            QueueJobItem queueJobItem = client.getQueueJobItem(new URL(submission.getJenkinsQueueJobItemUrl()));
            executable = queueJobItem.getExecutable();
            Thread.sleep(2000);
        }
        String siloText = null;
        for (LabSilo silo : siloService.getSilos()) {
            if (silo.getLab().getLab().equals(submission.getTimeslot().getLab().getLab())) {
                siloText = silo.getSilo();
            }
        }
        if (siloText == null) {
            throw new Exception("Could not retrieve silo of the selected lab : "
                    + submission.getTimeslot().getLab().getLab().toString());
        }
        String nexusUrl = UiUtils.NEXUS_URL + "/" + siloText + "/job/" + System.getenv("JENKINS_JOB_NAME") + "/"
                + String.valueOf(executable.getNumber() + "/results");
        if (!submission.getBlueprintInstanceForValidation().getLayer().equals(BlueprintLayer.All)) {
            nexusUrl = nexusUrl + "/" + submission.getBlueprintInstanceForValidation().getLayer().name().toLowerCase();
        }
        return new URL(nexusUrl);
    }

    public List<WrapperRobotTestResult> getRobotTestResults(String submissionId)
            throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
            UniformInterfaceException, NoSuchAlgorithmException, IOException {
        Submission submission = submissionService.getSubmission(submissionId);
        if (submission == null) {
            LOGGER.info(EELFLoggerDelegate.applicationLogger, "Requested submission does not exist");
            return null;
        }
        String nexusUrl = submission.getNexusResultUrl();
        String urlLastpart = nexusUrl.substring(nexusUrl.lastIndexOf('/') + 1);
        if (blueprintLayerContains(urlLastpart.substring(0, 1).toUpperCase() + urlLastpart.substring(1))) {
            nexusUrl = nexusUrl.substring(0, nexusUrl.lastIndexOf(urlLastpart) - 1);
        }
        NexusExecutorClient client = new NexusExecutorClient(nexusUrl);
        return client.getRobotTestResults();
    }

    private boolean blueprintLayerContains(String layer) {
        for (BlueprintLayer blueprintLayer : BlueprintLayer.values()) {
            if (blueprintLayer.name().equals(layer)) {
                return true;
            }
        }
        return false;
    }

}
