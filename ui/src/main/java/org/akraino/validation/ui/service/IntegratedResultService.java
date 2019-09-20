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
package org.akraino.validation.ui.service;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import javax.annotation.Nonnull;

import org.akraino.validation.ui.client.nexus.NexusExecutorClient;
import org.akraino.validation.ui.entity.LabInfo;
import org.akraino.validation.ui.entity.Submission;
import org.akraino.validation.ui.entity.ValidationDbTestResult;
import org.apache.commons.httpclient.HttpException;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.onap.portalsdk.core.web.support.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.UniformInterfaceException;

@Service
@Transactional
public class IntegratedResultService {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(IntegratedResultService.class);

    @Autowired
    private DbSubmissionAdapter submissionService;

    @Autowired
    NexusExecutorClient nexusService;

    @Autowired
    DbAdapter dbAdapter;

    public List<String> getLabsFromNexus()
            throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
            UniformInterfaceException, NoSuchAlgorithmException, IOException, IllegalArgumentException, ParseException {
        List<String> labs = new ArrayList<String>();
        for (String cLabSilo : nexusService.getResource(null)) {
            for (LabInfo labInfo : dbAdapter.getLabs()) {
                if (labInfo.getSilo().equals(cLabSilo)) {
                    labs.add(labInfo.getLab());
                }
            }
        }
        return labs;
    }

    public List<String> getBlueprintNamesOfLabFromNexus(@Nonnull String lab)
            throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
            UniformInterfaceException, NoSuchAlgorithmException, IOException, IllegalArgumentException, ParseException {
        LabInfo labInfo = dbAdapter.getLab(lab);
        if (labInfo == null) {
            throw new IllegalArgumentException("Could not retrieve lab : " + lab.toString());
        }
        List<String> rNames = new ArrayList<String>();
        try {
            List<String> cNames = nexusService.getResource(dbAdapter.getLab(lab).getSilo() + "/bluval_results");
            for (String cName : cNames) {
                if (cName.equals("family") || cName.equals("ta") || cName.equals("job")) {
                    continue;
                }
                rNames.add(cName);
            }
        } catch (HttpException ex) {
            LOGGER.warn(EELFLoggerDelegate.auditLogger,
                    "Error when retrieving blueprint names from nexus" + UserUtils.getStackTrace(ex));
        }
        return rNames;
    }

    public List<String> getBlueprintVersionsFromNexus(@Nonnull String name, @Nonnull String lab)
            throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
            UniformInterfaceException, NoSuchAlgorithmException, IOException, IllegalArgumentException, ParseException {
        LabInfo labInfo = dbAdapter.getLab(lab);
        if (labInfo == null) {
            throw new IllegalArgumentException("Could not retrieve lab : " + lab.toString());
        }
        return nexusService.getResource(labInfo.getSilo() + "/bluval_results", name);
    }

    public List<String> getBlueprintTimeStampsFromNexus(@Nonnull String name, @Nonnull String version,
            @Nonnull String lab) throws JsonParseException, JsonMappingException, KeyManagementException,
    ClientHandlerException, UniformInterfaceException, NoSuchAlgorithmException, IOException, ParseException {
        LabInfo labInfo = dbAdapter.getLab(lab);
        if (labInfo == null) {
            throw new IllegalArgumentException("Could not retrieve lab : " + lab.toString());
        }
        List<String> timestamps = new ArrayList<String>();
        try {
            timestamps = nexusService.getResource(labInfo.getSilo() + "/bluval_results", name, version);
        } catch (HttpException ex) {
            LOGGER.warn(EELFLoggerDelegate.auditLogger,
                    "Error when retrieving blueprint names from nexus" + UserUtils.getStackTrace(ex));
        }
        return timestamps;
    }

    public List<ValidationDbTestResult> getResultsFromNexus(@Nonnull String name, @Nonnull String version,
            @Nonnull String lab, int noTimestamps)
                    throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
                    UniformInterfaceException, NoSuchAlgorithmException, IOException, IllegalArgumentException, ParseException {
        LabInfo labInfo = dbAdapter.getLab(lab);
        if (labInfo == null) {
            throw new IllegalArgumentException("Could not retrieve lab : " + lab.toString());
        }
        return nexusService.getResults(name, version, labInfo.getSilo(), noTimestamps);
    }

