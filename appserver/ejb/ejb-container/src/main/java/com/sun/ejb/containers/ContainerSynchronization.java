/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejb.containers;

import com.sun.logging.LogDomains;

import jakarta.ejb.EJBException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.ejb.config.EjbContainerAvailability;


/**
 * This class intercepts Synchronization notifications from
 * the TransactionManager and forwards them to the appropriate container.
 * There is one ContainerSynchronization instance per tx.
 * All bean instances (of all types) participating in the tx must be
 * added by the container to the ContainerSynchronization, so that
 * the beans can be called during before/afterCompletion.
 *
 * This class also provides special methods for PersistenceManager Sync and
 * Timer objects which must be called AFTER the containers during
 * before/afterCompletion.
 *
 */
final class ContainerSynchronization implements Synchronization
{

    private static final Logger _logger =
        LogDomains.getLogger(ContainerSynchronization.class, LogDomains.EJB_LOGGER);

    private ArrayList beans = new ArrayList();
    private Vector pmSyncs = new Vector();

    private Hashtable timerSyncs = new Hashtable();

    private Transaction tx; // the tx with which this Sync was registered
    private EjbContainerUtil ejbContainerUtilImpl;

    SFSBTxCheckpointCoordinator sfsbTxCoordinator;

    // Note: this must be called only after a Tx is begun.
    ContainerSynchronization(Transaction tx, EjbContainerUtil ejbContainerUtilImpl) {
        this.tx = tx;
        this.ejbContainerUtilImpl = ejbContainerUtilImpl;
    }


    Vector getBeanList() {
        Vector vec = new Vector();
        for (Iterator iter = beans.iterator(); iter.hasNext();) {
            vec.add(iter.next());
        }
        return vec;
    }

    void addBean(EJBContextImpl bean)
    {
        beans.add(bean);
    }

    void removeBean(EJBContextImpl bean)
    {
        beans.remove(bean);
    }

    void addPMSynchronization(Synchronization sync)
    {
        pmSyncs.add(sync);
    }

    // Set synchronization object for a particular timer.
    void addTimerSynchronization(TimerPrimaryKey timerId, Synchronization sync)
    {
        timerSyncs.put(timerId, sync);
    }

    // Might be null if no timer synch object for this timerId in this tx
    Synchronization getTimerSynchronization(TimerPrimaryKey timerId) {
        return (Synchronization) timerSyncs.get(timerId);
    }

    void removeTimerSynchronization(TimerPrimaryKey timerId) {
        timerSyncs.remove(timerId);
    }

    public void beforeCompletion()
    {
        // first call beforeCompletion for each bean instance
        for (int i = 0; i < beans.size(); i++) {
            EJBContextImpl context = (EJBContextImpl)beans.get(i);
            BaseContainer container = (BaseContainer)context.getContainer();
            try {
                if( container != null ) {
                    boolean allowTxCompletion = true;
                    if (container.isUndeployed()) {
                        if (context instanceof SessionContextImpl) {
                            allowTxCompletion = ((SessionContextImpl) context).getInLifeCycleCallback();
                        } else {
                            allowTxCompletion = false;
                            _logger.log(Level.WARNING, "Marking Tx for rollback "
                                + " because container for " + container
                                + " is undeployed");
                        }
                    }

                    if (!allowTxCompletion) {
                        try {
                            tx.setRollbackOnly();
                        } catch (SystemException sysEx) {
                            _logger.log(Level.FINE, "Error while trying to "
                                + "mark for rollback", sysEx);
                        }
                    } else {
                        container.beforeCompletion(context);
                    }
                } else {
                    // Might be null if bean was removed.  Just skip it.
                    _logger.log(Level.FINE, "context with empty container in " +
                        " ContainerSynchronization.beforeCompletion");
                }
            } catch ( RuntimeException ex ) {
                logAndRollbackTransaction(ex);
                throw ex;
            } catch ( Exception ex ) {
                logAndRollbackTransaction(ex);
                // no need to call remaining beforeCompletions
                throw new EJBException("Error during beforeCompletion.", ex);
            }
        }

        // now call beforeCompletion for all pmSyncs
        for (int i = 0; i < pmSyncs.size(); i++) {
            Synchronization sync = (Synchronization)pmSyncs.elementAt(i);
            try {
                sync.beforeCompletion();
            } catch ( RuntimeException ex ) {
                logAndRollbackTransaction(ex);
                throw ex;
            } catch ( Exception ex ) {
                logAndRollbackTransaction(ex);
                // no need to call remaining beforeCompletions
                throw new EJBException("Error during beforeCompletion.", ex);
            }
        }
    }

    private void logAndRollbackTransaction(Exception ex)
    {
        // rollback the Tx. The client will get
        // a EJB/RemoteException or a TransactionRolledbackException.
        _logger.log(Level.SEVERE,"ejb.remote_or_txnrollback_exception",ex);
        try {
            tx.setRollbackOnly();
        } catch ( SystemException e ) {
            _logger.log(Level.FINE, "", ex);
        }
    }

    public void afterCompletion(int status)
    {
        for ( int i=0; i<pmSyncs.size(); i++ ) {
            Synchronization sync = (Synchronization)pmSyncs.elementAt(i);
            try {
                sync.afterCompletion(status);
            } catch ( Exception ex ) {
                _logger.log(Level.SEVERE, "ejb.after_completion_error", ex);
            }
        }

        // call afterCompletion for each bean instance
        for ( int i=0; i<beans.size();i++  ) {
            EJBContextImpl context = (EJBContextImpl)beans.get(i);
            BaseContainer container = (BaseContainer)context.getContainer();
            try {
                if( container != null ) {
                    container.afterCompletion(context, status);
                } else {
                    // Might be null if bean was removed.  Just skip it.
                    _logger.log(Level.FINE, "context with empty container in "
                                +
                                " ContainerSynchronization.afterCompletion");
                }
            } catch ( Exception ex ) {
                _logger.log(Level.SEVERE, "ejb.after_completion_error", ex);
            }
        }

        if (sfsbTxCoordinator != null) {
            sfsbTxCoordinator.doTxCheckpoint();
        }

        for ( Iterator iter = timerSyncs.values().iterator();
              iter.hasNext(); ) {
            Synchronization timerSync = (Synchronization) iter.next();
            try {
                timerSync.afterCompletion(status);
            } catch ( Exception ex ) {
                _logger.log(Level.SEVERE, "ejb.after_completion_error", ex);
            }
        }

        // tell ejbContainerUtilImpl to remove this tx/sync from its table
        ejbContainerUtilImpl.removeContainerSync(tx);
    }

    void registerForTxCheckpoint(SessionContextImpl sessionCtx) {
        //No need to synchronize
        if (sfsbTxCoordinator == null) {
            String sfsbHaPersistenceTypeFromConfig = "jdbc/hastore";
            EjbContainerAvailability ejbContainerAvailability = ejbContainerUtilImpl.getServices().
                    getService(EjbContainerAvailability.class);
            if (ejbContainerAvailability != null) {
                sfsbHaPersistenceTypeFromConfig = ejbContainerAvailability.getSfsbStorePoolName();
            }
            sfsbTxCoordinator = new SFSBTxCheckpointCoordinator(sfsbHaPersistenceTypeFromConfig);
        }

        sfsbTxCoordinator.registerContext(sessionCtx);
    }
}
