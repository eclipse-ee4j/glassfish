/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.ejb.security.factory;

import com.sun.enterprise.security.ee.audit.AppServerAuditManager;
import com.sun.enterprise.security.factory.SecurityManagerFactory;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.security.application.EJBSecurityManager;
import org.glassfish.ejb.security.application.EjbSecurityProbeProvider;
import org.jvnet.hk2.annotations.Service;

import static com.sun.logging.LogDomains.SECURITY_LOGGER;
import static java.util.logging.Level.FINE;

/**
 * EJB Security Manager Factory Implementation
 *
 * @author Harpreet Singh
 * Created on June 9, 2003, 5:42 PM
 */
@Service
@Singleton
public final class EJBSecurityManagerFactory extends SecurityManagerFactory {

    private static final Logger LOG = LogDomains.getLogger(EJBSecurityManagerFactory.class, SECURITY_LOGGER, false);

    @Inject
    InvocationManager invocationManager;

    @Inject
    AppServerAuditManager auditManager;

    private final EjbSecurityProbeProvider probeProvider = new EjbSecurityProbeProvider();

    /**
     * Creates a new instance of EJBSecurityManagerFactory
     */
    public EJBSecurityManagerFactory() {
    }

    // stores the context ids to appnames for apps
    private final Map<String, ArrayList<String>> CONTEXT_IDS = new HashMap<>();
    private final Map<String, Map<String, EJBSecurityManager>> SECURITY_MANAGERS = new HashMap<>();

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
                manager = new EJBSecurityManager(ejbDescriptor, this.invocationManager, this);
                probeProvider.securityManagerCreationEndedEvent(ejbName);
                if (register) {

                    String applicationName = ejbDescriptor.getApplication().getRegistrationName();
                    addManagerToApp(contextId, ejbName, applicationName, manager);
                    probeProvider.securityManagerCreationEvent(ejbName);
                }
            } catch (Exception ex) {
                LOG.log(FINE, "[EJB-Security] FATAL Exception. Unable to create EJBSecurityManager: " + ex.getMessage());
                throw new RuntimeException(ex);
            }
        }

        return manager;
    }

    public AppServerAuditManager getAuditManager() {
        return auditManager;
    }
}
