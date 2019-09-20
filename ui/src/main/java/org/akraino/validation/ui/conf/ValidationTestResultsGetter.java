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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.akraino.validation.ui.entity.ValidationDbTestResult;
import org.akraino.validation.ui.service.DbAdapter;
import org.akraino.validation.ui.service.IntegratedResultService;
import org.akraino.validation.ui.service.utils.PrioritySupplier;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.onap.portalsdk.core.onboarding.util.PortalApiProperties;
import org.onap.portalsdk.core.web.support.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ValidationTestResultsGetter implements ApplicationListener<ContextRefreshedEvent> {

    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(ValidationTestResultsGetter.class);

    @Autowired
    IntegratedResultService integratedService;

    @Autowired
    DbAdapter dbAdapter;

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent event) {
        ApplicationContext context = new AnnotationConfigApplicationContext(ExecutorServiceInitializer.class);
        ExecutorService service = (ExecutorService) context.getBean("executorService");
        ValidationTestResultsGetterExecution task = new ValidationTestResultsGetterExecution();
        CompletableFuture<Boolean> completableFuture = CompletableFuture
                .supplyAsync(new PrioritySupplier<>(1, task::execute), service);
        completableFuture.thenAcceptAsync(callOutcome -> this.callbackNotify(callOutcome));
    }

    private void callbackNotify(Boolean outcome) {
        LOGGER.debug(EELFLoggerDelegate.debugLogger, "Result of validation result getter execution: " + outcome);
        try {
            Thread.sleep(Integer.valueOf(PortalApiProperties.getProperty("thread_sleep")));
        } catch (Exception e) {
            LOGGER.error(EELFLoggerDelegate.errorLogger, "Error in thread sleep. " + UserUtils.getStackTrace(e));
        }
        // Trigger the next retrieval of results
        ApplicationContext context = new AnnotationConfigApplicationContext(ExecutorServiceInitializer.class);
        ExecutorService service = (ExecutorService) context.getBean("executorService");
        ValidationTestResultsGetterExecution task = new ValidationTestResultsGetterExecution();
        CompletableFuture<Boolean> completableFuture = CompletableFuture
                .supplyAsync(new PrioritySupplier<>(1, task::execute), service);
        completableFuture.thenAcceptAsync(callOutcome -> this.callbackNotify(callOutcome));
    }

    private class ValidationTestResultsGetterExecution {

        public ValidationTestResultsGetterExecution() {
        }

        public Boolean execute() {
            try {
                for (String lab : integratedService.getLabsFromNexus()) {
                    for (String blueprintName : integratedService.getBlueprintNamesOfLabFromNexus(lab)) {
                        for (String version : integratedService.getBlueprintVersionsFromNexus(blueprintName, lab)) {
                            LOGGER.debug(EELFLoggerDelegate.debugLogger,
                                    "Trying to retrieve validation test result from nexus for blueprint name: "
                                            + blueprintName + ", version: " + version + " and lab: " + lab);
                            try {
                                List<ValidationDbTestResult> results = integratedService.getResultsFromNexus(
                                        blueprintName, version, lab,
                                        Integer.valueOf(PortalApiProperties.getProperty("no_last_timestamps")));
                                LOGGER.debug(EELFLoggerDelegate.debugLogger,
                                        "Validation test results retrieved from nexus with size : " + results.size());
                                dbAdapter.deleteUnreferencedEntries(results);
                                dbAdapter.storeResultsInDb(results);
                            } catch (Exception e) {
                                LOGGER.error(EELFLoggerDelegate.errorLogger,
                                        "Error when trying to receive results from nexus for blueprint name: "
                                                + blueprintName + ", version: " + version + " and lab: " + lab + ". "
                                                + UserUtils.getStackTrace(e));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.error(EELFLoggerDelegate.errorLogger,
                        "Error when retrieving Nexus results. " + UserUtils.getStackTrace(e));
                return false;
            }
            return true;
        }
    }

}
