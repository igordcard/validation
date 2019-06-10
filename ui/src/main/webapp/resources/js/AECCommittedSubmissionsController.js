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

AECBlueprintValidationUIApp.controller('AECCommittedSubmissionsController',
        function($scope, restAPISvc, $interval, refreshPeriod) {

            $scope.submissionIdList = [];

            restAPISvc.getRestAPI("/api/submission/", function(data) {
                $scope.submissions = data;
            });

            $scope.refreshCommittedSubmissions = function() {
                restAPISvc.getRestAPI("/api/submission/", function(data) {
                    $scope.submissions = data;
                });
            }

            $scope.modifySubmissionIdList = function(id) {
                if ($scope.submissionIdList.indexOf(id) === -1) {
                    $scope.submissionIdList.push(id);
                } else {
                    $scope.submissionIdList.splice($scope.submissionIdList
                            .indexOf(id), 1);
                }
            }

            $scope.deleteSubmissions = function() {
                var confirmation = confirm("Are you sure?");
                if (confirmation == true) {
                    angular.forEach($scope.submissionIdList, function(id) {
                        var submission = {
                            "submissionId" : id
                        };
                        restAPISvc
                                .deleteRestAPI("/api/submission/", submission);
                    });
                }
            }

            $interval(function() {
                $scope.refreshCommittedSubmissions();
            }, refreshPeriod);

        });
