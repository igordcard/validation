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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.akraino.validation.ui.dao.BlueprintDAO;
import org.akraino.validation.ui.dao.BlueprintInstanceDAO;
import org.akraino.validation.ui.dao.BlueprintLayerDAO;
import org.akraino.validation.ui.dao.LabDAO;
import org.akraino.validation.ui.dao.TimeslotDAO;
import org.akraino.validation.ui.dao.ValidationTestResultDAO;
import org.akraino.validation.ui.dao.WRobotTestResultDAO;
import org.akraino.validation.ui.data.JnksJobNotify;
import org.akraino.validation.ui.entity.Blueprint;
import org.akraino.validation.ui.entity.BlueprintInstance;
import org.akraino.validation.ui.entity.BlueprintLayer;
import org.akraino.validation.ui.entity.LabInfo;
import org.akraino.validation.ui.entity.Submission;
import org.akraino.validation.ui.entity.Timeslot;
import org.akraino.validation.ui.entity.ValidationDbTestResult;
import org.akraino.validation.ui.entity.WRobotDbTestResult;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

@Service
@Transactional
public class DbAdapter {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(DbAdapter.class);
    private static final Object LOCK = new Object();

    @Autowired
    private ValidationTestResultDAO vTestResultDAO;

    @Autowired
    private WRobotTestResultDAO wRobotDAO;

    @Autowired
    private LabDAO labDAO;

    @Autowired
    private BlueprintLayerDAO layerDAO;

    @Autowired
    private BlueprintInstanceDAO bluInstDao;

    @Autowired
    private BlueprintDAO blueprintDAO;

    @Autowired
    private TimeslotDAO timeslotDAO;

    @Autowired
    DbSubmissionAdapter subService;

    public void associateSubmissionWithValidationResult(Submission submission)
            throws JsonParseException, JsonMappingException, IOException {
        synchronized (LOCK) {
            if (!compareBluInstances(submission.getValidationDbTestResult().getBlueprintInstance(),
                    bluInstDao.getBlueprintInstance(
                            submission.getValidationDbTestResult().getBlueprintInstance().getBlueprintInstanceId()))) {
                throw new RuntimeException("Blueprint instance data changed.");
            }
            if (!compareTimeslots(submission.getTimeslot(),
                    timeslotDAO.getTimeslot(submission.getTimeslot().getTimeslotId()))) {
                throw new RuntimeException("Timeslot data changed.");
            }

            boolean canContinue = false;
            for (Timeslot timeslot : bluInstDao
                    .getBlueprintInstance(
                            submission.getValidationDbTestResult().getBlueprintInstance().getBlueprintInstanceId())
                    .getTimeslots()) {
                if (compareTimeslots(timeslot, submission.getTimeslot())) {
                    canContinue = true;
                    break;
                }
            }
            if (!canContinue) {
                throw new RuntimeException("Configured timeslot is no longer assigned to the blueprint instance.");
            }
            submission.getValidationDbTestResult().setSubmission(submission);
            vTestResultDAO.saveOrUpdate(submission.getValidationDbTestResult());
            if (submission.getValidationDbTestResult().getWRobotDbTestResults() != null) {
                for (WRobotDbTestResult vRobotDbResult : submission.getValidationDbTestResult()
                        .getWRobotDbTestResults()) {
                    vRobotDbResult.setValidationDbTestResult(submission.getValidationDbTestResult());
                    wRobotDAO.saveOrUpdate(vRobotDbResult);
                }
            }
        }
    }

