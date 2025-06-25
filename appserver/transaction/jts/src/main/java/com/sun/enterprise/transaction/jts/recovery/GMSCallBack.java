/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.transaction.jts.recovery;

import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.ee.cms.core.CallBack;
import com.sun.enterprise.ee.cms.core.DistributedStateCache;
import com.sun.enterprise.ee.cms.core.FailureRecoverySignal;
import com.sun.enterprise.ee.cms.core.GroupManagementService;
import com.sun.enterprise.ee.cms.core.Signal;
import com.sun.enterprise.transaction.api.ResourceRecoveryManager;
import com.sun.enterprise.transaction.jts.api.DelegatedTransactionRecoveryFence;
import com.sun.jts.CosTransactions.Configuration;
import com.sun.jts.jta.TransactionServiceProperties;
import com.sun.logging.LogDomains;

import java.io.Serializable;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import org.glassfish.gms.bootstrap.GMSAdapter;
import org.glassfish.gms.bootstrap.GMSAdapterService;
import org.glassfish.hk2.api.ServiceLocator;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

public class GMSCallBack implements CallBack {

    private static final String component = "TRANSACTION-RECOVERY-SERVICE";
    private static final String TXLOGLOCATION = "TX_LOG_DIR";
    private static final String MEMBER_DETAILS = "MEMBERDETAILS";

    // Use a class from com.sun.jts subpackage
    static Logger _logger = LogDomains.getLogger(TransactionServiceProperties.class, LogDomains.TRANSACTION_LOGGER);

    private Servers servers;
    private ServiceLocator serviceLocator;

    private int waitTime;
    private DelegatedTransactionRecoveryFence fence;
    private GroupManagementService gms;
    private final long startTime;
    private final static Object lock = new Object();

    public GMSCallBack(int waitTime, ServiceLocator serviceLocator) {
        GMSAdapterService gmsAdapterService = serviceLocator.getService(GMSAdapterService.class);
        if (gmsAdapterService != null) {
            GMSAdapter gmsAdapter = gmsAdapterService.getGMSAdapter();
            if (gmsAdapter != null) {
                gmsAdapter.registerFailureRecoveryListener(component, this);

                this.serviceLocator = serviceLocator;
                servers = serviceLocator.getService(Servers.class);

                this.waitTime = waitTime;

                Properties props = TransactionServiceProperties.getJTSProperties(serviceLocator, false);
                if (!Configuration.isDBLoggingEnabled()) {
                    if (Configuration.getORB() == null) {
                        // IIOP listeners are not setup yet,
                        // Create recoveryfile file so that automatic recovery will find it even
                        // if no XA transaction is envolved.
                        fence = RecoveryLockFile.getDelegatedTransactionRecoveryFence(this);
                    }

                    gms = gmsAdapter.getModule();

                    // Set the member details when GMS service is ready to store it
                    String instanceName = props.getProperty(Configuration.INSTANCE_NAME);
                    String logdir = props.getProperty(Configuration.LOG_DIRECTORY);
                    try {
                        _logger.log(INFO, "Storing GMS instance " + instanceName + " data " + TXLOGLOCATION + " : " + logdir);
                        gms.updateMemberDetails(instanceName, TXLOGLOCATION, logdir);
                    } catch (Exception e) {
                        _logger.log(WARNING, "jts.error_updating_gms", e);
                    }
                }
            }
        }
        startTime = System.currentTimeMillis();
    }

