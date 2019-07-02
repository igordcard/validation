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

import org.akraino.validation.ui.data.SubmissionStatus;

@Entity
@Table(name = "submission")
public class Submission implements Serializable {

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

    @Column(name = "jenkins_queue_job_item_url")
    private String jnksJobUrl;

    @Column(name = "nexus_result_url")
    private String nexusResultUrl;

    @ManyToOne
    @JoinColumn(name = "blueprint_instance_for_validation_id")
    private BlueprintInstanceForValidation blueprintInst;

    @ManyToOne
    @JoinColumn(name = "timeslot_id")
    private Timeslot timeslot;

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

    public String getJenkinsQueueJobItemUrl() {
        return this.jnksJobUrl;
    }

    public void setJnksQueueJobItemUrl(String url) {
        this.jnksJobUrl = url;
    }

    public String getNexusResultUrl() {
        return this.nexusResultUrl;
    }

    public void setNexusResultUrl(String nexusResultUrl) {
        this.nexusResultUrl = nexusResultUrl;
    }

    public void setBlueprintInstanceForValidation(BlueprintInstanceForValidation blueprintInst) {
        this.blueprintInst = blueprintInst;
    }

    public BlueprintInstanceForValidation getBlueprintInstanceForValidation() {
        return this.blueprintInst;
    }


    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public Timeslot getTimeslot() {
        return this.timeslot;
    }


}
