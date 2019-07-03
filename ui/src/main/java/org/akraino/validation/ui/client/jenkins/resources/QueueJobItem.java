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
package org.akraino.validation.ui.client.jenkins.resources;

import java.net.URL;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class QueueJobItem implements IResource {

    @JsonProperty("_class")
    private String _class;

    @JsonProperty("executable")
    private Executable executable;

    public QueueJobItem() {

    }

    public String get_class() {
        return this._class;
    }

    public void set_class(String _class) {
        this._class = _class;
    }

    public Executable getExecutable() {
        return this.executable;
    }

    public void setExecutable(Executable executable) {
        this.executable = executable;
    }

    public class Executable {
        @JsonProperty("_class")
        private String _class;

        @JsonProperty("number")
        private Integer number;

        @JsonProperty("url")
        private URL url;

        public String get_class() {
            return this._class;
        }

        public void set_class(String _class) {
            this._class = _class;
        }

        public Integer getNumber() {
            return this.number;
        }

        public void setNumber(Integer number) {
            this.number = number;
        }

        public URL getUrl() {
            return this.url;
        }

        public void setUrl(URL url) {
            this.url = url;
        }
    }

}
