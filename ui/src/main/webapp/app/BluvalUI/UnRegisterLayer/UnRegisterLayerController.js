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

var app = angular.module('UnRegisterLayer');
app
        .controller(
                'UnRegisterLayerController',
                function($scope, restAPISvc) {

                    initialize();

                    function initialize() {
                        $scope.loadingLayers = true;
                        $scope.layerInfos = [];
                        $scope.layers = [];
                        $scope.selectedLayer = '';
                        restAPISvc.getRestAPI("/api/v1/layer/", function(data) {
                            if (data) {
                                $scope.layerInfos = data;
                                angular.forEach(data, function(layer) {
                                    $scope.layers.push(layer.layer);
                                });
                            } else {
                                confirm("No blueprint layers found");
                            }
                            $scope.loadingLayers = false;
                        });
                    }

                    $scope.unRegister = function() {
                        if (!$scope.selectedLayer) {
                            confirm("You must select a blueprint layer");
                            return;
                        }
                        var finalLayerInfo = '';
                        angular.forEach($scope.layerInfos, function(layerInfo) {
                            if ($scope.selectedLayer.trim() === layerInfo.layer
                                    .trim()) {
                                finalLayerInfo = layerInfo;
                            }
                        });
                        if (!finalLayerInfo) {
                            return;
                        }
                        restAPISvc
                                .deleteRestAPI(
                                        "/api/v1/layer/",
                                        finalLayerInfo,
                                        function(data) {
                                            if (data) {
                                                var confirmText = "The blueprint layer has been unregistered successfully."
                                                confirm(confirmText);
                                            } else {
                                                confirm("Error when unregistering the blueprint layer");
                                            }
                                            initialize();
                                        });
                    }
                });
