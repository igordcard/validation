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

var app = angular.module('NewSubmission');
app
        .controller(
                'NewSubmissionController',
                function($scope, restAPISvc) {

                    initialize();

                    function initialize() {
                        $scope.blueprintInstances = [];
                        $scope.blueprintInstance = '';
                        $scope.blueprintNames = [];
                        $scope.blueprintVersions = [];
                        $scope.declerativeTimeslots = [];
                        $scope.blueprintLayers = [];
                        $scope.optionals = [];
                        $scope.selectedBlueprintName = '';
                        $scope.selectedBlueprintVersion = '';
                        $scope.selectedDeclerativeTimeslot = '';
                        $scope.selectedBlueprintLayer = '';
                        $scope.selectedOptional = '';
                        restAPISvc
                                .getRestAPI(
                                        "/api/v1/blueprintinstance/",
                                        function(data) {
                                            $scope.blueprintInstances = data;
                                            angular
                                                    .forEach(
                                                            $scope.blueprintInstances,
                                                            function(
                                                                    blueprintInstance) {
                                                                if ($scope.blueprintNames
                                                                        .indexOf(blueprintInstance["blueprint"]["blueprintName"]
                                                                                .trim()) === -1) {
                                                                    $scope.blueprintNames
                                                                            .push(blueprintInstance["blueprint"]["blueprintName"]
                                                                                    .trim());
                                                                }
                                                            });
                                        });
                    }
                    $scope.selectedBlueprintNameChange = function() {
                        $scope.blueprintInstance = '';
                        $scope.blueprintVersions = [];
                        $scope.declerativeTimeslots = [];
                        $scope.blueprintLayers = [];
                        $scope.optionals = [];
                        $scope.selectedBlueprintVersion = '';
                        $scope.selectedDeclerativeTimeslot = '';
                        $scope.selectedBlueprintLayer = '';
                        $scope.selectedOptional = '';
                        angular
                                .forEach(
                                        $scope.blueprintInstances,
                                        function(blueprintInstance) {
                                            if ($scope.selectedBlueprintName
                                                    .trim() === blueprintInstance["blueprint"]["blueprintName"]
                                                    .trim()) {
                                                if ($scope.blueprintVersions
                                                        .indexOf(blueprintInstance["version"]
                                                                .trim()) === -1) {
                                                    $scope.blueprintVersions
                                                            .push(blueprintInstance["version"]
                                                                    .trim());
                                                }
                                            }
                                        });
                    }
                    $scope.selectedBlueprintVersionChange = function() {
                        if (!$scope.selectedBlueprintName) {
                            return;
                        }
                        $scope.blueprintInstance = '';
                        $scope.declerativeTimeslots = [];
                        $scope.blueprintLayers = [];
                        $scope.optionals = [];
                        $scope.selectedDeclerativeTimeslot = '';
                        $scope.selectedBlueprintLayer = '';
                        $scope.selectedOptional = '';
                        angular
                                .forEach(
                                        $scope.blueprintInstances,
                                        function(blueprintInstance) {
                                            if ($scope.selectedBlueprintName
                                                    .trim() === blueprintInstance["blueprint"]["blueprintName"]
                                                    .trim()) {
                                                if ($scope.selectedBlueprintVersion
                                                        .trim() === blueprintInstance["version"]
                                                        .trim()) {
                                                    $scope.blueprintInstance = blueprintInstance;
                                                }
                                            }
                                        });
                        if (!$scope.blueprintInstance
                                || !$scope.blueprintInstance.timeslots
                                || $scope.blueprintInstance.timeslots.length === 0) {
                            confirm("No available timeslots for this blueprint instance in this lab");
                            return;
                        }
                        angular.forEach($scope.blueprintInstance.timeslots,
                                function(timeslot) {
                                    var temp = "id: " + timeslot.timeslotId
                                            + " Start date and time: "
                                            + timeslot.startDateTime
                                            /*
                                             * + " duration(in sec) :" +
                                             * blueprintInstance["timeslot"].duration
                                             */
                                            + " lab :" + timeslot.labInfo.lab;
                                    $scope.declerativeTimeslots.push(temp);
                                });
                    }

                    $scope.selectedDeclerativeTimeslotChange = function() {
                        $scope.blueprintLayers = [];
                        $scope.optionals = [];
                        $scope.selectedBlueprintLayer = {};
                        $scope.selectedOptional = "";
                        angular.forEach(
                                $scope.blueprintInstance.blueprintLayers,
                                function(layer) {
                                    $scope.blueprintLayers.push(layer.layer);
                                });
                        $scope.blueprintLayers.push("all");
                    }

                    $scope.selectedBlueprintLayerChange = function() {
                        $scope.optionals = [];
                        $scope.selectedOptional = "";
                        $scope.optionals = [ 'true', 'false' ];
                    }

                    $scope.submit = function() {
                        if (!$scope.selectedBlueprintName
                                || !$scope.selectedBlueprintVersion
                                || !$scope.blueprintInstance
                                || !$scope.selectedBlueprintLayer
                                || !$scope.selectedOptional
                                || !$scope.selectedDeclerativeTimeslot) {
                            confirm("You must specify all data fields");
                            return;
                        }
                        var finalTimeslot;
                        var selectedDeclerativeTimeslotId = $scope.selectedDeclerativeTimeslot
                                .substring(
                                        $scope.selectedDeclerativeTimeslot
                                                .indexOf("id:") + 4,
                                        $scope.selectedDeclerativeTimeslot
                                                .indexOf("Start date and time:") - 1);
                        angular
                                .forEach(
                                        $scope.blueprintInstance.timeslots,
                                        function(timeslot) {
                                            if (selectedDeclerativeTimeslotId
                                                    .toString().trim() === timeslot.timeslotId
                                                    .toString().trim()) {
                                                finalTimeslot = timeslot;
                                            }
                                        });
                        var allLayers = "false";
                        if ($scope.selectedBlueprintLayer === 'all') {
                            allLayers = "true";
                        }
                        var wrobotTestResults = [];
                        if (allLayers === "false") {
                            wrobotTestResults = [ {
                                "layer" : $scope.selectedBlueprintLayer
                            } ];
                        }

                        var validationDbTestResult = {
                            "blueprintInstance" : $scope.blueprintInstance,
                            "allLayers" : allLayers,
                            "wrobotDbTestResults" : wrobotTestResults,
                            "optional" : $scope.selectedOptional,
                            "lab" : finalTimeslot.labInfo
                        };
                        var submission = {
                            "validationDbTestResult" : validationDbTestResult,
                            "timeslot" : finalTimeslot
                        };
                        restAPISvc
                                .postRestAPI(
                                        "/api/v1/submission/",
                                        submission,
                                        function(data) {
                                            if (data) {
                                                var confirmText = "The blueprint instance for validation has been submitted successfully. Submission id:"
                                                        + data.submissionId;
                                                confirm(confirmText);
                                            } else {
                                                confirm("Error when committing the submission");
                                            }
                                        });
                        $scope.selectedBlueprintName = '';
                        $scope.selectedBlueprintVersion = '';
                        $scope.selectedBlueprintLayer = '';
                        $scope.selectedOptional = "";
                        $scope.selectedDeclerativeTimeslot = '';
                        $scope.blueprintInstance = '';
                    }

                });
