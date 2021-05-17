/*
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

import java.util.logging.Logger;

import jakarta.inject.Inject;
import javax.naming.Context;
import javax.naming.NamingException;

import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.api.naming.GlassfishNamingManager;
import org.glassfish.api.naming.NamingObjectProxy;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.config.TransactionService;
import com.sun.logging.LogDomains;

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
//  public class TransactionLifecycleService implements Startup, PostConstruct, PreDestroy {

    @Inject
    ServiceLocator habitat;

    @Inject
    Events events;

    @Inject @Optional
    GlassfishNamingManager nm;

    static final String USER_TX_NO_JAVA_COMP = "UserTransaction";

    private static Logger _logger = LogDomains.getLogger(TransactionLifecycleService.class, LogDomains.JTA_LOGGER);

    private JavaEETransactionManager tm = null;

    @Override
    public void postConstruct() {
        EventListener glassfishEventListener = new EventListener() {
            @Override
            public void event(Event event) {
                if (event.is(EventTypes.SERVER_READY)) {
                    _logger.fine("TM LIFECYCLE SERVICE - ON READY");
                    onReady();
                } else if (event.is(EventTypes.PREPARE_SHUTDOWN)) {
                    _logger.fine("TM LIFECYCLE SERVICE - ON SHUTDOWN");
                    onShutdown();
                }
            }
        };
        events.register(glassfishEventListener);
        if (nm != null) {
            try {
                nm.publishObject(USER_TX_NO_JAVA_COMP, new NamingObjectProxy.InitializationNamingObjectProxy() {
                    @Override
                    public Object create(Context ic) throws NamingException {
                        ActiveDescriptor<?> descriptor = habitat.getBestDescriptor(
                                BuilderHelper.createContractFilter("jakarta.transaction.UserTransaction"));
                        if (descriptor == null) return null;

                        return habitat.getServiceHandle(descriptor).getService();
                    }
                }, false);
            } catch (NamingException e) {
                _logger.warning("Can't bind \"UserTransaction\" in JNDI");
            }
        }
    }

    @Override
    public void preDestroy() {
        if (nm != null) {
            try {
                nm.unpublishObject(USER_TX_NO_JAVA_COMP);
            } catch (NamingException e) {
                _logger.warning("Can't unbind \"UserTransaction\" in JNDI");
            }
        }
    }

    public void onReady() {
        _logger.fine("ON TM READY STARTED");

        TransactionService txnService = habitat.getService(TransactionService.class);
        if (txnService != null) {
            boolean isAutomaticRecovery = Boolean.valueOf(txnService.getAutomaticRecovery());
            if (isAutomaticRecovery) {
                _logger.fine("ON TM RECOVERY START");
                tm = habitat.getService(JavaEETransactionManager.class);
                tm.initRecovery(false);
                _logger.fine("ON TM RECOVERY END");
            }
        }

        _logger.fine("ON TM READY FINISHED");
    }

    public void onShutdown() {
        // Cleanup if TM was loaded
        if (tm == null) {
            ServiceHandle<JavaEETransactionManager> inhabitant =
                    habitat.getServiceHandle(JavaEETransactionManager.class);
            if (inhabitant != null && inhabitant.isActive()) {
                tm = inhabitant.getService();
            }
        }
        if (tm != null) {
            _logger.fine("ON TM SHUTDOWN STARTED");
            tm.shutdown();
            _logger.fine("ON TM SHUTDOWN FINISHED");
        }

    }

}