    public void storeResultsInDb(List<ValidationDbTestResult> vNexusResults) {
        synchronized (LOCK) {
            if (vNexusResults == null || vNexusResults.size() < 1) {
                return;
            }
            for (ValidationDbTestResult vNexusResult : vNexusResults) {
                if (vNexusResult.getWRobotDbTestResults() == null) {
                    continue;
                }
                if (!checkValidityOfNexusResult(vNexusResult)) {
                    continue;
                }
                LabInfo labInfo = labDAO.getLabBasedOnSilo(vNexusResult.getLab().getSilo());
                ValidationDbTestResult vDbResult = vTestResultDAO.getValidationTestResult(labInfo,
                        vNexusResult.getTimestamp());
                if (vDbResult == null) {
                    vDbResult = vNexusResult;
                    vDbResult.setLab(labInfo);
                    Blueprint blueprint = blueprintDAO
                            .getBlueprint(vNexusResult.getBlueprintInstance().getBlueprint().getBlueprintName());
                    if (blueprint == null) {
                        blueprint = vNexusResult.getBlueprintInstance().getBlueprint();
                        blueprintDAO.saveOrUpdate(blueprint);
                    }
                    BlueprintInstance blueprintInst = bluInstDao.getBlueprintInstance(blueprint,
                            (vNexusResult.getBlueprintInstance().getVersion()));
                    if (blueprintInst == null) {
                        blueprintInst = vNexusResult.getBlueprintInstance();
                        blueprintInst.setBlueprint(blueprint);
                        bluInstDao.saveOrUpdate(blueprintInst);
                    }
                    vDbResult.setBlueprintInstance(blueprintInst);
                }
                updateBlueInstLayers(vNexusResult);
                vDbResult.setResult(vNexusResult.getResult());
                vDbResult.setDateStorage(vNexusResult.getDateStorage());
                LOGGER.debug(EELFLoggerDelegate.debugLogger,
                        "Storing validation test result with keys: blueprint name: "
                                + vNexusResult.getBlueprintInstance().getBlueprint().getBlueprintName() + ", version: "
                                + vNexusResult.getBlueprintInstance().getVersion() + ", lab: "
                                + vNexusResult.getLab().getSilo() + ", timestamp: " + vNexusResult.getTimestamp());
                vTestResultDAO.saveOrUpdate(vDbResult);
                List<org.akraino.validation.ui.entity.WRobotDbTestResult> wRobotDbResults = wRobotDAO
                        .getWRobotTestResult(vDbResult);
                if (wRobotDbResults == null) {
                    // Store the new wrapper robot rest results in db
                    for (WRobotDbTestResult wNexusResult : vNexusResult.getWRobotDbTestResults()) {
                        wNexusResult.setValidationDbTestResult(vDbResult);
                        wRobotDAO.saveOrUpdate(wNexusResult);
                    }
                } else if (vDbResult.getSubmission() != null) {
                    // update validation result related to submission
                    for (WRobotDbTestResult wNexusResult : vNexusResult.getWRobotDbTestResults()) {
                        WRobotDbTestResult wRobotDbResult = wRobotDAO.getWRobotTestResult(wNexusResult.getLayer(),
                                vDbResult);
                        wRobotDbResult.setRobotTestResults(wNexusResult.getRobotTestResults());
                        wRobotDAO.saveOrUpdate(wRobotDbResult);
                    }
                }
            }
        }
    }

    public void updateTimestamp(JnksJobNotify jnksJobNotify) {
        synchronized (LOCK) {
            if (!checkValidityOfJenkinsNotification(jnksJobNotify)) {
                return;
            }
            ValidationDbTestResult vDbSubmission = vTestResultDAO
                    .getValidationTestResult(subService.getSubmission(String.valueOf(jnksJobNotify.getSubmissionId())));
            ValidationDbTestResult vDbTimestamp = vTestResultDAO.getValidationTestResult(vDbSubmission.getLab(),
                    jnksJobNotify.getTimestamp());
            if (vDbTimestamp == null) {
                vDbSubmission.setTimestamp(jnksJobNotify.getTimestamp());
                vTestResultDAO.saveOrUpdate(vDbSubmission);
                return;
            }
            // Delete the wrobot results associated with submission validation result
            List<WRobotDbTestResult> wRobotResults = wRobotDAO.getWRobotTestResult(vDbSubmission);
            if (wRobotResults != null && wRobotResults.size() > 0) {
                for (WRobotDbTestResult wRobotResult : wRobotResults) {
                    wRobotDAO.deleteWRobotTestResult(wRobotResult.getWRobotResultId());
                }
            }
            // Change the timestamp wrobot results to point to submission validation result
            wRobotResults = wRobotDAO.getWRobotTestResult(vDbTimestamp);
            if (wRobotResults != null && wRobotResults.size() > 0) {
                for (WRobotDbTestResult wRobotResult : wRobotResults) {
                    wRobotResult.setValidationDbTestResult(vDbSubmission);
                    wRobotDAO.saveOrUpdate(wRobotResult);
                }
            }
            vTestResultDAO.deleteValidationTestResult(vDbTimestamp);
            // Now vDbSubmission can be updated
            vDbSubmission.setDateStorage(vDbTimestamp.getDateStorage());
            vDbSubmission.setResult(vDbTimestamp.getResult());
            vDbSubmission.setTimestamp(vDbTimestamp.getTimestamp());
            vTestResultDAO.saveOrUpdate(vDbSubmission);
        }
    }

