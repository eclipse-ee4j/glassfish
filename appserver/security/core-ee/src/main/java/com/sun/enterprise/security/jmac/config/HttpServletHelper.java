/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package com.sun.enterprise.security.jmac.config;

import java.util.HashMap;
import java.util.Map;

import javax.security.auth.callback.CallbackHandler;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.runtime.web.SunWebApp;
import com.sun.enterprise.deployment.web.LoginConfiguration;
import com.sun.enterprise.security.auth.realm.certificate.CertificateRealm;
import com.sun.enterprise.security.web.integration.WebSecurityManager;

import jakarta.security.auth.message.config.AuthConfigProvider;

public class HttpServletHelper extends ConfigHelper {
    private String realmName = null;

    public static final String AUTH_TYPE = "jakarta.servlet.http.authType";

    public HttpServletHelper(String appContext, Map map, CallbackHandler cbh, String realmName, boolean isSystemApp,
            String defaultSystemProviderID) {

        WebBundleDescriptor webBundle = null;
        if (map != null) {
            webBundle = (WebBundleDescriptor) map.get(HttpServletConstants.WEB_BUNDLE);
            if (webBundle != null) {
                LoginConfiguration loginConfig = webBundle.getLoginConfiguration();
                if (loginConfig != null
                        && LoginConfiguration.CLIENT_CERTIFICATION_AUTHENTICATION.equals(loginConfig.getAuthenticationMethod())) {
                    this.realmName = CertificateRealm.AUTH_TYPE;
                } else {
                    this.realmName = realmName;
                }
            }
        }

        // set realmName before init
        init(GFServerConfigProvider.HTTPSERVLET, appContext, map, cbh);

        if (webBundle != null) {
            String policyContextId = WebSecurityManager.getContextID(webBundle);
            map.put(HttpServletConstants.POLICY_CONTEXT, policyContextId);

            SunWebApp sunWebApp = webBundle.getSunDescriptor();
            String pid = sunWebApp != null ? sunWebApp.getAttributeValue(SunWebApp.HTTPSERVLET_SECURITY_PROVIDER) : null;
            boolean nullConfigProvider = false;

            if (isSystemApp && (pid == null || pid.length() == 0)) {
                pid = defaultSystemProviderID;
                if (pid == null || pid.length() == 0) {
                    nullConfigProvider = true;
                }
            }

            if ((pid != null && pid.length() > 0 || nullConfigProvider) && !hasExactMatchAuthProvider()) {
                AuthConfigProvider configProvider = nullConfigProvider ? null : new GFServerConfigProvider(new HashMap(), null);
                String jmacProviderRegisID = factory.registerConfigProvider(configProvider, GFServerConfigProvider.HTTPSERVLET, appContext,
                        "GlassFish provider: " + GFServerConfigProvider.HTTPSERVLET + ":" + appContext);
                this.setJmacProviderRegisID(jmacProviderRegisID);

            }
        }

    }

    // realmName must be set first and this is invoked inside the init()
    @Override
    protected HandlerContext getHandlerContext(Map map) {
        final String fRealmName = realmName;
        return new HandlerContext() {
            @Override
            public String getRealmName() {
                return fRealmName;
            }
        };
    }
}
