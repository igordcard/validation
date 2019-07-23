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

import java.util.ArrayList;
import java.util.List;

import org.akraino.validation.ui.client.nexus.resources.ValidationNexusTestResult;
import org.akraino.validation.ui.dao.ValidationTestResultDAO;
import org.akraino.validation.ui.data.JnksJobNotify;
import org.akraino.validation.ui.data.SubmissionStatus;
import org.akraino.validation.ui.entity.LabSilo;
import org.akraino.validation.ui.entity.Submission;
import org.akraino.validation.ui.entity.ValidationDbTestResult;
import org.akraino.validation.ui.service.utils.SubmissionHelper;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JenkinsJobNotificationService {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(JenkinsJobNotificationService.class);

    @Autowired
    private SubmissionHelper submissionHelper;

    @Autowired
    private DbSubmissionAdapter submissionService;

    @Autowired
    private SiloService siloService;

    @Autowired
    private DbResultAdapter dbAdapter;

    @Autowired
    private IntegratedResultService iService;

    @Autowired
    private ValidationTestResultDAO vTestResultDAO;

    public void handle(JnksJobNotify jnksJobNotify) throws Exception {
        String jenkinsJobName = System.getenv("JENKINS_JOB_NAME");
        if (!jenkinsJobName.equals(jnksJobNotify.getName())) {
            return;
        }
        Submission submission = submissionService.getSubmission(Integer.toString(jnksJobNotify.getSubmissionId()));
        if (submission == null) {
            LOGGER.debug(EELFLoggerDelegate.debugLogger, "No related submission was found");
            return;
        }
        String siloText = null;
        for (LabSilo silo : siloService.getSilos()) {
            if (silo.getLab().getLab().equals(submission.getTimeslot().getLab().getLab())) {
                siloText = silo.getSilo();
            }
        }
        if (siloText == null) {
            throw new IllegalArgumentException("Could not retrieve silo of the selected lab : "
                    + submission.getTimeslot().getLab().getLab().toString());
        }
        LOGGER.info(EELFLoggerDelegate.applicationLogger,
                "Updating submission with id: " + submission.getSubmissionId());
        submission.setSubmissionStatus(SubmissionStatus.Completed);
        submissionHelper.saveSubmission(submission);
        ValidationDbTestResult vDbResult = vTestResultDAO.getValidationTestResult(submission);
        if (vDbResult != null) {
            ValidationNexusTestResult vNexusResult = iService.getResult(vDbResult.getBlueprintName(),
                    vDbResult.getVersion(), vDbResult.getLab().getLab(), jnksJobNotify.getTimestamp());
            if (vNexusResult != null) {
                List<ValidationNexusTestResult> vNexusResults = new ArrayList<ValidationNexusTestResult>();
                vNexusResults.add(vNexusResult);
                dbAdapter.storeResultInDb(vNexusResults);
            }
        }
        dbAdapter.updateTimestamp(jnksJobNotify);
    }

}
