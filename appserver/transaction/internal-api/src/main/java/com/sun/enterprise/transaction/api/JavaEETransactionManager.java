/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.transaction.api;

import com.sun.enterprise.transaction.spi.JavaEETransactionManagerDelegate;
import com.sun.enterprise.transaction.spi.TransactionalResource;

import jakarta.resource.spi.XATerminator;
import jakarta.resource.spi.work.WorkException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import jakarta.transaction.TransactionManager;

import java.rmi.RemoteException;
import java.util.List;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationException;
import org.glassfish.api.invocation.ResourceHandler;
import org.jvnet.hk2.annotations.Contract;

/**
 * Manages transactions, acting as a gateway to the TM state machine.
 *
 * @author Tony Ng
 */
@Contract
public interface JavaEETransactionManager extends TransactionManager {

    /**
     * Register a synchronization object with the transaction
     * associated with the current thread
     *
     * @param sync the synchronization object
     *
     * @exception IllegalStateException Thrown if the transaction in the
     *    target object is in prepared state or the transaction is inactive.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     *
     */
    void registerSynchronization(Synchronization sync)
        throws RollbackException, IllegalStateException, SystemException;


    /**
     * Enlist the resource specified with the transaction
     *
     * @param tran The transaction object
     * @param h The resource handle object
     * @return <i>true</i> if the resource was enlisted successfully; otherwise false.
     *
     * @exception RollbackException Thrown to indicate that
     *    the transaction has been marked for rollback only.
     *
     * @exception IllegalStateException Thrown if the transaction in the
     *    target object is in prepared state or the transaction is inactive.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     *
     */
    boolean enlistResource(Transaction tran,
                                  TransactionalResource h)
        throws RollbackException,
               IllegalStateException, SystemException;

    /**
     * Delist the resource specified from the transaction
     *
     * @param tran The transaction object
     * @param h The resource handle object
     * @param flag One of the values of TMSUCCESS, TMSUSPEND, or TMFAIL.
     *
     * @exception IllegalStateException Thrown if the transaction in the
     *    target object is inactive.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition
     *
     */
    boolean delistResource(Transaction tran,
                                  TransactionalResource h,
                                  int flag)
        throws IllegalStateException, SystemException;

    /**
     * This is called by the Container to ask the Transaction
     * Manager to enlist all resources held by a component and
     * to associate the current Transaction with the current
     * Invocation
     * The TM finds the component through the InvocationManager
     */
    void enlistComponentResources() throws RemoteException;

    /**
     * This is called by the Container to ask the Transaction
     * Manager to delist all resources held by a component
     *
     * The TM finds the component through the InvocationManager
     *
     * @param suspend true if the resources should be delisted
     * with TMSUSPEND flag; false otherwise
     *
     */
    void delistComponentResources(boolean suspend)
        throws RemoteException;

    /**
     * This is called by Container to indicate that a component
     * is being destroyed. All resources registered in the context
     * should be released. The ComponentInvocation will be used for
     * callback to calculate the resource table key.
     *
     * @param instance The component instance
     * @param inv The ComponentInvocation
     */
    void componentDestroyed(Object instance, ComponentInvocation inv);

    /**
     * This is called by Container to indicate that a component
     * is being destroyed. All resources registered in the context
     * should be released
     *
     * @param instance The component instance
     */
    void componentDestroyed(Object instance);

    /**
     * This is called by Container to indicate that a component
     * is being destroyed. All resources registered with this ResourceHandler
     * should be released.
     *
     * @param rh The ResourceHandler
     */
    void componentDestroyed(ResourceHandler rh);

    /**
     * Called by InvocationManager
     */

    void preInvoke(ComponentInvocation prev)
    throws InvocationException;

    /**
     * Called by InvocationManager
     */

    void postInvoke(ComponentInvocation curr, ComponentInvocation prev)
    throws InvocationException;

    void setDefaultTransactionTimeout(int seconds);
    void cleanTxnTimeout(); // clean up thread specific timeout
    /**
     * Returns a list of resource handles held by the component
     */

    List getExistingResourceList(Object instance, ComponentInvocation inv);

    void registerComponentResource(TransactionalResource h);

    void unregisterComponentResource(TransactionalResource h);

    void recover(XAResource[] resourceList);

    /**
     * Initialize recovery framework
     * @param force if true, forces initialization, otherwise relies on the TimerService
     * configuration.
     */
    void initRecovery(boolean force);

    /**
     * Perform shutdown cleanup.
     */
    void shutdown();

    void begin(int timeout)
        throws NotSupportedException, SystemException;

    /**
     * Return true if a "null transaction context" was received
     * from the client or if the server's transaction.interoperability
     * flag is false.
     * A null tx context indicates that the client had an active
     * tx but the client container did not support tx interop.
     * See EJB2.0 spec section 18.5.2.1.
     */
    boolean isNullTransaction();

