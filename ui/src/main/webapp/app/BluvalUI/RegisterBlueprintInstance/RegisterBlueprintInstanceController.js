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

var app = angular.module('RegisterBlueprintInstance');
app
        .controller(
                'RegisterBlueprintInstanceController',
                function($scope, restAPISvc) {

                    initialize();

                    function initialize() {
                        $scope.loadingBlueprints = true;
                        $scope.loadingLayers = true;
                        $scope.blueprints = [];
                        $scope.blueprintInfos = [];
                        $scope.layers = [];
                        $scope.layerInfos = [];
                        $scope.selectedLayer = '';
                        $scope.configuredLayers = [];
                        restAPISvc
                                .getRestAPI(
                                        "/api/v1/blueprint/",
                                        function(data) {
                                            if (data) {
                                                $scope.blueprintInfos = data;
                                                angular
                                                        .forEach(
                                                                data,
                                                                function(
                                                                        blueprint) {
                                                                    $scope.blueprints
                                                                            .push(blueprint.blueprintName);
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
                                                confirm("No blueprints found");
                                            }
                                            $scope.loadingBlueprints = false;
                                            $scope.loadingLayers = false;
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

                    $scope.register = function() {
                        if (!$scope.selectedBlueprint || !$scope.definedVersion
                                || !$scope.configuredLayers
                                || $scope.configuredLayers.length === 0) {
                            confirm("You must specify all the fields");
                            return;
                        }
                        var blueprint = '';
                        angular
                                .forEach(
                                        $scope.blueprintInfos,
                                        function(blueprintInfo) {
                                            if (blueprintInfo.blueprintName
                                                    .toString().trim() === $scope.selectedBlueprint
                                                    .toString().trim()) {
                                                blueprint = blueprintInfo;
                                            }
                                        });
                        if (!blueprint) {
                            confirm("Error in blueprint data");
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
                        var blueprintInstance = {
                            "blueprint" : blueprint,
                            "version" : $scope.definedVersion,
                            "blueprintLayers" : blueprintLayers
                        };
                        restAPISvc
                                .postRestAPI(
                                        "/api/v1/blueprintinstance/",
                                        blueprintInstance,
                                        function(data) {
                                            if (data) {
                                                var confirmText = "The blueprint instance has been registered successfully. Blueprint instance id:"
                                                        + data.blueprintInstanceId;
                                                confirm(confirmText);
                                            } else {
                                                confirm("Error when registering the blueprint instance");
                                            }
                                            initialize();
                                        });
                    }
                });
