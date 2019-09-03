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

import org.akraino.validation.ui.dao.BlueprintDAO;
import org.akraino.validation.ui.entity.Blueprint;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BlueprintDAOImpl implements BlueprintDAO {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(BlueprintDAOImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<Blueprint> getBlueprints() {
        Criteria criteria = getSession().createCriteria(Blueprint.class);
        return criteria.list();
    }

    @Override
    public Blueprint getBlueprint(@Nonnull Integer blueprintId) {
        Criteria criteria = getSession().createCriteria(Blueprint.class);
        criteria.add(Restrictions.eq("id", String.valueOf(blueprintId)));
        return criteria.list() == null ? null : (Blueprint) criteria.list().get(0);
    }

    @Override
    public void saveOrUpdate(@Nonnull Blueprint blueprint) {
        getSession().saveOrUpdate(blueprint);
        getSession().flush();
    }

    @Override
    public void merge(@Nonnull Blueprint blueprint) {
        getSession().merge(blueprint);
        getSession().flush();
    }

    @Override
    public void deleteBlueprint(@Nonnull Blueprint blueprint) {
        getSession().delete(blueprint);
        getSession().flush();
    }

    @Override
    public void deleteAll() {
        if (getSession().createQuery("delete from Blueprint").executeUpdate() > 0) {
            getSession().flush();
            LOGGER.info(EELFLoggerDelegate.applicationLogger, "All blueprint entries are cleaned up");
        }
    }

}
