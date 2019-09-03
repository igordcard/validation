package org.akraino.validation.ui.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.annotation.Nonnull;

import org.akraino.validation.ui.client.nexus.resources.RobotTestResult;
import org.akraino.validation.ui.client.nexus.resources.ValidationNexusTestResult;
import org.akraino.validation.ui.client.nexus.resources.WRobotNexusTestResult;
import org.akraino.validation.ui.dao.ValidationTestResultDAO;
import org.akraino.validation.ui.dao.WRobotTestResultDAO;
import org.akraino.validation.ui.data.BlueprintLayer;
import org.akraino.validation.ui.data.JnksJobNotify;
import org.akraino.validation.ui.data.Lab;
import org.akraino.validation.ui.data.SubmissionData;
import org.akraino.validation.ui.entity.LabInfo;
import org.akraino.validation.ui.entity.LabSilo;
import org.akraino.validation.ui.entity.Submission;
import org.akraino.validation.ui.entity.ValidationDbTestResult;
import org.akraino.validation.ui.entity.WRobotDbTestResult;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.onap.portalsdk.core.web.support.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
@Transactional
public class DbResultAdapter {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(DbResultAdapter.class);
    private static final Object LOCK = new Object();

    @Autowired
    LabService labService;

    @Autowired
    private ValidationTestResultDAO vTestResultDAO;

    @Autowired
    private WRobotTestResultDAO wRobotDAO;

    @Autowired
    private SiloService siloService;

    @Autowired
    DbSubmissionAdapter subService;

    public void associateSubmissionWithValidationResult(SubmissionData submissionData)
            throws JsonParseException, JsonMappingException, IOException {
        synchronized (LOCK) {
            ValidationDbTestResult vDbTestResult = this
                    .convertValidationNexusToDb(submissionData.getValidationNexusTestResult());
            Submission submission = new Submission();
            submission.setSubmissionId(submissionData.getSubmissionId());
            vDbTestResult.setSubmission(submission);
            vTestResultDAO.saveOrUpdate(vDbTestResult);
            List<WRobotDbTestResult> vRobotDbResults = this.convertWRobotNexusResultsToDb(
                    submissionData.getValidationNexusTestResult().getwRobotNexusTestResults());
            if (vRobotDbResults != null) {
                for (WRobotDbTestResult vRobotDbResult : vRobotDbResults) {
                    vRobotDbResult.setValidationTestResult(vDbTestResult);
                    wRobotDAO.saveOrUpdate(vRobotDbResult);
                }
            }
        }
    }

