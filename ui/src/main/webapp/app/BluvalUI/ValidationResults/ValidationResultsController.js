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
        .controller(
                'ValidationResultsController',
                function($scope, restAPISvc, generalValidationResultsSvc,
                        $window, appContext, $location, $modal, $rootScope) {

                    $scope.getBlueprintLayers = generalValidationResultsSvc.getBlueprintLayers;
                    $scope.mapResult = generalValidationResultsSvc.mapResult;
                    $scope.filterWithLayer = generalValidationResultsSvc.filterWithLayer;
                    $scope.filterWithResult = generalValidationResultsSvc.filterWithResult;
                    $scope.filterWithTimestamp = generalValidationResultsSvc.filterWithTimestamp;

                    initialize();

                    function initialize() {
                        $scope.loadingResults = true;
                        $scope.validationDbTestResults = [];
                        $scope.silos = [];
                        var searchObject = $location.search();
                        var submissionId = searchObject.submissionId;
                        var blueprintName = searchObject.blueprintName;
                        var version = searchObject.version;
                        var lab = searchObject.lab;
                        var allLayers = searchObject.allLayers;
                        var layer = searchObject.layer;
                        var optional = searchObject.optional;
                        var outcome = searchObject.outcome;
                        var timestamp = searchObject.timestamp;
                        var date = searchObject.date;
                        var reqUrl = "";
                        if (submissionId) {
                            reqUrl = "/api/v1/results/getbysubmissionid/"
                                    + submissionId;
                        } else if (outcome !== undefined && !layer) {
                            reqUrl = "/api/v1/results/getlastrun/" + lab + "/"
                                    + blueprintName + "/" + version + "/"
                                    + allLayers + "/" + optional + "/"
                                    + outcome;
                        } else if (outcome !== undefined) {
                            var layers = [];
                            layers.push(layer);
                            reqUrl = "/api/v1/results/getlastrunoflayers/"
                                    + lab + "/" + blueprintName + "/" + version
                                    + "/" + layers + "/" + optional + "/"
                                    + outcome;
                        } else if (timestamp) {
                            reqUrl = "/api/v1/results/getbytimestamp/" + lab
                                    + "/" + blueprintName + "/" + version + "/"
                                    + timestamp;
                        } else if (date) {
                            reqUrl = "/api/v1/results/getbasedondate/" + lab
                                    + "/" + blueprintName + "/" + version + "/"
                                    + date;
                        } else {
                            reqUrl = "/api/v1/results/getmostrecent/"
                                    + blueprintName + "/" + version + "/" + lab;
                        }
                        restAPISvc
                                .getRestAPI(
                                        reqUrl,
                                        function(resultData) {
                                            if (resultData) {
                                                $scope.loadingResults = false;
                                                if (!Array.isArray(resultData)) {
                                                    $scope.validationDbTestResults
                                                            .push(resultData);
                                                } else {
                                                    $scope.validationDbTestResults = resultData;
                                                }
                                            } else {
                                                confirm("No data was found");
                                                $scope.loadingResults = false;
                                            }
                                        });
                        $scope.descending = true;
                    }

                    $scope.dateTimeSort = function(validationDbTestResult) {
                        return new Date(validationDbTestResult.dateStorage)
                                .getTime();
                    }

                    $scope.descendingOrder = function() {
                        $scope.descending = true;
                    }

                    $scope.ascendingOrder = function() {
                        $scope.descending = false;
                    }

                    $scope.refreshValidationResults = function() {
                        initialize();
                    }

                    $scope.getTestSuiteResults = function(
                            validationDbTestResult) {
                        if (!generalValidationResultsSvc
                                .mapResult(validationDbTestResult)) {
                            return;
                        }
                        var scope = $rootScope.$new();
                        scope.params = validationDbTestResult;
                        $modal
                                .open({
                                    scope : scope,
                                    templateUrl : 'app/BluvalUI/ValidationResults/TestSuiteResults/TestSuiteResultsModal.html',
                                    controller : 'TestSuiteResultsController'
                                });
                    }

                });
