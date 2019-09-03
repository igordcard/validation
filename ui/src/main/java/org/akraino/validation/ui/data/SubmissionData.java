/*
 * Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.akraino.validation.ui.data;

import org.akraino.validation.ui.client.nexus.resources.ValidationNexusTestResult;
import org.akraino.validation.ui.entity.Timeslot;

public class SubmissionData {

    private int submissionId;

    private SubmissionStatus status;

    private Timeslot timeslot;

    private ValidationNexusTestResult validationNexusTestResult;

    public SubmissionData() {

    }

    public int getSubmissionId() {
        return this.submissionId;
    }

    public void setSubmissionId(int submissionId) {
        this.submissionId = submissionId;
    }

    public SubmissionStatus getStatus() {
        return this.status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public Timeslot getTimeslot() {
        return this.timeslot;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public ValidationNexusTestResult getValidationNexusTestResult() {
        return this.validationNexusTestResult;
    }

    public void setValidationNexusTestResult(ValidationNexusTestResult validationNexusTestResult) {
        this.validationNexusTestResult = validationNexusTestResult;
    }
}
