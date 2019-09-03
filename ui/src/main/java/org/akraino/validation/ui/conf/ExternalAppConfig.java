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

import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.akraino.validation.ui.login.LoginStrategyImpl;
import org.akraino.validation.ui.scheduler.RegistryAdapter;
import org.onap.portalapp.music.conf.MusicSessionConfig;
import org.onap.portalsdk.core.auth.LoginStrategy;
import org.onap.portalsdk.core.conf.AppConfig;
import org.onap.portalsdk.core.conf.Configurable;
import org.onap.portalsdk.core.logging.format.AlarmSeverityEnum;
import org.onap.portalsdk.core.logging.format.AppMessagesEnum;
import org.onap.portalsdk.core.logging.logic.EELFLoggerDelegate;
import org.onap.portalsdk.core.objectcache.AbstractCacheManager;
import org.onap.portalsdk.core.onboarding.util.CipherUtil;
import org.onap.portalsdk.core.onboarding.util.PortalApiProperties;
import org.onap.portalsdk.core.service.DataAccessService;
import org.onap.portalsdk.core.util.CacheManager;
import org.onap.portalsdk.core.util.SystemProperties;
import org.onap.portalsdk.core.web.support.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;

import com.mchange.v2.c3p0.ComboPooledDataSource;

@Configuration
@EnableWebMvc
@ComponentScan(basePackages = { "org.akraino", "org.onap" })
@PropertySource(value = { "${container.classpath:}/WEB-INF/conf/app/test.properties" }, ignoreResourceNotFound = true)
@Profile("src")
@EnableAsync
@EnableScheduling
@Import({ MusicSessionConfig.class })
public class ExternalAppConfig extends AppConfig implements Configurable {

    private RegistryAdapter schedulerRegistryAdapter;
    private static final EELFLoggerDelegate LOGGER = EELFLoggerDelegate.getLogger(ExternalAppConfig.class);

    @Configuration
    @Import(SystemProperties.class)
    static class InnerConfiguration {
    }

    /**
     * @see org.onap.portalsdk.core.conf.AppConfig#viewResolver()
     */
    @Override
    public ViewResolver viewResolver() {
        return super.viewResolver();
    }

    /**
     * @see org.onap.portalsdk.core.conf.AppConfig#addResourceHandlers(ResourceHandlerRegistry)
     *
     * @param registry
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        super.addResourceHandlers(registry);
    }

    /**
     * @see org.onap.portalsdk.core.conf.AppConfig#dataAccessService()
     */
    @Override
    public DataAccessService dataAccessService() {
        return super.dataAccessService();
    }

    /**
     *
     * Creates the Application Data Source.
     *
     * @return DataSource Object
     * @throws Exception on failure to create data source object
     */
    @Override
    @Bean
    public DataSource dataSource() throws Exception {

        systemProperties();

        ComboPooledDataSource dataSource = new ComboPooledDataSource();
        try {
            dataSource.setDriverClass(SystemProperties.getProperty(SystemProperties.DB_DRIVER));
            dataSource.setJdbcUrl("jdbc:mariadb://" + System.getenv("DB_IP_PORT") + "/"
                    + PortalApiProperties.getProperty("akraino_database_name"));
            dataSource.setUser(SystemProperties.getProperty(SystemProperties.DB_USERNAME));
            String password = System.getenv("MARIADB_AKRAINO_PASSWORD");
            if (SystemProperties.containsProperty(SystemProperties.DB_ENCRYPT_FLAG)) {
                String encryptFlag = SystemProperties.getProperty(SystemProperties.DB_ENCRYPT_FLAG);
                if (encryptFlag != null && encryptFlag.equalsIgnoreCase("true")) {
                    password = CipherUtil.decrypt(password);
                }
            }
            dataSource.setPassword(password);
            dataSource
            .setMinPoolSize(Integer.parseInt(SystemProperties.getProperty(SystemProperties.DB_MIN_POOL_SIZE)));
            dataSource
            .setMaxPoolSize(Integer.parseInt(SystemProperties.getProperty(SystemProperties.DB_MAX_POOL_SIZE)));
            dataSource.setIdleConnectionTestPeriod(
                    Integer.parseInt(SystemProperties.getProperty(SystemProperties.IDLE_CONNECTION_TEST_PERIOD)));
            dataSource.setTestConnectionOnCheckout(getConnectionOnCheckout());
            dataSource.setPreferredTestQuery(getPreferredTestQuery());
        } catch (Exception e) {
            LOGGER.error(EELFLoggerDelegate.errorLogger,
                    "Error initializing database, verify database settings in properties file: "
                            + UserUtils.getStackTrace(e),
                            AlarmSeverityEnum.CRITICAL);
            LOGGER.error(EELFLoggerDelegate.debugLogger,
                    "Error initializing database, verify database settings in properties file: "
                            + UserUtils.getStackTrace(e),
                            AlarmSeverityEnum.CRITICAL);
            // Raise an alarm that opening a connection to the database failed.
            LOGGER.logEcompError(AppMessagesEnum.BeDaoSystemError);
            throw e;
        }
        return dataSource;
    }

