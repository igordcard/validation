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

import org.akraino.validation.ui.data.JnksJobNotify;
import org.akraino.validation.ui.data.SubmissionStatus;
import org.akraino.validation.ui.entity.Submission;
import org.akraino.validation.ui.service.utils.SubmissionHelper;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class JenkinsJobNotificationService {

    @Autowired
    private SubmissionHelper submissionHelper;

    @Autowired
    private SubmissionService submissionService;

    private static final Logger LOGGER = Logger.getLogger(JenkinsJobNotificationService.class);

    public void handle(JnksJobNotify jnksJobNotify) {
        String jenkinsJobName = System.getenv("jenkins_job_name");
        if (!jenkinsJobName.equals(jnksJobNotify.getName())) {
            return;
        }
        Submission submission = submissionService.getSubmission(Integer.toString(jnksJobNotify.getSubmissionId()));
        if (submission == null) {
            LOGGER.debug("No related submission was found.");
            return;
        }
        submission.setNexusResultUrl(System.getenv("nexus_results_url") + "/"
                + submission.getBlueprintInstance().getTimeslot().getLab().name().toLowerCase() + "-blu-val"
                + "/job/validation/" + String.valueOf(jnksJobNotify.getbuildNumber()));
        LOGGER.info("Updating submission with id: " + submission.getSubmissionId());
        submission.setSubmissionStatus(SubmissionStatus.Completed);
        submissionHelper.saveSubmission(submission);
    }

}
