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
import org.akraino.validation.ui.client.nexus.resources.ValidationNexusTestResult;
import org.akraino.validation.ui.data.BlueprintLayer;
import org.akraino.validation.ui.data.Lab;
import org.akraino.validation.ui.data.SubmissionData;
import org.akraino.validation.ui.entity.LabInfo;
import org.akraino.validation.ui.entity.LabSilo;
import org.akraino.validation.ui.entity.ValidationDbTestResult;
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
    private SiloService siloService;

    @Autowired
    NexusExecutorClient nexusService;

    @Autowired
    LabService labService;

    @Autowired
    DbResultAdapter dbAdapter;

    public List<Lab> getLabsFromNexus()
            throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
            UniformInterfaceException, NoSuchAlgorithmException, IOException, IllegalArgumentException, ParseException {
        List<Lab> labs = new ArrayList<Lab>();
        for (String cLabSilo : nexusService.getResource(null)) {
            for (LabSilo silo : siloService.getSilos()) {
                if (silo.getSilo().equals(cLabSilo)) {
                    labs.add(silo.getLab().getLab());
                }
            }
        }
        return labs;
    }

    public List<String> getBlueprintNamesOfLabFromNexus(@Nonnull Lab lab)
            throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
            UniformInterfaceException, NoSuchAlgorithmException, IOException, IllegalArgumentException, ParseException {
        String siloText = null;
        for (LabSilo silo : siloService.getSilos()) {
            if (silo.getLab().getLab().equals(lab)) {
                siloText = silo.getSilo();
            }
        }
        if (siloText == null) {
            throw new IllegalArgumentException("Could not retrieve blueprint names of lab : " + lab.toString());
        }
        List<String> blueprintNames = new ArrayList<String>();
        List<String> cBlueprintNames = nexusService.getResource(siloText);
        for (String cBlueprintName : cBlueprintNames) {
            if (!cBlueprintName.equals("job")) {
                blueprintNames.add(cBlueprintName);
            }
        }
        return blueprintNames;
    }

    public List<String> getBlueprintVersionsFromNexus(@Nonnull String name, @Nonnull Lab lab)
            throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
            UniformInterfaceException, NoSuchAlgorithmException, IOException, IllegalArgumentException, ParseException {
        String siloText = null;
        for (LabSilo silo : siloService.getSilos()) {
            if (silo.getLab().getLab().equals(lab)) {
                siloText = silo.getSilo();
            }
        }
        if (siloText == null) {
            throw new IllegalArgumentException("Could not retrieve silo of the lab : " + lab.toString());
        }
        return nexusService.getResource(siloText, name);
    }

    public List<String> getBlueprintTimeStampsFromNexus(@Nonnull String name, @Nonnull String version, @Nonnull Lab lab)
            throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
            UniformInterfaceException, NoSuchAlgorithmException, IOException, ParseException {
        String siloText = null;
        for (LabSilo silo : siloService.getSilos()) {
            if (silo.getLab().getLab().equals(lab)) {
                siloText = silo.getSilo();
            }
        }
        if (siloText == null) {
            throw new IllegalArgumentException("Could not retrieve silo of the lab : " + lab.toString());
        }
        return nexusService.getResource(siloText, name, version);
    }

    public List<ValidationNexusTestResult> getResultsFromNexus(@Nonnull String name, @Nonnull String version,
            @Nonnull Lab lab, int noTimestamps)
                    throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
                    UniformInterfaceException, NoSuchAlgorithmException, IOException, IllegalArgumentException, ParseException {
        String siloText = null;
        for (LabSilo silo : siloService.getSilos()) {
            if (silo.getLab().getLab().equals(lab)) {
                siloText = silo.getSilo();
            }
        }
        if (siloText == null) {
            throw new IllegalArgumentException("Could not retrieve silo of the lab : " + lab.toString());
        }
        return nexusService.getResults(name, version, siloText, noTimestamps);
    }

    public ValidationNexusTestResult getResultFromNexus(@Nonnull String name, @Nonnull String version, @Nonnull Lab lab,
            @Nonnull String timestamp) throws JsonParseException, JsonMappingException, IOException,
    KeyManagementException, ClientHandlerException, UniformInterfaceException, NoSuchAlgorithmException,
    NullPointerException, ParseException {
        String siloText = null;
        for (LabSilo silo : siloService.getSilos()) {
            if (silo.getLab().getLab().equals(lab)) {
                siloText = silo.getSilo();
            }
        }
        if (siloText == null) {
            throw new IllegalArgumentException("Could not retrieve silo of the lab : " + lab.toString());
        }
        return nexusService.getResult(name, version, siloText, timestamp);
    }

    public ValidationNexusTestResult getLastResultBasedOnOutcomeFromNexus(@Nonnull String name, @Nonnull String version,
            @Nonnull Lab lab, Boolean allLayers, Boolean optional, boolean outcome)
                    throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
                    UniformInterfaceException, NoSuchAlgorithmException, NullPointerException, IOException, ParseException {
        String siloText = null;
        for (LabSilo silo : siloService.getSilos()) {
            if (silo.getLab().getLab().equals(lab)) {
                siloText = silo.getSilo();
            }
        }
        if (siloText == null) {
            throw new IllegalArgumentException("Lab does not exist: " + lab.toString());
        }
        return nexusService.getLastResultBasedOnOutcome(name, version, siloText, allLayers, optional, outcome);
    }

    public ValidationNexusTestResult getLastResultBasedOnOutcomeFromNexus(@Nonnull String name, @Nonnull String version,
            @Nonnull Lab lab, @Nonnull List<BlueprintLayer> layers, Boolean optional, boolean outcome)
                    throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
                    UniformInterfaceException, NoSuchAlgorithmException, NullPointerException, IOException, ParseException {
        String siloText = null;
        for (LabSilo silo : siloService.getSilos()) {
            if (silo.getLab().getLab().equals(lab)) {
                siloText = silo.getSilo();
            }
        }
        if (siloText == null) {
            throw new IllegalArgumentException("Lab does not exist: " + lab.toString());
        }
        return nexusService.getLastResultBasedOnOutcome(name, version, siloText, layers, optional, outcome);
    }

    public List<ValidationNexusTestResult> getBasedOnDateFromNexus(@Nonnull String name, @Nonnull String version,
            @Nonnull Lab lab, @Nonnull Date date)
                    throws JsonParseException, JsonMappingException, IOException, ParseException, KeyManagementException,
                    ClientHandlerException, UniformInterfaceException, NoSuchAlgorithmException, NullPointerException {
        String siloText = null;
        for (LabSilo silo : siloService.getSilos()) {
            if (silo.getLab().getLab().equals(lab)) {
                siloText = silo.getSilo();
            }
        }
        if (siloText == null) {
            throw new IllegalArgumentException("Lab does not exist: " + lab.toString());
        }
        return nexusService.getResults(name, version, siloText, date);
    }

    public Set<Lab> getLabsFromDb() {
        Set<Lab> labs = new HashSet<Lab>();
        for (ValidationDbTestResult result : dbAdapter.getValidationTestResults()) {
            labs.add(result.getLab().getLab());
        }
        return labs;
    }

    public Set<String> getBlueprintNamesOfLabFromDb(Lab lab) {
        Set<String> blueprintNames = new HashSet<String>();
        for (ValidationDbTestResult result : dbAdapter.getValidationTestResults()) {
            if (result.getLab().getLab().equals(lab)) {
                blueprintNames.add(result.getBlueprintName());
            }
        }
        return blueprintNames;
    }

    public Set<String> getBlueprintVersionsFromDb(String name, Lab lab) {
        Set<String> blueprintVersions = new HashSet<String>();
        for (ValidationDbTestResult result : dbAdapter.getValidationTestResults()) {
            if (result.getLab().getLab().equals(lab) && result.getBlueprintName().equals(name)) {
                blueprintVersions.add(result.getVersion());
            }
        }
        return blueprintVersions;
    }

    public ValidationNexusTestResult getResults(@Nonnull String submissionId)
            throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
            UniformInterfaceException, NoSuchAlgorithmException, IOException, NullPointerException, ParseException {
        SubmissionData submissionData = submissionService.getSubmissionData(submissionId);
        ValidationNexusTestResult vNexusResult = dbAdapter.readResultFromDb(submissionId);
        return vNexusResult == null
                ? this.getResultFromNexus(submissionData.getValidationNexusTestResult().getBlueprintName(),
                        submissionData.getValidationNexusTestResult().getVersion(),
                        submissionData.getTimeslot().getLab().getLab(),
                        submissionData.getValidationNexusTestResult().getTimestamp())
                        : vNexusResult;
    }

    public ValidationNexusTestResult getResult(@Nonnull String name, @Nonnull String version, @Nonnull Lab lab,
            @Nonnull String timestamp)
                    throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
                    UniformInterfaceException, NoSuchAlgorithmException, NullPointerException, IOException, ParseException {
        LabInfo actualLabInfo = labService.getLab(lab);
        if (actualLabInfo == null) {
            return null;
        }
        ValidationNexusTestResult vNexusResult = dbAdapter.readResultFromDb(lab, timestamp);
        return vNexusResult == null ? this.getResultFromNexus(name, version, lab, timestamp) : vNexusResult;
    }

    public ValidationNexusTestResult getLastResultBasedOnOutcome(@Nonnull String name, @Nonnull String version,
            @Nonnull Lab lab, Boolean allLayers, Boolean optional, boolean outcome)
                    throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
                    UniformInterfaceException, NoSuchAlgorithmException, IOException, NullPointerException, ParseException {
        LabInfo actualLabInfo = labService.getLab(lab);
        if (actualLabInfo == null) {
            return null;
        }
        List<ValidationNexusTestResult> vNexusResults = dbAdapter.readResultFromDb(name, version, lab, null, allLayers,
                optional, outcome);
        if (vNexusResults != null) {
            vNexusResults.removeIf(entry -> entry.getDateOfStorage() == null);
            DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
            Collections.sort(vNexusResults, new Comparator<ValidationNexusTestResult>() {
                @Override
                public int compare(ValidationNexusTestResult vNexusResult1, ValidationNexusTestResult vNexusResult2) {
                    try {
                        return dateFormat.parse(vNexusResult2.getDateOfStorage())
                                .compareTo(dateFormat.parse(vNexusResult1.getDateOfStorage()));
                    } catch (ParseException e) {
                        LOGGER.error(EELFLoggerDelegate.errorLogger,
                                "Error when parsing date. " + UserUtils.getStackTrace(e));
                        return 0;
                    }
                }
            });
            return vNexusResults.get(0);
        }
        return this.getLastResultBasedOnOutcomeFromNexus(name, version, lab, allLayers, optional, outcome);
    }

    public ValidationNexusTestResult getLastResultBasedOnOutcome(@Nonnull String name, @Nonnull String version,
            @Nonnull Lab lab, List<BlueprintLayer> layers, Boolean optional, boolean outcome)
                    throws JsonParseException, JsonMappingException, KeyManagementException, ClientHandlerException,
                    UniformInterfaceException, NoSuchAlgorithmException, IOException, NullPointerException, ParseException {
        LabInfo actualLabInfo = labService.getLab(lab);
        if (actualLabInfo == null) {
            return null;
        }
        List<ValidationNexusTestResult> vNexusResults = dbAdapter.readResultFromDb(name, version, lab, layers, null,
                optional, outcome);
        if (vNexusResults != null) {
            vNexusResults.removeIf(entry -> entry.getDateOfStorage() == null);
            DateFormat dateFormat = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy", Locale.US);
            Collections.sort(vNexusResults, new Comparator<ValidationNexusTestResult>() {
                @Override
                public int compare(ValidationNexusTestResult vNexusResult1, ValidationNexusTestResult vNexusResult2) {
                    try {
                        return dateFormat.parse(vNexusResult2.getDateOfStorage())
                                .compareTo(dateFormat.parse(vNexusResult1.getDateOfStorage()));
                    } catch (ParseException e) {
                        LOGGER.error(EELFLoggerDelegate.errorLogger,
                                "Error when parsing date. " + UserUtils.getStackTrace(e));
                        return 0;
                    }
                }
            });
            return vNexusResults.get(0);
        }
        return this.getLastResultBasedOnOutcomeFromNexus(name, version, lab, layers, optional, outcome);
    }

}
