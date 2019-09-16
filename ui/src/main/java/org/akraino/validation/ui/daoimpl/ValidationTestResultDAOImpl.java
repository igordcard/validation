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
package org.akraino.validation.ui.daoimpl;

import java.util.List;

import javax.annotation.Nonnull;

import org.akraino.validation.ui.dao.ValidationTestResultDAO;
import org.akraino.validation.ui.entity.BlueprintInstance;
import org.akraino.validation.ui.entity.LabInfo;
import org.akraino.validation.ui.entity.Submission;
import org.akraino.validation.ui.entity.ValidationDbTestResult;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class ValidationTestResultDAOImpl implements ValidationTestResultDAO {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(ValidationTestResultDAOImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<ValidationDbTestResult> getValidationTestResults() {
        Criteria criteria = getSession().createCriteria(ValidationDbTestResult.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @Override
    public ValidationDbTestResult getValidationTestResult(@Nonnull Integer resultId) {
        Criteria criteria = getSession().createCriteria(ValidationDbTestResult.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("id", resultId));
        return (criteria.list() == null || criteria.list().size() < 1) ? null
                : (ValidationDbTestResult) criteria.list().get(0);
    }

    @Override
    public List<ValidationDbTestResult> getValidationTestResults(BlueprintInstance bluInst, LabInfo labInfo,
            Boolean allLayers, Boolean optional, Boolean outcome) {
        Criteria criteria = getSession().createCriteria(ValidationDbTestResult.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        if (bluInst != null) {
            criteria.add(Restrictions.eq("blueprintInstance", bluInst));
        }
        if (labInfo != null) {
            criteria.add(Restrictions.eq("lab", labInfo));
        }
        if (allLayers != null) {
            criteria.add(Restrictions.eq("allLayers", allLayers));
        }
        if (optional != null) {
            criteria.add(Restrictions.eq("optional", optional));
        }
        if (outcome != null) {
            criteria.add(Restrictions.eq("result", outcome));
        }
        return criteria.list() == null || criteria.list().size() == 0 ? null
                : (List<ValidationDbTestResult>) criteria.list();
    }

    @Override
    public ValidationDbTestResult getValidationTestResult(LabInfo labInfo, String timestamp) {
        Criteria criteria = getSession().createCriteria(ValidationDbTestResult.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        if (labInfo != null) {
            criteria.add(Restrictions.eq("lab", labInfo));
        }
        if (timestamp != null) {
            criteria.add(Restrictions.eq("timestamp", timestamp));
        }
        return criteria.list() == null || criteria.list().size() == 0 ? null
                : (ValidationDbTestResult) criteria.list().get(0);
    }

    @Override
    public ValidationDbTestResult getValidationTestResult(@Nonnull Submission submission) {
        Criteria criteria = getSession().createCriteria(ValidationDbTestResult.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("submission", submission));
        return criteria.list() == null || criteria.list().size() == 0 ? null
                : (ValidationDbTestResult) criteria.list().get(0);
    }

    @Override
    public void saveOrUpdate(@Nonnull ValidationDbTestResult vResult) {
        getSession().saveOrUpdate(vResult);
        getSession().flush();
    }

    @Override
    public void merge(@Nonnull ValidationDbTestResult vResult) {
        getSession().merge(vResult);
        getSession().flush();
    }

    @Override
    public void deleteValidationTestResult(@Nonnull ValidationDbTestResult vResult) {
        getSession().delete(vResult);
        getSession().flush();
    }

    @Override
    public void deleteValidationTestResult(@Nonnull LabInfo labInfo, @Nonnull String timestamp) {
        Criteria criteria = getSession().createCriteria(ValidationDbTestResult.class);
        criteria.add(Restrictions.eq("lab", labInfo));
        criteria.add(Restrictions.eq("timestamp", timestamp));
        if (criteria.list() == null || criteria.list().size() == 0) {
            return;
        }
        getSession().delete(criteria.list().get(0));
        getSession().flush();
    }

    @Override
    public void deleteAll() {
        if (getSession().createQuery("delete from ValidationTestResult").executeUpdate() > 0) {
            LOGGER.info(EELFLoggerDelegate.applicationLogger, "All validation test results are cleaned up");
            getSession().flush();
        }
    }

}