    public List<ValidationDbTestResult> readResultFromDb(String blueprintName, String version, String lab,
            List<String> layers, Boolean allLayers, Boolean optional, Boolean outcome)
            throws JsonParseException, JsonMappingException, IOException {
        synchronized (LOCK) {
            LabInfo actualLabInfo = null;
            if (lab != null) {
                actualLabInfo = labDAO.getLab(lab);
                if (actualLabInfo == null) {
                    return null;
                }
            }
            Blueprint blueprint = null;
            if (blueprintName != null) {
                blueprint = blueprintDAO.getBlueprint(blueprintName);
                if (blueprint == null) {
                    return null;
                }
            }
            BlueprintInstance blueprintInst = bluInstDao.getBlueprintInstance(blueprint, version);
            if (blueprintInst == null) {
                return null;
            }
            List<ValidationDbTestResult> vDbResults = vTestResultDAO.getValidationTestResults(blueprintInst,
                    actualLabInfo, allLayers, optional, outcome);
            if (vDbResults == null || vDbResults.size() < 1) {
                return null;
            }
            List<ValidationDbTestResult> actualResults = new ArrayList<ValidationDbTestResult>();
            for (ValidationDbTestResult vDbResult : vDbResults) {
                if (layers != null && layers.size() > 0) {
                    List<String> storedLayers = new ArrayList<String>();
                    List<WRobotDbTestResult> wDbResults = wRobotDAO.getWRobotTestResult(vDbResult);
                    if (wDbResults == null || wDbResults.size() < 1) {
                        continue;
                    }
                    for (WRobotDbTestResult wRobot : wDbResults) {
                        storedLayers.add(wRobot.getLayer());
                    }
                    if (!new HashSet<>(storedLayers).equals(new HashSet<>(layers))) {
                        continue;
                    }
                }
                actualResults.add(vDbResult);
            }
            return actualResults;
        }
    }

    public ValidationDbTestResult readResultFromDb(@Nonnull String lab, @Nonnull String timestamp)
            throws JsonParseException, JsonMappingException, IOException {
        synchronized (LOCK) {
            LabInfo actualLabInfo = labDAO.getLab(lab);
            ValidationDbTestResult vDbResult = vTestResultDAO.getValidationTestResult(actualLabInfo, timestamp);
            if (vDbResult == null) {
                return null;
            }
            return vDbResult;
        }
    }

    public ValidationDbTestResult readResultFromDb(@Nonnull String submissionId)
            throws JsonParseException, JsonMappingException, IOException {
        synchronized (LOCK) {
            return vTestResultDAO.getValidationTestResult(subService.getSubmission(submissionId));
        }
    }

