/*
 * Copyright (c) 2019 AT&T Intellectual Property. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.akraino.validation.ui.daoimpl;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.akraino.validation.ui.dao.BlueprintInstanceDAO;
import org.akraino.validation.ui.entity.BlueprintInstance;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BlueprintInstanceDAOImpl implements BlueprintInstanceDAO {

    private static final Logger LOGGER = Logger.getLogger(BlueprintInstanceDAOImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<BlueprintInstance> getBlueprintInstances() {

        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<BlueprintInstance> criteria = builder.createQuery(BlueprintInstance.class);

        Root<BlueprintInstance> root = criteria.from(BlueprintInstance.class);
        criteria.select(root);

        Query<BlueprintInstance> query = getSession().createQuery(criteria);

        return query.getResultList();

    }

    @Override
    public BlueprintInstance getBlueprintInstance(Integer instId) {

        EntityManager entityManager = getSession().getEntityManagerFactory().createEntityManager();

        return entityManager.find(BlueprintInstance.class, instId);
    }

    @Override
    public void saveOrUpdate(BlueprintInstance blueprintInstance) {
        getSession().saveOrUpdate(blueprintInstance);

    }

    @Override
    public void merge(BlueprintInstance blueprintInstance) {
        getSession().merge(blueprintInstance);

    }

    @Override
    public void deleteBlueprintInstance(BlueprintInstance blueprintInstance) {
        getSession().delete(blueprintInstance);

    }

    @Override
    public void deleteAll() {

        Query<?> query = getSession().createQuery("delete from BlueprintInstance");

        int result = query.executeUpdate();

        if (result > 0) {
            LOGGER.info("All blueprint instance entries are cleaned up");
        }
    }

}