    public void storeResultInDb(List<ValidationNexusTestResult> vNexusResults) {
        synchronized (LOCK) {
            if (vNexusResults == null || vNexusResults.size() < 1) {
                return;
            }
            for (ValidationNexusTestResult vNexusResult : vNexusResults) {
                if (!checkValidityOfValidationNexusTestResult(vNexusResult)) {
                    continue;
                }
                LabInfo labInfo = null;
                for (LabSilo silo : siloService.getSilos()) {
                    if (silo.getSilo().equals(vNexusResult.getSilo())) {
                        labInfo = silo.getLab();
                    }
                }
                ValidationDbTestResult vDbResult = vTestResultDAO.getValidationTestResult(labInfo,
                        vNexusResult.getTimestamp());
                if (vDbResult == null) {
                    vDbResult = new ValidationDbTestResult();
                    vDbResult.setLab(labInfo);
                    vDbResult.setTimestamp(vNexusResult.getTimestamp());
                    vDbResult.setBlueprintName(vNexusResult.getBlueprintName());
                    vDbResult.setVersion(vNexusResult.getVersion());
                    vDbResult.setAllLayers(vNexusResult.getAllLayers());
                    vDbResult.setOptional(vNexusResult.getOptional());
                }
                vDbResult.setResult(vNexusResult.getResult());
                vDbResult.setDateStorage(vNexusResult.getDateOfStorage());
                LOGGER.debug(EELFLoggerDelegate.debugLogger,
                        "Storing validation test result with keys: blueprint name: " + vNexusResult.getBlueprintName()
                        + ", version: " + vNexusResult.getVersion() + ", lab: " + vNexusResult.getSilo()
                        + ", timestamp: " + vNexusResult.getTimestamp());
                vTestResultDAO.saveOrUpdate(vDbResult);
                List<org.akraino.validation.ui.entity.WRobotDbTestResult> wRobotDbResults = wRobotDAO
                        .getWRobotTestResult(vDbResult);
                if (wRobotDbResults == null) {
                    // Store the new wrapper robot rest results in db
                    for (WRobotNexusTestResult wNexusResult : vNexusResult.getwRobotNexusTestResults()) {
                        WRobotDbTestResult wRobotDbResult = new WRobotDbTestResult();
                        wRobotDbResult.setLayer(wNexusResult.getBlueprintLayer().name());
                        wRobotDbResult.setValidationTestResult(vDbResult);
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            wRobotDbResult
                            .setRobotTestResults(mapper.writeValueAsString(wNexusResult.getRobotTestResults()));
                        } catch (JsonProcessingException e) {
                            LOGGER.error(EELFLoggerDelegate.errorLogger,
                                    "Error while converting POJO to string. " + UserUtils.getStackTrace(e));
                            continue;
                        }
                        wRobotDAO.saveOrUpdate(wRobotDbResult);
                    }
                } else if (vDbResult.getSubmission() != null) {
                    // update validation result related to submission
                    for (WRobotNexusTestResult wNexusResult : vNexusResult.getwRobotNexusTestResults()) {
                        WRobotDbTestResult wRobotDbResult = wRobotDAO
                                .getWRobotTestResult(wNexusResult.getBlueprintLayer().name(), vDbResult);
                        ObjectMapper mapper = new ObjectMapper();
                        try {
                            wRobotDbResult
                            .setRobotTestResults(mapper.writeValueAsString(wNexusResult.getRobotTestResults()));
                        } catch (JsonProcessingException e) {
                            LOGGER.error(EELFLoggerDelegate.errorLogger,
                                    "Error while converting POJO to string. " + UserUtils.getStackTrace(e));
                            continue;
                        }
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
                    wRobotResult.setValidationTestResult(vDbSubmission);
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

    public List<ValidationNexusTestResult> readResultFromDb(String blueprintName, String version, Lab lab,
            List<BlueprintLayer> layers, Boolean allLayers, Boolean optional, Boolean outcome)
                    throws JsonParseException, JsonMappingException, IOException {
        synchronized (LOCK) {
            LabInfo actualLabInfo = labService.getLab(lab);
            List<ValidationDbTestResult> vDbResults = vTestResultDAO.getValidationTestResults(blueprintName, version,
                    actualLabInfo, allLayers, optional, outcome);
            if (vDbResults == null || vDbResults.size() < 1) {
                return null;
            }
            List<ValidationNexusTestResult> vNexusResults = new ArrayList<ValidationNexusTestResult>();
            for (ValidationDbTestResult vDbResult : vDbResults) {
                if (layers != null && layers.size() > 0) {
                    List<BlueprintLayer> storedLayers = new ArrayList<BlueprintLayer>();
                    List<WRobotDbTestResult> wDbResults = wRobotDAO.getWRobotTestResult(vDbResult);
                    if (wDbResults == null || wDbResults.size() < 1) {
                        continue;
                    }
                    for (WRobotDbTestResult wRobot : wDbResults) {
                        storedLayers.add(BlueprintLayer.valueOf(wRobot.getLayer()));
                    }
                    if (!new HashSet<>(storedLayers).equals(new HashSet<>(layers))) {
                        continue;
                    }
                }
                vNexusResults.add(convertValidationDbToNexus(vDbResult));
            }
            return vNexusResults;
        }
    }

    public ValidationNexusTestResult readResultFromDb(@Nonnull Lab lab, @Nonnull String timestamp)
            throws JsonParseException, JsonMappingException, IOException {
        synchronized (LOCK) {
            LabInfo actualLabInfo = labService.getLab(lab);
            ValidationDbTestResult vDbResult = vTestResultDAO.getValidationTestResult(actualLabInfo, timestamp);
            if (vDbResult == null) {
                return null;
            }
            return convertValidationDbToNexus(vDbResult);
        }
    }

    public ValidationNexusTestResult readResultFromDb(@Nonnull String submissionId)
            throws JsonParseException, JsonMappingException, IOException {
        synchronized (LOCK) {
            ValidationDbTestResult vDbResult = vTestResultDAO
                    .getValidationTestResult(subService.getSubmission(submissionId));
            if (vDbResult == null) {
                return null;
            }
            return convertValidationDbToNexus(vDbResult);
        }
    }

    public void deleteUnreferencedEntries(List<ValidationNexusTestResult> vNexusResults) {
        synchronized (LOCK) {
            if (vNexusResults == null || vNexusResults.size() < 1) {
                return;
            }
            LabInfo labInfo = null;
            for (LabSilo silo : siloService.getSilos()) {
                if (silo.getSilo().equals(vNexusResults.get(0).getSilo())) {
                    labInfo = silo.getLab();
                }
            }
            if (labInfo == null) {
                return;
            }
            List<ValidationDbTestResult> vDbResults = vTestResultDAO.getValidationTestResults(
                    vNexusResults.get(0).getBlueprintName(), vNexusResults.get(0).getVersion(), labInfo, null, null,
                    null);
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
                for (ValidationNexusTestResult vNexusResult : vNexusResults) {
                    LabInfo nexusLabInfo = null;
                    for (LabSilo silo : siloService.getSilos()) {
                        if (silo.getSilo().equals(vNexusResult.getSilo())) {
                            nexusLabInfo = silo.getLab();
                        }
                    }
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

    public List<ValidationDbTestResult> getValidationTestResults(String blueprintName, String version, LabInfo labInfo,
            Boolean allLayers, Boolean optional, Boolean outcome) {
        synchronized (LOCK) {
            return vTestResultDAO.getValidationTestResults(blueprintName, version, labInfo, allLayers, optional,
                    outcome);
        }
    }

    public ValidationDbTestResult getValidationTestResult(LabInfo labInfo, String timestamp) {
        synchronized (LOCK) {
            return vTestResultDAO.getValidationTestResult(labInfo, timestamp);
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

    private ValidationNexusTestResult convertValidationDbToNexus(ValidationDbTestResult vDbResult)
            throws JsonParseException, JsonMappingException, IOException {
        ValidationNexusTestResult vNexusResult = new ValidationNexusTestResult();
        vNexusResult.setResultId(vDbResult.getResultId());
        vNexusResult.setBlueprintName(vDbResult.getBlueprintName());
        vNexusResult.setVersion(vDbResult.getVersion());
        vNexusResult.setAllLayers(vDbResult.getAllLayers());
        vNexusResult.setDateOfStorage(vDbResult.getDateStorage());
        vNexusResult.setOptional(vDbResult.getOptional());
        vNexusResult.setResult(vDbResult.getResult());
        String siloText = null;
        for (LabSilo silo : siloService.getSilos()) {
            if (silo.getLab().getLab().equals(vDbResult.getLab().getLab())) {
                siloText = silo.getSilo();
            }
        }
        if (siloText == null) {
            throw new IllegalArgumentException("Lab does not exist: " + vDbResult.getLab().toString());
        }
        vNexusResult.setSilo(siloText);
        vNexusResult.setTimestamp(vDbResult.getTimestamp());
        if (vDbResult.getSubmission() != null) {
            vNexusResult.setSubmissionId(String.valueOf(vDbResult.getSubmission().getSubmissionId()));
        }
        List<WRobotNexusTestResult> wNexusResults = new ArrayList<WRobotNexusTestResult>();
        List<WRobotDbTestResult> wDbResults = wRobotDAO.getWRobotTestResult(vDbResult);
        if (wDbResults != null && wDbResults.size() > 0) {
            for (WRobotDbTestResult wRobot : wDbResults) {
                WRobotNexusTestResult wNexusResult = new WRobotNexusTestResult();
                wNexusResult.setBlueprintLayer(BlueprintLayer.valueOf(wRobot.getLayer()));
                if (wRobot.getRobotTestResults() != null) {
                    ObjectMapper mapper = new ObjectMapper();
                    wNexusResult.setRobotTestResults(
                            mapper.readValue(wRobot.getRobotTestResults(), new TypeReference<List<RobotTestResult>>() {
                            }));
                }
                wNexusResults.add(wNexusResult);
            }
            vNexusResult.setwRobotNexusTestResults(wNexusResults);
        }
        return vNexusResult;
    }

    private ValidationDbTestResult convertValidationNexusToDb(ValidationNexusTestResult vNexusResult)
            throws JsonParseException, JsonMappingException, IOException {
        LabInfo labInfo = null;
        for (LabSilo silo : siloService.getSilos()) {
            if (silo.getSilo().equals(vNexusResult.getSilo())) {
                labInfo = silo.getLab();
            }
        }
        if (labInfo == null) {
            return null;
        }
        ValidationDbTestResult vDbResult = new ValidationDbTestResult();
        vDbResult.setBlueprintName(vNexusResult.getBlueprintName());
        vDbResult.setVersion(vNexusResult.getVersion());
        vDbResult.setLab(labInfo);
        vDbResult.setOptional(vNexusResult.getOptional());
        vDbResult.setAllLayers(vNexusResult.getAllLayers());
        vDbResult.setDateStorage(vNexusResult.getDateOfStorage());
        vDbResult.setResult(vNexusResult.getResult());
        vDbResult.setTimestamp(vNexusResult.getTimestamp());
        return vDbResult;
    }

    private List<WRobotDbTestResult> convertWRobotNexusResultsToDb(List<WRobotNexusTestResult> wRobotNexusResults) {
        if (wRobotNexusResults == null || wRobotNexusResults.size() < 1) {
            return null;
        }
        List<WRobotDbTestResult> wDbResults = new ArrayList<WRobotDbTestResult>();
        for (WRobotNexusTestResult wRobotNexusResult : wRobotNexusResults) {
            WRobotDbTestResult wDbResult = new WRobotDbTestResult();
            if (wRobotNexusResult.getBlueprintLayer() != null) {
                wDbResult.setLayer(wRobotNexusResult.getBlueprintLayer().toString());
            }
            ObjectMapper mapper = new ObjectMapper();
            if (wRobotNexusResult.getRobotTestResults() != null && wRobotNexusResult.getRobotTestResults().size() > 0) {
                try {
                    wDbResult.setRobotTestResults(mapper.writeValueAsString(wRobotNexusResult.getRobotTestResults()));
                } catch (JsonProcessingException e) {
                    LOGGER.error(EELFLoggerDelegate.errorLogger,
                            "Error while converting POJO to string. " + UserUtils.getStackTrace(e));
                    continue;
                }
            }
            wDbResults.add(wDbResult);
        }
        return wDbResults;
    }

    private boolean checkValidityOfValidationNexusTestResult(ValidationNexusTestResult vNexusResult) {
        LabInfo labInfo = null;
        for (LabSilo silo : siloService.getSilos()) {
            if (silo.getSilo().equals(vNexusResult.getSilo())) {
                labInfo = silo.getLab();
            }
        }
        if (labInfo == null) {
            LOGGER.error(EELFLoggerDelegate.errorLogger, "No lab Info found for silo. " + vNexusResult.getSilo());
            return false;
        }
        ValidationDbTestResult vDbResult = vTestResultDAO.getValidationTestResult(labInfo, vNexusResult.getTimestamp());
        if (vDbResult != null) {
            // Be elastic for allLayers and optional
            if (!vDbResult.getBlueprintName().equals(vNexusResult.getBlueprintName())
                    || !vDbResult.getVersion().equals(vNexusResult.getVersion()) || !vDbResult.getLab().equals(labInfo)
                    || !vDbResult.getTimestamp().equals(vNexusResult.getTimestamp())) {
                LOGGER.error(EELFLoggerDelegate.errorLogger,
                        "Nexus has different data for blueprint : " + vDbResult.getBlueprintName() + ", version: "
                                + vDbResult.getVersion() + " and lab: " + vDbResult.getLab().getLab().name());
                return false;
            }
        }
        List<org.akraino.validation.ui.entity.WRobotDbTestResult> wRobotDbResults = wRobotDAO
                .getWRobotTestResult(vDbResult);
        if (wRobotDbResults != null) {
            if (vDbResult.getSubmission() != null) {
                for (WRobotNexusTestResult wNexusResult : vNexusResult.getwRobotNexusTestResults()) {
                    WRobotDbTestResult wRobotDbResult = wRobotDAO
                            .getWRobotTestResult(wNexusResult.getBlueprintLayer().name(), vDbResult);
                    if (wRobotDbResult == null) {
                        LOGGER.error(EELFLoggerDelegate.errorLogger,
                                "Nexus has different layer results for submission id: "
                                        + vDbResult.getSubmission().getSubmissionId());
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean checkValidityOfJenkinsNotification(JnksJobNotify jnksJobNotify) {
        ValidationDbTestResult vDbSubmission = vTestResultDAO
                .getValidationTestResult(subService.getSubmission(String.valueOf(jnksJobNotify.getSubmissionId())));
        if (vDbSubmission == null) {
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
        // Be elastic for allLayers and optional
        if (!vDbSubmission.getBlueprintName().equals(vDbTimestamp.getBlueprintName())
                || !vDbSubmission.getVersion().equals(vDbTimestamp.getVersion())
                || !vDbSubmission.getLab().equals(vDbTimestamp.getLab())) {
            LOGGER.error(EELFLoggerDelegate.errorLogger, "No consistency exists in database records.");
            return false;
        }
        return true;
    }

}
