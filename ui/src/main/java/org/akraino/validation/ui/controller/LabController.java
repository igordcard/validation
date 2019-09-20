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

import java.util.List;

import org.akraino.validation.ui.entity.LabInfo;
import org.akraino.validation.ui.service.DbAdapter;
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
@RequestMapping("/api/v1/lab")
public class LabController extends RestrictedBaseController {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(LabController.class);

    @Autowired
    DbAdapter service;

    public LabController() {
        super();
    }

    @RequestMapping(value = { "/" }, method = RequestMethod.GET)
    public ResponseEntity<List<LabInfo>> getLabs() {
        try {
            return new ResponseEntity<>(service.getLabs(), HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(EELFLoggerDelegate.errorLogger,
                    "Error when trying to get labs. " + UserUtils.getStackTrace(e));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @RequestMapping(value = { "/" }, method = RequestMethod.POST)
    public ResponseEntity<LabInfo> saveLab(@RequestBody LabInfo labInfo) {
        try {
            service.saveLab(labInfo);
            return new ResponseEntity<>(labInfo, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(EELFLoggerDelegate.errorLogger, "Save of lab failed. " + UserUtils.getStackTrace(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @RequestMapping(value = { "/" }, method = RequestMethod.DELETE)
    public ResponseEntity<Boolean> deleteLab(@RequestBody LabInfo labInfo) {
        try {
            service.deleteLab(labInfo);
            return new ResponseEntity<>(true, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(EELFLoggerDelegate.errorLogger, "Deletion of lab failed. " + UserUtils.getStackTrace(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
