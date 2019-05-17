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
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.akraino.validation.ui.data.BlueprintLayer;

@Entity
@Table(name = "akraino.blueprint_instance")
public class BlueprintInstance implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "blueprint_instance_id_generator")
    @SequenceGenerator(name = "blueprint_instance_id_generator", sequenceName = "akraino.seq_blueprint_instance",
            allocationSize = 1)
    @Column(name = "blueprint_instance_id")
    private int blueprintInstId;

    @ManyToOne
    @JoinColumn(name = "blueprint_id")
    private Blueprint blueprint;

    @Column(name = "version")
    private String version;

    @Column(name = "layer")
    private BlueprintLayer layer;

    @Column(name = "layer_description")
    private String layerDescription;

    @OneToOne
    @JoinColumn(name = "timeslot_id")
    private Timeslot timeslot;

    public int getBlueprintInstanceId() {
        return blueprintInstId;
    }

    public void setBlueprintInstanceId(int blueprintInstId) {
        this.blueprintInstId = blueprintInstId;
    }

    public Blueprint getBlueprint() {
        return blueprint;
    }

    public void setBlueprint(Blueprint blueprint) {
        this.blueprint = blueprint;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }

    public BlueprintLayer getLayer() {
        return layer;
    }

    public void setLayer(BlueprintLayer layer) {
        this.layer = layer;
    }

    public void setLayerDescription(String layerDescription) {
        this.layerDescription = layerDescription;
    }

    public String getLayerDescription() {
        return layerDescription;
    }

    public void setTimeslot(Timeslot timeslot) {
        this.timeslot = timeslot;
    }

    public Timeslot getTimeslot() {
        return this.timeslot;
    }

}
