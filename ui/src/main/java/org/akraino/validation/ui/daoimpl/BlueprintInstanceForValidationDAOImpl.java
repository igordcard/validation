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

import org.akraino.validation.ui.dao.BlueprintInstanceForValidationDAO;
import org.akraino.validation.ui.entity.BlueprintInstanceForValidation;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BlueprintInstanceForValidationDAOImpl implements BlueprintInstanceForValidationDAO {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate
            .getLogger(BlueprintInstanceForValidationDAOImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<BlueprintInstanceForValidation> getBlueprintInstancesForValidation() {
        Criteria criteria = getSession().createCriteria(BlueprintInstanceForValidation.class);
        return criteria.list();
    }

    @Override
    public BlueprintInstanceForValidation getBlueprintInstanceForValidation(@Nonnull Integer instId) {
        Criteria criteria = getSession().createCriteria(BlueprintInstanceForValidation.class);
        criteria.add(Restrictions.eq("id", String.valueOf(instId)));
        return criteria.list() == null ? null : (BlueprintInstanceForValidation) criteria.list().get(0);
    }

    @Override
    public void saveOrUpdate(@Nonnull BlueprintInstanceForValidation blueprintInst) {
        getSession().saveOrUpdate(blueprintInst);
        getSession().flush();
    }

    @Override
    public void merge(@Nonnull BlueprintInstanceForValidation blueprintInst) {
        getSession().merge(blueprintInst);
        getSession().flush();
    }

    @Override
    public void deleteBlueprintInstanceForValidation(@Nonnull BlueprintInstanceForValidation blueprintInst) {
        getSession().delete(blueprintInst);
        getSession().flush();
    }

    @Override
    public void deleteAll() {
        if (getSession().createQuery("delete from BlueprintInstanceForValidation").executeUpdate() > 0) {
            LOGGER.info(EELFLoggerDelegate.applicationLogger,
                    "All blueprint instances for validation entries are cleaned up");
            getSession().flush();
        }
    }

}
