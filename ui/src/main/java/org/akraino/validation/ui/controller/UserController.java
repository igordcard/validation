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

package org.akraino.validation.ui.controller;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.onap.portalsdk.core.controller.RestrictedBaseController;
import org.onap.portalsdk.core.domain.Role;
import org.onap.portalsdk.core.domain.User;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.onap.portalsdk.core.onboarding.exception.CipherUtilException;
import org.onap.portalsdk.core.onboarding.util.CipherUtil;
import org.onap.portalsdk.core.service.RoleService;
import org.onap.portalsdk.core.service.UserProfileService;
import org.onap.portalsdk.core.web.support.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/api/v1/user")
public class UserController extends RestrictedBaseController {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(UserController.class);

    @Autowired
    UserProfileService userService;

    @Autowired
    RoleService roleService;

    public UserController() {
        super();
    }

    @RequestMapping(value = { "/" }, method = RequestMethod.POST)
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {
            return new ResponseEntity<>(createUser(user.getFirstName(), user.getLoginId(), user.getLoginPwd()),
                    HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.error(EELFLoggerDelegate.errorLogger, "Creation of user failed. " + UserUtils.getStackTrace(e));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    private User createUser(String firstName, String loginId, String loginPwd) throws IOException, CipherUtilException {
        User newUser = new User();
        newUser.setActive(true);
        newUser.setCreated(new Date());
        newUser.setFirstName(firstName);
        newUser.setInternal(false);
        newUser.setLoginId(loginId);
        newUser.setOrgUserId(loginId);
        newUser.setLoginPwd(CipherUtil.encryptPKC(loginPwd, System.getenv("ENCRYPTION_KEY")));
        newUser.setModified(new Date());
        newUser.setModifiedId(1L);
        newUser.setOnline(true);
        newUser.setTimeZoneId(10L);
        userService.saveUser(newUser);
        Role actualRole = null;
        List<Role> roles = roleService.getActiveRoles(null);
        for (Role role : roles) {
            if (role.getName().equals("Blueprint Validation UI user")) {
                actualRole = role;
                break;
            }
        }
        if (actualRole == null) {
            throw new RuntimeException("Blueprint Validation UI user role does not exist");
        }
        SortedSet<Role> actualRoles = new TreeSet<Role>();
        actualRoles.add(actualRole);
        newUser.setRoles(actualRoles);
        userService.saveUser(newUser);
        return newUser;
    }

}
