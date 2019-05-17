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
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import org.akraino.validation.ui.client.jenkins.JenkinsExecutorClient;
import org.akraino.validation.ui.client.jenkins.resources.QueueJobItem;
import org.akraino.validation.ui.client.jenkins.resources.QueueJobItem.Executable;
import org.akraino.validation.ui.client.nexus.NexusExecutorClient;
import org.akraino.validation.ui.client.nexus.resources.RobotTestResult;
import org.akraino.validation.ui.entity.Submission;
import org.apache.commons.httpclient.HttpException;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

@Service
public class ResultService {

    private static final Logger LOGGER = Logger.getLogger(ResultService.class);

    @Autowired
    private SubmissionService submissionService;

    @Deprecated
    public URL getNexusResultUrl(Submission submission)
            throws MalformedURLException, KeyManagementException, HttpException, ClientHandlerException,
            UniformInterfaceException, NoSuchAlgorithmException, InterruptedException {

        String url = System.getenv("jenkins_url");
        String userName = System.getenv("jenkins_user_name");
        String password = System.getenv("jenkins_user_pwd");

        Executable executable = null;
        while (executable == null) {
            JenkinsExecutorClient client;
            client = JenkinsExecutorClient.getInstance(userName, password, url);
            QueueJobItem queueJobItem = client.getQueueJobItem(new URL(submission.getJenkinsQueueJobItemUrl()));
            executable = queueJobItem.getExecutable();
            Thread.sleep(2000);
        }
        return new URL(System.getenv("nexus_results_url") + "/"
                + submission.getBlueprintInstance().getTimeslot().getLab().name().toLowerCase() + "-blu-val"
                + "/job/validation/" + String.valueOf(executable.getNumber()));
    }

    public List<RobotTestResult> getRobotTestResults(String submissionId)
            throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
            UniformInterfaceException, NoSuchAlgorithmException, IOException {
        Submission submission = submissionService.getSubmission(submissionId);
        if (submission == null) {
            LOGGER.info("Requested submission does not exist");
            return null;
        }
        String nexusUrl = submission.getNexusResultUrl();
        NexusExecutorClient client = new NexusExecutorClient(nexusUrl + "/results");
        return client.getRobotTestResults();
    }

}