    public void deleteUnreferencedEntries(List<ValidationDbTestResult> vNexusResults) {
        synchronized (LOCK) {
            if (vNexusResults == null || vNexusResults.size() < 1) {
                return;
            }
            LabInfo labInfo = labDAO.getLabBasedOnSilo(vNexusResults.get(0).getLab().getSilo());
            if (labInfo == null) {
                return;
            }
            Blueprint blueprint = blueprintDAO
                    .getBlueprint(vNexusResults.get(0).getBlueprintInstance().getBlueprint().getBlueprintName());
            if (blueprint == null) {
                return;
            }
            BlueprintInstance blueInst = bluInstDao.getBlueprintInstance(blueprint,
                    vNexusResults.get(0).getBlueprintInstance().getVersion());
            if (blueInst == null) {
                return;
            }
            List<ValidationDbTestResult> vDbResults = vTestResultDAO.getValidationTestResults(blueInst, labInfo, null,
                    null, null);
            if (vDbResults == null || vDbResults.size() < 1) {
                return;
            }
            for (ValidationDbTestResult vDbResult : vDbResults) {
                if (vDbResult.getSubmission() != null) {
                    continue;
                }
                boolean deletion = true;
                String dbTimestamp = vDbResult.getTimestamp();
                LabInfo dbLabInfo = vDbResult.getLab();
                for (ValidationDbTestResult vNexusResult : vNexusResults) {
                    LabInfo nexusLabInfo = labDAO.getLabBasedOnSilo(vNexusResult.getLab().getSilo());
                    if (nexusLabInfo == null) {
                        continue;
                    }
                    if (vNexusResult.getTimestamp().equals(dbTimestamp) && nexusLabInfo.equals(dbLabInfo)) {
                        deletion = false;
                        break;
                    }
                }
                if (deletion) {
                    LOGGER.debug(EELFLoggerDelegate.debugLogger,
                            "Deleting unreferenced validation result with id: " + vDbResult.getResultId());
                    // Delete old associated wrapper robot rest results from db
                    for (WRobotDbTestResult wRobotDbResult : wRobotDAO.getWRobotTestResult(vDbResult)) {
                        wRobotDAO.deleteWRobotTestResult(wRobotDbResult.getWRobotResultId());
                    }
                    vTestResultDAO.deleteValidationTestResult(vDbResult);
                }
            }
        }
    }

    public List<ValidationDbTestResult> getValidationTestResults() {
        synchronized (LOCK) {
            return vTestResultDAO.getValidationTestResults();
        }
    }

    public ValidationDbTestResult getValidationTestResult(Integer resultId) {
        synchronized (LOCK) {
            return vTestResultDAO.getValidationTestResult(resultId);
        }
    }

    public List<ValidationDbTestResult> getValidationTestResults(String blueprintName, @Nonnull String version,
            LabInfo labInfo, Boolean allLayers, Boolean optional, Boolean outcome) {
        synchronized (LOCK) {
            Blueprint blueprint = null;
            if (blueprintName != null) {
                blueprint = blueprintDAO.getBlueprint(blueprintName);
                if (blueprint == null) {
                    return null;
                }
            }
            BlueprintInstance bluInst = bluInstDao.getBlueprintInstance(blueprint, version);
            if (bluInst == null) {
                return null;
            }
            return vTestResultDAO.getValidationTestResults(bluInst, labInfo, allLayers, optional, outcome);
        }
    }

    public ValidationDbTestResult getValidationTestResult(LabInfo labInfo, String timestamp) {
        synchronized (LOCK) {
            return vTestResultDAO.getValidationTestResult(labInfo, timestamp);
        }
    }

    public ValidationDbTestResult getValidationTestResult(String labSilo, String timestamp) {
        synchronized (LOCK) {
            return vTestResultDAO.getValidationTestResult(labDAO.getLabBasedOnSilo(labSilo), timestamp);
        }
    }

    public ValidationDbTestResult getValidationTestResult(@Nonnull Submission submission) {
        synchronized (LOCK) {
            return vTestResultDAO.getValidationTestResult(submission);
        }
    }

    public List<WRobotDbTestResult> getWRobotTestResults() {
        synchronized (LOCK) {
            return wRobotDAO.getWRobotTestResults();
        }
    }

    public WRobotDbTestResult getWRobotTestResult(Integer wRobotResultId) {
        synchronized (LOCK) {
            return wRobotDAO.getWRobotTestResult(wRobotResultId);
        }
    }

    public List<WRobotDbTestResult> getWRobotTestResult(ValidationDbTestResult vResult) {
        synchronized (LOCK) {
            return wRobotDAO.getWRobotTestResult(vResult);
        }
    }

