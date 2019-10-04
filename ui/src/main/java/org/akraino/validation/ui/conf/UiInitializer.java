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

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.onap.portalsdk.core.domain.User;
import org.onap.portalsdk.core.onboarding.exception.CipherUtilException;
import org.onap.portalsdk.core.onboarding.util.CipherUtil;
import org.onap.portalsdk.core.service.UserProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

@Component
public class UiInitializer {

    @Autowired
    UserProfileService userService;

    // Create all-trusting host name verifier
    private final HostnameVerifier hostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };
    // Create a trust manager that does not validate certificate chains
    private final TrustManager[] trustAll = new TrustManager[] { new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null; // Not relevant.
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            // Do nothing. Just allow them all.
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            // Do nothing. Just allow them all.
        }
    } };

    @EventListener(ContextRefreshedEvent.class)
    public void setHttpProperties() throws NoSuchAlgorithmException, KeyManagementException {
        if (System.getenv("TRUST_ALL") != null && System.getenv("TRUST_ALL").equals("true")) {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, this.trustAll, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(this.hostnameVerifier);
            DefaultClientConfig config = new DefaultClientConfig();
            Map<String, Object> properties = config.getProperties();
            HTTPSProperties httpsProperties = new HTTPSProperties((str, sslSession) -> true, sslContext);
            properties.put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES, httpsProperties);
        }
    }

    @EventListener(ContextRefreshedEvent.class)
    public void updateAdminUser() throws RuntimeException, IOException, CipherUtilException {
        User admin = null;
        List<User> users = userService.findAllActive();
        for (User user : users) {
            if (user.getLoginId().equals("admin")) {
                admin = user;
            }
        }
        if (admin == null) {
            throw new RuntimeException("Admin user does not exist");
        }
        if (admin.getLoginPwd().equals("admin_password")) {
            admin.setLoginPwd(
                    CipherUtil.encryptPKC(System.getenv("UI_ADMIN_PASSWORD"), System.getenv("ENCRYPTION_KEY")));
            userService.saveUser(admin);
        }
    }

}
