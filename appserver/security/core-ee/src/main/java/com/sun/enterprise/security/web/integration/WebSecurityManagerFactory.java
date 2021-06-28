/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.web.integration;

import static java.util.logging.Level.FINE;

import java.security.Principal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.security.WebSecurityDeployerProbeProvider;
import com.sun.enterprise.security.authorize.PolicyContextHandlerImpl;
import com.sun.enterprise.security.factory.SecurityManagerFactory;

import jakarta.inject.Singleton;
import jakarta.security.jacc.PolicyContextException;

/**
 * @author JeanFrancois Arcand
 * @author Harpreet Singh
 */
@Service
@Singleton
public class WebSecurityManagerFactory extends SecurityManagerFactory {

    private static Logger logger = LogUtils.getLogger();
    private WebSecurityDeployerProbeProvider probeProvider = new WebSecurityDeployerProbeProvider();

    final PolicyContextHandlerImpl pcHandlerImpl = (PolicyContextHandlerImpl) PolicyContextHandlerImpl.getInstance();

    private final Map<String, Principal> adminPrincipals = new ConcurrentHashMap<>();
    private final Map<String, Principal> adminGroups = new ConcurrentHashMap<>();

    // stores the context ids to appnames for standalone web apps
    private Map<String, ArrayList<String>> CONTEXT_IDS = new HashMap<>();
    private Map<String, Map<String, WebSecurityManager>> SECURITY_MANAGERS = new HashMap<>();

    public WebSecurityManager getManager(String ctxId) {
        return getManager(SECURITY_MANAGERS, ctxId, null, false);
    }

    public WebSecurityManager getManager(String ctxId, boolean remove) {
        return getManager(SECURITY_MANAGERS, ctxId, null, remove);
    }

    public <T> ArrayList<WebSecurityManager> getManagers(String ctxId, boolean remove) {
        return getManagers(SECURITY_MANAGERS, ctxId, remove);
    }

    public <T> ArrayList<WebSecurityManager> getManagersForApp(String appName, boolean remove) {
        return getManagersForApp(SECURITY_MANAGERS, CONTEXT_IDS, appName, remove);
    }

    public <T> String[] getContextsForApp(String appName, boolean remove) {
        return getContextsForApp(CONTEXT_IDS, appName, remove);
    }

    public WebSecurityManager createManager(WebBundleDescriptor webBundleDescriptor, boolean register, ServerContext context) {
        String contextId = WebSecurityManager.getContextID(webBundleDescriptor);

        WebSecurityManager manager = null;
        if (register) {
            manager = getManager(contextId, false);
        }

        if (manager == null || !register) {
            try {
                probeProvider.securityManagerCreationStartedEvent(webBundleDescriptor.getModuleID());
                manager = new WebSecurityManager(webBundleDescriptor, context, this, register);
                probeProvider.securityManagerCreationEndedEvent(webBundleDescriptor.getModuleID());

                if (register) {
                    String applicationName = webBundleDescriptor.getApplication().getRegistrationName();
                    addManagerToApp(contextId, null, applicationName, manager);
                    probeProvider.securityManagerCreationEvent(contextId);
                }
            } catch (PolicyContextException e) {
                logger.log(FINE, "[Web-Security] FATAL Exception. Unable to create WebSecurityManager: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }

        return manager;
    }

    public <T> void addManagerToApp(String ctxId, String name, String appName, WebSecurityManager manager) {
        addManagerToApp(SECURITY_MANAGERS, CONTEXT_IDS, ctxId, name, appName, manager);
    }

    public Principal getAdminPrincipal(String username, String realmName) {
        return adminPrincipals.get(realmName + username);
    }

    public void putAdminPrincipal(String username, String realmName, Principal principal) {
        adminPrincipals.put(realmName + username, principal);
    }

    public Principal getAdminGroup(String group, String realmName) {
        return adminGroups.get(realmName + group);
    }

    public void putAdminGroup(String group, String realmName, Principal principal) {
        adminGroups.put(realmName + group, principal);
    }
}
