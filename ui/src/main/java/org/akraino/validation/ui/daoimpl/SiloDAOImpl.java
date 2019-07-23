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

import org.akraino.validation.ui.dao.SiloDAO;
import org.akraino.validation.ui.entity.LabSilo;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SiloDAOImpl implements SiloDAO {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(SiloDAOImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<LabSilo> getSilos() {
        Criteria criteria = getSession().createCriteria(LabSilo.class);
        return criteria.list();
    }

    @Override
    public LabSilo getSilo(@Nonnull Integer siloId) {
        Criteria criteria = getSession().createCriteria(LabSilo.class);
        criteria.add(Restrictions.eq("id", String.valueOf(siloId)));
        return criteria.list() == null ? null : (LabSilo) criteria.list().get(0);
    }

    @Override
    public void saveOrUpdate(@Nonnull LabSilo silo) {
        getSession().saveOrUpdate(silo);
        getSession().flush();
    }

    @Override
    public void merge(@Nonnull LabSilo silo) {
        getSession().merge(silo);
        getSession().flush();
    }

    @Override
    public void deleteSilo(@Nonnull LabSilo silo) {
        getSession().delete(silo);
        getSession().flush();
    }

    @Override
    public void deleteAll() {
        if (getSession().createQuery("delete from Silo").executeUpdate() > 0) {
            LOGGER.info(EELFLoggerDelegate.applicationLogger, "All silo entries are cleaned up");
            getSession().flush();
        }
    }

}
