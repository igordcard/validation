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
package org.akraino.validation.ui.controller;

import java.util.List;

import org.akraino.validation.ui.client.nexus.resources.RobotTestResult;
import org.akraino.validation.ui.service.ResultService;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/results")
public class ResultsController {

    private static final Logger LOGGER = Logger.getLogger(ResultsController.class);

    @Autowired
    ResultService service;

    @RequestMapping(value = "/findBySubmissionId/{id}")
    public ResponseEntity<List<RobotTestResult>> findByBlueprintId(@PathVariable("id") String submissionId) {
        try {
            return new ResponseEntity<>(service.getRobotTestResults(submissionId), HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(e);
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

}
