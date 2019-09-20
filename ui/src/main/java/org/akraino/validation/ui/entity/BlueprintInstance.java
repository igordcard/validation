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

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.onap.portalsdk.core.web.support.UserUtils;

import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

@Entity
@Table(name = "blueprint_instance")
public class BlueprintInstance implements Serializable {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(BlueprintInstance.class);

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int blueprintInstId;

    @ManyToOne
    @JoinColumn(name = "blueprint_id")
    private Blueprint blueprint;

    @Column(name = "version")
    private String version;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "blueprint_instance_blueprint_layer", joinColumns = {
            @JoinColumn(name = "blueprint_instance_id") }, inverseJoinColumns = {
                    @JoinColumn(name = "blueprint_layer_id") })
    private Set<BlueprintLayer> blueprintLayers = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "blueprint_instance_timeslot", joinColumns = {
            @JoinColumn(name = "blueprint_instance_id") }, inverseJoinColumns = { @JoinColumn(name = "timeslot_id") })
    @JsonSerialize(using = TimeslotsSerializer.class)
    private Set<Timeslot> timeslots = new HashSet<>();

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

    public Set<BlueprintLayer> getBlueprintLayers() {
        return blueprintLayers;
    }

    public void setBlueprintLayers(Set<BlueprintLayer> blueprintLayers) {
        this.blueprintLayers = blueprintLayers;
    }

    public Set<Timeslot> getTimeslots() {
        return timeslots;
    }

    public void setTimeslots(Set<Timeslot> timeslots) {
        this.timeslots = timeslots;
    }

    static class TimeslotsSerializer extends StdSerializer<Set<Timeslot>> {

        public TimeslotsSerializer() {
            this(null);
        }

        public TimeslotsSerializer(Class<Set<Timeslot>> tclass) {
            super(tclass);
        }

        @Override
        public void serialize(Set<Timeslot> timeslots, com.fasterxml.jackson.core.JsonGenerator gen,
                SerializerProvider provider) throws IOException {
            Set<Timeslot> results = new HashSet<>();
            for (Timeslot timeslot : timeslots) {
                try {
                    Timeslot result = new Timeslot();
                    result.setDuration(timeslot.getDuration());
                    result.setLabInfo(timeslot.getLabInfo());
                    result.setStartDateTime(timeslot.getStartDateTime());
                    result.setTimeslotId(timeslot.getTimeslotId());
                    results.add(result);
                } catch (Exception ex) {
                    LOGGER.error(EELFLoggerDelegate.errorLogger,
                            "Error when serializing." + UserUtils.getStackTrace(ex));
                }
            }
            gen.writeObject(results);
        }
    }

}