    public ValidationDbTestResult getResultFromNexus(@Nonnull String name, @Nonnull String version, @Nonnull String lab,
            @Nonnull String timestamp) throws JsonParseException, JsonMappingException, IOException,
    KeyManagementException, ClientHandlerException, UniformInterfaceException, NoSuchAlgorithmException,
    NullPointerException, ParseException {
        LabInfo labInfo = dbAdapter.getLab(lab);
        if (labInfo == null) {
            throw new IllegalArgumentException("Could not retrieve lab : " + lab.toString());
        }
        ValidationDbTestResult vNexusResult = nexusService.getResult(name, version, labInfo.getSilo(), timestamp);
        if (!dbAdapter.checkValidityOfNexusResult(vNexusResult)) {
            return null;
        }
        vNexusResult.setLab(labInfo);
        return vNexusResult;
    }

    public ValidationDbTestResult getLastResultBasedOnOutcomeFromNexus(@Nonnull String name, @Nonnull String version,
            @Nonnull String lab, Boolean allLayers, Boolean optional, boolean outcome)
                    throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
                    UniformInterfaceException, NoSuchAlgorithmException, NullPointerException, IOException, ParseException {
        LabInfo labInfo = dbAdapter.getLab(lab);
        if (labInfo == null) {
            throw new IllegalArgumentException("Could not retrieve lab : " + lab.toString());
        }
        ValidationDbTestResult vNexusResult = nexusService.getLastResultBasedOnOutcome(name, version, labInfo.getSilo(),
                allLayers, optional, outcome);
        if (!dbAdapter.checkValidityOfNexusResult(vNexusResult)) {
            return null;
        }
        vNexusResult.setLab(labInfo);
        return vNexusResult;
    }

    public ValidationDbTestResult getLastResultBasedOnOutcomeFromNexus(@Nonnull String name, @Nonnull String version,
            @Nonnull String lab, @Nonnull List<String> layers, Boolean optional, boolean outcome)
                    throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
                    UniformInterfaceException, NoSuchAlgorithmException, NullPointerException, IOException, ParseException {
        LabInfo labInfo = dbAdapter.getLab(lab);
        if (labInfo == null) {
            throw new IllegalArgumentException("Could not retrieve lab : " + lab.toString());
        }
        ValidationDbTestResult vNexusResult = nexusService.getLastResultBasedOnOutcome(name, version, labInfo.getSilo(),
                layers, optional, outcome);
        if (!dbAdapter.checkValidityOfNexusResult(vNexusResult)) {
            return null;
        }
        vNexusResult.setLab(labInfo);
        return vNexusResult;
    }

    public List<ValidationDbTestResult> getBasedOnDateFromNexus(@Nonnull String name, @Nonnull String version,
            @Nonnull String lab, @Nonnull Date date)
                    throws JsonParseException, JsonMappingException, IOException, ParseException, KeyManagementException,
                    ClientHandlerException, UniformInterfaceException, NoSuchAlgorithmException, NullPointerException {
        LabInfo labInfo = dbAdapter.getLab(lab);
        if (labInfo == null) {
            throw new IllegalArgumentException("Could not retrieve lab : " + lab.toString());
        }
        List<ValidationDbTestResult> vNexusResults = new ArrayList<ValidationDbTestResult>();
        List<ValidationDbTestResult> vResults = nexusService.getResults(name, version, labInfo.getSilo(), date);
        if (vResults != null && vResults.size() > 1) {
            for (ValidationDbTestResult vNexusResult : vResults) {
                if (dbAdapter.checkValidityOfNexusResult(vNexusResult)) {
                    vNexusResult.setLab(labInfo);
                    vNexusResults.add(vNexusResult);
                }
            }
        }
        return vNexusResults;
    }

    public Set<String> getBlueprintNamesOfLabFromDb(String lab) {
        Set<String> blueprintNames = new HashSet<String>();
        for (ValidationDbTestResult result : dbAdapter.getValidationTestResults()) {
            if (result.getLab().getLab().equals(lab)) {
                blueprintNames.add(result.getBlueprintInstance().getBlueprint().getBlueprintName());
            }
        }
        return blueprintNames;
    }

    public Set<String> getBlueprintVersionsFromDb(String name, String lab) {
        Set<String> blueprintVersions = new HashSet<String>();
        for (ValidationDbTestResult result : dbAdapter.getValidationTestResults()) {
            if (result.getLab().getLab().equals(lab)
                    && result.getBlueprintInstance().getBlueprint().getBlueprintName().equals(name)) {
                blueprintVersions.add(result.getBlueprintInstance().getVersion());
            }
        }
        return blueprintVersions;
    }

