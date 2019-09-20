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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.onap.portalsdk.core.controller.RestrictedBaseController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/")
public class ModelsViewsController extends RestrictedBaseController {

    public ModelsViewsController() {
        super();
    }

    @RequestMapping(value = { "/newsubmission" }, method = RequestMethod.GET)
    public ModelAndView newSubmission(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/committedsubmissions" }, method = RequestMethod.GET)
    public ModelAndView committedSubmissions(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/getmostrecent" }, method = RequestMethod.GET)
    public ModelAndView getMostrecent(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/getbytimestamp" }, method = RequestMethod.GET)
    public ModelAndView getByTimestamp(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/getlastrun" }, method = RequestMethod.GET)
    public ModelAndView getLastRun(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/getbasedondate" }, method = RequestMethod.GET)
    public ModelAndView getBasedOnDate(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/validationresults" }, method = RequestMethod.GET)
    public ModelAndView validationResults(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/registerlab" }, method = RequestMethod.GET)
    public ModelAndView registerLab(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/unregisterlab" }, method = RequestMethod.GET)
    public ModelAndView unRegisterLab(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/modifylab" }, method = RequestMethod.GET)
    public ModelAndView modifyLab(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/getlabs" }, method = RequestMethod.GET)
    public ModelAndView getLabs(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/registerblueprint" }, method = RequestMethod.GET)
    public ModelAndView registerBlueprint(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/unregisterblueprint" }, method = RequestMethod.GET)
    public ModelAndView unRegisterBlueprint(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/getblueprints" }, method = RequestMethod.GET)
    public ModelAndView getBlueprints(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/registerlayer" }, method = RequestMethod.GET)
    public ModelAndView registerLayer(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/unregisterlayer" }, method = RequestMethod.GET)
    public ModelAndView unRegisterLayer(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/getlayers" }, method = RequestMethod.GET)
    public ModelAndView getLayers(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/registertimeslot" }, method = RequestMethod.GET)
    public ModelAndView registerTimeslot(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/gettimeslots" }, method = RequestMethod.GET)
    public ModelAndView getTimeslots(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/registerblueprintinstance" }, method = RequestMethod.GET)
    public ModelAndView registerBlueprintInstance(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/unregisterblueprintinstance" }, method = RequestMethod.GET)
    public ModelAndView unRegisterBlueprintInstance(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/getblueprintinstances" }, method = RequestMethod.GET)
    public ModelAndView getBlueprintInstances(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/modifyblueprintinstance" }, method = RequestMethod.GET)
    public ModelAndView modifyBlueprintInstance(HttpServletRequest request) {
        final String defaultViewName = null;
        return new ModelAndView(defaultViewName);
    }

    @RequestMapping(value = { "/logout.htm" }, method = RequestMethod.GET)
    public ModelAndView login() {
        Map<String, Object> model = new HashMap<>();
        return new ModelAndView("logout", "model", model);
    }

}
