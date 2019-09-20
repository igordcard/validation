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

var app = angular.module('ModifyBlueprintInstance');
app
        .controller(
                'ModifyBlueprintInstanceController',
                function($scope, restAPISvc) {

                    initialize();

                    function initialize() {
                        $scope.loadingBlueprintInstances = true;
                        $scope.loadingLayers = true;
                        $scope.blueprintInstanceInfos = [];
                        $scope.declerativeInsts = [];
                        $scope.selectedBlueprintInstance = '';
                        $scope.layers = [];
                        $scope.layerInfos = [];
                        $scope.selectedLayer = '';
                        $scope.configuredLayers = [];
                        $scope.oldLayers = [];
                        $scope.newLayers = [];
                        restAPISvc
                                .getRestAPI(
                                        "/api/v1/blueprintinstance/",
                                        function(data) {
                                            if (data) {
                                                $scope.blueprintInstanceInfos = data;
                                                angular
                                                        .forEach(
                                                                data,
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
                                                                    restAPISvc
                                                                            .getRestAPI(
                                                                                    "/api/v1/layer/",
                                                                                    function(
                                                                                            data2) {
                                                                                        if (data2) {
                                                                                            $scope.layerInfos = data2;
                                                                                            angular
                                                                                                    .forEach(
                                                                                                            data2,
                                                                                                            function(
                                                                                                                    layer) {
                                                                                                                if ($scope.layers
                                                                                                                        .indexOf(layer.layer) === -1) {
                                                                                                                    $scope.layers
                                                                                                                            .push(layer.layer);
                                                                                                                }
                                                                                                            });
                                                                                        } else {
                                                                                            confirm("No layers found");
                                                                                        }
                                                                                    });
                                                                });
                                            } else {
                                                confirm("No blueprint instances found");
                                            }
                                            $scope.loadingBlueprintInstances = false;
                                            $scope.loadingLayers = false;
                                        });
                    }

                    $scope.selectedBlueprintInstanceChange = function() {
                        $scope.oldLayers = [];
                        $scope.newLayers = [];
                        var finalBlueprintInstanceInfo = '';
                        var id = $scope.selectedBlueprintInstance
                                .substring($scope.selectedBlueprintInstance
                                        .indexOf("id:") + 4,
                                        $scope.selectedBlueprintInstance
                                                .indexOf("name") - 1);
                        angular
                                .forEach(
                                        $scope.blueprintInstanceInfos,
                                        function(blueprintInstanceInfo) {
                                            if (blueprintInstanceInfo.blueprintInstanceId
                                                    .toString().trim() === id
                                                    .toString().trim()) {
                                                finalBlueprintInstanceInfo = blueprintInstanceInfo;
                                            }
                                        });
                        if (!finalBlueprintInstanceInfo) {
                            confirm("Error in blueprint instance data");
                            return;
                        }
                        angular.forEach(
                                finalBlueprintInstanceInfo.blueprintLayers,
                                function(layer) {
                                    $scope.oldLayers.push(layer.layer);
                                });
                    }

                    $scope.addConfiguredLayer = function(configuredLayer) {
                        if ($scope.configuredLayers.indexOf(configuredLayer
                                .trim()) === -1) {
                            $scope.configuredLayers.push(configuredLayer);
                        }
                    }

                    $scope.deleteConfiguredLayer = function(index) {
                        $scope.configuredLayers.splice(index, 1);
                    }

                    $scope.modify = function() {
                        if (!$scope.selectedBlueprintInstance
                                || !$scope.configuredLayers
                                || $scope.configuredLayers.length === 0) {
                            confirm("You must specify all the data");
                            return;
                        }
                        var finalBlueprintInstanceInfo = '';
                        var id = $scope.selectedBlueprintInstance
                                .substring($scope.selectedBlueprintInstance
                                        .indexOf("id:") + 4,
                                        $scope.selectedBlueprintInstance
                                                .indexOf("name") - 1);
                        angular
                                .forEach(
                                        $scope.blueprintInstanceInfos,
                                        function(blueprintInstanceInfo) {
                                            if (blueprintInstanceInfo.blueprintInstanceId
                                                    .toString().trim() === id
                                                    .toString().trim()) {
                                                finalBlueprintInstanceInfo = blueprintInstanceInfo;
                                            }
                                        });
                        if (!finalBlueprintInstanceInfo) {
                            confirm("Error in blueprint instance data");
                            return;
                        }

                        var blueprintLayers = [];
                        angular.forEach($scope.layerInfos, function(layerInfo) {
                            if ($scope.configuredLayers
                                    .indexOf(layerInfo.layer) !== -1) {
                                blueprintLayers.push(layerInfo);
                            }
                        });
                        if (!blueprintLayers || blueprintLayers.length === 0) {
                            confirm("Error in blueprint layers data");
                            return;
                        }
                        finalBlueprintInstanceInfo.blueprintLayers = blueprintLayers;
                        restAPISvc
                                .postRestAPI(
                                        "/api/v1/blueprintinstance/",
                                        finalBlueprintInstanceInfo,
                                        function(data) {
                                            if (data) {
                                                var confirmText = "The blueprint instance has been modified successfully.";
                                                confirm(confirmText);
                                            } else {
                                                confirm("Error when modifying the blueprint instance");
                                            }
                                            initialize();
                                        });
                    }
                });
