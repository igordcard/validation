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
package org.akraino.validation.ui.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

public class SessionManagerFilter implements HandlerInterceptor {

    private static final Logger LOGGER = Logger.getLogger(SessionManagerFilter.class);

    @Override
    public void afterCompletion(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, Exception arg3)
            throws Exception {

        LOGGER.info("user authenticated");

    }

    @Override
    public void postHandle(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, ModelAndView arg3)
            throws Exception {

        LOGGER.info("user authenticated");

    }

    @Override
    public boolean preHandle(HttpServletRequest req, HttpServletResponse res, Object data) throws Exception {

        try {
            return true;
            /*
             * if (StringUtil.notEmpty(req.getHeader("tokenId"))) {
             *
             * String clientToken = req.getHeader("tokenId");
             *
             * AccessService service = new AccessService();
             *
             * UserSession user = service.getUserSession(LoginUtil.decode(LoginUtil.getUserName(clientToken)));
             *
             * if (user.getTokenId()!= null && !sessionExpired(user)) {
             *
             * if (user.getTokenId().equals(LoginUtil.getPassword(clientToken))) { // user authorized return true;
             *
             * } else { // unauthorized access res.sendError(401); } } else { // session does not exist/expired,
             * temporary re-direct, ask user to re-login res.sendError(307); }
             *
             * } else { // bad request, no authToken sent in the request res.sendError(400); }
             */
        } catch (Exception e) {
            LOGGER.error(e);
        }


        return false;
    }


}
