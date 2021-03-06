/*
 * Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package org.akraino.validation.ui.controller;

import org.akraino.validation.ui.data.JnksJobNotify;
import org.akraino.validation.ui.service.JenkinsJobNotificationService;
import org.onap.portalsdk.core.controller.RestrictedBaseController;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.onap.portalsdk.core.web.support.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/api/v1/jenkinsjobnotification")
public class JenkinsJobNotificationController extends RestrictedBaseController {

    @Autowired
    JenkinsJobNotificationService service;

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate
            .getLogger(JenkinsJobNotificationController.class);

    public JenkinsJobNotificationController() {
        super();
    }

    @RequestMapping(value = { "/" }, method = RequestMethod.POST)
    public ResponseEntity<Void> handle(@RequestBody JnksJobNotify jnksJobNotify) {
        try {
            service.handle(jnksJobNotify);
            return new ResponseEntity<Void>(HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(EELFLoggerDelegate.errorLogger,
                    "Error when trying to process Jenkins notification. " + UserUtils.getStackTrace(e));
        }
        return new ResponseEntity<Void>(HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
