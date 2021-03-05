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

import java.util.HashMap;
import java.util.Map;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.security.web.integration.LogUtils;
import com.sun.enterprise.security.WebSecurityDeployerProbeProvider;
import com.sun.enterprise.security.authorize.PolicyContextHandlerImpl;
import com.sun.enterprise.security.factory.SecurityManagerFactory;
import org.glassfish.internal.api.ServerContext;
import java.util.logging.*;
import com.sun.logging.LogDomains;
import java.security.Principal;
import java.util.ArrayList;
import jakarta.security.jacc.PolicyContext;
import jakarta.security.jacc.PolicyContextException;
import jakarta.security.jacc.PolicyContextHandler;

import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Singleton;

/**
 * @author JeanFrancois Arcand
 * @author Harpreet Singh
 */
@Service
@Singleton
public class WebSecurityManagerFactory extends SecurityManagerFactory {

    private static Logger logger = LogUtils.getLogger();
    private WebSecurityDeployerProbeProvider probeProvider = new WebSecurityDeployerProbeProvider();

    public WebSecurityManagerFactory() {
        registerPolicyHandlers();
    }

    /*
     * private Map securityManagerPool = new HashMap(); // stores the context ids to appnames for standalone web apps
     * private Map CONTEXT_ID = new HashMap(); private static WebSecurityManagerFactory factory = null;
     * 
     * private WebSecurityManagerFactory() { } // returns the singleton instance of WebSecurityManagerFactory public static
     * synchronized WebSecurityManagerFactory getInstance() { if (factory == null) { factory = new
     * WebSecurityManagerFactory(); registerPolicyHandlers(); } return factory; } // generates a webSecurityManager public
     * WebSecurityManager newWebSecurityManager(WebBundleDescriptor wbd) { String contextId =
     * WebSecurityManager.getContextID(wbd); String appname = wbd.getApplication().getRegistrationName();
     * 
     * synchronized (CONTEXT_ID) { List lst = (List) CONTEXT_ID.get(appname); if (lst == null) { lst = new ArrayList();
     * CONTEXT_ID.put(appname, lst); } if (!lst.contains(contextId)) { lst.add(contextId); } }
     * 
     * if (logger.isLoggable(Level.FINE)) { logger.log(Level.FINE,
     * "[Web-Security] Web Security:Creating WebSecurityManager for contextId = " + contextId); }
     * 
     * WebSecurityManager wsManager = getWebSecurityManager(contextId); if (wsManager == null) {
     * 
     * // we should see if it is safe to do the security manager // construction within the synchronize block. // for the
     * time being, we will just make sure that we // synchronize access to the pool. try { wsManager = new
     * WebSecurityManager(wbd); } catch (jakarta.security.jacc.PolicyContextException e) { logger.log(Level.FINE,
     * "[Web-Security] FATAl Exception. Unable to create WebSecurityManager: " + e.getMessage()); throw new
     * RuntimeException(e); }
     * 
     * synchronized (securityManagerPool) { WebSecurityManager other = (WebSecurityManager)
     * securityManagerPool.get(contextId); if (other == null) { securityManagerPool.put(contextId, wsManager); } else {
     * wsManager = other; } } } return wsManager; }
     * 
     * public WebSecurityManager newWebSecurityManager(WebBundleDescriptor wbd, ServerContext context) { String contextId =
     * WebSecurityManager.getContextID(wbd); String appname = wbd.getApplication().getRegistrationName();
     * 
     * synchronized (CONTEXT_ID) { List lst = (List) CONTEXT_ID.get(appname); if (lst == null) { lst = new ArrayList();
     * CONTEXT_ID.put(appname, lst); } if (!lst.contains(contextId)) { lst.add(contextId); } }
     * 
     * if (logger.isLoggable(Level.FINE)) { logger.log(Level.FINE,
     * "[Web-Security] Web Security:Creating WebSecurityManager for contextId = " + contextId); }
     * 
     * WebSecurityManager wsManager = getWebSecurityManager(contextId); if (wsManager == null) {
     * 
     * // we should see if it is safe to do the security manager // construction within the synchronize block. // for the
     * time being, we will just make sure that we // synchronize access to the pool. try { wsManager = new
     * WebSecurityManager(wbd, context); } catch (jakarta.security.jacc.PolicyContextException e) { logger.log(Level.FINE,
     * "[Web-Security] FATAl Exception. Unable to create WebSecurityManager: " + e.getMessage()); throw new
     * RuntimeException(e); }
     * 
     * synchronized (securityManagerPool) { WebSecurityManager other = (WebSecurityManager)
     * securityManagerPool.get(contextId); if (other == null) { securityManagerPool.put(contextId, wsManager); } else {
     * wsManager = other; } } } return wsManager; }
     * 
     * public WebSecurityManager getWebSecurityManager(String contextId) { synchronized (securityManagerPool) { return
     * (WebSecurityManager) securityManagerPool.get(contextId); } }
     * 
     * public void removeWebSecurityManager(String contextId) { synchronized (securityManagerPool) {
     * securityManagerPool.remove(contextId); } }
     */

