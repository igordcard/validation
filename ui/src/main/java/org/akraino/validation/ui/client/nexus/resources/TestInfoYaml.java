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

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class TestInfoYaml {

    @JsonProperty("test_info")
    private test_info test_info;

    public TestInfoYaml() {

    }

    public test_info gettest_info() {
        return this.test_info;
    }

    public void settest_info(test_info test_info) {
        this.test_info = test_info;
    }

    public class test_info {

        @JsonProperty("layer")
        private String layer;

        @JsonProperty("optional")
        private Boolean optional;

        public test_info() {

        }

        public String getLayer() {
            return this.layer;
        }

        public void setLayer(String layer) {
            this.layer = layer;
        }

        public Boolean getOptional() {
            return this.optional;
        }

        public void setOptional(Boolean optional) {
            this.optional = optional;
        }
    }

}
