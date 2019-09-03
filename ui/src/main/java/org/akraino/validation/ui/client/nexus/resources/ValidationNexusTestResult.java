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
package org.akraino.validation.ui.client.nexus.resources;

import java.util.List;

public class ValidationNexusTestResult {

    private int resultId;

    private String blueprintName;

    private String version;

    private String silo;

    private Boolean allLayers;

    private Boolean optional;

    private boolean result;

    private String dateOfStorage;

    private String timestamp;

    private String submissionId;

    private List<WRobotNexusTestResult> wRobotNexusTestResults;

    public ValidationNexusTestResult() {

    }

    public Integer getResultId() {
        return this.resultId;
    }

    public void setResultId(Integer resultId) {
        this.resultId = resultId;
    }

    public String getBlueprintName() {
        return this.blueprintName;
    }

    public void setBlueprintName(String blueprintName) {
        this.blueprintName = blueprintName;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getSilo() {
        return this.silo;
    }

    public void setSilo(String silo) {
        this.silo = silo;
    }

    public Boolean getAllLayers() {
        return this.allLayers;
    }

    public void setAllLayers(Boolean allLayers) {
        this.allLayers = allLayers;
    }

    public Boolean getOptional() {
        return this.optional;
    }

    public void setOptional(Boolean optional) {
        this.optional = optional;
    }

    public boolean getResult() {
        return this.result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getDateOfStorage() {
        return this.dateOfStorage;
    }

    public void setDateOfStorage(String dateOfStorage) {
        this.dateOfStorage = dateOfStorage;
    }

    public String getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getSubmissionId() {
        return this.submissionId;
    }

    public void setSubmissionId(String submissionId) {
        this.submissionId = submissionId;
    }

    public List<WRobotNexusTestResult> getwRobotNexusTestResults() {
        return this.wRobotNexusTestResults;
    }

    public void setwRobotNexusTestResults(List<WRobotNexusTestResult> wRobotNexusTestResults) {
        this.wRobotNexusTestResults = wRobotNexusTestResults;
    }

}
