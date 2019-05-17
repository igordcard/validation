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

var AECBlueprintValidationUIApp = angular.module('BlueprintValidationUIManagement', ['ngDialog', 'ui.router', 'base64','App.config','ngStorage','ui.bootstrap', 'ngResource','ngFileUpload','ngMaterial']);

AECBlueprintValidationUIApp.config(function($stateProvider, $urlRouterProvider) {
    $urlRouterProvider.otherwise('/login')
    $stateProvider
        .state('common', {
            templateUrl: 'views/indexMain.html',
            abstract: true
        })
        .state('login', {
            url: "/login",
            controller: 'Login',
            templateUrl: 'views/login.html'
        })
        .state('newSubmission', {
            url: "/newSubmission",
            parent: "common",
            views: {
                "main": {
                    controller: 'AECNewSubmissionController',
                    templateUrl: 'views/newSubmission.html'
                }
            }
        })
        .state('committedSubmissions', {
            url: "/committedSubmissions",
            parent: "common",
            views: {
                "main": {
                    controller: 'AECCommittedSubmissionsController',
                    templateUrl: 'views/committedSubmissions.html'
                }
            }
        })
        .state('findBySubmissionId', {
            url: "/findBySubmissionId",
            parent: "common",
            views: {
                "main": {
                    controller: 'AECFindBySubmissionIdController',
                    templateUrl: 'views/findBySubmissionId.html'
                }
            }
        })
});

AECBlueprintValidationUIApp.controller('Login',function($scope, $http, $filter, filterFilter, $state, $base64,$rootScope,$controller,appContext) {
    $rootScope.tokenId ="";
    $scope.usernameVal = '';
    $scope.passwordVal = '';
    $rootScope.message = "Please enter credentials";
    $scope.$state = $state;

    var baseURL = window.location.protocol + '//' + window.location.host;
    /* eslint-disable no-console */
    console.log('Base URL for current frame is: ' + baseURL);
    /* eslint-enable no-console */
    $scope.goLogin = function() {
        var arr = $scope.passwordVal;
        if ($scope.usernameVal == '' && $scope.passwordVal == '') {
            $scope.userMessage = 'Please enter a username.';
            $scope.passwordMessage = 'Please enter a password.';
        } else if ($scope.usernameVal == '') {
            $scope.userMessage = 'Please enter a username.';
            $scope.passwordMessage = '';
        } else if ($scope.passwordVal == '') {
            $scope.passwordMessage = 'Please enter a password.';
            $scope.userMessage = '';
        } else if (arr.length < 6) {
            $scope.passwordMessage = 'Please enter a valid password.';
            $scope.userMessage = '';
        }
       else {
            $scope.passwordMessage = '';
            $scope.userMessage ='';
            // var userPwd = $scope.usernameVal + ":" + $scope.passwordVal;
            // var auth = $base64.encode(userPwd);
            /*
             * $http({ method: 'POST', url: appContext+'/login', //url:
             * 'http://'+hostUrl+'/AECPortalMgmt/login', headers: {
             * 'Authorization': "Basic " + auth, 'Content-Type':
             * "application/json", 'Accept': "application/json" }, data: { } }).
             * then(function(response) { if (response.data.statusCode == 200) {
             * $rootScope.tokenId = response.data.tokenId;
             * localStorage.setItem("tokenId",response.data.tokenId);
             * $state.transitionTo('sites'); } else if (response.data.statusCode ==
             * 401){ $scope.passwordVal= null; $scope.passwordMessage = 'Invalid
             * Credentials, please try again...';
             *
             * localStorage.removeItem("tokenId"); } }, function(error) { if
             * (error.status == 401) { $scope.passwordMessage = 'Invalid
             * Credentials, please try again...'; $scope.passwordVal ="";
             * localStorage.removeItem("tokenId"); } else if (error.status ==
             * 400) { $scope.passwordMessage = 'Session Invalid, please login
             * again...'; $scope.passwordVal ="";
             * localStorage.removeItem("tokenId"); } else if (error.status ==
             * 307) { $scope.passwordMessage = 'Session expired,Please try
             * again...'; $scope.passwordVal ="";
             * localStorage.removeItem("tokenId"); } });
             */
            $state.transitionTo('committedSubmissions');
        }
    }
    $scope.goLogout = function() {
        $http({
            method: 'POST',
            url: appContext+'/logout',
            headers: {
                'Content-Type': "application/json",
                'Accept': "application/json",
                'tokenId' : $rootScope.tokenId
            },
        data:{
        }
            /*
             * data: { 'username': $scope.usernameVal, 'passowrd':
             * $scope.passwordVal }
             */
        }).then(function(response) {
            if (response.data.statusCode == 200) {
                $rootScope.tokenId ="";
                localStorage.removeItem("tokenId");
                $state.transitionTo('login');
                $rootScope.message = 'User logged out, please login again...';
            }
        }, function(response) {
            $scope.message = 'Unknown error,Try again later' + response.status;
        });
    }
});

