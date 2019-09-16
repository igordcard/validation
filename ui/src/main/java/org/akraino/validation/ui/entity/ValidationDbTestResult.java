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
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@Entity
@Table(name = "validation_test_result")
public class ValidationDbTestResult implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int resultId;

    @ManyToOne
    @JoinColumn(name = "blueprint_instance_id")
    private BlueprintInstance blueprintInstance;

    @ManyToOne
    @JoinColumn(name = "lab_id")
    private LabInfo lab;

    @Column(name = "timestamp")
    private String timestamp;

    @Column(name = "all_layers")
    private Boolean allLayers;

    @Column(name = "optional")
    private Boolean optional;

    @Column(name = "result")
    private Boolean result;

    @Column(name = "date_of_storage")
    private String dateStorage;

    @OneToOne
    @JoinColumn(name = "submission_id")
    @JsonSerialize(using = SubmissionSerializer.class)
    private Submission submission;

    @OneToMany(mappedBy = "validationDbTestResult", targetEntity = WRobotDbTestResult.class, fetch = FetchType.EAGER)
    private Set<WRobotDbTestResult> wRobotDbTestResults;

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public BlueprintInstance getBlueprintInstance() {
        return blueprintInstance;
    }

    public void setBlueprintInstance(BlueprintInstance blueprintInstance) {
        this.blueprintInstance = blueprintInstance;
    }

    public Boolean getAllLayers() {
        return allLayers;
    }

    public void setAllLayers(Boolean allLayers) {
        this.allLayers = allLayers;
    }

    public Boolean getOptional() {
        return optional;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }

    public LabInfo getLab() {
        return lab;
    }

    public void setLab(LabInfo lab) {
        this.lab = lab;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public String getDateStorage() {
        return dateStorage;
    }

    public void setDateStorage(String dateStorage) {
        this.dateStorage = dateStorage;
    }

    public Submission getSubmission() {
        return this.submission;
    }

    public void setSubmission(Submission submission) {
        this.submission = submission;
    }

    public Set<WRobotDbTestResult> getWRobotDbTestResults() {
        return this.wRobotDbTestResults;
    }

    public void setWRobotDbTestResults(Set<WRobotDbTestResult> wRobotDbTestResults) {
        this.wRobotDbTestResults = wRobotDbTestResults;
    }

    static class SubmissionSerializer extends StdSerializer<Submission> {

        public SubmissionSerializer() {
            this(null);
        }

        public SubmissionSerializer(Class<Submission> t) {
            super(t);
        }

        @Override
        public void serialize(Submission submission, JsonGenerator gen, SerializerProvider provider)
                throws IOException {
            Submission result = new Submission();
            result.setSubmissionId(submission.getSubmissionId());
            result.setSubmissionStatus(submission.getSubmissionStatus());
            result.setTimeslot(submission.getTimeslot());
            gen.writeObject(result);
        }
    }

}
