/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.security;

import com.sun.enterprise.security.audit.AuditManager;
import com.sun.enterprise.security.auth.realm.RealmsManager;
import com.sun.enterprise.security.common.Util;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.Events;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

import static com.sun.enterprise.security.SecurityLoggerInfo.secMgrDisabled;
import static com.sun.enterprise.security.SecurityLoggerInfo.secMgrEnabled;
import static com.sun.enterprise.security.common.Util.writeConfigFileToTempDir;
import static java.util.logging.Level.INFO;
import static org.glassfish.api.event.EventTypes.SERVER_SHUTDOWN;
import static org.glassfish.main.jdke.props.SystemProperties.setProperty;

/**
 * This class extends default implementation of ServerLifecycle interface. It provides security initialization and setup
 * for the server.
 *
 * @author Shing Wai Chan
 */
@Service
@Singleton
public class SecurityLifecycle implements PostConstruct, PreDestroy {

    private static final Logger _logger = SecurityLoggerInfo.getLogger();

    private static final String SYS_PROP_LOGIN_CONF = "java.security.auth.login.config";
    private static final String SYS_PROP_JAVA_SEC_POLICY = "java.security.policy";

    @Inject
    private SecurityServicesUtil securityServicesUtil;

    @Inject
    private ServiceLocator serviceLocator;

    @Inject
    private RealmsManager realmsManager;

    @Inject
    @Optional
    private ContainerSecurityLifecycle eeSecLifecycle;

    private EventListener listener;


    public SecurityLifecycle() {
        try {
            if (Util.isEmbeddedServer()) {
                // If the user-defined login.conf/server.policy are set as system properties, then they are given priority
                setProperty(SYS_PROP_LOGIN_CONF, writeConfigFileToTempDir("login.conf").toURI().toURL().toExternalForm(), false);
                setProperty(SYS_PROP_JAVA_SEC_POLICY, writeConfigFileToTempDir("server.policy").getAbsolutePath(), false);
            }

            // security manager is set here so that it can be accessed from
            // other lifecycles, like PEWebContainer
            _logger.info(System.getSecurityManager() == null ? secMgrDisabled : secMgrEnabled);
        } catch (Exception ex) {
            _logger.log(Level.SEVERE, "java_security.init_securitylifecycle_fail", ex);
            throw new RuntimeException(ex.toString(), ex);
        }
    }

    // override default
    public void onInitialization() {
        try {
            _logger.log(INFO, SecurityLoggerInfo.secServiceStartupEnter);

            realmsManager.createRealms();

            // Start the audit mechanism
            AuditManager auditManager = securityServicesUtil.getAuditManager();
            auditManager.loadAuditModules();

            // Audit the server started event
            auditManager.serverStarted();

            _logger.log(INFO, SecurityLoggerInfo.secServiceStartupExit);
        } catch (Exception ex) {
            throw new SecurityLifecycleException(ex);
        }
    }

    @Override
    public void postConstruct() {
        onInitialization();
        listener = new AuditServerShutdownListener();
        Events events = serviceLocator.getService(Events.class);
        events.register(listener);
    }

    @Override
    public void preDestroy() {
        // DO Nothing ?
        // TODO:V3 need to see if something needs cleanup

    }

    // To audit the server shutdown event
    public class AuditServerShutdownListener implements EventListener {
        @Override
        public void event(Event<?> event) {
            if (SERVER_SHUTDOWN.equals(event.type())) {
                securityServicesUtil.getAuditManager().serverShutdown();
            }
        }
    }
}
