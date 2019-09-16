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

var app = angular.module('ValidationResults');

app
        .factory(
                'generalValidationResultsSvc',
                [ function() {
                    var svc = [];
                    svc.getBlueprintLayers = function(wrobotDbTestResults) {
                        if (!wrobotDbTestResults
                                || wrobotDbTestResults.length === 0) {
                            return null;
                        }
                        var layers = [];
                        angular
                                .forEach(
                                        angular.fromJson(wrobotDbTestResults),
                                        function(wrobotDbTestResult) {
                                            if (wrobotDbTestResult.layer !== undefined) {
                                                layers
                                                        .push(wrobotDbTestResult.layer);
                                            }
                                        });
                        return layers;
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
                    svc.filterWithLayer = function(validationDbTestResults,
                            filterLayer) {
                        if (filterLayer === undefined || filterLayer === '') {
                            return validationDbTestResults;
                        }
                        var filteredResults = [];
                        angular
                                .forEach(
                                        validationDbTestResults,
                                        function(validationDbTestResult) {
                                            angular
                                                    .forEach(
                                                            angular
                                                                    .fromJson(validationDbTestResult.wrobotDbTestResults),
                                                            function(
                                                                    wrobotDbTestResult) {
                                                                if (wrobotDbTestResult.layer
                                                                        .toLowerCase()
                                                                        .includes(
                                                                                filterLayer
                                                                                        .toLowerCase())) {
                                                                    filteredResults
                                                                            .push(validationDbTestResult);
                                                                }
                                                            });
                                        });
                        return filteredResults;
                    }
                    svc.filterWithResult = function(validationDbTestResults,
                            filterResult) {
                        if (filterResult === undefined || filterResult === '') {
                            return validationDbTestResults;
                        }
                        var filteredResults = [];
                        angular.forEach(validationDbTestResults, function(
                                validationDbTestResult) {
                            if (validationDbTestResult.result === true
                                    && 'success'.includes(filterResult
                                            .toLowerCase())) {
                                filteredResults.push(validationDbTestResult);
                            } else if (validationDbTestResult.result === false
                                    && 'failure'.includes(filterResult
                                            .toLowerCase())) {
                                filteredResults.push(validationDbTestResult);
                            }
                        });
                        return filteredResults;
                    }
                    svc.filterWithTimestamp = function(validationDbTestResults,
                            filterTimestamp) {
                        if (filterTimestamp === undefined
                                || filterTimestamp === '') {
                            return validationDbTestResults;
                        }
                        var filteredResults = [];
                        angular.forEach(validationDbTestResults, function(
                                validationDbTestResult) {
                            if (validationDbTestResult.timestamp
                                    && validationDbTestResult.timestamp
                                            .toLowerCase().includes(
                                                    filterTimestamp
                                                            .toLowerCase())) {
                                filteredResults.push(validationDbTestResult);
                            }
                        });
                        return filteredResults;
                    }
                    return svc;
                } ]);