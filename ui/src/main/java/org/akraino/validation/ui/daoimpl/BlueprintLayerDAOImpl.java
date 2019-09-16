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

import org.akraino.validation.ui.dao.BlueprintLayerDAO;
import org.akraino.validation.ui.entity.BlueprintLayer;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BlueprintLayerDAOImpl implements BlueprintLayerDAO {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(BlueprintLayerDAOImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<BlueprintLayer> getBlueprintLayers() {
        Criteria criteria = getSession().createCriteria(BlueprintLayer.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        return criteria.list();
    }

    @Override
    public BlueprintLayer getBlueprintLayer(Integer bluLayerId) {
        Criteria criteria = getSession().createCriteria(BlueprintLayer.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("id", bluLayerId));
        return criteria.list() == null || criteria.list().size() < 1 ? null : (BlueprintLayer) criteria.list().get(0);
    }

    @Override
    public BlueprintLayer getBlueprintLayer(String layer) {
        Criteria criteria = getSession().createCriteria(BlueprintLayer.class);
        criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        criteria.add(Restrictions.eq("layer", layer));
        return criteria.list() == null || criteria.list().size() < 1 ? null : (BlueprintLayer) criteria.list().get(0);
    }

    @Override
    public void saveOrUpdate(BlueprintLayer blueprintLayer) {
        getSession().saveOrUpdate(blueprintLayer);
        getSession().flush();
    }

    @Override
    public void merge(BlueprintLayer blueprintLayer) {
        getSession().merge(blueprintLayer);
        getSession().flush();
    }

    @Override
    public void deleteBlueprintLayer(BlueprintLayer blueprintLayer) {
        getSession().delete(blueprintLayer);
        getSession().flush();
    }

    @Override
    public void deleteAll() {
        if (getSession().createQuery("delete from BlueprintLayer").executeUpdate() > 0) {
            getSession().flush();
            LOGGER.info(EELFLoggerDelegate.applicationLogger, "All blueprint layers are cleaned up");
        }
    }

}
