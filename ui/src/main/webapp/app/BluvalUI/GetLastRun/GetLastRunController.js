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

var app = angular.module('GetLastRun');
app.controller('GetLastRunController',

function($scope, restAPISvc, $window, appContext) {
    initialize();

    function initialize() {
        $scope.loadingLabs = true;
        $scope.loadingBlueprints = false;
        $scope.loadingVersions = false;
        $scope.labs = [];
        restAPISvc.getRestAPI("/api/v1/lab/", function(data) {
            angular.forEach(data, function(lab) {
                $scope.labs.push(lab.lab);
            });
            $scope.loadingLabs = false;
        });
    }

    $scope.selectedLabChange = function() {
        $scope.loadingLabs = false;
        $scope.loadingBlueprints = true;
        $scope.loadingVersions = false;
        $scope.blueprints = [];
        $scope.versions = [];
        $scope.layers = [];
        $scope.optionals = [];
        $scope.outcomes = [];
        $scope.selectedBlueprint = {};
        $scope.selectedVersion = {};
        $scope.selectedLayer = {};
        $scope.selectedOptional = {};
        $scope.selectedOutcome = {};
        restAPISvc.getRestAPI("/api/v1/results/getblueprintnamesoflab/"
                + $scope.selectedLab, function(data) {
            $scope.blueprints = data;
            $scope.loadingBlueprints = false;
        });
    }

    $scope.selectedBlueprintChange = function() {
        if (!$scope.selectedLab) {
            return;
        }
        $scope.loadingLabs = false;
        $scope.loadingBlueprints = false;
        $scope.loadingVersions = true;
        $scope.versions = [];
        $scope.layers = [];
        $scope.optionals = [];
        $scope.outcomes = [];
        $scope.selectedVersion = {};
        $scope.selectedLayer = {};
        $scope.selectedOptional = {};
        $scope.selectedOutcome = {};
        restAPISvc.getRestAPI("/api/v1/results/getblueprintversions/"
                + $scope.selectedBlueprint + "/" + $scope.selectedLab,
                function(data) {
                    $scope.versions = data;
                    $scope.loadingVersions = false;
                });
    }

    $scope.selectedVersionChange = function() {
        $scope.layers = [];
        $scope.optionals = [];
        $scope.outcomes = [];
        $scope.selectedLayer = {};
        $scope.selectedOptional = {};
        $scope.selectedOutcome = {};
        $scope.loadingLabs = false;
        $scope.loadingBlueprints = false;
        $scope.loadingVersions = false;
        $scope.loadingResults = false;
        $scope.layers = [ 'all', 'hardware', 'os', 'k8s', 'openstack' ];
    }

    $scope.selectedLayerChange = function() {
        $scope.optionals = [];
        $scope.outcomes = [];
        $scope.selectedOptional = {};
        $scope.selectedOutcome = {};
        $scope.loadingLabs = false;
        $scope.loadingBlueprints = false;
        $scope.loadingVersions = false;
        $scope.loadingResults = false;
        $scope.optionals = [ 'true', 'false' ];
    }

    $scope.selectedOptionalChange = function() {
        $scope.outcomes = [];
        $scope.selectedOutcome = {};
        $scope.loadingLabs = false;
        $scope.loadingBlueprints = false;
        $scope.loadingVersions = false;
        $scope.loadingResults = false;
        $scope.outcomes = [ 'SUCCESS', 'FAILURE' ];
    }

    $scope.selectedOutcomeChange = function() {
        $scope.loadingLabs = false;
        $scope.loadingBlueprints = false;
        $scope.loadingVersions = false;
        $scope.loadingResults = false;
    }

    $scope.get = function() {
        if (!$scope.selectedLab || !$scope.selectedBlueprint
                || !$scope.selectedVersion || !$scope.selectedLayer
                || !$scope.selectedOptional || !$scope.selectedOutcome) {
            confirm("You must specify all data fields");
            return;
        }
        var outcome = "";
        if ($scope.selectedOutcome === 'SUCCESS') {
            outcome = true;
        } else {
            outcome = false;
        }
        var allLayers = "";
        var layer = "";
        if ($scope.selectedLayer === 'all') {
            allLayers = "true";
        } else {
            layer = $scope.selectedLayer;
        }
        $window.location.href = appContext
                + "/validationresults#?blueprintName="
                + $scope.selectedBlueprint + "&" + "version="
                + $scope.selectedVersion + "&" + "lab=" + $scope.selectedLab
                + "&" + "allLayers=" + allLayers + "&" + "layer=" + layer + "&"
                + "optional=" + $scope.selectedOptional + "&" + "outcome="
                + outcome;
    }

});
