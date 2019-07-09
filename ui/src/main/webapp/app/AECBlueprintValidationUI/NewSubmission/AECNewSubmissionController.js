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

var app = angular.module('AECNewSubmission');
app
        .controller(
                'AECNewSubmissionController',
                function($scope, restAPISvc) {

                    initialize();

                    function initialize() {
                        restAPISvc
                                .getRestAPI(
                                        "/api/blueprintInstanceForValidation/",
                                        function(data) {
                                            $scope.blueprintInstancesForValidation = data;
                                            $scope.blueprintNames = [];
                                            angular
                                                    .forEach(
                                                            $scope.blueprintInstancesForValidation,
                                                            function(
                                                                    blueprintInstance) {
                                                                if ($scope.blueprintNames
                                                                        .indexOf(blueprintInstance["blueprint"]["blueprintName"]) === -1) {
                                                                    $scope.blueprintNames
                                                                            .push(blueprintInstance["blueprint"]["blueprintName"]);
                                                                }
                                                            });
                                        });
                        restAPISvc.getRestAPI("/api/timeslots/",
                                function(data) {
                                    $scope.timeslots = data;
                                    $scope.declerativeTimeslots = [];
                                    angular.forEach($scope.timeslots, function(
                                            timeslot) {
                                        var temp = "id: " + timeslot.timeslotId
                                                + " Start date and time: "
                                                + timeslot.startDateTime
                                                /*
                                                 * + " duration(in sec) :" +
                                                 * blueprintInstance["timeslot"].duration
                                                 */
                                                + " lab :" + timeslot.lab.lab;
                                        $scope.declerativeTimeslots.push(temp);
                                    });
                                });
                    }
                    $scope.selectedBluePrintNameChange = function() {
                        $scope.blueprintVersions = [];
                        $scope.blueprintLayers = [];
                        angular
                                .forEach(
                                        $scope.blueprintInstancesForValidation,
                                        function(blueprintInstance) {
                                            if ($scope.selectedBlueprintName === blueprintInstance["blueprint"]["blueprintName"]) {
                                                if ($scope.blueprintVersions
                                                        .indexOf(blueprintInstance["version"]) === -1) {
                                                    $scope.blueprintVersions
                                                            .push(blueprintInstance["version"]);
                                                }
                                            }
                                        });
                    }
                    $scope.selectedBluePrintVersionChange = function() {
                        $scope.blueprintLayers = [];
                        angular
                                .forEach(
                                        $scope.blueprintInstancesForValidation,
                                        function(blueprintInstance) {
                                            if ($scope.selectedBlueprintName === blueprintInstance["blueprint"]["blueprintName"]) {
                                                if ($scope.selectedBlueprintVersion === blueprintInstance["version"]) {
                                                    if ($scope.blueprintLayers
                                                            .indexOf(blueprintInstance["layer"]) === -1) {
                                                        $scope.blueprintLayers
                                                                .push(blueprintInstance["layer"]);
                                                    }
                                                }
                                            }
                                        });
                    }

                    $scope.submit = function() {
                        if (!$scope.selectedBlueprintName
                                || !$scope.selectedBlueprintVersion
                                || !$scope.selectedBlueprintLayer
                                || !$scope.selectedDeclerativeTimeslot) {
                            confirm("You must specify all data fields");
                            return;
                        }
                        var finalBlueprint;
                        var finalTimeslot;
                        angular
                                .forEach(
                                        $scope.blueprintInstancesForValidation,
                                        function(blueprintInstance) {
                                            if (blueprintInstance["blueprint"]["blueprintName"] === $scope.selectedBlueprintName) {
                                                if (blueprintInstance["version"] === $scope.selectedBlueprintVersion) {
                                                    if (blueprintInstance["layer"] === $scope.selectedBlueprintLayer) {
                                                        finalBlueprint = blueprintInstance;
                                                    }
                                                }
                                            }
                                        });
                        var selectedDeclerativeTimeslotId = $scope.selectedDeclerativeTimeslot
                                .substring(
                                        $scope.selectedDeclerativeTimeslot
                                                .indexOf("id:") + 4,
                                        $scope.selectedDeclerativeTimeslot
                                                .indexOf("Start date and time:") - 1);
                        angular
                                .forEach(
                                        $scope.timeslots,
                                        function(timeslot) {
                                            if (selectedDeclerativeTimeslotId
                                                    .toString().trim() === timeslot.timeslotId
                                                    .toString().trim()) {
                                                finalTimeslot = timeslot;
                                            }
                                        });
                        var submission = {
                            "blueprintInstanceForValidation" : finalBlueprint,
                            "timeslot" : finalTimeslot
                        };
                        restAPISvc
                                .postRestAPI(
                                        "/api/submission/",
                                        submission,
                                        function(data) {
                                            if (data !== undefined) {
                                                var confirmText = "The blueprint instance for validation has been submitted successfully. Submissionn id:"
                                                        + data.submissionId;
                                                confirm(confirmText);
                                            } else {
                                                confirm("Error when committing the submission");
                                            }
                                        });
                        $scope.selectedBlueprintName = {};
                        $scope.selectedBlueprintVersion = {};
                        $scope.selectedBlueprintLayer = {};
                        $scope.selectedDeclerativeTimeslot = {};
                    }

                });
