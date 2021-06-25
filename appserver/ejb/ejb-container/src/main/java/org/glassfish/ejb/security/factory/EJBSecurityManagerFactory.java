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

import static java.util.logging.Level.FINE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.security.application.EJBSecurityManager;
import org.glassfish.ejb.security.application.EjbSecurityProbeProvider;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.security.ee.audit.AppServerAuditManager;
import com.sun.enterprise.security.factory.SecurityManagerFactory;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * EJB Security Manager Factory Implementation
 *
 * @author Harpreet Singh
 */
@Service
@Singleton
public final class EJBSecurityManagerFactory extends SecurityManagerFactory {

    private static Logger _logger = LogDomains.getLogger(EJBSecurityManagerFactory.class, LogDomains.SECURITY_LOGGER);

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

    // stores the context ids to appnames for apps
    private Map<String, ArrayList<String>> CONTEXT_IDS = new HashMap<String, ArrayList<String>>();
    private Map<String, Map<String, EJBSecurityManager>> SECURITY_MANAGERS = new HashMap<String, Map<String, EJBSecurityManager>>();

    public <T> EJBSecurityManager getManager(String ctxId, String name, boolean remove) {
        return getManager(SECURITY_MANAGERS, ctxId, name, remove);
    }

    public <T> ArrayList<EJBSecurityManager> getManagers(String ctxId, boolean remove) {
        return getManagers(SECURITY_MANAGERS, ctxId, remove);
    }

    public <T> ArrayList<EJBSecurityManager> getManagersForApp(String appName, boolean remove) {
        return getManagersForApp(SECURITY_MANAGERS, CONTEXT_IDS, appName, remove);
    }

    public <T> String[] getContextsForApp(String appName, boolean remove) {
        return getContextsForApp(CONTEXT_IDS, appName, remove);
    }

    public <T> void addManagerToApp(String ctxId, String name, String appName, EJBSecurityManager manager) {
        addManagerToApp(SECURITY_MANAGERS, CONTEXT_IDS, ctxId, name, appName, manager);
    }

    public EJBSecurityManager createManager(EjbDescriptor ejbDescriptor, boolean register) {
        String contextId = EJBSecurityManager.getContextID(ejbDescriptor);
        String ejbName = ejbDescriptor.getName();

        EJBSecurityManager manager = null;
        if (register) {
            manager = getManager(contextId, ejbName, false);
        }

        if (manager == null || !register) {
            try {
                probeProvider.securityManagerCreationStartedEvent(ejbName);
                manager = new EJBSecurityManager(ejbDescriptor, this.invMgr, this);
                probeProvider.securityManagerCreationEndedEvent(ejbName);
                if (register) {

                    String applicationName = ejbDescriptor.getApplication().getRegistrationName();
                    addManagerToApp(contextId, ejbName, applicationName, manager);
                    probeProvider.securityManagerCreationEvent(ejbName);
                }
            } catch (Exception ex) {
                _logger.log(FINE, "[EJB-Security] FATAL Exception. Unable to create EJBSecurityManager: " + ex.getMessage());
                throw new RuntimeException(ex);
            }
        }

        return manager;
    }

    public final AppServerAuditManager getAuditManager() {
        return this.auditManager;
    }
}
