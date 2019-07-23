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

import org.akraino.validation.ui.dao.SubmissionDAO;
import org.akraino.validation.ui.entity.Submission;
import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SubmissionDAOImpl implements SubmissionDAO {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(SubmissionDAOImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<Submission> getSubmissions() {
        Criteria criteria = getSession().createCriteria(Submission.class);
        return criteria.list();
    }

    @Override
    public Submission getSubmission(@Nonnull Integer submissionId) {
        Criteria criteria = getSession().createCriteria(Submission.class);
        criteria.add(Restrictions.eq("id", submissionId));
        return criteria.list() == null || criteria.list().size() < 1 ? null : (Submission) criteria.list().get(0);
    }

    @Override
    public void saveOrUpdate(@Nonnull Submission submission) {
        getSession().saveOrUpdate(submission);
        getSession().flush();
    }

    @Override
    public void merge(@Nonnull Submission submission) {
        getSession().merge(submission);
        getSession().flush();
    }

    @Override
    public void deleteSubmission(@Nonnull Submission submission) {
        getSession().delete(submission);
        getSession().flush();
    }

    @Override
    public void deleteSubmission(@Nonnull Integer submissionId) {
        getSession().delete(this.getSubmission(submissionId));
        getSession().flush();
    }

    @Override
    public void deleteAll() {
        if (getSession().createQuery("delete from Submission").executeUpdate() > 0) {
            LOGGER.info(EELFLoggerDelegate.applicationLogger, "All submission entries are cleaned up");
            getSession().flush();
        }
    }

}
