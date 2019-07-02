appDS2
        .config(function($routeProvider) {
            $routeProvider

                    .otherwise({
                        templateUrl : 'app/AECBlueprintValidationUI/GetBySubmissionId/GetBySubmissionIdTemplate.html',
                        controller : "AECGetBySubmissionIdController"
                    });
        });