    public void saveLab(LabInfo lab) {
        synchronized (LOCK) {
            labDAO.saveOrUpdate(lab);
        }
    }

    public void deleteLab(LabInfo lab) {
        synchronized (LOCK) {
            labDAO.deleteLab(lab);
        }
    }

    public LabInfo getLab(String lab) {
        synchronized (LOCK) {
            return labDAO.getLab(lab);
        }
    }

    public LabInfo getLabBasedOnSilo(String silo) {
        synchronized (LOCK) {
            return labDAO.getLabBasedOnSilo(silo);
        }
    }

    public List<LabInfo> getLabs() {
        synchronized (LOCK) {
            return labDAO.getLabs();
        }
    }

    public void deleteLabAll() {
        synchronized (LOCK) {
            labDAO.deleteAll();
        }
    }

    public void saveBlueprintInstance(BlueprintInstance blueprintIns) {
        synchronized (LOCK) {
            Set<Timeslot> timeslots = blueprintIns.getTimeslots();
            if (timeslots != null && timeslots.size() > 1) {
                for (Timeslot timeslot : timeslots) {
                    if (!compareTimeslots(timeslot, timeslotDAO.getTimeslot(timeslot.getTimeslotId()))) {
                        throw new RuntimeException("Timeslot instance data changed.");
                    }
                }
                bluInstDao.merge(blueprintIns);
                return;
            }
            bluInstDao.saveOrUpdate(blueprintIns);
        }
    }

    public List<BlueprintInstance> getBlueprintInstances() {
        synchronized (LOCK) {
            return bluInstDao.getBlueprintInstances();
        }
    }

    public BlueprintInstance getBlueprintInstance(int instId) {
        synchronized (LOCK) {
            return bluInstDao.getBlueprintInstance(instId);
        }
    }

    public BlueprintInstance getBlueprintInstance(Blueprint blueprint, String version) {
        synchronized (LOCK) {
            return bluInstDao.getBlueprintInstance(blueprint, version);
        }
    }

    public void deleteBlueprintInstance(BlueprintInstance blueprintIns) {
        synchronized (LOCK) {
            bluInstDao.deleteBlueprintInstance(blueprintIns);
        }
    }

    public void deleteBluInstAll() {
        synchronized (LOCK) {
            bluInstDao.deleteAll();
        }
    }

    public void saveBlueprintLayer(BlueprintLayer layer) {
        synchronized (LOCK) {
            layerDAO.saveOrUpdate(layer);
        }
    }

    public BlueprintLayer getBlueprintLayer(Integer layerId) {
        synchronized (LOCK) {
            return layerDAO.getBlueprintLayer(layerId);
        }
    }

    public BlueprintLayer getBlueprintLayer(String layerData) {
        synchronized (LOCK) {
            return layerDAO.getBlueprintLayer(layerData);
        }
    }

    public List<BlueprintLayer> getBlueprintLayers() {
        synchronized (LOCK) {
            return layerDAO.getBlueprintLayers();
        }
    }

    public void deleteBlueprintLayer(BlueprintLayer layer) {
        synchronized (LOCK) {
            layerDAO.deleteBlueprintLayer(layer);
        }
    }

    public void deleteBluLayersAll() {
        synchronized (LOCK) {
            layerDAO.deleteAll();
        }
    }

    public void saveBlueprint(Blueprint blueprint) {
        synchronized (LOCK) {
            blueprintDAO.saveOrUpdate(blueprint);
        }
    }

    public Blueprint getBlueprint(String name) {
        synchronized (LOCK) {
            return blueprintDAO.getBlueprint(name);
        }
    }

    public List<Blueprint> getBlueprints() {
        synchronized (LOCK) {
            return blueprintDAO.getBlueprints();
        }
    }

    public void deleteBlueprint(Blueprint blueprint) {
        synchronized (LOCK) {
            blueprintDAO.deleteBlueprint(blueprint);
        }
    }

    public void deleteBluAll() {
        synchronized (LOCK) {
            blueprintDAO.deleteAll();
        }
    }

