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
                'AECFindBySubmissionIdController',
                function($scope, restAPISvc) {

                    initialize();

                    function initialize() {
                        $scope.results = [];
                        restAPISvc
                                .getRestAPI(
                                        "/api/submission/",
                                        function(data) {
                                            $scope.submissions = data;
                                            $scope.submissionsForDisplay = [];
                                            angular
                                                    .forEach(
                                                            $scope.submissions,
                                                            function(
                                                                    submissionData) {
                                                                var temp = "id: "
                                                                        + submissionData.submissionId
                                                                        + " blueprint: "
                                                                        + submissionData.blueprintInstance.blueprint.blueprintName
                                                                        + " version: "
                                                                        + submissionData.blueprintInstance.version
                                                                        + " layer: "
                                                                        + submissionData.blueprintInstance.layer
                                                                        + " lab: "
                                                                        + submissionData.blueprintInstance.timeslot.lab
                                                                        + " Start date and time: "
                                                                        + submissionData.blueprintInstance.timeslot.startDateTime
                                                                        + " duration: "
                                                                        + submissionData.blueprintInstance.timeslot.duration;
                                                                $scope.submissionsForDisplay
                                                                        .push(temp);
                                                            });
                                        });
                    }
                    $scope.selectedSubmissionChange = function(
                            selectedSubmission) {
                        $scope.results = [];
                        var id = selectedSubmission.substring(
                                selectedSubmission.indexOf("id:") + 4,
                                selectedSubmission.indexOf("blueprint") - 1);
                        restAPISvc.getRestAPI(
                                "/api/results/findBySubmissionId/" + id,
                                function(data) {
                                    $scope.results = data;
                                });
                    }

                });