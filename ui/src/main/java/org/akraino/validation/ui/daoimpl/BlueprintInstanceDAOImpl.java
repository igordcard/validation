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

import org.akraino.validation.ui.dao.BlueprintInstanceDAO;
import org.akraino.validation.ui.entity.Blueprint;
import org.akraino.validation.ui.entity.BlueprintInstance;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BlueprintInstanceDAOImpl implements BlueprintInstanceDAO {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(BlueprintInstanceDAOImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<BlueprintInstance> getBlueprintInstances() {
        Criteria criteria = getSession().createCriteria(BlueprintInstance.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @Override
    public BlueprintInstance getBlueprintInstance(@Nonnull Integer instId) {
        Criteria criteria = getSession().createCriteria(BlueprintInstance.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("id", instId));
        return criteria.list() == null || criteria.list().size() < 1 ? null
                : (BlueprintInstance) criteria.list().get(0);
    }

    @Override
    public BlueprintInstance getBlueprintInstance(Blueprint blueprint, String version) {
        Criteria criteria = getSession().createCriteria(BlueprintInstance.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        if (blueprint != null) {
            criteria.add(Restrictions.eq("blueprint", blueprint));
        }
        if (version != null) {
            criteria.add(Restrictions.eq("version", version));
        }
        return criteria.list() == null || criteria.list().size() < 1 ? null
                : (BlueprintInstance) criteria.list().get(0);
    }

    @Override
    public void saveOrUpdate(@Nonnull BlueprintInstance blueprintInst) {
        getSession().saveOrUpdate(blueprintInst);
        getSession().flush();
    }

    @Override
    public void merge(@Nonnull BlueprintInstance blueprintInst) {
        getSession().merge(blueprintInst);
        getSession().flush();
    }

    @Override
    public void deleteBlueprintInstance(@Nonnull BlueprintInstance blueprintInst) {
        getSession().delete(blueprintInst);
        getSession().flush();
    }

    @Override
    public void deleteAll() {
        if (getSession().createQuery("delete from BlueprintInstance").executeUpdate() > 0) {
            LOGGER.info(EELFLoggerDelegate.applicationLogger, "All blueprint instances entries are cleaned up");
            getSession().flush();
        }
    }

}
