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

import org.akraino.validation.ui.dao.LabDAO;
import org.akraino.validation.ui.entity.LabInfo;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class LabDAOImpl implements LabDAO {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(LabDAOImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<LabInfo> getLabs() {
        Criteria criteria = getSession().createCriteria(LabInfo.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @Override
    public LabInfo getLab(@Nonnull Integer labId) {
        Criteria criteria = getSession().createCriteria(LabInfo.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("id", labId));
        return criteria.list() == null || criteria.list().size() < 1 ? null : (LabInfo) criteria.list().get(0);
    }

    @Override
    public LabInfo getLab(@Nonnull String lab) {
        Criteria criteria = getSession().createCriteria(LabInfo.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("lab", lab));
        return criteria.list() == null || criteria.list().size() < 1 ? null : (LabInfo) criteria.list().get(0);
    }

    @Override
    public LabInfo getLabBasedOnSilo(String silo) {
        Criteria criteria = getSession().createCriteria(LabInfo.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("silo", silo));
        return criteria.list() == null || criteria.list().size() < 1 ? null : (LabInfo) criteria.list().get(0);
    }

    @Override
    public void saveOrUpdate(@Nonnull LabInfo lab) {
        getSession().saveOrUpdate(lab);
        getSession().flush();
    }

    @Override
    public void merge(@Nonnull LabInfo lab) {
        getSession().merge(lab);
        getSession().flush();
    }

    @Override
    public void deleteLab(@Nonnull LabInfo lab) {
        getSession().delete(lab);
        getSession().flush();
    }

    @Override
    public void deleteAll() {
        if (getSession().createQuery("delete from Lab").executeUpdate() > 0) {
            LOGGER.info(EELFLoggerDelegate.applicationLogger, "All lab entries are cleaned up");
            getSession().flush();
        }
    }

}
