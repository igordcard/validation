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

import org.akraino.validation.ui.dao.TimeslotDAO;
import org.akraino.validation.ui.entity.Timeslot;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TimeslotDAOImpl implements TimeslotDAO {

    private static final Logger LOGGER = Logger.getLogger(TimeslotDAOImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<Timeslot> getTimeslots() {

        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<Timeslot> criteria = builder.createQuery(Timeslot.class);

        Root<Timeslot> root = criteria.from(Timeslot.class);
        criteria.select(root);

        Query<Timeslot> query = getSession().createQuery(criteria);

        return query.getResultList();

    }

    @Override
    public Timeslot getTimeslot(Integer timeslotId) {

        EntityManager entityManager = getSession().getEntityManagerFactory().createEntityManager();

        return entityManager.find(Timeslot.class, timeslotId);
    }

    @Override
    public void saveOrUpdate(Timeslot timeslot) {
        getSession().saveOrUpdate(timeslot);

    }

    @Override
    public void merge(Timeslot timeslot) {
        getSession().merge(timeslot);

    }

    @Override
    public void deleteTimeslot(Timeslot timeslot) {
        getSession().delete(timeslot);

    }

    @Override
    public void deleteAll() {

        Query<?> query = getSession().createQuery("delete from Timeslot");

        int result = query.executeUpdate();

        if (result > 0) {
            LOGGER.info("All timeslot entries are cleaned up");
        }
    }

}
