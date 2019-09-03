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
import javax.persistence.Table;

@Entity
@Table(name = "w_robot_test_result")
public class WRobotDbTestResult implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int wRobotResultId;

    @Column(name = "layer")
    private String layer;

    @ManyToOne
    @JoinColumn(name = "validation_test_result_id")
    private ValidationDbTestResult vResult;

    @Column(name = "robot_test_results")
    private String rResults;

    public int getWRobotResultId() {
        return wRobotResultId;
    }

    public void setWRobotResultId(int wRobotResultId) {
        this.wRobotResultId = wRobotResultId;
    }

    public String getLayer() {
        return layer;
    }

    public void setLayer(String layer) {
        this.layer = layer;
    }

    public ValidationDbTestResult getValidationTestResult() {
        return vResult;
    }

    public void setValidationTestResult(ValidationDbTestResult vResult) {
        this.vResult = vResult;
    }

    public String getRobotTestResults() {
        return rResults;
    }

    public void setRobotTestResults(String rResults) {
        this.rResults = rResults;
    }

}