    /**
     * Perform checks during export of a transaction on a remote call.
     */
    void checkTransactionExport(boolean isLocal);

    /**
     * Perform checks during import of a transaction on a remote call.
     * This is called from the reply interceptors after a remote call completes.
     */
    void checkTransactionImport();


    /**
     * Utility for the ejb container to check if the transaction is marked for
     * rollback because of timeout. This is applicable only for local transactions
     * as jts transaction will rollback instead of setting the txn for rollback
     */
    boolean isTimedOut();

    // START IASRI 4662745

    /*
     * Returns the list of ActiveTransactions. Called by Admin framework
     *  The ArrayList contains TransactionAdminBean
     */
    java.util.ArrayList getActiveTransactions();

    /*
     * Called by Admin Framework. Forces the given transaction to be rolled back
     */
    void forceRollback(String txnId) throws IllegalStateException, SystemException;

    /*
     * Called by Admin Framework.
     */
    void setMonitoringEnabled(boolean enabled);

    /*
     * Called by Admin Framework.
     */
    void freeze();
    /*
     * Called by Admin Framework.
     */
    void unfreeze();

    /*
     * Called by Admin Framework
     */
    boolean isFrozen();

    // END IASRI 4662745


   /**
     * recreate a transaction based on the Xid. This call causes the calling
     * thread to be associated with the specified transaction. <p>
     * This is used by importing transactions via the Connector contract.
     *
     * @param xid the Xid object representing a transaction.
     */
    void recreate(Xid xid, long timeout) throws WorkException ;

    /**
     * Release a transaction. This call causes the calling thread to be
     * dissociated from the specified transaction. <p>
     * This is used by importing transactions via the Connector contract.
     *
     * @param xid the Xid object representing a transaction.
     */
    void release(Xid xid) throws WorkException ;

    /**
     * Provides a handle to a <code>XATerminator</code> instance. The
     * <code>XATerminator</code> instance could be used by a resource adapter
     * to flow-in transaction completion and crash recovery calls from an EIS.
     * <p>
     * This is used by importing transactions via the Connector contract.
     *
     * @return a <code>XATerminator</code> instance.
     */
    XATerminator getXATerminator() ;

    /**
     * Explicitly set the JavaEETransactionManagerDelegate instance
     * for implementation-specific callbacks.
     *
     * @param delegate the JavaEETransactionManagerDelegate instance.
     */
    void setDelegate(JavaEETransactionManagerDelegate delegate);

    /**
     *
     * Return JavaEETransaction instance associated with the current thread.
     *
     * @return the JavaEETransaction associated with the current thread or null
     * if it there is none.
     */
    JavaEETransaction getCurrentTransaction();

    /**
     *
     * Update JavaEETransaction associated with the current thread.
     *
     * @param tx the JavaEETransaction associated with the current thread or null
     * if the existing transaction had been completed.
     */
    void setCurrentTransaction(JavaEETransaction tx);

    /**
     *
     * Return XAResourceWrapper instance specific to this datasource class name
     * that can be used instead of the driver provided version for transaction recovery.
     *
     * @param clName the class name of a datasource.
     * @return the XAResourceWrapper instance specific to this datasource class
     * name or null if there is no special wrapper available.
     */
    XAResourceWrapper getXAResourceWrapper(String clName);

    /**
     * Handle configuration change. Actual change will be performed by the delegate.
     *
     * @param name the name of the configuration property.
     * @param value the ne value of the configuration.
     */
    void handlePropertyUpdate(String name, Object value);

    /**
     * Called by the ResourceRecoveryManager to recover the populated
     * array of XAResource.
     *
     * @param delegated <code>true</code> if the recovery process is owned by this instance.
     * @param logPath the name of the transaction logging directory
     * @param xaresArray the array of XA Resources to be recovered.
     * @return true if the recovery has been successful.
     */
    boolean recoverIncompleteTx(boolean delegated, String logPath,
            XAResource[] xaresArray) throws Exception;

    /**
     * get the resources being used in the calling component's invocation context
     * @param instance calling component instance
     * @param inv Calling component's invocation information
     * @return List of resources
     */
    List getResourceList(Object instance, ComponentInvocation inv);

    /**
     * Clears the transaction associated with the caller thread
     */
    void clearThreadTx();

    /**
     * Return location of transaction logs
     *
     * @return String location of transaction logs
     */
    String getTxLogLocation();

    /**
     * Allows an arbitrary XAResource to register for recovery
     *
     * @param xaResource XAResource to register for recovery
     */
    void registerRecoveryResourceHandler(XAResource xaResource);

    /**
     * Returns the value to be used to purge transaction tasks after the specified number of cancelled tasks
     */
    int getPurgeCancelledTtransactionsAfter();

    /**
     * Allows to purge transaction tasks after the specified value of cancelled tasks
     */
    void setPurgeCancelledTtransactionsAfter(int value);


}
