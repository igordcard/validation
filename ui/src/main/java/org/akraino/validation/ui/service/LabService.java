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
package org.akraino.validation.ui.service;

import java.util.List;

import org.akraino.validation.ui.dao.LabDAO;
import org.akraino.validation.ui.entity.LabInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LabService {

    @Autowired
    private LabDAO labDAO;

    public void saveLab(LabInfo lab) {
        labDAO.saveOrUpdate(lab);
    }

    public LabInfo getLab(String lab) {
        return labDAO.getLab(lab);
    }

    public LabInfo getLabBasedOnSilo(String silo) {
        return labDAO.getLabBasedOnSilo(silo);
    }

    public List<LabInfo> getLabs() {
        return labDAO.getLabs();
    }

    public void deleteAll() {
        labDAO.deleteAll();
    }

}