    /**
     * Creates a new list with a single entry that is the external app
     * definitions.xml path.
     *
     * @return List of String, size 1
     */
    @Override
    public List<String> addTileDefinitions() {
        List<String> definitions = new ArrayList<>();
        definitions.add("/WEB-INF/defs/definitions.xml");
        return definitions;
    }

    /**
     * Adds request interceptors to the specified registry by calling
     * {@link AppConfig#addInterceptors(InterceptorRegistry)}, but excludes certain
     * paths from the session timeout interceptor.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        super.setExcludeUrlPathsForSessionTimeout("/login_external", "*/login_external.htm", "login", "/login.htm",
                "/api*", "/single_signon.htm", "/single_signon", "logout", "/logout.htm");
        super.addInterceptors(registry);
    }

    /**
     * Creates and returns a new instance of a {@link CacheManager} class.
     *
     * @return New instance of {@link CacheManager}
     */
    @Bean
    public AbstractCacheManager cacheManager() {
        return new CacheManager();
    }

    /**
     * Creates and returns a new instance of a {@link SchedulerFactoryBean} and
     * populates it with triggers.
     *
     * @return New instance of {@link SchedulerFactoryBean}
     * @throws Exception
     */
    // @Bean // ANNOTATION COMMENTED OUT
    // APPLICATIONS REQUIRING QUARTZ SHOULD RESTORE ANNOTATION
    public SchedulerFactoryBean schedulerFactoryBean() throws Exception {
        SchedulerFactoryBean scheduler = new SchedulerFactoryBean();
        scheduler.setTriggers(schedulerRegistryAdapter.getTriggers());
        scheduler.setConfigLocation(appApplicationContext.getResource("WEB-INF/conf/quartz.properties"));
        scheduler.setDataSource(dataSource());
        return scheduler;
    }

    /**
     * Sets the scheduler registry adapter.
     *
     * @param schedulerRegistryAdapter
     */
    @Autowired
    public void setSchedulerRegistryAdapter(final RegistryAdapter schedulerRegistryAdapter) {
        this.schedulerRegistryAdapter = schedulerRegistryAdapter;
    }

    @Bean
    public LoginStrategy loginStrategy() {
        return new LoginStrategyImpl();
    }

    /**
     * Gets the value of the property {@link SystemProperties#PREFERRED_TEST_QUERY};
     * defaults to "Select 1" if the property is not defined.
     *
     * @return String value that is a SQL query
     */
    private String getPreferredTestQuery() {
        // Use simple default
        String preferredTestQueryStr = "SELECT 1";
        if (SystemProperties.containsProperty(SystemProperties.PREFERRED_TEST_QUERY)) {
            preferredTestQueryStr = SystemProperties.getProperty(SystemProperties.PREFERRED_TEST_QUERY);
            LOGGER.debug(EELFLoggerDelegate.debugLogger, "getPreferredTestQuery: property key {} value is {}",
                    SystemProperties.PREFERRED_TEST_QUERY, preferredTestQueryStr);
        } else {
            LOGGER.info(EELFLoggerDelegate.errorLogger,
                    "getPreferredTestQuery: property key {} not found, using default value {}",
                    SystemProperties.PREFERRED_TEST_QUERY, preferredTestQueryStr);
        }
        return preferredTestQueryStr;
    }

    /**
     * Gets the value of the property
     * {@link SystemProperties#TEST_CONNECTION_ON_CHECKOUT}; defaults to true if the
     * property is not defined.
     *
     * @return Boolean value
     */
    private Boolean getConnectionOnCheckout() {
        // Default to true, always test connection
        boolean testConnectionOnCheckout = true;
        if (SystemProperties.containsProperty(SystemProperties.TEST_CONNECTION_ON_CHECKOUT)) {
            testConnectionOnCheckout = Boolean
                    .valueOf(SystemProperties.getProperty(SystemProperties.TEST_CONNECTION_ON_CHECKOUT));
            LOGGER.debug(EELFLoggerDelegate.debugLogger, "getConnectionOnCheckout: property key {} value is {}",
                    SystemProperties.TEST_CONNECTION_ON_CHECKOUT, testConnectionOnCheckout);
        } else {
            LOGGER.info(EELFLoggerDelegate.errorLogger,
                    "getConnectionOnCheckout: property key {} not found, using default value {}",
                    SystemProperties.TEST_CONNECTION_ON_CHECKOUT, testConnectionOnCheckout);
        }
        return testConnectionOnCheckout;
    }
}