    @Override
    public void processNotification(Signal signal) {
        if (signal instanceof FailureRecoverySignal) {
            long timestamp = System.currentTimeMillis();

            if (_logger.isLoggable(INFO)) {
                _logger.log(INFO, "[GMSCallBack] failure recovery signal: " + signal);
            }

            // Waiting for 1 minute (or the user set value) to ensure that indoubt xids are updated into
            // the database, otherwise while doing the recovery an instance may not
            // get all the correct indoubt xids.
            try {
                Thread.sleep(waitTime * 1000L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String instance = signal.getMemberToken();
            String logdir = null;
            if (Configuration.isDBLoggingEnabled()) {
                logdir = instance; // this is how logdir will be used inside the db recovery
            } else {
                Map<Serializable, Serializable> failedMemberDetails = signal.getMemberDetails();
                if (failedMemberDetails != null) {
                    logdir = (String) failedMemberDetails.get(TXLOGLOCATION);
                }
            }

            synchronized (lock) {
                _logger.log(INFO, "[GMSCallBack] Recovering for instance: " + instance + " logdir: " + logdir);
                doRecovery(logdir, instance, timestamp);

                if (!Configuration.isDBLoggingEnabled()) {
                    // Find records of not finished delegated recovery and do delegated recovery on those instances.
                    while (logdir != null) {
                        logdir = finishDelegatedRecovery(logdir, timestamp);
                    }
                }
                _logger.log(INFO, "[GMSCallBack] Finished recovery for instance: " + instance);
            }
        } else {
            if (_logger.isLoggable(FINE)) {
                _logger.log(FINE, "[GMSCallBack] ignoring signal: " + signal);
            }
        }
    }

    /**
     * Find records of not finished delegated recovery in the recovery lock file on this path, and do delegated recovery if
     * such record exists
     */
    String finishDelegatedRecovery(String logdir) {
        return finishDelegatedRecovery(logdir, startTime);
    }

    /**
     * Find records of not finished delegated recovery in the recovery lock file on this path and recorded before specified
     * timestamp, and do delegated recovery if such record exists
     */
    String finishDelegatedRecovery(String logdir, long timestamp) {
        String delegatedLogDir = null;
        String instance = fence.getInstanceRecoveredFor(logdir, timestamp);
        if (_logger.isLoggable(INFO)) {
            _logger.log(INFO, "[GMSCallBack] Instance " + instance + " need to finish delegated recovering");
        }

        if (instance != null) {
            DistributedStateCache dsc = gms.getGroupHandle().getDistributedStateCache();
            Map<Serializable, Serializable> memberDetails = dsc.getFromCacheForPattern(MEMBER_DETAILS, instance);
            delegatedLogDir = (String) memberDetails.get(TXLOGLOCATION);
            if (_logger.isLoggable(INFO)) {
                _logger.log(INFO, "[GMSCallBack] Tx log dir for instance " + instance + " is " + delegatedLogDir);
            }

            doRecovery(delegatedLogDir, instance, timestamp);
        }

        return delegatedLogDir;
    }

    private void doRecovery(String logdir, String instance, long timestamp) {
        if (isInstanceRunning(instance)) {
            return;
        }

        if (!Configuration.isDBLoggingEnabled()) {
            if (logdir == null) {
                // Could happen if instance fails BEFORE actually getting this info into distributed state cache.
                // Could also be a gms distributed state cache bug.
                _logger.log(WARNING, "jts.error_getting_member_details", instance);
                return;
            }

            if (fence.isFenceRaised(logdir, instance, timestamp)) {
                if (_logger.isLoggable(INFO)) {
                    _logger.log(INFO, "Instance " + instance + " is already recovering");
                }
                return;
            }
        }

        try {
            if (!Configuration.isDBLoggingEnabled()) {
                fence.raiseFence(logdir, instance);
            }

            if (_logger.isLoggable(FINE)) {
                _logger.log(FINE, "Transaction log directory for " + instance + " is " + logdir);
                _logger.log(FINE, "Starting transaction recovery of " + instance);
            }

            ResourceRecoveryManager recoveryManager = serviceLocator.getService(ResourceRecoveryManager.class);
            recoveryManager.recoverIncompleteTx(true, logdir, instance, true);
            if (_logger.isLoggable(FINE)) {
                _logger.log(FINE, "Transaction recovery of " + instance + " is completed");
            }
        } catch (Throwable e) {
            _logger.log(WARNING, "jts.recovery_error", e);
        } finally {
            if (!Configuration.isDBLoggingEnabled()) {
                fence.lowerFence(logdir, instance);
            }
        }
    }

    private boolean isInstanceRunning(String instance) {
        for (Server server : servers.getServer()) {
            if (instance.equals(server.getName())) {
                return server.isListeningOnAdminPort();
            }
        }

        return false;
    }
}
