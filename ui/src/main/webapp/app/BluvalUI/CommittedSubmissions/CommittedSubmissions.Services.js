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
                    svc.getLayer = function(validationNexusTestResult) {
                        if (validationNexusTestResult.allLayers) {
                            return "all";
                        }
                        var layers = [];
                        angular
                                .forEach(
                                        validationNexusTestResult.wRobotNexusTestResults,
                                        function(result) {
                                            layers.push(result.blueprintLayer);
                                        });
                        return layers;
                    };
                    svc.getResultUrl = function(submissionData) {
                        if (submissionData.status !== "Completed") {
                            return null;
                        }
                        if (!submissionData.validationNexusTestResult.wRobotNexusTestResults) {
                            return null;
                        }
                        if (submissionData.validationNexusTestResult.wRobotNexusTestResults.length === 0) {
                            return null;
                        }
                        var resultExistence = false;
                        angular
                                .forEach(
                                        submissionData.validationNexusTestResult.wRobotNexusTestResults,
                                        function(result) {
                                            if (result.robotTestResults
                                                    && result.robotTestResults.length > 0) {
                                                resultExistence = true;
                                            }
                                        });
                        if (resultExistence) {
                            return "https://nexus.akraino.org/content/sites/logs/"
                                    + submissionData.validationNexusTestResult.silo
                                    + "/"
                                    + submissionData.validationNexusTestResult.blueprintName
                                    + "/"
                                    + submissionData.validationNexusTestResult.version
                                    + "/"
                                    + submissionData.validationNexusTestResult.timestamp
                                    + "/";
                        }
                        return null;
                    };
                    svc.mapResult = function(validationNexusTestResult) {
                        if (!validationNexusTestResult.timestamp) {
                            return null;
                        }
                        if (!validationNexusTestResult.wRobotNexusTestResults) {
                            return null;
                        }
                        if (validationNexusTestResult.wRobotNexusTestResults.length === 0) {
                            return null;
                        }
                        var resultExistence = false;
                        angular
                                .forEach(
                                        validationNexusTestResult.wRobotNexusTestResults,
                                        function(result) {
                                            if (result.robotTestResults
                                                    && result.robotTestResults.length > 0) {
                                                resultExistence = true;
                                            }
                                        });
                        if (resultExistence) {
                            if (validationNexusTestResult.result === true) {
                                return 'SUCCESS';
                            }
                            return 'FAILURE'
                        }
                        return null;
                    };
                    return svc;
                } ]);