    public void saveTimeslot(Timeslot timeslot) {
        synchronized (LOCK) {
            LabInfo labInfo = timeslot.getLabInfo();
            if (labInfo != null) {
                if (!compareLabInfos(labInfo, labDAO.getLab(labInfo.getLabId()))) {
                    throw new RuntimeException("Lab data changed.");
                }
            }
            timeslotDAO.saveOrUpdate(timeslot);
        }
    }

    public List<Timeslot> getTimeslots() {
        synchronized (LOCK) {
            return timeslotDAO.getTimeslots();
        }
    }

    public void deleteTimeslot(Timeslot timeslot) {
        synchronized (LOCK) {
            timeslotDAO.deleteTimeslot(timeslot);
        }
    }

    public void deleteTimeslotAll() {
        synchronized (LOCK) {
            timeslotDAO.deleteAll();
        }
    }

    public boolean checkValidityOfNexusResult(ValidationDbTestResult vNexusResult) {
        if (vNexusResult == null) {
            return true;
        }
        LabInfo labInfo = labDAO.getLabBasedOnSilo(vNexusResult.getLab().getSilo());
        if (labInfo == null) {
            throw new RuntimeException("Lab silo : " + vNexusResult.getLab().getSilo() + " not found");
        }
        ValidationDbTestResult vDbResult = vTestResultDAO.getValidationTestResult(
                labDAO.getLabBasedOnSilo(vNexusResult.getLab().getSilo()), vNexusResult.getTimestamp());
        Blueprint blueprint = null;
        BlueprintInstance bluInst = null;
        List<WRobotDbTestResult> wRobotDbResults = null;
        if (vDbResult != null) {
            blueprint = vDbResult.getBlueprintInstance().getBlueprint();
            labInfo = vDbResult.getLab();
            wRobotDbResults = wRobotDAO.getWRobotTestResult(vDbResult);
        } else {
            blueprint = blueprintDAO
                    .getBlueprint(vNexusResult.getBlueprintInstance().getBlueprint().getBlueprintName());
        }
        if (blueprint != null) {
            if (vDbResult != null) {
                bluInst = vDbResult.getBlueprintInstance();
            } else {
                bluInst = bluInstDao.getBlueprintInstance(blueprint, vNexusResult.getBlueprintInstance().getVersion());
            }
        }
        // Start comparison, be elastic with allLayers and optional
        if (!labInfo.getSilo().equals(vNexusResult.getLab().getSilo())) {
            LOGGER.error(EELFLoggerDelegate.errorLogger,
                    "Nexus has different data for blueprint : "
                            + vDbResult.getBlueprintInstance().getBlueprint().getBlueprintName() + ", version: "
                            + vDbResult.getBlueprintInstance().getVersion() + " and lab: " + vDbResult.getLab().getLab()
                            + ". Lab inconsistency : " + vDbResult.getLab() + " " + labInfo);
            return false;
        }
        if (blueprint != null) {
            if (!blueprint.getBlueprintName()
                    .equals(vNexusResult.getBlueprintInstance().getBlueprint().getBlueprintName())) {
                LOGGER.error(EELFLoggerDelegate.errorLogger,
                        "Nexus has different data for blueprint : " + blueprint.getBlueprintName()
                                + ". Name inconsistency : " + blueprint.getBlueprintName() + " "
                                + vNexusResult.getBlueprintInstance().getBlueprint().getBlueprintName());
                return false;
            }
        }
        if (bluInst != null) {
            if (!bluInst.getVersion().equals(vNexusResult.getBlueprintInstance().getVersion())) {
                LOGGER.error(EELFLoggerDelegate.errorLogger,
                        "Nexus has different data for blueprint : " + bluInst.getBlueprint().getBlueprintName()
                                + ", version: " + bluInst.getVersion() + ". Version inconsistency : "
                                + bluInst.getVersion() + " " + vNexusResult.getBlueprintInstance().getVersion());
                return false;
            }
        }
        if (wRobotDbResults != null) {
            List<String> storedLayers1 = new ArrayList<String>();
            for (WRobotDbTestResult wNexusResult : vNexusResult.getWRobotDbTestResults()) {
                storedLayers1.add(wNexusResult.getLayer());
            }
            List<String> storedLayers2 = new ArrayList<String>();
            for (WRobotDbTestResult wDbResult : wRobotDbResults) {
                storedLayers2.add(wDbResult.getLayer());
            }
            if (!new HashSet<>(storedLayers1).equals(new HashSet<>(storedLayers2))) {
                LOGGER.error(EELFLoggerDelegate.errorLogger,
                        "Nexus has different layer results for validation result id: " + vDbResult.getResultId());
                return false;
            }
        }
        return true;
    }

