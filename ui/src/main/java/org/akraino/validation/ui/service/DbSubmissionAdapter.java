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
import org.akraino.validation.ui.data.SubmissionData;
import org.akraino.validation.ui.data.SubmissionStatus;
import org.akraino.validation.ui.entity.LabSilo;
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

    @Autowired
    private SubmissionDAO submissionDAO;

    @Autowired
    private SubmissionHelper submissionHelper;

    @Autowired
    private JenkinsExecutorClient jenkinsService;

    @Autowired
    private DbResultAdapter dbAdapter;

    @Autowired
    SiloService siloService;

    public SubmissionData saveSubmission(SubmissionData submissionData)
            throws JsonParseException, JsonMappingException, IOException {
        Submission submission = new Submission();
        submission.setSubmissionStatus(SubmissionStatus.Submitted);
        submission.setTimeslot(submissionData.getTimeslot());
        submissionDAO.saveOrUpdate(submission);
        submissionData.setSubmissionId(submission.getSubmissionId());
        String siloText = null;
        for (LabSilo silo : siloService.getSilos()) {
            if (silo.getLab().getLab().equals(submissionData.getTimeslot().getLab().getLab())) {
                siloText = silo.getSilo();
            }
        }
        if (siloText == null) {
            throw new IllegalArgumentException(
                    "Lab does not exist: " + submissionData.getTimeslot().getLab().toString());
        }
        submissionData.getValidationNexusTestResult().setSilo(siloText);
        dbAdapter.associateSubmissionWithValidationResult(submissionData);
        ApplicationContext context = new AnnotationConfigApplicationContext(ExecutorServiceInitializer.class);
        ExecutorService service = (ExecutorService) context.getBean("executorService");
        JenkinsTriggerSubmissionJob task = new JenkinsTriggerSubmissionJob(submissionData);
        CompletableFuture<SubmissionData> completableFuture = CompletableFuture
                .supplyAsync(new PrioritySupplier<>(1, task::execute), service);
        completableFuture.thenAcceptAsync(result -> this.callbackNotify(result));
        submissionData.setSubmissionId(submission.getSubmissionId());
        return submissionData;
    }

    public List<Submission> getSubmissions() {
        return submissionDAO.getSubmissions();
    }

    public List<SubmissionData> getSubmissionDatas() throws JsonParseException, JsonMappingException, IOException {
        List<Submission> submissions = submissionDAO.getSubmissions();
        if (submissions == null || submissions.size() < 1) {
            return null;
        }
        List<SubmissionData> datas = new ArrayList<SubmissionData>();
        for (Submission submission : submissions) {
            SubmissionData submissionData = new SubmissionData();
            submissionData.setStatus(submission.getSubmissionStatus());
            submissionData.setSubmissionId(submission.getSubmissionId());
            submissionData.setTimeslot(submission.getTimeslot());
            submissionData.setValidationNexusTestResult(
                    dbAdapter.readResultFromDb(String.valueOf(submission.getSubmissionId())));
            datas.add(submissionData);
        }
        return datas;
    }

    public SubmissionData getSubmissionData(String submissionId)
            throws JsonParseException, JsonMappingException, IOException {
        Submission submission = submissionDAO.getSubmission(Integer.valueOf(submissionId));
        if (submission == null) {
            return null;
        }
        SubmissionData submissionData = new SubmissionData();
        submissionData.setStatus(submission.getSubmissionStatus());
        submissionData.setSubmissionId(submission.getSubmissionId());
        submissionData.setTimeslot(submission.getTimeslot());
        submissionData.setValidationNexusTestResult(dbAdapter.readResultFromDb(submissionId));
        return submissionData;
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

    private void callbackNotify(SubmissionData submissionData) {
        if (submissionData == null) {
            return;
        }
        Submission submission = submissionHelper.getSubmission(submissionData.getSubmissionId());
        submission.setSubmissionStatus(SubmissionStatus.Running);
        submissionHelper.saveSubmission(submission);
    }

    private class JenkinsTriggerSubmissionJob {

        private SubmissionData submissionData;

        public JenkinsTriggerSubmissionJob(SubmissionData submissionData) {
            this.submissionData = submissionData;
        }

        public SubmissionData execute() {
            try (final DatagramSocket socket = new DatagramSocket()) {
                String jobName = System.getenv("JENKINS_JOB_NAME");
                List<Parameter> listOfParameters = new ArrayList<Parameter>();
                Parameters parameters = new Parameters();
                Parameter parameter = new Parameter();
                parameter.setName("SUBMISSION_ID");
                parameter.setValue(String.valueOf(submissionData.getSubmissionId()));
                listOfParameters.add(parameter);
                parameter = new Parameter();
                parameter.setName("BLUEPRINT");
                parameter.setValue(submissionData.getValidationNexusTestResult().getBlueprintName());
                listOfParameters.add(parameter);
                parameter = new Parameter();
                parameter.setName("LAYER");
                if (submissionData.getValidationNexusTestResult().getAllLayers()) {
                    parameter.setValue("all");
                } else {
                    parameter.setValue(submissionData.getValidationNexusTestResult().getwRobotNexusTestResults().get(0)
                            .getBlueprintLayer().name().toLowerCase());
                }
                listOfParameters.add(parameter);
                parameter = new Parameter();
                parameter.setName("VERSION");
                parameter.setValue(submissionData.getValidationNexusTestResult().getVersion());
                listOfParameters.add(parameter);
                parameter = new Parameter();
                parameter.setName("LAB");
                parameter.setValue(submissionData.getTimeslot().getLab().getLab().name());
                listOfParameters.add(parameter);
                parameter = new Parameter();
                parameter.setName("OPTIONAL");
                parameter.setValue(
                        String.valueOf(submissionData.getValidationNexusTestResult().getOptional().toString()));
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
                return submissionData;
            } catch (Exception e) {
                LOGGER.error(EELFLoggerDelegate.errorLogger,
                        "Error when triggering Jenkins job. " + UserUtils.getStackTrace(e));
                return null;
            }
        }
    }

}
