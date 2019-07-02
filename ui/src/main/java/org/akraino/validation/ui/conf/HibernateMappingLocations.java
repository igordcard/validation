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
package org.akraino.validation.ui.conf;


import org.onap.portalsdk.core.conf.HibernateMappingLocatable;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
@Profile("src")
public class HibernateMappingLocations implements HibernateMappingLocatable {

    @Override
    public Resource[] getMappingLocations() {
        return new Resource[] {new ClassPathResource("../fusion/orm/Fusion.hbm.xml"),
                new ClassPathResource("../fusion/orm/Workflow.hbm.xml"),
                new ClassPathResource("../fusion/orm/RNoteBookIntegration.hbm.xml"),
                new ClassPathResource("../fusion/orm/Analytics.hbm.xml")};
    }

    @Override
    public String[] getPackagesToScan() {
        return new String[] {"org.onap", "org.akraino"};
    }

}
