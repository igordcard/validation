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

var app = angular.module('RegisterTimeslot');
app
        .controller(
                'RegisterTimeslotController',
                function($scope, restAPISvc, $q) {

                    initialize();

                    function initialize() {
                        $scope.loadingLabs = true;
                        $scope.loadingBlueprintInstances = true;
                        $scope.definedStart = "now";
                        $scope.labInfos = [];
                        $scope.labs = [];
                        $scope.selectedLab = '';
                        $scope.blueprintInstances = [];
                        $scope.declerativeInsts = [];
                        $scope.selectedDeclerativeInst = '';
                        $scope.insts = [];
                        restAPISvc
                                .getRestAPI(
                                        "/api/v1/lab/",
                                        function(data) {
                                            if (data) {
                                                $scope.labInfos = data;
                                                angular.forEach(data, function(
                                                        lab) {
                                                    $scope.labs.push(lab.lab);
                                                });
                                                restAPISvc
                                                        .getRestAPI(
                                                                "/api/v1/blueprintinstance/",
                                                                function(data2) {
                                                                    if (data2) {
                                                                        $scope.blueprintInstances = data2;
                                                                        angular
                                                                                .forEach(
                                                                                        $scope.blueprintInstances,
                                                                                        function(
                                                                                                blueprintInstance) {
                                                                                            var temp = "id: "
                                                                                                    + blueprintInstance.blueprintInstanceId
                                                                                                    + " name: "
                                                                                                    + blueprintInstance["blueprint"]["blueprintName"]
                                                                                                    + " version: "
                                                                                                    + blueprintInstance["version"];
                                                                                            $scope.declerativeInsts
                                                                                                    .push(temp);
                                                                                        });
                                                                    } else {
                                                                        confirm("No blueprint instances found");
                                                                    }
                                                                });
                                            } else {
                                                confirm("No labs found");
                                            }
                                            $scope.loadingLabs = false;
                                            $scope.loadingBlueprintInstances = false;
                                        });
                    }

                    $scope.addInst = function(selectedDeclerativeInst) {
                        if ($scope.insts
                                .indexOf(selectedDeclerativeInst.trim()) === -1) {
                            $scope.insts.push(selectedDeclerativeInst);
                        }
                    }

                    $scope.deleteInst = function(index) {
                        $scope.insts.splice(index, 1);
                    }

                    $scope.register = function() {
                        if (!$scope.selectedLab || !$scope.insts
                                || $scope.insts.length === 0) {
                            confirm("You must specify all the fields");
                            return;
                        }
                        var lab = '';
                        angular.forEach($scope.labInfos, function(labInfo) {
                            if (labInfo.lab.trim() === $scope.selectedLab) {
                                lab = labInfo;
                            }
                        });
                        if (!lab) {
                            confirm("Error in lab data");
                            return;
                        }
                        var timeslot = {
                            "startDateTime" : $scope.definedStart,
                            "labInfo" : lab
                        };
                        restAPISvc
                                .postRestAPI(
                                        "/api/v1/timeslot/",
                                        timeslot,
                                        function(data) {
                                            if (data) {
                                                var confirmText = "The timeslot has been registered successfully. Timeslot id:"
                                                        + data.timeslotId;
                                                confirm(confirmText);
                                                updateBlusInsts(data);
                                            } else {
                                                confirm("Error when registering the timeslot");
                                            }
                                        });
                    }

                    function updateBlusInsts(timeslot) {
                        var blueprintInstances = [];
                        angular
                                .forEach(
                                        $scope.insts,
                                        function(inst) {
                                            var id = inst.substring(inst
                                                    .indexOf("id:") + 4, inst
                                                    .indexOf("name") - 1);
                                            angular
                                                    .forEach(
                                                            $scope.blueprintInstances,
                                                            function(
                                                                    blueprintInstance) {
                                                                if (blueprintInstance.blueprintInstanceId
                                                                        .toString()
                                                                        .trim() === id
                                                                        .toString()
                                                                        .trim()) {
                                                                    blueprintInstances
                                                                            .push(blueprintInstance);
                                                                }
                                                            });
                                        });
                        if (!blueprintInstances) {
                            confirm("Error in blueprint instances data");
                            return;
                        }
                        var promises = [];
                        angular
                                .forEach(
                                        blueprintInstances,
                                        function(blueprintInstance) {
                                            if (blueprintInstance.timeslots) {
                                                blueprintInstance.timeslots
                                                        .push(timeslot);
                                            } else {
                                                blueprintInstance.timeslots = [ timeslot ];
                                            }
                                            promises
                                                    .push(restAPISvc
                                                            .postRestAPI(
                                                                    "/api/v1/blueprintinstance/",
                                                                    blueprintInstance,
                                                                    function(
                                                                            data) {
                                                                        if (data) {
                                                                            var text = "Blueprint instance: "
                                                                                    + blueprintInstance.blueprint.blueprintName
                                                                                    + " version: "
                                                                                    + blueprintInstance.version
                                                                                    + " updated successfully";
                                                                            confirm(text);
                                                                        } else {
                                                                            var text2 = "Failed to update blueprint instance: "
                                                                                    + blueprintInstance.blueprint.blueprintName
                                                                                    + " version: "
                                                                                    + blueprintInstance.version;
                                                                            confirm(text2);
                                                                        }
                                                                    }));
                                        });
                        $q
                                .all(promises)
                                .then(
                                        function() {
                                            confirm("All blueprint instances have been updated");
                                            $scope.selectedLab = '';
                                            $scope.selectedDeclerativeInst = '';
                                            $scope.insts = [];
                                            initialize();
                                        });
                    }
                });
