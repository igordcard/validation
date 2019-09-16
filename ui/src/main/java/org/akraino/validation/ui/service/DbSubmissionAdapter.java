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
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.akraino.validation.ui.client.jenkins.JenkinsExecutorClient;
import org.akraino.validation.ui.client.jenkins.resources.Parameter;
import org.akraino.validation.ui.client.jenkins.resources.Parameters;
import org.akraino.validation.ui.conf.ExecutorServiceInitializer;
import org.akraino.validation.ui.dao.SubmissionDAO;
import org.akraino.validation.ui.data.SubmissionStatus;
import org.akraino.validation.ui.entity.Submission;
import org.akraino.validation.ui.service.utils.PrioritySupplier;
import org.akraino.validation.ui.service.utils.SubmissionHelper;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.onap.portalsdk.core.web.support.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Service
@Transactional
public class DbSubmissionAdapter {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(DbSubmissionAdapter.class);
    private static final Object LOCK = new Object();

    @Autowired
    private SubmissionDAO submissionDAO;

    @Autowired
    private SubmissionHelper submissionHelper;

    @Autowired
    private JenkinsExecutorClient jenkinsService;

    @Autowired
    private DbResultAdapter dbAdapter;

    @Autowired
    BlueprintInstanceService bluInstService;

    public Submission saveSubmission(Submission submission)
            throws JsonParseException, JsonMappingException, IOException {
        synchronized (LOCK) {
            submission.setSubmissionStatus(SubmissionStatus.Submitted);
            submissionDAO.saveOrUpdate(submission);
            dbAdapter.associateSubmissionWithValidationResult(submission);
            ApplicationContext context = new AnnotationConfigApplicationContext(ExecutorServiceInitializer.class);
            ExecutorService service = (ExecutorService) context.getBean("executorService");
            JenkinsTriggerSubmissionJob task = new JenkinsTriggerSubmissionJob(submission);
            CompletableFuture<Submission> completableFuture = CompletableFuture
                    .supplyAsync(new PrioritySupplier<>(1, task::execute), service);
            completableFuture.thenAcceptAsync(result -> this.callbackNotify(result));
            return submission;
        }
    }

    public List<Submission> getSubmissions() {
        synchronized (LOCK) {
            return submissionDAO.getSubmissions();
        }
    }

    public Submission getSubmission(String submissionId) {
        synchronized (LOCK) {
            return submissionDAO.getSubmission(Integer.valueOf(submissionId));
        }
    }

    public void deleteSubmission(Integer submissionId) {
        synchronized (LOCK) {
            submissionDAO.deleteSubmission(submissionId);
        }
    }

    public void deleteAll() {
        synchronized (LOCK) {
            submissionDAO.deleteAll();
        }
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
            try (final DatagramSocket socket = new DatagramSocket()) {
                String jobName = System.getenv("JENKINS_JOB_NAME");
                List<Parameter> listOfParameters = new ArrayList<Parameter>();
                Parameters parameters = new Parameters();
                Parameter parameter = new Parameter();
                parameter.setName("SUBMISSION_ID");
                parameter.setValue(String.valueOf(submission.getSubmissionId()));
                listOfParameters.add(parameter);
                parameter = new Parameter();
                parameter.setName("BLUEPRINT");
                parameter.setValue(submission.getValidationDbTestResult().getBlueprintInstance().getBlueprint()
                        .getBlueprintName());
                listOfParameters.add(parameter);
                parameter = new Parameter();
                parameter.setName("LAYER");
                if (submission.getValidationDbTestResult().getAllLayers()) {
                    parameter.setValue("all");
                } else {
                    parameter.setValue(submission.getValidationDbTestResult().getBlueprintInstance()
                            .getBlueprintLayers().iterator().next().getLayer().toLowerCase());
                }
                listOfParameters.add(parameter);
                parameter = new Parameter();
                parameter.setName("VERSION");
                parameter.setValue(submission.getValidationDbTestResult().getBlueprintInstance().getVersion());
                listOfParameters.add(parameter);
                parameter = new Parameter();
                parameter.setName("LAB");
                parameter.setValue(submission.getTimeslot().getLabInfo().getLab());
                listOfParameters.add(parameter);
                parameter = new Parameter();
                parameter.setName("OPTIONAL");
                parameter.setValue(String.valueOf(submission.getValidationDbTestResult().getOptional().toString()));
                listOfParameters.add(parameter);
                parameter = new Parameter();
                parameter.setName("UI_IP");
                Random random = new Random();
                socket.connect(InetAddress.getByName(random.nextInt(256) + "." + random.nextInt(256) + "."
                        + random.nextInt(256) + "." + random.nextInt(256)), 10002);
                String localIP = socket.getLocalAddress().getHostAddress();
                parameter.setValue(localIP);
                listOfParameters.add(parameter);
                parameters.setParameter(listOfParameters);
                jenkinsService.postJobWithQueryParams(jobName, parameters).toString();
                return submission;
            } catch (Exception e) {
                LOGGER.error(EELFLoggerDelegate.errorLogger,
                        "Error when triggering Jenkins job. " + UserUtils.getStackTrace(e));
                return null;
            }
        }
    }

}
