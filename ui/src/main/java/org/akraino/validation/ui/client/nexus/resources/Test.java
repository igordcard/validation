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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class Test {
    @JsonProperty("doc")
    private String doc;

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String name;

    @JsonProperty("status")
    private Test.Status status;

    @JsonProperty("kw")
    private List<Kw> kw;

    public Test() {

    }

    public String getDoc() {
        return this.doc;
    }

    public void setDoc(String doc) {
        this.doc = doc;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Test.Status getStatus() {
        return this.status;
    }

    public void setStatus(Test.Status status) {
        this.status = status;
    }

    public List<Kw> getKw() {
        return this.kw;
    }

    public void setKw(List<Kw> kw) {
        this.kw = kw;
    }

    public class Status {
        @JsonProperty("status")
        private String status;

        @JsonProperty("starttime")
        private String starttime;

        @JsonProperty("endtime")
        private String endtime;

        @JsonProperty("critical")
        private String critical;

        @JsonProperty("content")
        private String content;

        public Status() {

        }

        public String getStatus() {
            return this.status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public String getStarttime() {
            return this.starttime;
        }

        public void setStarttime(String starttime) {
            this.starttime = starttime;
        }

        public String getEndtime() {
            return this.endtime;
        }

        public void setEndtime(String endtime) {
            this.endtime = endtime;
        }

        public String getCritical() {
            return this.critical;
        }

        public void setCritical(String critical) {
            this.critical = critical;
        }

        public String getContent() {
            return this.content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
