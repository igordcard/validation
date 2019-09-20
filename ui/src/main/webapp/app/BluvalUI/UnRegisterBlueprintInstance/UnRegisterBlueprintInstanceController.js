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

var app = angular.module('UnRegisterBlueprintInstance');
app
        .controller(
                'UnRegisterBlueprintInstanceController',
                function($scope, restAPISvc) {

                    initialize();

                    function initialize() {
                        $scope.loadingBlueprintInstances = true;
                        $scope.blueprintInstanceInfos = [];
                        $scope.declerativeInsts = [];
                        $scope.selectedBlueprintInstance = '';
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
                                                                });
                                            } else {
                                                confirm("No blueprint instances found");
                                            }
                                            $scope.loadingBlueprintInstances = false;
                                        });
                    }

                    $scope.unRegister = function() {
                        if (!$scope.selectedBlueprintInstance) {
                            confirm("You must select a blueprint instance");
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
                        restAPISvc
                                .deleteRestAPI(
                                        "/api/v1/blueprintinstance/",
                                        finalBlueprintInstanceInfo,
                                        function(data) {
                                            if (data) {
                                                var confirmText = "The blueprint instance has been unregistered successfully."
                                                confirm(confirmText);
                                            } else {
                                                confirm("Error when unregistering the blueprint instance");
                                            }
                                            initialize();
                                        });
                    }
                });