    /**
     * valid for standalone web apps
     * 
     * public String[] getContextIdsOfApp(String appName) { synchronized (CONTEXT_ID) { List contextId = (List)
     * CONTEXT_ID.get(appName); if (contextId == null) { return null; } String[] arrayContext = new
     * String[contextId.size()]; arrayContext = (String[]) contextId.toArray(arrayContext); return arrayContext; } }
     */

    /**
     * valid for standalone web apps
     * 
     * public String[] getAndRemoveContextIdForWebAppName(String appName) { synchronized (CONTEXT_ID) { String[] rvalue =
     * getContextIdsOfApp(appName); CONTEXT_ID.remove(appName); return rvalue; } }
     */

    final PolicyContextHandlerImpl pcHandlerImpl = (PolicyContextHandlerImpl) PolicyContextHandlerImpl.getInstance();

    final Map ADMIN_PRINCIPAL = new HashMap();
    final Map ADMIN_GROUP = new HashMap();

    public Principal getAdminPrincipal(String username, String realmName) {
        return (Principal) ADMIN_PRINCIPAL.get(realmName + username);
    }

    public Principal getAdminGroup(String group, String realmName) {
        return (Principal) ADMIN_GROUP.get(realmName + group);
    }

    private static void registerPolicyHandlers() {
        try {
            PolicyContextHandler pch = PolicyContextHandlerImpl.getInstance();
            PolicyContext.registerHandler(PolicyContextHandlerImpl.ENTERPRISE_BEAN, pch, true);
            PolicyContext.registerHandler(PolicyContextHandlerImpl.SUBJECT, pch, true);
            PolicyContext.registerHandler(PolicyContextHandlerImpl.EJB_ARGUMENTS, pch, true);
            PolicyContext.registerHandler(PolicyContextHandlerImpl.SOAP_MESSAGE, pch, true);
            PolicyContext.registerHandler(PolicyContextHandlerImpl.HTTP_SERVLET_REQUEST, pch, true);
            PolicyContext.registerHandler(PolicyContextHandlerImpl.REUSE, pch, true);
        } catch (PolicyContextException ex) {
            Logger.getLogger(WebSecurityManagerFactory.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // stores the context ids to appnames for standalone web apps
    private Map<String, ArrayList<String>> CONTEXT_IDS = new HashMap<String, ArrayList<String>>();
    private Map<String, Map<String, WebSecurityManager>> SECURITY_MANAGERS = new HashMap<String, Map<String, WebSecurityManager>>();

    public WebSecurityManager getManager(String ctxId, String name, boolean remove) {
        return getManager(SECURITY_MANAGERS, ctxId, name, remove);
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

    public <T> void addManagerToApp(String ctxId, String name, String appName, WebSecurityManager manager) {
        addManagerToApp(SECURITY_MANAGERS, CONTEXT_IDS, ctxId, name, appName, manager);
    }

    public WebSecurityManager createManager(WebBundleDescriptor wbd, boolean register, ServerContext context) {
        String ctxId = WebSecurityManager.getContextID(wbd);
        WebSecurityManager manager = null;
        if (register) {
            manager = getManager(ctxId, null, false);
        }
        if (manager == null || !register) {
            try {
                probeProvider.securityManagerCreationStartedEvent(wbd.getModuleID());
                manager = new WebSecurityManager(wbd, context, this, register);
                probeProvider.securityManagerCreationEndedEvent(wbd.getModuleID());
                if (register) {

                    String appName = wbd.getApplication().getRegistrationName();
                    addManagerToApp(ctxId, null, appName, manager);
                    probeProvider.securityManagerCreationEvent(ctxId);
                }
            } catch (jakarta.security.jacc.PolicyContextException e) {
                logger.log(Level.FINE, "[Web-Security] FATAL Exception. Unable to create WebSecurityManager: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
        return manager;
    }
}
