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

package org.glassfish.ejb.persistent.timer;

import com.sun.ejb.PersistentTimerService;
import com.sun.ejb.containers.EJBTimerService;
import com.sun.ejb.containers.EjbContainerUtil;
import com.sun.ejb.containers.EjbContainerUtilImpl;
import com.sun.enterprise.ee.cms.core.CallBack;
import com.sun.enterprise.ee.cms.core.GMSConstants;
import com.sun.enterprise.ee.cms.core.PlannedShutdownSignal;
import com.sun.enterprise.ee.cms.core.Signal;
import com.sun.enterprise.transaction.api.RecoveryResourceRegistry;
import com.sun.enterprise.transaction.spi.RecoveryEventListener;

import jakarta.inject.Inject;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.gms.bootstrap.GMSAdapter;
import org.glassfish.gms.bootstrap.GMSAdapterService;
import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;

@Service
public class DistributedEJBTimerService
    implements PersistentTimerService, RecoveryEventListener, PostConstruct, CallBack {

    private static Logger logger = EjbContainerUtilImpl.getLogger();

    @Inject
    private EjbContainerUtil ejbContainerUtil;

    @Inject
    GMSAdapterService gmsAdapterService;

    @Inject
    RecoveryResourceRegistry recoveryResourceRegistry;

    public void postConstruct() {
        if (!ejbContainerUtil.isDas()) {
            if (gmsAdapterService != null) {
                GMSAdapter gmsAdapter = gmsAdapterService.getGMSAdapter();
                if (gmsAdapter != null) {
                    // We only register interest in the Planned Shutdown event here.
                    // Because of the dependency between transaction recovery and
                    // timer migration, the timer migration operation during an
                    // unexpected failure is initiated by the transaction recovery
                    // subsystem.
                    gmsAdapter.registerPlannedShutdownListener(this);
                }
            }
            // Register for transaction recovery events
            recoveryResourceRegistry.addEventListener(this);
        }
    }

    public void initPersistentTimerService(String target) {
        PersistentEJBTimerService.initEJBTimerService(target);
    }

    @Override
    public void processNotification(Signal signal) {
        if (signal instanceof PlannedShutdownSignal) {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "[DistributedEJBTimerService] planned shutdown signal: " + signal);
            }
            PlannedShutdownSignal pssig = (PlannedShutdownSignal)signal;
            if (pssig.getEventSubType() == GMSConstants.shutdownType.INSTANCE_SHUTDOWN) {
                migrateTimers(signal.getMemberToken());
            }
        } else {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "[DistributedEJBTimerService] ignoring signal: " + signal);
            }
        }
    }

    @Override
    public void beforeRecovery(boolean delegated, String instance) {}

    @Override
    public void afterRecovery(boolean success, boolean delegated, String instance) {
        if (!delegated) {
            return; // nothing to do
        }

        if (logger.isLoggable(Level.INFO)) {
                logger.log(Level.INFO, "[DistributedEJBTimerService] afterRecovery event for instance " + instance);
        }

        if (instance != null && !instance.equals(ejbContainerUtil.getServerEnvironment().getInstanceName())) {
            if (success) {
                migrateTimers(instance);
            } else {
                logger.log(Level.WARNING, "[DistributedEJBTimerService] Cannot perform automatic timer migration after failed transaction recovery");
            }
        }
    }

    /**
     *--------------------------------------------------------------
     * Private methods for DistributedEJBTimerService
     *--------------------------------------------------------------
     */
    private int migrateTimers( String serverId ) {
        if (logger.isLoggable(Level.INFO)) {
            logger.log(Level.INFO, "[DistributedEJBTimerService] migrating timers from " + serverId);
        }

        int result = 0;
        // Force loading TimerService if it hadn't been started
        EJBTimerService ejbTimerService = EJBTimerService.getEJBTimerService();
        if (ejbTimerService != null && ejbTimerService.isPersistent()) {
            result = ejbTimerService.migrateTimers( serverId );
        } else {
            //throw new IllegalStateException("EJB Timer service is null. "
                    //+ "Cannot migrate timers for: " + serverId);
        }

        return result;
    }

} //DistributedEJBTimerService.java

