/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.transaction.jts;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.transaction.JavaEETransactionManagerSimplified;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.api.RecoveryResourceRegistry;
import com.sun.enterprise.transaction.api.ResourceRecoveryManager;
import com.sun.enterprise.transaction.config.TransactionService;
import com.sun.enterprise.transaction.spi.RecoveryEventListener;
import com.sun.enterprise.transaction.spi.RecoveryResourceHandler;
import com.sun.enterprise.transaction.spi.RecoveryResourceListener;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.jts.CosTransactions.Configuration;
import com.sun.jts.CosTransactions.DelegatedRecoveryManager;
import com.sun.jts.CosTransactions.RecoveryManager;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Logger;

import javax.transaction.xa.XAResource;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;

/**
 * Resource recovery manager to recover transactions.
 *
 * @author Jagadish Ramu
 */
@Service
public class ResourceRecoveryManagerImpl implements PostConstruct, ResourceRecoveryManager {

    private static Logger _logger = LogDomains.getLogger(JavaEETransactionManagerSimplified.class, LogDomains.JTA_LOGGER);
    private static StringManager localStrings = StringManager.getManager(JavaEETransactionManagerSimplified.class);

    // Externally registered (ie not via habitat.getAllByContract) ResourceHandlers
    private static List<RecoveryResourceHandler> externallyRegisteredRecoveryResourceHandlers = new ArrayList<>();

    @Inject
    private ServiceLocator serviceLocator;

    private TransactionService transactionService;
    private JavaEETransactionManager eeTransactionManager;

    private Collection<RecoveryResourceHandler> recoveryResourceHandlers;

    private RecoveryResourceRegistry recoveryListenersRegistry;

    private volatile boolean lazyRecovery;
    private volatile boolean configured;

    public void postConstruct() {
        if (configured) {
            _logger.log(WARNING, "", new IllegalStateException());
            return;
        }

        // Recover XA resources if the auto-recovery flag in tx service is set to true
        recoverXAResources();
    }

    /**
     * recover incomplete transactions
     *
     * @param delegated indicates whether delegated recovery is needed
     * @param logPath transaction log directory path
     * @return boolean indicating the status of transaction recovery
     * @throws Exception when unable to recover
     */
    public boolean recoverIncompleteTx(boolean delegated, String logPath) throws Exception {
        return recoverIncompleteTx(delegated, logPath, ((delegated) ? null : Configuration.getPropertyValue(Configuration.INSTANCE_NAME)),
                false);
    }

    /**
     * recover incomplete transactions
     *
     * @param delegated indicates whether delegated recovery is needed
     * @param logPath transaction log directory path
     * @param instance the name of the instance for which delegated recovery is performed, null if unknown.
     * @param notifyRecoveryListeners specifies whether recovery listeners are to be notified
     * @return boolean indicating the status of transaction recovery
     * @throws Exception when unable to recover
     */
    public boolean recoverIncompleteTx(boolean delegated, String logPath, String instance, boolean notifyRecoveryListeners)
            throws Exception {
        boolean result = false;
        Map<RecoveryResourceHandler, Vector> handlerToXAResourcesMap = null;
        try {
            _logger.log(FINE, "Performing recovery of incomplete Tx...");

            configure();
            if (notifyRecoveryListeners) {
                beforeRecovery(delegated, instance);
            }

            Vector xaresList = new Vector();

            // TODO V3 will handle ThirdPartyXAResources also (v2 is not so). Is this fine ?
            handlerToXAResourcesMap = getAllRecoverableResources(xaresList);

            int size = xaresList.size();
            XAResource[] xaresArray = new XAResource[size];
            for (int i = 0; i < size; i++) {
                xaresArray[i] = (XAResource) xaresList.elementAt(i);
            }

            if (_logger.isLoggable(FINE)) {
                String msg = localStrings.getStringWithDefault("xaresource.recovering", "Recovering {0} XA resources...",
                        new Object[] { String.valueOf(size) });

                _logger.log(FINE, msg);
            }
            if (!delegated) {
                RecoveryManager.recoverIncompleteTx(xaresArray);
                result = true;
            } else {
                result = DelegatedRecoveryManager.delegated_recover(logPath, xaresArray);
            }

            return result;
        } catch (Exception ex1) {
            _logger.log(WARNING, "xaresource.recover_error", ex1);
            throw ex1;
        } finally {
            try {
                closeAllResources(handlerToXAResourcesMap);
            } catch (Exception ex1) {
                _logger.log(WARNING, "xaresource.recover_error", ex1);
            }
            if (notifyRecoveryListeners) {
                afterRecovery(result, delegated, instance);
            }
        }
    }

    /**
     * close all resources provided using their handlers
     *
     * @param resourcesToHandlers map that holds handlers and their resources
     */
    private void closeAllResources(Map<RecoveryResourceHandler, Vector> resourcesToHandlers) {
        if (resourcesToHandlers != null) {
            Set<Map.Entry<RecoveryResourceHandler, Vector>> entries = resourcesToHandlers.entrySet();
            for (Map.Entry<RecoveryResourceHandler, Vector> entry : entries) {
                RecoveryResourceHandler handler = entry.getKey();
                Vector resources = entry.getValue();
                handler.closeConnections(resources);
            }
        }
    }

