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

var AECBlueprintValidationUIApp = angular
        .module('BlueprintValidationUIManagement');

AECBlueprintValidationUIApp
        .controller(
                'AECNewSubmissionController',
                function($scope, appContext, restAPISvc) {

                    initialize();

                    function initialize() {
                        restAPISvc
                                .getRestAPI(
                                        "/api/blueprintInstance/",
                                        function(data) {
                                            $scope.blueprintInstances = data;
                                            $scope.blueprintNames = [];
                                            angular
                                                    .forEach(
                                                            $scope.blueprintInstances,
                                                            function(
                                                                    blueprintInstance) {
                                                                if ($scope.blueprintNames
                                                                        .indexOf(blueprintInstance["blueprint"]["blueprintName"]) === -1) {
                                                                    $scope.blueprintNames
                                                                            .push(blueprintInstance["blueprint"]["blueprintName"]);
                                                                }
                                                            });
                                        });
                    }
                    $scope.selectedBluePrintNameChange = function() {
                        $scope.blueprintVersions = [];
                        $scope.blueprintLayers = [];
                        $scope.declerativeTimeslots = [];
                        angular
                                .forEach(
                                        $scope.blueprintInstances,
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
                        $scope.declerativeTimeslots = [];
                        angular
                                .forEach(
                                        $scope.blueprintInstances,
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
                    $scope.selectedBluePrintLayerChange = function() {
                        $scope.declerativeTimeslots = [];
                        angular
                                .forEach(
                                        $scope.blueprintInstances,
                                        function(blueprintInstance) {
                                            if ($scope.selectedBlueprintName === blueprintInstance["blueprint"]["blueprintName"]) {
                                                if ($scope.selectedBlueprintVersion === blueprintInstance["version"]) {
                                                    if ($scope.selectedBlueprintLayer === blueprintInstance["layer"]) {
                                                        var temp = "id: "
                                                                + blueprintInstance["timeslot"].timeslotId
                                                                + " Start date and time: "
                                                                + blueprintInstance["timeslot"].startDateTime
                                                                + " duration(in sec) :"
                                                                + blueprintInstance["timeslot"].duration
                                                                + " lab :"
                                                                + blueprintInstance["timeslot"].lab;
                                                        if ($scope.declerativeTimeslots
                                                                .indexOf(temp) === -1) {
                                                            $scope.declerativeTimeslots
                                                                    .push(temp);
                                                        }
                                                    }
                                                }
                                            }
                                        });
                    }
                    $scope.submit = function() {
                        var finalBlueprint;
                        angular
                                .forEach(
                                        $scope.blueprintInstances,
                                        function(blueprintInstance) {
                                            if (blueprintInstance["blueprint"]["blueprintName"] === $scope.selectedBlueprintName) {
                                                if (blueprintInstance["version"] === $scope.selectedBlueprintVersion) {
                                                    if (blueprintInstance["layer"] === $scope.selectedBlueprintLayer) {
                                                        var selectedDeclerativeTimeslotId = $scope.selectedDeclerativeTimeslot
                                                                .substring(
                                                                        $scope.selectedDeclerativeTimeslot
                                                                                .indexOf("id:") + 4,
                                                                        $scope.selectedDeclerativeTimeslot
                                                                                .indexOf("Start date and time:") - 1);
                                                        if (selectedDeclerativeTimeslotId
                                                                .toString()
                                                                .trim() === blueprintInstance["timeslot"]["timeslotId"]
                                                                .toString()
                                                                .trim()) {
                                                            finalBlueprint = blueprintInstance;
                                                        }
                                                    }
                                                }
                                            }
                                        });
                        var submission = {
                            "blueprintInstance" : finalBlueprint
                        };
                        restAPISvc
                                .postRestAPI(
                                        "/api/submission/",
                                        submission,
                                        function(data) {
                                            if (data !== undefined) {
                                                confirm("Submission committed successfully");
                                            } else {
                                                confirm("Error when committing the submission");
                                            }
                                        });
                    }

                });
