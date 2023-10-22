/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.security.ee.web.integration;

import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.security.WebSecurityDeployerProbeProvider;
import com.sun.enterprise.security.ee.authorize.PolicyContextHandlerImpl;
import com.sun.enterprise.security.factory.SecurityManagerFactory;

import jakarta.inject.Singleton;
import jakarta.security.jacc.PolicyContextException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.glassfish.internal.api.ServerContext;
import org.glassfish.security.common.Group;
import org.glassfish.security.common.UserPrincipal;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.FINE;

/**
 * @author JeanFrancois Arcand
 * @author Harpreet Singh
 */
@Service
@Singleton
public class WebSecurityManagerFactory extends SecurityManagerFactory {

    private static Logger logger = LogUtils.getLogger();
    private final WebSecurityDeployerProbeProvider probeProvider = new WebSecurityDeployerProbeProvider();

    final PolicyContextHandlerImpl pcHandlerImpl = PolicyContextHandlerImpl.getInstance();

    private final Map<String, UserPrincipal> adminPrincipals = new ConcurrentHashMap<>();
    private final Map<String, Group> adminGroups = new ConcurrentHashMap<>();

    // stores the context ids to appnames for standalone web apps
    private final Map<String, ArrayList<String>> CONTEXT_IDS = new HashMap<>();
    private final Map<String, Map<String, WebSecurityManager>> SECURITY_MANAGERS = new HashMap<>();

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


    public UserPrincipal getAdminPrincipal(String username, String realmName) {
        // FIXME: can be hacked: "ab+cd" = "a+bcd"
        return adminPrincipals.get(realmName + username);
    }


    public void putAdminPrincipal(String realmName, UserPrincipal principal) {
        // FIXME: can be hacked: "ab+cd" = "a+bcd"
        adminPrincipals.put(realmName + principal.getName(), principal);
    }


    public Group getAdminGroup(String group, String realmName) {
        // FIXME: can be hacked: "ab+cd" = "a+bcd"
        return adminGroups.get(realmName + group);
    }


    public void putAdminGroup(String group, String realmName, Group principal) {
        // FIXME: can be hacked: "ab+cd" = "a+bcd"
        adminGroups.put(realmName + group, principal);
    }
}
