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

var app = angular.module('CommittedSubmissions');

app
        .controller(
                'CommittedSubmissionsController',
                function($scope, restAPISvc, $interval, refreshPeriod,
                        committedSubmissionsSvc, NgTableParams, appContext,
                        $window) {

                    $scope.getLayer = committedSubmissionsSvc.getLayer;
                    $scope.getResultUrl = committedSubmissionsSvc.getResultUrl;
                    $scope.mapResult = committedSubmissionsSvc.mapResult;

                    initialize();

                    function initialize() {
                        restAPISvc.getRestAPI("/api/v1/submission/", function(
                                submissions) {
                            $scope.submissions = submissions;
                            var data = submissions;
                            $scope.tableParams = new NgTableParams({
                                page : 1,
                                count : 5
                            }, {
                                dataset : data
                            });
                        });
                    }

                    $scope.refreshCommittedSubmissions = function() {
                        initialize();
                    }

                    $scope.getValidationResults = function(submission) {
                        if (!submission.validationDbTestResult
                                || !submission.validationDbTestResult.timestamp
                                || !submission.validationDbTestResult.wrobotDbTestResults
                                || submission.validationDbTestResult.wrobotDbTestResults.length === 0) {
                            return;
                        }
                        $window.location.href = appContext
                                + "/validationresults#?submissionId="
                                + submission.submissionId;
                    }

                    /*
                     * $interval(function() {
                     * $scope.refreshCommittedSubmissions(); }, refreshPeriod);
                     */

                });
