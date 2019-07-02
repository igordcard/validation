/*
 * ============LICENSE_START==========================================
 * ONAP Portal SDK
 * ===================================================================
 * Copyright Â© 2017 AT&T Intellectual Property. All rights reserved.
 * ===================================================================
 *
 * Unless otherwise specified, all software contained herein is licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use
 * this software except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * Unless otherwise specified, all documentation contained herein is
 * licensed under the Creative Commons License, Attribution 4.0 Intl. (the
 * "License"); you may not use this documentation except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * https://creativecommons.org/licenses/by/4.0/
 *
 * Unless required by applicable law or agreed to in writing,
 * documentation distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * ============LICENSE_END============================================
 *
 *
 */

package org.akraino.validation.ui.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.onap.portalsdk.core.auth.LoginStrategy;
import org.onap.portalsdk.core.controller.UnRestrictedBaseController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
public class OnapLoginController extends UnRestrictedBaseController {

    @Autowired
    private LoginStrategy loginStrategy;

    private String viewName;

    @Override
    public String getViewName() {
        return viewName;
    }

    @Override
    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    @RequestMapping(value = {"/do_login_external"}, method = RequestMethod.POST)
    public ModelAndView doexternalLogin(HttpServletRequest request, HttpServletResponse response) throws Exception {
        return loginStrategy.doLogin(request, response);
    }

}
