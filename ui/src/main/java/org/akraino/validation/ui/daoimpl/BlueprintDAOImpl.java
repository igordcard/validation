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

import org.akraino.validation.ui.dao.BlueprintDAO;
import org.akraino.validation.ui.entity.Blueprint;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class BlueprintDAOImpl implements BlueprintDAO {

    private static final Logger LOGGER = Logger.getLogger(BlueprintDAOImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<Blueprint> getBlueprints() {

        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<Blueprint> criteria = builder.createQuery(Blueprint.class);

        Root<Blueprint> root = criteria.from(Blueprint.class);
        criteria.select(root);

        Query<Blueprint> query = getSession().createQuery(criteria);

        return query.getResultList();

    }

    @Override
    public Blueprint getBlueprint(Integer blueprintId) {

        EntityManager entityManager = getSession().getEntityManagerFactory().createEntityManager();

        return entityManager.find(Blueprint.class, blueprintId);
    }

    @Override
    public void saveOrUpdate(Blueprint blueprint) {
        getSession().saveOrUpdate(blueprint);

    }

    @Override
    public void merge(Blueprint blueprint) {
        getSession().merge(blueprint);

    }

    @Override
    public void deleteBlueprint(Blueprint blueprint) {
        getSession().delete(blueprint);

    }

    @Override
    public void deleteAll() {

        Query<?> query = getSession().createQuery("delete from Blueprint");

        int result = query.executeUpdate();

        if (result > 0) {
            LOGGER.info("All blueprint entries are cleaned up");
        }
    }

}
