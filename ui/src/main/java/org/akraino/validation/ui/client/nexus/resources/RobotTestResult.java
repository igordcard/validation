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
public class RobotTestResult implements IResource {

    @JsonProperty("robot")
    private Robot robot;

    private String name;

    public RobotTestResult() {

    }

    public Robot getRobot() {
        return this.robot;
    }

    public void setRobot(Robot robot) {
        this.robot = robot;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public class Robot {
        @JsonProperty("suite")
        private Suite suite;

        @JsonProperty("statistics")
        private Statistics statistics;

        @JsonProperty("generated")
        private String generated;

        @JsonProperty("generator")
        private String generator;

        @JsonProperty("errors")
        private String errors;

        public Robot() {

        }

        public Suite getSuite() {
            return this.suite;
        }

        public void setSuite(Suite suite) {
            this.suite = suite;
        }

        public Statistics getStatistics() {
            return this.statistics;
        }

        public void setStatistics(Statistics statistics) {
            this.statistics = statistics;
        }

        public String getGenerated() {
            return this.generated;
        }

        public void setGenerated(String generated) {
            this.generated = generated;
        }

        public String getGenerator() {
            return this.generator;
        }

        public void setGenerator(String generator) {
            this.generator = generator;
        }

        public String getErrors() {
            return this.errors;
        }

        public void setErrors(String errors) {
            this.errors = errors;
        }

        public class Suite {
            @JsonProperty("status")
            private Suite.Status status;

            @JsonProperty("_id")
            private String suiteId;

            @JsonProperty("_name")
            private String name;

            public Suite() {

            }

            public String getSuiteId() {
                return this.suiteId;
            }

            public void setSuiteId(String suiteId) {
                this.suiteId = suiteId;
            }

            public String getName() {
                return this.name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public Suite.Status getStatus() {
                return this.status;
            }

            public void setStatus(Suite.Status status) {
                this.status = status;
            }

            public class Status {
                @JsonProperty("_status")
                private String statusValue;

                @JsonProperty("_starttime")
                private String startTime;

                @JsonProperty("_endtime")
                private String endTime;

                public Status() {

                }

                public String getStatusValue() {
                    return this.statusValue;
                }

                public void setStatusValue(String statusValue) {
                    this.statusValue = statusValue;
                }

                public String getStartTime() {
                    return this.startTime;
                }

                public void setStartTime(String startTime) {
                    this.startTime = startTime;
                }

                public String getEndTime() {
                    return this.endTime;
                }

                public void setEndTime(String endTime) {
                    this.endTime = endTime;
                }
            }

        }

        public class Statistics {
            @JsonProperty("suite")
            private Suite suite;

            @JsonProperty("total")
            private Total total;

            @JsonProperty("tag")
            private TagStat tag;

            public Statistics() {

            }

            public Suite getSuite() {
                return this.suite;
            }

            public void setSuite(Suite suite) {
                this.suite = suite;
            }

            public Total getTotal() {
                return this.total;
            }

            public void setTotal(Total total) {
                this.total = total;
            }

            public TagStat getTag() {
                return this.tag;
            }

            public void setTag(TagStat tag) {
                this.tag = tag;
            }

            public class Suite {
                @JsonProperty("stat")
                private List<Status> stat;

                public Suite() {

                }

                public List<Status> getStat() {
                    return this.stat;
                }

                public void setStat(List<Status> stat) {
                    this.stat = stat;
                }
            }

            public class Total {
                @JsonProperty("stat")
                private List<Status> stat;

                public Total() {

                }

                public List<Status> getStat() {
                    return this.stat;
                }

                public void setStat(List<Status> stat) {
                    this.stat = stat;
                }
            }

            public class TagStat {
                @JsonProperty("stat")
                private List<Status> stat;

                public TagStat() {

                }

                public List<Status> getStat() {
                    return this.stat;
                }

                public void setStat(List<Status> stat) {
                    this.stat = stat;
                }
            }
        }
    }

}
