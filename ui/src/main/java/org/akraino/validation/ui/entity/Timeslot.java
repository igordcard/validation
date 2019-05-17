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
package org.akraino.validation.ui.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.akraino.validation.ui.data.Lab;

@Entity
@Table(name = "akraino.timeslot")
public class Timeslot implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "timeslot_id_generator")
    @SequenceGenerator(name = "timeslot_id_generator", sequenceName = "akraino.seq_timeslot", allocationSize = 1)
    @Column(name = "timeslot_id")
    private int timeslotId;

    @Column(name = "start_date_time")
    private String startDateTime;

    @Column(name = "duration")
    private int duration;

    @Column(name = "lab")
    private Lab lab;

    public void setTimeslotId(int timeslotId) {
        this.timeslotId = timeslotId;
    }

    public int getTimeslotId() {
        return timeslotId;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getStartDateTime() {
        return startDateTime;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getDuration() {
        return duration;
    }

    public void setLab(Lab lab) {
        this.lab = lab;
    }

    public Lab getLab() {
        return lab;
    }
}
