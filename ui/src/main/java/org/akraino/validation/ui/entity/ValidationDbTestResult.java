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

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

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

    @Column(name = "blueprint_name")
    private String blueprintName;

    @Column(name = "version")
    private String version;

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
    private Submission submission;

    public int getResultId() {
        return resultId;
    }

    public void setResultId(int resultId) {
        this.resultId = resultId;
    }

    public String getBlueprintName() {
        return blueprintName;
    }

    public void setBlueprintName(String blueprintName) {
        this.blueprintName = blueprintName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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

}
