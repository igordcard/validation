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

import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

import org.akraino.validation.ui.client.jenkins.JenkinsExecutorClient;
import org.akraino.validation.ui.client.jenkins.resources.Parameter;
import org.akraino.validation.ui.client.jenkins.resources.Parameters;
import org.akraino.validation.ui.conf.UiUtils;
import org.akraino.validation.ui.dao.SubmissionDAO;
import org.akraino.validation.ui.data.SubmissionStatus;
import org.akraino.validation.ui.entity.Submission;
import org.akraino.validation.ui.service.utils.PrioritySupplier;
import org.akraino.validation.ui.service.utils.SubmissionHelper;
import org.apache.commons.httpclient.HttpException;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.onap.portalsdk.core.web.support.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

@Service
@Transactional
public class SubmissionService {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(SubmissionService.class);

    @Autowired
    private SubmissionDAO submissionDAO;

    @Autowired
    private SubmissionHelper submissionHelper;

    public Submission saveSubmission(Submission submission) {
        submission.setSubmissionStatus(SubmissionStatus.Submitted);
        submissionDAO.saveOrUpdate(submission);

        JenkinsTriggerSubmissionJob task = new JenkinsTriggerSubmissionJob(submission);
        CompletableFuture<Submission> completableFuture =
                CompletableFuture.supplyAsync(new PrioritySupplier<>(1, task::execute), UiUtils.executorService);
        completableFuture.thenAcceptAsync(result -> this.callbackNotify(result));

        return submission;
    }

    public List<Submission> getSubmissions() {
        return submissionDAO.getSubmissions();
    }

    public Submission getSubmission(String submissionId) {
        return submissionDAO.getSubmission(Integer.valueOf(submissionId));
    }

    public void deleteSubmission(Integer submissionId) {
        submissionDAO.deleteSubmission(submissionId);
    }

    public void deleteAll() {
        submissionDAO.deleteAll();
    }

    private void callbackNotify(Submission submission) {
        if (submission == null) {
            return;
        }
        submission.setSubmissionStatus(SubmissionStatus.Running);
        submissionHelper.saveSubmission(submission);
    }

    private class JenkinsTriggerSubmissionJob {

        private Submission submission;

        public JenkinsTriggerSubmissionJob(Submission submission) {
            this.submission = submission;
        }

        public Submission execute() {
            String url = System.getenv("JENKINS_URL");
            String userName = System.getenv("JENKINS_USERNAME");
            String userPassword = System.getenv("JENKINS_USER_PASSWORD");
            String jobName = System.getenv("JENKINS_JOB_NAME");
            List<Parameter> listOfParameters = new ArrayList<Parameter>();
            Parameters parameters = new Parameters();
            Parameter parameter = new Parameter();
            parameter.setName("SUBMISSION_ID");
            parameter.setValue(String.valueOf(submission.getSubmissionId()));
            listOfParameters.add(parameter);
            parameter = new Parameter();
            parameter.setName("BLUEPRINT");
            parameter.setValue(
                    submission.getBlueprintInstanceForValidation().getBlueprint().getBlueprintName().toLowerCase());
            listOfParameters.add(parameter);
            parameter = new Parameter();
            parameter.setName("LAYER");
            parameter.setValue(submission.getBlueprintInstanceForValidation().getLayer().name().toLowerCase());
            listOfParameters.add(parameter);
            parameter = new Parameter();
            parameter.setName("VERSION");
            parameter.setValue(submission.getBlueprintInstanceForValidation().getVersion().toLowerCase());
            listOfParameters.add(parameter);
            parameter = new Parameter();
            parameter.setName("UI_IP");
            Random random = new Random();
            String localIP = null;
            try (final DatagramSocket socket = new DatagramSocket()) {
                socket.connect(InetAddress.getByName(random.nextInt(256) + "." + random.nextInt(256) + "."
                        + random.nextInt(256) + "." + random.nextInt(256)), 10002);
                localIP = socket.getLocalAddress().getHostAddress();
                parameter.setValue(localIP);
                listOfParameters.add(parameter);
                parameters.setParameter(listOfParameters);
                JenkinsExecutorClient client;
                client = JenkinsExecutorClient.getInstance(userName, userPassword, url);
                submission.setJnksQueueJobItemUrl(client.postJobWithQueryParams(jobName, parameters).toString());
                return submission;
            } catch (SocketException | UnknownHostException | KeyManagementException | HttpException
                    | ClientHandlerException | UniformInterfaceException | MalformedURLException
                    | NoSuchAlgorithmException e) {
                LOGGER.error(EELFLoggerDelegate.errorLogger,
                        "Error when triggering Jenkins job. " + UserUtils.getStackTrace(e));
                return null;
            }
        }
    }

}
