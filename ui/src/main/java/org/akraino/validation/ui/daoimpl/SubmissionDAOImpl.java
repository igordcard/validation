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

import org.akraino.validation.ui.dao.SubmissionDAO;
import org.akraino.validation.ui.entity.Submission;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

@Repository
public class SubmissionDAOImpl implements SubmissionDAO {

    private static final Logger LOGGER = Logger.getLogger(SubmissionDAOImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    protected Session getSession() {
        return sessionFactory.getCurrentSession();
    }

    @Override
    public List<Submission> getSubmissions() {

        CriteriaBuilder builder = getSession().getCriteriaBuilder();
        CriteriaQuery<Submission> criteria = builder.createQuery(Submission.class);

        Root<Submission> root = criteria.from(Submission.class);
        criteria.select(root);

        Query<Submission> query = getSession().createQuery(criteria);

        return query.getResultList();

    }

    @Override
    public Submission getSubmission(Integer submissionId) {

        EntityManager entityManager = getSession().getEntityManagerFactory().createEntityManager();

        return entityManager.find(Submission.class, submissionId);
    }

    @Override
    public void saveOrUpdate(Submission submission) {
        getSession().saveOrUpdate(submission);

    }

    @Override
    public void merge(Submission submission) {
        getSession().merge(submission);

    }

    @Override
    public void deleteSubmission(Submission submission) {
        getSession().delete(submission);

    }

    @Override
    public void deleteSubmission(Integer submissionId) {
        getSession().delete(this.getSubmission(submissionId));
    }

    @Override
    public void deleteAll() {

        Query<?> query = getSession().createQuery("delete from Submission");

        int result = query.executeUpdate();

        if (result > 0) {
            LOGGER.info("All submission entries are cleaned up");
        }
    }

}