    public ValidationDbTestResult getResults(@Nonnull String submissionId)
            throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
            UniformInterfaceException, NoSuchAlgorithmException, IOException, NullPointerException, ParseException {
        Submission submission = submissionService.getSubmission(submissionId);
        ValidationDbTestResult vDbResult = dbAdapter.readResultFromDb(submissionId);
        return vDbResult == null ? this.getResultFromNexus(
                submission.getValidationDbTestResult().getBlueprintInstance().getBlueprint().getBlueprintName(),
                submission.getValidationDbTestResult().getBlueprintInstance().getVersion(),
                submission.getTimeslot().getLabInfo().getLab(), submission.getValidationDbTestResult().getTimestamp())
                : vDbResult;
    }

    public ValidationDbTestResult getResult(@Nonnull String name, @Nonnull String version, @Nonnull String lab,
            @Nonnull String timestamp)
                    throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
                    UniformInterfaceException, NoSuchAlgorithmException, NullPointerException, IOException, ParseException {
        LabInfo actualLabInfo = dbAdapter.getLab(lab);
        if (actualLabInfo == null) {
            return null;
        }
        ValidationDbTestResult vDbResult = dbAdapter.readResultFromDb(lab, timestamp);
        return vDbResult == null ? this.getResultFromNexus(name, version, lab, timestamp) : vDbResult;
    }

    public ValidationDbTestResult getLastResultBasedOnOutcome(@Nonnull String name, @Nonnull String version,
            @Nonnull String lab, Boolean allLayers, Boolean optional, boolean outcome)
                    throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
                    UniformInterfaceException, NoSuchAlgorithmException, IOException, NullPointerException, ParseException {
        LabInfo actualLabInfo = dbAdapter.getLab(lab);
        if (actualLabInfo == null) {
            return null;
        }
        List<ValidationDbTestResult> vDbResults = dbAdapter.readResultFromDb(name, version, lab, null, allLayers,
                optional, outcome);
        if (vDbResults != null) {
            vDbResults.removeIf(entry -> entry.getDateStorage() == null);
            DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
            Collections.sort(vDbResults, new Comparator<ValidationDbTestResult>() {
                @Override
                public int compare(ValidationDbTestResult vDbResult1, ValidationDbTestResult vDbResult2) {
                    try {
                        return dateFormat.parse(vDbResult2.getDateStorage())
                                .compareTo(dateFormat.parse(vDbResult1.getDateStorage()));
                    } catch (ParseException e) {
                        LOGGER.error(EELFLoggerDelegate.errorLogger,
                                "Error when parsing date. " + UserUtils.getStackTrace(e));
                        return 0;
                    }
                }
            });
            return vDbResults.get(0);
        }
        return this.getLastResultBasedOnOutcomeFromNexus(name, version, lab, allLayers, optional, outcome);
    }

    public ValidationDbTestResult getLastResultBasedOnOutcome(@Nonnull String name, @Nonnull String version,
            @Nonnull String lab, List<String> layers, Boolean optional, boolean outcome)
                    throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
                    UniformInterfaceException, NoSuchAlgorithmException, IOException, NullPointerException, ParseException {
        LabInfo actualLabInfo = dbAdapter.getLab(lab);
        if (actualLabInfo == null) {
            return null;
        }
        List<ValidationDbTestResult> vDbResults = dbAdapter.readResultFromDb(name, version, lab, layers, null, optional,
                outcome);
        if (vDbResults != null && vDbResults.size() > 0) {
            vDbResults.removeIf(entry -> entry.getDateStorage() == null);
            DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
            Collections.sort(vDbResults, new Comparator<ValidationDbTestResult>() {
                @Override
                public int compare(ValidationDbTestResult vDbResult1, ValidationDbTestResult vDbResult2) {
                    try {
                        return dateFormat.parse(vDbResult2.getDateStorage())
                                .compareTo(dateFormat.parse(vDbResult2.getDateStorage()));
                    } catch (ParseException e) {
                        LOGGER.error(EELFLoggerDelegate.errorLogger,
                                "Error when parsing date. " + UserUtils.getStackTrace(e));
                        return 0;
                    }
                }
            });
            return vDbResults.get(0);
        }
        return this.getLastResultBasedOnOutcomeFromNexus(name, version, lab, layers, optional, outcome);
    }

}
