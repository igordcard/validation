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

import org.akraino.validation.ui.conf.UiUtils;
import org.akraino.validation.ui.data.BlueprintLayer;
import org.akraino.validation.ui.data.JnksJobNotify;
import org.akraino.validation.ui.data.SubmissionStatus;
import org.akraino.validation.ui.entity.LabSilo;
import org.akraino.validation.ui.entity.Submission;
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
    private SubmissionService submissionService;

    @Autowired
    private SiloService siloService;

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
            throw new Exception("Could not retrieve silo of the selected lab : "
                    + submission.getTimeslot().getLab().getLab().toString());
        }

        String nexusUrl = UiUtils.NEXUS_URL + "/" + siloText + "/job/" + System.getenv("JENKINS_JOB_NAME") + "/"
                + String.valueOf(jnksJobNotify.getbuildNumber() + "/results");
        if (!submission.getBlueprintInstanceForValidation().getLayer().equals(BlueprintLayer.All)) {
            nexusUrl = nexusUrl + "/" + submission.getBlueprintInstanceForValidation().getLayer().name().toLowerCase();
        }
        submission.setNexusResultUrl(nexusUrl);
        LOGGER.info(EELFLoggerDelegate.applicationLogger,
                "Updating submission with id: " + submission.getSubmissionId());
        submission.setSubmissionStatus(SubmissionStatus.Completed);
        submissionHelper.saveSubmission(submission);
    }

}