    private boolean checkValidityOfJenkinsNotification(JnksJobNotify jnksJobNotify) {
        ValidationDbTestResult vDbSubmission = vTestResultDAO
                .getValidationTestResult(subService.getSubmission(String.valueOf(jnksJobNotify.getSubmissionId())));
        if (vDbSubmission == null) {
            LOGGER.error(EELFLoggerDelegate.errorLogger, "Received timestamp for submission id : "
                    + jnksJobNotify.getSubmissionId() + " which has not validation result associated with it");
            return false;
        }
        if (!vDbSubmission.getAllLayers() && (vDbSubmission.getWRobotDbTestResults() == null
                || vDbSubmission.getWRobotDbTestResults().size() < 1)) {
            LOGGER.error(EELFLoggerDelegate.errorLogger, "Received timestamp for submission id : "
                    + jnksJobNotify.getSubmissionId() + " which is not stored correctly");
            return false;
        }
        ValidationDbTestResult vDbTimestamp = vTestResultDAO.getValidationTestResult(vDbSubmission.getLab(),
                jnksJobNotify.getTimestamp());
        if (vDbTimestamp == null) {
            return true;
        }
        if (vDbTimestamp.equals(vDbSubmission) || (vDbTimestamp.getSubmission() != null
                && !jnksJobNotify.getSubmissionId().equals(vDbTimestamp.getSubmission().getSubmissionId()))) {
            LOGGER.error(EELFLoggerDelegate.errorLogger, "Received same timestamp: " + jnksJobNotify.getTimestamp()
                    + " from nexus for submission id: " + jnksJobNotify.getSubmissionId());
            return false;
        }
        if (!vDbSubmission.getAllLayers()) {
            if (wRobotDAO.getWRobotTestResult(vDbSubmission).size() != wRobotDAO.getWRobotTestResult(vDbTimestamp)
                    .size()) {
                LOGGER.error(EELFLoggerDelegate.errorLogger, "No consistency exists in stored layers records.");
                return false;
            }
            List<String> storedLayers1 = new ArrayList<String>();
            List<String> storedLayers2 = new ArrayList<String>();
            List<WRobotDbTestResult> wDbResults = wRobotDAO.getWRobotTestResult(vDbSubmission);
            for (WRobotDbTestResult wRobot : wDbResults) {
                storedLayers1.add(wRobot.getLayer());
            }
            wDbResults = wRobotDAO.getWRobotTestResult(vDbTimestamp);
            for (WRobotDbTestResult wRobot : wDbResults) {
                storedLayers2.add(wRobot.getLayer());
            }
            if (!new HashSet<>(storedLayers1).equals(new HashSet<>(storedLayers2))) {
                LOGGER.error(EELFLoggerDelegate.errorLogger, "No consistency exists in stored layers records.");
                return false;
            }
        }
        // Be elastic with allLayers and optional
        if (!vDbSubmission.getBlueprintInstance().getBlueprint().getBlueprintName()
                .equals(vDbTimestamp.getBlueprintInstance().getBlueprint().getBlueprintName())
                || !vDbSubmission.getBlueprintInstance().getVersion()
                        .equals(vDbTimestamp.getBlueprintInstance().getVersion())
                || !compareLabInfos(vDbSubmission.getLab(), vDbTimestamp.getLab())) {
            LOGGER.error(EELFLoggerDelegate.errorLogger, "No consistency exists in database records.");
            return false;
        }
        return true;
    }

