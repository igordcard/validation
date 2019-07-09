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

            @JsonProperty("suite")
            private Suite.NestedSuite suite;

            @JsonProperty("id")
            private String id;

            @JsonProperty("name")
            private String name;

            @JsonProperty("source")
            private String source;

            public Suite() {

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

            public String getSource() {
                return this.source;
            }

            public void setSource(String source) {
                this.source = source;
            }

            public Suite.Status getStatus() {
                return this.status;
            }

            public void setStatus(Suite.Status status) {
                this.status = status;
            }

            public Suite.NestedSuite getSuite() {
                return this.suite;
            }

            public void setSuite(Suite.NestedSuite suite) {
                this.suite = suite;
            }

            public class Status {
                @JsonProperty("status")
                private String status;

                @JsonProperty("starttime")
                private String starttime;

                @JsonProperty("endtime")
                private String endtime;

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
            }

            public class NestedSuite {
                @JsonProperty("doc")
                private String doc;

                @JsonProperty("id")
                private String id;

                @JsonProperty("name")
                private String name;

                @JsonProperty("source")
                private String source;

                @JsonProperty("test")
                private List<Test> test;

                @JsonProperty("kw")
                private List<Kw> kw;

                @JsonProperty("status")
                private NestedSuite.Status status;

                public NestedSuite() {

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

                public String getSource() {
                    return this.source;
                }

                public void setSource(String source) {
                    this.source = source;
                }

                public List<Test> getTest() {
                    return this.test;
                }

                public void setTest(List<Test> test) {
                    this.test = test;
                }

                public List<Kw> getKw() {
                    return this.kw;
                }

                public void setKw(List<Kw> kw) {
                    this.kw = kw;
                }

                public NestedSuite.Status getStatus() {
                    return this.status;
                }

                public void setStatus(NestedSuite.Status status) {
                    this.status = status;
                }

                public class Status {
                    @JsonProperty("status")
                    private String status;

                    @JsonProperty("starttime")
                    private String starttime;

                    @JsonProperty("endtime")
                    private String endtime;

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
