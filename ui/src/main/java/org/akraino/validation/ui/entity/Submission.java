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
package org.akraino.validation.ui.entity;

import java.io.IOException;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.akraino.validation.ui.data.SubmissionStatus;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.onap.portalsdk.core.web.support.UserUtils;

import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@Entity
@Table(name = "submission")
public class Submission implements Serializable {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(Submission.class);

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int submissionId;

    @Column(name = "status")
    private SubmissionStatus status;

    @ManyToOne
    @JoinColumn(name = "timeslot_id")
    private Timeslot timeslot;

    @OneToOne(mappedBy = "submission", targetEntity = ValidationDbTestResult.class, fetch = FetchType.EAGER)
    @JsonSerialize(using = ValidationDbTestResultSerializer.class)
    private ValidationDbTestResult validationDbTestResult;

    public void setSubmissionId(int submissionId) {
        this.submissionId = submissionId;
    }

    public int getSubmissionId() {
        return submissionId;
    }

    public SubmissionStatus getSubmissionStatus() {
        return this.status;
    }

    public void setSubmissionStatus(SubmissionStatus submissionStatus) {
        this.status = submissionStatus;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public Timeslot getTimeslot() {
        return this.timeslot;
    }

    public void setValidationDbTestResult(ValidationDbTestResult validationDbTestResult) {
        this.validationDbTestResult = validationDbTestResult;
    }

    public ValidationDbTestResult getValidationDbTestResult() {
        return validationDbTestResult;
    }

    static class ValidationDbTestResultSerializer extends StdSerializer<ValidationDbTestResult> {

        public ValidationDbTestResultSerializer() {
            this(null);
        }

        public ValidationDbTestResultSerializer(Class<ValidationDbTestResult> t) {
            super(t);
        }

        @Override
        public void serialize(ValidationDbTestResult validationDbTestResult,
                com.fasterxml.jackson.core.JsonGenerator gen, SerializerProvider provider) throws IOException {
            ValidationDbTestResult result = new ValidationDbTestResult();
            try {
                result.setResultId(validationDbTestResult.getResultId());
                result.setAllLayers(validationDbTestResult.getAllLayers());
                result.setBlueprintInstance(validationDbTestResult.getBlueprintInstance());
                result.setDateStorage(validationDbTestResult.getDateStorage());
                result.setLab(validationDbTestResult.getLab());
                result.setOptional(validationDbTestResult.getOptional());
                result.setResult(validationDbTestResult.getResult());
                result.setTimestamp(validationDbTestResult.getTimestamp());
                Set<WRobotDbTestResult> wRobotDbTestResults = new HashSet<WRobotDbTestResult>();
                if (validationDbTestResult.getWRobotDbTestResults() != null
                        && validationDbTestResult.getWRobotDbTestResults().size() > 0) {
                    for (WRobotDbTestResult wRobotDbTestResult : validationDbTestResult.getWRobotDbTestResults()) {
                        WRobotDbTestResult temp = new WRobotDbTestResult();
                        temp.setLayer(wRobotDbTestResult.getLayer());
                        // No need for robot results when fetching submissions
                        // temp.setRobotTestResults(wRobotDbTestResult.getRobotTestResults());
                        temp.setWRobotResultId(wRobotDbTestResult.getWRobotResultId());
                        wRobotDbTestResults.add(temp);
                    }
                }
                result.setWRobotDbTestResults(wRobotDbTestResults);
            } catch (Exception ex) {
                LOGGER.error(EELFLoggerDelegate.errorLogger, "Error when serializing." + UserUtils.getStackTrace(ex));
            }
            gen.writeObject(result);
        }
    }

}
