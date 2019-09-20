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

var app = angular.module('UnRegisterBlueprint');
app
        .controller(
                'UnRegisterBlueprintController',
                function($scope, restAPISvc) {

                    initialize();

                    function initialize() {
                        $scope.loadingBlueprints = true;
                        $scope.blueprintInfos = [];
                        $scope.blueprints = [];
                        $scope.selectedBlueprint = '';
                        restAPISvc.getRestAPI("/api/v1/blueprint/", function(
                                data) {
                            if (data) {
                                $scope.blueprintInfos = data;
                                angular.forEach(data, function(blueprint) {
                                    $scope.blueprints
                                            .push(blueprint.blueprintName);
                                });
                            } else {
                                confirm("No blueprints found");
                            }
                            $scope.loadingBlueprints = false;
                        });
                    }

                    $scope.unRegister = function() {
                        if (!$scope.selectedBlueprint) {
                            confirm("You must select a blueprint");
                            return;
                        }
                        var finalBlueprintInfo = '';
                        angular
                                .forEach(
                                        $scope.blueprintInfos,
                                        function(blueprintInfo) {
                                            if ($scope.selectedBlueprint.trim() === blueprintInfo.blueprintName
                                                    .trim()) {
                                                finalBlueprintInfo = blueprintInfo;
                                            }
                                        });
                        if (!finalBlueprintInfo) {
                            return;
                        }
                        restAPISvc
                                .deleteRestAPI(
                                        "/api/v1/blueprint/",
                                        finalBlueprintInfo,
                                        function(data) {
                                            if (data) {
                                                var confirmText = "The blueprint has been unregistered successfully."
                                                confirm(confirmText);
                                            } else {
                                                confirm("Error when unregistering the blueprint");
                                            }
                                            initialize();
                                        });
                    }
                });