    /**
     * get all recoverable resources
     *
     * @param xaresList xa resources
     * @return recovery-handlers and their resources
     */
    private Map<RecoveryResourceHandler, Vector> getAllRecoverableResources(Vector xaresList) {
        Map<RecoveryResourceHandler, Vector> resourcesToHandlers = new HashMap<RecoveryResourceHandler, Vector>();

        for (RecoveryResourceHandler handler : recoveryResourceHandlers) {
            // TODO V3 FINE LOG
            Vector resources = new Vector();
            handler.loadXAResourcesAndItsConnections(xaresList, resources);
            resourcesToHandlers.put(handler, resources);
        }
        return resourcesToHandlers;
    }

    /**
     * recover the xa-resources
     *
     * @param force boolean to indicate if it has to be forced.
     */
    public void recoverXAResources(boolean force) {
        if (force) {
            try {
                if (transactionService == null) {
                    transactionService = serviceLocator.getService(Config.class, ServerEnvironment.DEFAULT_INSTANCE_NAME)
                                                       .getExtensionByType(TransactionService.class);
                }
                if (!Boolean.valueOf(transactionService.getAutomaticRecovery())) {
                    return;
                }

                _logger.log(FINE, "ejbserver.recovery", "Perform recovery of XAResources...");

                configure();

                Vector xaresList = new Vector();
                Map<RecoveryResourceHandler, Vector> resourcesToHandler = getAllRecoverableResources(xaresList);

                int size = xaresList.size();
                XAResource[] xaresArray = new XAResource[size];
                for (int i = 0; i < size; i++) {
                    xaresArray[i] = (XAResource) xaresList.elementAt(i);
                }

                resourceRecoveryStarted();
                if (_logger.isLoggable(FINE)) {
                    String msg = localStrings.getStringWithDefault("xaresource.recovering", "Recovering {0} XA resources...",
                            new Object[] { String.valueOf(size) });

                    _logger.log(FINE, msg);
                }
                eeTransactionManager.recover(xaresArray);
                resourceRecoveryCompleted();

                closeAllResources(resourcesToHandler);
            } catch (Exception ex) {
                _logger.log(SEVERE, "xaresource.recover_error", ex);
            }
        }
    }

    /**
     * notifies the resource listeners that recovery has started
     */
    private void resourceRecoveryStarted() {
        for (RecoveryResourceListener listener : recoveryListenersRegistry.getListeners()) {
            listener.recoveryStarted();
        }
    }

    /**
     * notifies the resource listeners that recovery has completed
     */
    private void resourceRecoveryCompleted() {
        for (RecoveryResourceListener listeners : recoveryListenersRegistry.getListeners()) {
            listeners.recoveryCompleted();
        }
    }

    /**
     * notifies the event listeners that recovery is about to start
     */
    private void beforeRecovery(boolean delegated, String instance) {
        for (RecoveryEventListener listener : recoveryListenersRegistry.getEventListeners()) {
            try {
                listener.beforeRecovery(delegated, instance);
            } catch (Throwable e) {
                _logger.log(WARNING, "", e);
                _logger.log(WARNING, "jts.before_recovery_excep", e);
            }
        }
    }

    /**
     * notifies the event listeners that all recovery operations are completed
     */
    private void afterRecovery(boolean success, boolean delegated, String instance) {
        for (RecoveryEventListener listener : recoveryListenersRegistry.getEventListeners()) {
            try {
                listener.afterRecovery(success, delegated, instance);
            } catch (Throwable e) {
                _logger.log(WARNING, "", e);
                _logger.log(WARNING, "jts.after_recovery_excep", e);
            }
        }
    }

    /**
     * to enable lazy recovery, setting lazy to "true" will
     *
     * @param lazy boolean
     */
    public void setLazyRecovery(boolean lazy) {
        lazyRecovery = lazy;
    }

    /**
     * to recover xa resources
     */
    public void recoverXAResources() {
        recoverXAResources(!lazyRecovery);
    }

    private void configure() {
        if (configured) {
            return;
        }

        recoveryResourceHandlers = new ArrayList<>(serviceLocator.getAllServices(RecoveryResourceHandler.class));
        recoveryResourceHandlers.addAll(externallyRegisteredRecoveryResourceHandlers);
        eeTransactionManager = serviceLocator.getService(JavaEETransactionManager.class);
        recoveryListenersRegistry = serviceLocator.getService(RecoveryResourceRegistry.class);

        if (recoveryListenersRegistry == null) {
            throw new IllegalStateException();
        }

        RecoveryManager.startTransactionRecoveryFence();

        configured = true;
    }

    public static void registerRecoveryResourceHandler(final XAResource xaResource) {
        RecoveryResourceHandler recoveryResourceHandler = new RecoveryResourceHandler() {
            public void loadXAResourcesAndItsConnections(List xaresList, List connList) {
                xaresList.add(xaResource);
            }

            public void closeConnections(List connList) {
                ;
            }
        };
        externallyRegisteredRecoveryResourceHandlers.add(recoveryResourceHandler);
    }
}
