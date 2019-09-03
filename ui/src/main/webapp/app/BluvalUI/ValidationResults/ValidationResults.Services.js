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

app.factory('generalValidationResultsSvc', [ function() {
    var svc = [];
    svc.getBlueprintLayers = function(wRobotNexusTestResults) {
        var layers = [];
        angular.forEach(wRobotNexusTestResults,
                function(wRobotNexusTestResult) {
                    if (wRobotNexusTestResult.blueprintLayer !== undefined) {
                        layers.push(wRobotNexusTestResult.blueprintLayer);
                    }
                });
        return layers;
    };
    svc.mapResult = function(validationNexusTestResult) {
        if (!validationNexusTestResult.timestamp) {
            return null;
        }
        if (!validationNexusTestResult.wRobotNexusTestResults) {
            return null;
        }
        if (validationNexusTestResult.wRobotNexusTestResults.length === 0) {
            return null;
        }
        var resultExistence = false;
        angular.forEach(validationNexusTestResult.wRobotNexusTestResults,
                function(result) {
                    if (result.robotTestResults
                            && result.robotTestResults.length > 0) {
                        resultExistence = true;
                    }
                });
        if (resultExistence) {
            if (validationNexusTestResult.result === true) {
                return 'SUCCESS';
            }
            return 'FAILURE'
        }
        return null;
    };
    svc.filterWithLayer = function(validationNexusTestResults, filterLayer) {
        if (filterLayer === undefined || filterLayer === '') {
            return validationNexusTestResults;
        }
        var filteredResults = [];
        angular.forEach(validationNexusTestResults, function(
                validationNexusTestResult) {
            angular.forEach(validationNexusTestResult.wRobotNexusTestResults,
                    function(wRobotNexusTestResult) {
                        if (wRobotNexusTestResult.blueprintLayer.toLowerCase()
                                .includes(filterLayer.toLowerCase())) {
                            filteredResults.push(validationNexusTestResult);
                        }
                    });
        });
        return filteredResults;
    }
    svc.filterWithResult = function(validationNexusTestResults, filterResult) {
        if (filterResult === undefined || filterResult === '') {
            return validationNexusTestResults;
        }
        var filteredResults = [];
        angular.forEach(validationNexusTestResults, function(
                validationNexusTestResult) {
            if (validationNexusTestResult.result === true
                    && 'success'.includes(filterResult.toLowerCase())) {
                filteredResults.push(validationNexusTestResult);
            } else if (validationNexusTestResult.result === false
                    && 'failure'.includes(filterResult.toLowerCase())) {
                filteredResults.push(validationNexusTestResult);
            }
        });
        return filteredResults;
    }
    svc.getLab = function(silo, silos) {
        var lab = null;
        angular.forEach(silos, function(siloData) {
            if (silo === siloData.silo) {
                lab = siloData.lab.lab;
            }
        });
        return lab;
    }
    return svc;
} ]);