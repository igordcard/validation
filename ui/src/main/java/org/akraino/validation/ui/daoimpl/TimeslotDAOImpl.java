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

import org.akraino.validation.ui.dao.TimeslotDAO;
import org.akraino.validation.ui.entity.Timeslot;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class TimeslotDAOImpl implements TimeslotDAO {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(TimeslotDAOImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<Timeslot> getTimeslots() {
        Criteria criteria = getSession().createCriteria(Timeslot.class);
        return criteria.list();
    }

    @Override
    public Timeslot getTimeslot(Integer timeslotId) {
        Criteria criteria = getSession().createCriteria(Timeslot.class);
        criteria.add(Restrictions.eq("id", String.valueOf(timeslotId)));
        return criteria.list() == null ? null : (Timeslot) criteria.list().get(0);
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
        if (getSession().createQuery("delete from Timeslot").executeUpdate() > 0) {
            LOGGER.info(EELFLoggerDelegate.applicationLogger, "All timeslot entries are cleaned up");
        }
    }

}
