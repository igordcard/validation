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

var app = angular.module('AECGetBySubmissionId');
app
        .controller(
                'AECGetBySubmissionIdController',
                function($scope, restAPISvc) {

                    initialize();

                    function initialize() {
                        $scope.loading = false;
                        $scope.showResults = false;
                        $scope.results = [];
                        $scope.resultsLayers = [];
                        $scope.resultsLayerTestSuitesNames = [];
                        $scope.selectedRobotTestResult = [];
                        restAPISvc
                                .getRestAPI(
                                        "/api/submission/",
                                        function(data) {
                                            $scope.submissions = data;
                                            $scope.submissionsForDisplay = [];
                                            angular
                                                    .forEach(
                                                            $scope.submissions,
                                                            function(
                                                                    submissionData) {
                                                                if (submissionData.submissionStatus === "Completed") {
                                                                    var temp = "id: "
                                                                            + submissionData.submissionId
                                                                            + " blueprint: "
                                                                            + submissionData.blueprintInstanceForValidation.blueprint.blueprintName
                                                                            + " version: "
                                                                            + submissionData.blueprintInstanceForValidation.version
                                                                            + " layer: "
                                                                            + submissionData.blueprintInstanceForValidation.layer
                                                                            + " lab: "
                                                                            + submissionData.timeslot.lab.lab
                                                                            + " Start date and time: "
                                                                            + submissionData.timeslot.startDateTime
                                                                    /*
                                                                     * + "
                                                                     * duration: " +
                                                                     * submissionData.blueprintInstanceForValidation.timeslot.duration
                                                                     */;
                                                                    $scope.submissionsForDisplay
                                                                            .push(temp);
                                                                }
                                                            });
                                        });
                    }
                    $scope.selectedSubmissionChange = function(
                            selectedSubmission) {
                        $scope.results = [];
                        $scope.resultsLayers = [];
                        $scope.resultsLayerTestSuitesNames = [];
                        $scope.selectedRobotTestResult = [];
                        $scope.loading = true;
                        $scope.showResults = false;
                        var id = selectedSubmission.substring(
                                selectedSubmission.indexOf("id:") + 4,
                                selectedSubmission.indexOf("blueprint") - 1);
                        restAPISvc
                                .getRestAPI(
                                        "/api/results/getBySubmissionId/" + id,
                                        function(data) {
                                            $scope.loading = false;
                                            if (data !== undefined) {
                                                $scope.results = data;
                                                angular
                                                        .forEach(
                                                                $scope.results,
                                                                function(result) {
                                                                    $scope.resultsLayers
                                                                            .push(result.blueprintLayer);
                                                                });
                                                $scope.showResults = true;
                                            } else {
                                                confirm("Error when committing the submission");
                                            }
                                        });
                    }

                    $scope.selectedResultsLayerChange = function(selectedLayer) {
                        $scope.resultsLayerTestSuitesNames = [];
                        $scope.robotTestResults = [];
                        $scope.selectedRobotTestResult = [];
                        var selectedLayerResult = [];
                        angular.forEach($scope.results, function(result) {
                            if (result.blueprintLayer === selectedLayer) {
                                selectedLayerResult = result;
                            }
                        });
                        $scope.robotTestResults = selectedLayerResult.robotTestResults;
                        angular.forEach($scope.robotTestResults, function(
                                robotTestResult) {
                            $scope.resultsLayerTestSuitesNames
                                    .push(robotTestResult.name);
                        });
                    }

                    $scope.selectedTestSuitesNameChange = function(
                            selectedTestSuiteName) {
                        angular
                                .forEach(
                                        $scope.robotTestResults,
                                        function(robotTestResult) {
                                            if (robotTestResult.name.trim() === selectedTestSuiteName
                                                    .trim()) {
                                                $scope.selectedRobotTestResult = robotTestResult;
                                            }
                                        });
                    }

                });
