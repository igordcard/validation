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

var app = angular.module('CommittedSubmissions');

app
        .factory(
                'committedSubmissionsSvc',
                [ function() {
                    var svc = [];
                    svc.getLayer = function(validationDbTestResult) {
                        if (!validationDbTestResult) {
                            return null;
                        }
                        if (validationDbTestResult.allLayers) {
                            return "all";
                        }
                        var layers = [];
                        angular.forEach(
                                validationDbTestResult.wrobotDbTestResults,
                                function(result) {
                                    layers.push(result.layer);
                                });
                        return layers;
                    };
                    svc.getResultUrl = function(submission) {
                        if (submission.submissionStatus !== "Completed") {
                            return null;
                        }
                        if (!submission.validationDbTestResult) {
                            return null;
                        }
                        if (!submission.validationDbTestResult.wrobotDbTestResults) {
                            return null;
                        }
                        if (submission.validationDbTestResult.wrobotDbTestResults.length === 0) {
                            return null;
                        }
                        if (submission.validationDbTestResult.dateStorage) {
                            return "https://nexus.akraino.org/content/sites/logs/"
                                    + submission.timeslot.labInfo.silo
                                    + "/"
                                    + submission.validationDbTestResult.blueprintInstance.blueprint.blueprintName
                                    + "/"
                                    + submission.validationDbTestResult.blueprintInstance.version
                                    + "/"
                                    + submission.validationDbTestResult.timestamp
                                    + "/";
                        }
                        return null;
                    };
                    svc.mapResult = function(validationDbTestResult) {
                        if (validationDbTestResult
                                && validationDbTestResult.dateStorage) {
                            if (validationDbTestResult.result === true) {
                                return 'SUCCESS';
                            }
                            return 'FAILURE'
                        }
                        return null;
                    };
                    return svc;
                } ]);