    private void updateBlueInstLayers(ValidationDbTestResult vNexusResult) {
        for (BlueprintInstance blueprintInst : bluInstDao.getBlueprintInstances()) {
            if (!blueprintInst.getBlueprint().getBlueprintName()
                    .equals(vNexusResult.getBlueprintInstance().getBlueprint().getBlueprintName())) {
                continue;
            }
            Set<BlueprintLayer> blueprintLayers = blueprintInst.getBlueprintLayers();
            if (blueprintLayers == null) {
                blueprintLayers = new HashSet<BlueprintLayer>();
            }
            for (WRobotDbTestResult nexusResult : vNexusResult.getWRobotDbTestResults()) {
                BlueprintLayer layer = layerDAO.getBlueprintLayer(nexusResult.getLayer());
                if (layer == null) {
                    layer = new BlueprintLayer();
                    layer.setLayer(nexusResult.getLayer());
                    layerDAO.saveOrUpdate(layer);
                }
                if (!blueprintLayers.contains(layer)) {
                    blueprintLayers.add(layer);
                }
            }
            blueprintInst.setBlueprintLayers(blueprintLayers);
            bluInstDao.saveOrUpdate(blueprintInst);
        }
    }

    private boolean compareBluInstances(BlueprintInstance inst1, BlueprintInstance inst2) {
        if (!inst1.getVersion().equals(inst2.getVersion())) {
            return false;
        }
        if (inst1.getBlueprintInstanceId() != inst2.getBlueprintInstanceId()) {
            return false;
        }
        Set<BlueprintLayer> layers1 = inst1.getBlueprintLayers();
        Set<BlueprintLayer> layers2 = inst2.getBlueprintLayers();
        if (!(layers1 == null && layers2 == null)) {
            if (layers1 != null && layers2 == null) {
                return false;
            }
            if (layers1 == null && layers2 != null) {
                return false;
            }
            if (!(layers1.size() == layers2.size())) {
                return false;
            }
            boolean overallLayerEquality = true;
            for (BlueprintLayer blulayer1 : layers1) {
                boolean layerEquality = false;
                for (BlueprintLayer blulayer2 : layers2) {
                    if (blulayer1.getLayer().equals(blulayer2.getLayer())) {
                        layerEquality = true;
                    }
                }
                if (!layerEquality) {
                    overallLayerEquality = false;
                    break;
                }
            }
            if (!overallLayerEquality) {
                return false;
            }
        }
        if (!compareBlueprints(inst1.getBlueprint(), inst2.getBlueprint())) {
            return false;
        }
        return true;
    }

    private boolean compareTimeslots(Timeslot timeslot1, Timeslot timeslot2) {
        if (!timeslot1.getStartDateTime().equals(timeslot2.getStartDateTime())
                || timeslot1.getTimeslotId() != timeslot2.getTimeslotId()) {
            return false;
        }
        if (!compareLabInfos(timeslot1.getLabInfo(), timeslot2.getLabInfo())) {
            return false;
        }
        return true;
    }

    private boolean compareBlueprints(Blueprint blueprint1, Blueprint blueprint2) {
        if (blueprint1 != null || blueprint2 != null) {
            if (blueprint1 != null && blueprint2 == null) {
                return false;
            }
            if (blueprint1 == null && blueprint2 != null) {
                return false;
            }
            if (blueprint1.getBlueprintId() != blueprint2.getBlueprintId()) {
                return false;
            }
            if (!blueprint1.getBlueprintName().equals(blueprint2.getBlueprintName())) {
                return false;
            }
        }
        return true;
    }

    private boolean compareLabInfos(LabInfo labInfo1, LabInfo labInfo2) {
        if (labInfo1 != null || labInfo2 != null) {
            if (labInfo1 != null && labInfo2 == null) {
                return false;
            }
            if (labInfo1 == null && labInfo2 != null) {
                return false;
            }
            if (labInfo1.getLabId() != labInfo2.getLabId()) {
                return false;
            }
            if (!labInfo1.getSilo().equals(labInfo2.getSilo())) {
                return false;
            }
            if (!labInfo1.getLab().equals(labInfo2.getLab())) {
                return false;
            }
        }
        return true;
    }

}
