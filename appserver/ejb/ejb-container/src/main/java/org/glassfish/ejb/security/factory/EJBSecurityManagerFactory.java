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

/*
 * EJBSecurityManagerFactory.java
 *
 * Created on June 9, 2003, 5:42 PM
 */

package org.glassfish.ejb.security.factory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.inject.Inject;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.security.application.EJBSecurityManager;
import org.glassfish.ejb.security.application.EjbSecurityProbeProvider;

import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Singleton;

import com.sun.enterprise.security.ee.audit.AppServerAuditManager;
import com.sun.enterprise.security.factory.SecurityManagerFactory;
import com.sun.logging.LogDomains;

/**
 * EJB Security Manager Factory Implementation
 *
 * @author Harpreet Singh
 */
@Service
@Singleton
public final class EJBSecurityManagerFactory extends SecurityManagerFactory {

    private static Logger _logger = null;

    static {
        _logger = LogDomains.getLogger(EJBSecurityManagerFactory.class, LogDomains.SECURITY_LOGGER);
    }

    @Inject
    InvocationManager invMgr;


    @Inject
    AppServerAuditManager auditManager;

    private EjbSecurityProbeProvider probeProvider = new EjbSecurityProbeProvider();
    /**
     * Creates a new instance of EJBSecurityManagerFactory
     */
    public EJBSecurityManagerFactory() {
    }

    /*
    public SecurityManager getSecurityManager(String contextId) {
        if (_poolHas(contextId)) {
            return (SecurityManager) _poolGet(contextId);
        }
        return null;
    }

    public EJBSecurityManager createSecurityManager(Descriptor descriptor) {
        EJBSecurityManager ejbSM = null;
        String contextId = null;
        String appName = null;
        try {

            if (descriptor == null || !(descriptor instanceof EjbDescriptor)) {
                throw new IllegalArgumentException("Illegal Deployment Descriptor Information.");
            }
            EjbDescriptor ejbdes = (EjbDescriptor) descriptor;
            ejbSM = new EJBSecurityManager(ejbdes, invMgr);

            // if the descriptor is not a EjbDescriptor the EJBSM will
            // throw an exception. So the following will always work.
            appName = ejbdes.getApplication().getRegistrationName();
            contextId = EJBSecurityManager.getContextID(ejbdes);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE,
                        "[EJB-Security] EJB Security:Creating EJBSecurityManager for contextId = "
                                + contextId);
            }

        } catch (Exception e) {
            _logger.log(Level.FINE,
                    "[EJB-Security] FATAl Exception. Unable to create EJBSecurityManager: "
                            + e.getMessage());
            throw new RuntimeException(e);
        }

        synchronized (CONTEXT_ID) {
            List lst = (List) CONTEXT_ID.get(appName);
            if (lst == null) {
                lst = new ArrayList();
                CONTEXT_ID.put(appName, lst);
            }
            if (!lst.contains(contextId)) {
                lst.add(contextId);
            }
        }

        _poolPut(contextId, ejbSM);
        return ejbSM;
    }

    public String[] getAndRemoveContextIdForEjbAppName(String appName) {
        synchronized (CONTEXT_ID) {
            List contextId = (List) CONTEXT_ID.get(appName);
            if (contextId == null) {
                return null;
            }
            String[] rvalue = new String[contextId.size()];
            rvalue = (String[]) contextId.toArray(rvalue);

            CONTEXT_ID.remove(appName);
            return rvalue;
        }
    }*/
     // stores the context ids to appnames for apps
    private Map<String, ArrayList<String>> CONTEXT_IDS =
            new HashMap<String, ArrayList<String>>();
    private Map<String, Map<String, EJBSecurityManager>> SECURITY_MANAGERS =
            new HashMap<String, Map<String, EJBSecurityManager>>();

    public <T> EJBSecurityManager getManager(String ctxId, String name, boolean remove) {
        return getManager(SECURITY_MANAGERS, ctxId, name, remove);
    }

    public  <T> ArrayList<EJBSecurityManager>
            getManagers(String ctxId, boolean remove) {
        return getManagers(SECURITY_MANAGERS, ctxId, remove);
    }

    public  <T> ArrayList<EJBSecurityManager>
            getManagersForApp(String appName, boolean remove) {
        return getManagersForApp(SECURITY_MANAGERS, CONTEXT_IDS, appName, remove);
    }

    public <T> String[] getContextsForApp(String appName, boolean remove) {
        return getContextsForApp(CONTEXT_IDS, appName, remove);
    }

    public <T> void addManagerToApp(String ctxId, String name,
            String appName, EJBSecurityManager manager) {
        addManagerToApp(SECURITY_MANAGERS, CONTEXT_IDS, ctxId, name, appName, manager);
    }

    public EJBSecurityManager createManager(EjbDescriptor ejbDesc,
            boolean register) {
        String ctxId = EJBSecurityManager.getContextID(ejbDesc);
        String ejbName = ejbDesc.getName();
        EJBSecurityManager manager = null;
        if (register) {
            manager = getManager(ctxId, ejbName, false);
        }
        if (manager == null || !register) {
            try {
                probeProvider.securityManagerCreationStartedEvent(ejbName);
                manager = new EJBSecurityManager(ejbDesc, this.invMgr, this);
                probeProvider.securityManagerCreationEndedEvent(ejbName);
                if (register) {

                    String appName = ejbDesc.getApplication().getRegistrationName();
                    addManagerToApp(ctxId, ejbName, appName, manager);
                    probeProvider.securityManagerCreationEvent(ejbName);
                }
            } catch (Exception ex) {
                _logger.log(Level.FINE, "[EJB-Security] FATAL Exception. Unable to create EJBSecurityManager: " + ex.getMessage());
                throw new RuntimeException(ex);
            }
        }
        return manager;
    }

    public final AppServerAuditManager getAuditManager() {
        return this.auditManager;
    }
}
