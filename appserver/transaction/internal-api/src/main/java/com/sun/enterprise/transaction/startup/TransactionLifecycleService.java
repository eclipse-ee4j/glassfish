/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.transaction.startup;

import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.config.TransactionService;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingException;

import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.NamingObjectProxy;
import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

/**
 * Service wrapper to only lookup the transaction recovery when there
 * are applications deployed since the actual service has ORB dependency.
 *
 * This is also responsible for binding (non java:comp) UserTransaction in naming tree.
 */
@Service
//todo: change value=10 to a public constant
@RunLevel( value=10 )
public class TransactionLifecycleService implements PostConstruct, PreDestroy {

    private static final Logger LOG = LogDomains.getLogger(TransactionLifecycleService.class, LogDomains.JTA_LOGGER, false);
    private static final SimpleJndiName USER_TX_NO_JAVA_COMP = new SimpleJndiName("UserTransaction");

    @Inject
    ServiceLocator loader;

    @Inject
    Events events;

    @Inject
    @Optional
    GlassfishNamingManager nm;


    private JavaEETransactionManager tm;

    @Override
    public void postConstruct() {
        EventListener glassfishEventListener = event -> {
            if (event.is(EventTypes.SERVER_READY)) {
                LOG.fine("TM LIFECYCLE SERVICE - ON READY");
                onReady();
            } else if (event.is(EventTypes.PREPARE_SHUTDOWN)) {
                LOG.fine("TM LIFECYCLE SERVICE - ON SHUTDOWN");
                onShutdown();
            }
        };
        events.register(glassfishEventListener);
        if (nm != null) {
            try {
                nm.publishObject(USER_TX_NO_JAVA_COMP, new NamingObjectProxy.InitializationNamingObjectProxy() {
                    @Override
                    public Object create(Context ic) throws NamingException {
                        ActiveDescriptor<?> descriptor = loader.getBestDescriptor(
                                BuilderHelper.createContractFilter("jakarta.transaction.UserTransaction"));
                        if (descriptor == null) {
                            return null;
                        }

                        return loader.getServiceHandle(descriptor).getService();
                    }
                }, false);
            } catch (NamingException e) {
                LOG.log(Level.SEVERE, "Can't bind \"UserTransaction\" in JNDI", e);
            }
        }
    }

    @Override
    public void preDestroy() {
        if (nm != null) {
            try {
                nm.unpublishObject(USER_TX_NO_JAVA_COMP);
            } catch (NamingException e) {
                LOG.log(Level.SEVERE, "Can't unbind \"UserTransaction\" in JNDI", e);
            }
        }
    }

    public void onReady() {
        LOG.fine("ON TM READY STARTED");

        TransactionService txnService = loader.getService(TransactionService.class);
        if (txnService != null) {
            boolean isAutomaticRecovery = Boolean.parseBoolean(txnService.getAutomaticRecovery());
            if (isAutomaticRecovery) {
                LOG.fine("ON TM RECOVERY START");
                tm = loader.getService(JavaEETransactionManager.class);
                tm.initRecovery(false);
                LOG.fine("ON TM RECOVERY END");
            }
        }

        LOG.fine("ON TM READY FINISHED");
    }

    public void onShutdown() {
        // Cleanup if TM was loaded
        if (tm == null) {
            ServiceHandle<JavaEETransactionManager> handle = loader.getServiceHandle(JavaEETransactionManager.class);
            if (handle != null && handle.isActive()) {
                tm = handle.getService();
            }
        }
        if (tm != null) {
            LOG.fine("ON TM SHUTDOWN STARTED");
            tm.shutdown();
            LOG.fine("ON TM SHUTDOWN FINISHED");
        }
    }
}
