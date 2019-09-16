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

var app = angular.module('ValidationResults');
app.controller('TestSuiteResultsController', function($scope,
        generalValidationResultsSvc) {

    initialize();

    function initialize() {
        $scope.showTestSuitesResults = false;
        $scope.wrobotTestResults = [];
        $scope.selectedTestId = '';
        $scope.selectedTest = '';
        $scope.resultsLayers = [];
        $scope.resultsLayerTestSuitesNames = [];
        $scope.selectedRobotTestResult = '';
        $scope.selectedLayer = '';
        $scope.selectedTestSuiteName = '';
        $scope.validationDbTestResult = $scope.params;
        $scope.wrobotTestResults = $scope.params.wrobotDbTestResults;
        if (generalValidationResultsSvc
                .mapResult($scope.validationDbTestResult) === null) {
            confirm("No data was found");
        } else {
            $scope.showTestSuitesResults = true;
            angular.forEach($scope.wrobotTestResults, function(result) {
                $scope.resultsLayers.push(result.layer);
            });
        }

    }

    $scope.selectedResultsLayerChange = function(selectedLayer) {
        $scope.selectedTestId = null;
        $scope.selectedTest = null;
        $scope.resultsLayerTestSuitesNames = [];
        $scope.robotTestResults = [];
        $scope.selectedRobotTestResult = '';
        $scope.selectedTestSuiteName = '';
        var selectedLayerResult = [];
        angular.forEach($scope.wrobotTestResults, function(result) {
            if (result.layer === selectedLayer) {
                selectedLayerResult = result;
            }
        });
        $scope.robotTestResults = angular
                .fromJson(selectedLayerResult.robotTestResults);
        angular.forEach($scope.robotTestResults, function(robotTestResult) {
            $scope.resultsLayerTestSuitesNames.push(robotTestResult.name);
        });
    }

    $scope.selectedTestSuitesNameChange = function(selectedTestSuiteName) {
        if (!selectedTestSuiteName) {
            return;
        }
        $scope.selectedTestId = '';
        $scope.selectedTest = '';
        angular.forEach($scope.robotTestResults, function(robotTestResult) {
            if (robotTestResult.name.trim() === selectedTestSuiteName.trim()) {
                $scope.selectedRobotTestResult = robotTestResult;
            }
        });
    }

    $scope.setClickedTest = function(test) {
        $scope.selectedTestId = test.id;
        $scope.selectedTest = test;
    }

});
