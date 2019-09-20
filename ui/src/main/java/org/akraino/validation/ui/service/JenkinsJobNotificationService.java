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

import org.akraino.validation.ui.dao.ValidationTestResultDAO;
import org.akraino.validation.ui.data.JnksJobNotify;
import org.akraino.validation.ui.data.SubmissionStatus;
import org.akraino.validation.ui.entity.LabInfo;
import org.akraino.validation.ui.entity.Submission;
import org.akraino.validation.ui.entity.ValidationDbTestResult;
import org.akraino.validation.ui.service.utils.SubmissionHelper;
import org.apache.commons.httpclient.HttpException;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.onap.portalsdk.core.web.support.UserUtils;
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
    private DbAdapter dbAdapter;

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
        LabInfo labInfo = dbAdapter.getLab(submission.getTimeslot().getLabInfo().getLab());
        if (labInfo == null) {
            throw new IllegalArgumentException(
                    "Could not retrieve lab : " + submission.getTimeslot().getLabInfo().getLab().toString());
        }
        LOGGER.info(EELFLoggerDelegate.applicationLogger,
                "Updating submission with id: " + submission.getSubmissionId());
        submission.setSubmissionStatus(SubmissionStatus.Completed);
        submissionHelper.saveSubmission(submission);
        ValidationDbTestResult vDbResult = vTestResultDAO.getValidationTestResult(submission);
        try {
            if (vDbResult != null) {
                // Fetch submission result from nexus
                ValidationDbTestResult vNexusResult = iService.getResult(
                        vDbResult.getBlueprintInstance().getBlueprint().getBlueprintName(),
                        vDbResult.getBlueprintInstance().getVersion(), vDbResult.getLab().getLab(),
                        jnksJobNotify.getTimestamp());
                if (vNexusResult != null) {
                    List<ValidationDbTestResult> vNexusResults = new ArrayList<ValidationDbTestResult>();
                    vNexusResults.add(vNexusResult);
                    dbAdapter.storeResultsInDb(vNexusResults);
                }
            }
        } catch (HttpException ex) {
            LOGGER.error(EELFLoggerDelegate.errorLogger,
                    "Error when retrieving Nexus results. " + UserUtils.getStackTrace(ex));
        }
        dbAdapter.updateTimestamp(jnksJobNotify);
    }

}
