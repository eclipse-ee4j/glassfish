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

package com.sun.enterprise.transaction.spi;

import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.api.TransactionAdminBean;
import com.sun.enterprise.transaction.api.XAResourceWrapper;

import jakarta.resource.spi.XATerminator;
import jakarta.resource.spi.work.WorkException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import java.util.concurrent.locks.Lock;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.jvnet.hk2.annotations.Contract;

@Contract
public interface JavaEETransactionManagerDelegate {

    /**
     * Returns <code>true</code> if this implementation supports
     * last agent optimization.
     */
    boolean useLAO();

    /**
     * Reset LAO value.
     *
     * @param b the boolean value for the LAO flag.
     */
    void setUseLAO(boolean b);

    /**
     * Commit distributed transaction if there is any.
     */
    void commitDistributedTransaction() throws RollbackException,
            HeuristicMixedException, HeuristicRollbackException,
            SecurityException,
            IllegalStateException, SystemException;

    /**
     * Rollback distributed transaction if there is any.
     */
    void rollbackDistributedTransaction() throws IllegalStateException,
            SecurityException, SystemException;

    /**
     * Get implementation specific status of the transaction associated with
     * the current thread.
     *
     * @return the status value as an int.
     */
    int getStatus() throws SystemException;

    /**
     * Get implementation specific transaction object that represents the transaction
     * context of the calling thread.
     *
     * @return the transaction object.
     */
    Transaction getTransaction() throws SystemException;

    /**
     * Get local transaction object that corresponds to this transaction instance.
     *
     * @return the transaction object.
     */
    JavaEETransaction getJavaEETransaction(Transaction t);

    /**
     * Perform implementation specific steps to enlist a non-XA resource
     * with a distribute transaction.
     *
     * @param tran the Transaction object to be used to enlist the resource.
     * @param h the TransactionalResource object to be enlisted.
     * @return <code>true</code> if the resource was enlisted successfully.
     */
    boolean enlistDistributedNonXAResource(
            Transaction tran, TransactionalResource h)
            throws RollbackException, IllegalStateException, SystemException;

    /**
     * Perform implementation specific steps to enlist resource as a LAO.
     *
     * @param tran the Transaction object to be used to enlist the resource.
     * @param h the TransactionalResource object to be enlisted.
     * @return <code>true</code> if the resource was enlisted successfully.
     */
    boolean enlistLAOResource(Transaction tran, TransactionalResource h)
        throws RollbackException, IllegalStateException, SystemException;

    /**
     * Perform implementation specific steps to set setRollbackOnly status
     * for distributed transaction if there is any.
     */
    void setRollbackOnlyDistributedTransaction()
            throws IllegalStateException, SystemException;

    /**
     * Perform implementation specific steps to suspend a JavaEETransaction.
     *
     * @param tx the JavaEETransaction object to be suspend.
     * @return Transaction object representing the suspended transaction.
     */
    Transaction suspend(JavaEETransaction tx) throws SystemException;

    /**
     * Perform implementation specific steps to resume a Transaction.
     *
     * @param tx the Transaction object that represents the transaction to be resumed.
     */
    void resume(Transaction tx)
            throws InvalidTransactionException, IllegalStateException,
            SystemException;

    /**
     * Remove the Transaction object from the cache.
     *
     * @param tx the Transaction object to be removed.
     */
    void removeTransaction(Transaction tx);

    /**
     * The delegate with the largest order will be used.
     *
     * @return the order in which this delegate should be used.
     */
    int getOrder();

    /**
     * Set the JavaEETransactionManager reference.
     *
     * @param tm the JavaEETransactionManager object.
     */
    void setTransactionManager(JavaEETransactionManager tm);

    /**
     * Returns <code>true</code> if this delegate supports XA resources.
     */
    boolean supportsXAResource();

    /**
     * Start new JTS transaction for the existing local transaction object.
     *
     * @param t the JavaEETransaction object.
     * @param isAssociatedTimeout <code>true</code> if transaction has a timeout
     * associated with it.
     * @return the new JTS Transaction instance.
     */
    TransactionInternal startJTSTx(JavaEETransaction t, boolean isAssociatedTimeout)
            throws RollbackException, IllegalStateException, SystemException;

    /**
     * Recover an array of XAResource objects for a failed XA transaction.
     *
     * @param resourceList the array of XAResource objects to recover.
     * @throws UnsupportedOperationException if a delegate doesn't support
     * this functionality.
     */
    void recover(XAResource[] resourceList);

    /**
     * Initialize recovery framework.
     *
     * Is a no-op if a delegate doesn't support this functionality.
     */
    void initRecovery(boolean force);

    /**
     * This is used by importing transactions via the Connector contract.
     * Should not be called
     *
     * @return a <code>XATerminator</code> instance.
     * @throws UnsupportedOperationException if a delegate doesn't support
     * this functionality.
     */
    XATerminator getXATerminator();

    /**
     * Release a transaction. This call causes the calling thread to be
     * dissociated from the specified transaction. <p>
     * This is used by importing transactions via the Connector contract.
     *
     * @param xid the Xid object representing a transaction.
     * @throws UnsupportedOperationException if a delegate doesn't support
     * this functionality.
     */
    void release(Xid xid) throws WorkException;

    /**
     * Recreate a transaction based on the Xid. This call causes the calling
     * thread to be associated with the specified transaction. <p>
     * This is used by importing transactions via the Connector contract.
     *
     * @param xid the Xid object representing a transaction.
     * @param timeout the timeout for the transaction to be recreated.
     * @throws UnsupportedOperationException if a delegate doesn't support
     * this functionality.
     */
    void recreate(Xid xid, long timeout) throws WorkException;

    /**
     * Returns an instance of an XAResourceWrapper if this delegate supports
     * transaction recovery and there is a wrapper available for this class name.
     * Returns <code>null</code> otherwise.
     *
     *
     * @return an instance of an XAResourceWrapper or <code>null</code> if this
     * delegate doesn't support transaction recovery or a wrapper is not available.
     */
    XAResourceWrapper getXAResourceWrapper(String clName);

    /**
     * Handle configuration change.
     *
     * @param name the name of the configuration property.
     * @param value the ne value of the configuration.
     */
    void handlePropertyUpdate(String name, Object value);

    /**
     * Recover the populated array of XAResource if this delegate supports
     * transaction recovery.
     *
     * @param delegated <code>true</code> if the recovery process is owned by this instance.
     * @param logPath the name of the transaction logging directory
     * @param xaresArray the array of XA Resources to be recovered.
     * @return true if the recovery has been successful.
     */
    boolean recoverIncompleteTx(boolean delegated, String logPath,
            XAResource[] xaresArray) throws Exception;

    /**
     * Return the delegate specific read lock that implements Lock interface.
     *
     */
    Lock getReadLock();

    /**
     * Return <code>true</code> if the delegate had its write lock acquired.
     */
    boolean isWriteLocked();

    /**
     * Allows the delegate to acquire a write lock.
     */
    void acquireWriteLock();

    /**
     * Allows the delegate to release a write lock.
     */
    void releaseWriteLock();

    /**
     * Return the delegate specific implementation when a "null transaction context"
     * was received from the client. See EJB2.0 spec section 19.6.2.1.
     * A null tx context has no Coordinator objref. It indicates
     * that the client had an active
     * tx but the client container did not support tx interop.
     */
    boolean isNullTransaction();

    /**
     * Return TransactionAdminBean with delegate specific implementation details
     * of an active Transaction.
     */
    TransactionAdminBean getTransactionAdminBean(Transaction t)
            throws SystemException;

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
}
