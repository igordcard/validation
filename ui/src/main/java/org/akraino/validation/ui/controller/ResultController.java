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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Set;

import org.akraino.validation.ui.entity.ValidationDbTestResult;
import org.akraino.validation.ui.service.DbAdapter;
import org.akraino.validation.ui.service.IntegratedResultService;
import org.onap.portalsdk.core.controller.RestrictedBaseController;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.onap.portalsdk.core.web.support.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/api/v1/results")
public class ResultController extends RestrictedBaseController {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(ResultController.class);

    @Autowired
    IntegratedResultService resultService;

    @Autowired
    DbAdapter dbAdapter;

    public ResultController() {
        super();
    }

    @RequestMapping(value = { "/getblueprintnamesoflab/{lab}" }, method = RequestMethod.GET)
    public ResponseEntity<Set<String>> getBlueprintNamesOfLab(@PathVariable("lab") String lab) {
        try {
            return new ResponseEntity<>(resultService.getBlueprintNamesOfLabFromDb(lab), HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(EELFLoggerDelegate.errorLogger,
                    "Error when retrieving blueprint names of a lab. " + UserUtils.getStackTrace(e));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @RequestMapping(value = { "/getblueprintversions/{name}/{lab}" }, method = RequestMethod.GET)
    public ResponseEntity<Set<String>> getBlueprintVersions(@PathVariable("name") String name,
            @PathVariable("lab") String lab) {
        try {
            return new ResponseEntity<>(resultService.getBlueprintVersionsFromDb(name, lab), HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(EELFLoggerDelegate.errorLogger,
                    "Error when retrieving blueprint versions. " + UserUtils.getStackTrace(e));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @RequestMapping(value = { "/getbysubmissionid/{id}" }, method = RequestMethod.GET)
    public ResponseEntity<ValidationDbTestResult> getBySubmissionId(@PathVariable("id") String submissionId) {
        try {
            return new ResponseEntity<>(resultService.getResults(submissionId), HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(EELFLoggerDelegate.errorLogger,
                    "Error when retrieving results using submission id. " + UserUtils.getStackTrace(e));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @RequestMapping(value = { "/getmostrecent/{name}/{version}/{lab}" }, method = RequestMethod.GET)
    public ResponseEntity<List<ValidationDbTestResult>> getMostRecent(@PathVariable("name") String name,
            @PathVariable("version") String version, @PathVariable("lab") String lab) {
        try {
            return new ResponseEntity<>(dbAdapter.readResultFromDb(name, version, lab, null, null, null, null),
                    HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(EELFLoggerDelegate.errorLogger,
                    "Error when retrieving most recent results. " + UserUtils.getStackTrace(e));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @RequestMapping(value = { "/getbytimestamp/{lab}/{name}/{version}/{timestamp}" }, method = RequestMethod.GET)
    public ResponseEntity<ValidationDbTestResult> getByTimestamp(@PathVariable("lab") String lab,
            @PathVariable("name") String name, @PathVariable("version") String version,
            @PathVariable("timestamp") String timestamp) {
        try {
            return new ResponseEntity<>(resultService.getResult(name, version, lab, timestamp), HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(EELFLoggerDelegate.errorLogger,
                    "Error when retrieving results using timestamp data. " + UserUtils.getStackTrace(e));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @RequestMapping(value = {
    "/getlastrun/{lab}/{name}/{version}/{allLayers}/{optional}/{outcome}" }, method = RequestMethod.GET)
    public ResponseEntity<ValidationDbTestResult> getLastRun(@PathVariable("lab") String lab,
            @PathVariable("name") String name, @PathVariable("version") String version,
            @PathVariable("allLayers") Boolean allLayers, @PathVariable("optional") Boolean optional,
            @PathVariable("outcome") boolean outcome) {
        try {
            return new ResponseEntity<>(
                    resultService.getLastResultBasedOnOutcome(name, version, lab, allLayers, optional, outcome),
                    HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(EELFLoggerDelegate.errorLogger,
                    "Error when retrieving last result. " + UserUtils.getStackTrace(e));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @RequestMapping(value = {
    "/getlastrunoflayers/{lab}/{name}/{version}/{layers}/{optional}/{outcome}" }, method = RequestMethod.GET)
    public ResponseEntity<ValidationDbTestResult> getLastRunOfLayers(@PathVariable("lab") String lab,
            @PathVariable("name") String name, @PathVariable("version") String version,
            @PathVariable("layers") List<String> layers, @PathVariable("optional") Boolean optional,
            @PathVariable("outcome") boolean outcome) {
        try {
            return new ResponseEntity<>(
                    resultService.getLastResultBasedOnOutcome(name, version, lab, layers, optional, outcome),
                    HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(EELFLoggerDelegate.errorLogger,
                    "Error when retrieving last result. " + UserUtils.getStackTrace(e));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

    @RequestMapping(value = { "/getbasedondate/{lab}/{name}/{version}/{date}" }, method = RequestMethod.GET)
    public ResponseEntity<List<ValidationDbTestResult>> getBasedOnDate(@PathVariable("lab") String lab,
            @PathVariable("name") String name, @PathVariable("version") String version,
            @PathVariable("date") String date) {
        try {
            DateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy");
            return new ResponseEntity<>(
                    resultService.getBasedOnDateFromNexus(name, version, lab, dateFormat.parse(date)), HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(EELFLoggerDelegate.errorLogger,
                    "Error when retrieving results based on date. " + UserUtils.getStackTrace(e));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
    }

}
