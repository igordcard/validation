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
package org.akraino.validation.ui.client.jenkins.resources;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class CrumbResponse implements IResource {

    @JsonProperty("_class")
    private String classCrumb;

    @JsonProperty("crumb")
    private String crumb;

    @JsonProperty("crumbRequestField")
    private String crumbRequestField;

    public CrumbResponse() {

    }

    public String getClassCrumb() {
        return this.classCrumb;
    }

    public void setClassCrumb(String classCrumb) {
        this.classCrumb = classCrumb;
    }

    public String getCrumb() {
        return this.crumb;
    }

    public void setCrumb(String crumb) {
        this.crumb = crumb;
    }

    public String getCrumbRequestField() {
        return this.crumbRequestField;
    }

    public void setCrumbRequestField(String crumbRequestField) {
        this.crumbRequestField = crumbRequestField;
    }

}
