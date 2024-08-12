/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.transaction;

import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.JavaEETransactionManager;
import com.sun.enterprise.transaction.api.TransactionAdminBean;
import com.sun.enterprise.transaction.api.XAResourceWrapper;
import com.sun.enterprise.transaction.spi.JavaEETransactionManagerDelegate;
import com.sun.enterprise.transaction.spi.TransactionInternal;
import com.sun.enterprise.transaction.spi.TransactionalResource;
import com.sun.enterprise.util.i18n.StringManager;

import jakarta.resource.spi.XATerminator;
import jakarta.resource.spi.work.WorkException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import org.glassfish.hk2.api.PostConstruct;
import org.jvnet.hk2.annotations.Service;

/**
 ** Implementation of JavaEETransactionManagerDelegate that supports only local transactions with a single non-XA
 * resource.
 *
 * @author Marina Vatkina
 */
@Service
public class JavaEETransactionManagerSimplifiedDelegate implements JavaEETransactionManagerDelegate, PostConstruct {

    // @Inject
    private JavaEETransactionManager tm;

    // Sting Manager for Localization
    private static StringManager sm = StringManager.getManager(JavaEETransactionManagerSimplified.class);

    private Logger _logger;

    private boolean lao;

    private static final ReentrantReadWriteLock.ReadLock readLock = new ReentrantReadWriteLock().readLock();

    private final Semaphore writeLock = new Semaphore(1, true);

    public JavaEETransactionManagerSimplifiedDelegate() {
    }

    @Override
    public void postConstruct() {
    }

    @Override
    public boolean useLAO() {
        return lao;
    }

    @Override
    public void setUseLAO(boolean b) {
        lao = b;
    }

    /**
     * Throws an exception if called as it means that there is no active local transaction to commit.
     */
    @Override
    public void commitDistributedTransaction() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        throw new IllegalStateException(sm.getString("enterprise_distributedtx.transaction_notactive"));
    }

    /**
     * Throws an exception if called as it means that there is no active local transaction to rollback.
     */
    @Override
    public void rollbackDistributedTransaction() throws IllegalStateException, SecurityException, SystemException {
        throw new IllegalStateException(sm.getString("enterprise_distributedtx.transaction_notactive"));
    }

    @Override
    public int getStatus() throws SystemException {
        JavaEETransaction tx = tm.getCurrentTransaction();
        if (tx != null && tx.isLocalTx())
            return tx.getStatus();
        else
            return jakarta.transaction.Status.STATUS_NO_TRANSACTION;
    }

    @Override
    public Transaction getTransaction() throws SystemException {
        return tm.getCurrentTransaction();
    }

    @Override
    public JavaEETransaction getJavaEETransaction(Transaction t) {
        if (t instanceof JavaEETransaction) {
            return (JavaEETransaction) t;
        }

        throw new IllegalStateException(sm.getString("enterprise_distributedtx.nonxa_usein_jts"));

    }

    @Override
    public boolean enlistDistributedNonXAResource(Transaction tran, TransactionalResource h) throws RollbackException, IllegalStateException, SystemException {
        throw new IllegalStateException(sm.getString("enterprise_distributedtx.nonxa_usein_jts"));
    }

    @Override
    public boolean enlistLAOResource(Transaction tran, TransactionalResource h) throws RollbackException, IllegalStateException, SystemException {
        return false;
    }

    /**
     * Throws an exception if called as it means that there is no active local transaction.
     */
    @Override
    public void setRollbackOnlyDistributedTransaction() throws IllegalStateException, SystemException {
        throw new IllegalStateException(sm.getString("enterprise_distributedtx.transaction_notactive"));
    }

    @Override
    public Transaction suspend(JavaEETransaction tx) throws SystemException {
        if (tx != null) {
            tm.setCurrentTransaction(null);
        }

        return tx;
    }

    @Override
    public void resume(Transaction tx) throws InvalidTransactionException, IllegalStateException, SystemException {
        /** XXX Throw an exception ??? The process should happen in the caller. XXX **/
    }

    @Override
    public void removeTransaction(Transaction tx) {
    }

    @Override
    public int getOrder() {
        return 1;
    }

    @Override
    public void setTransactionManager(JavaEETransactionManager tm) {
        this.tm = tm;
        _logger = ((JavaEETransactionManagerSimplified) tm).getLogger();
    }

    @Override
    public TransactionInternal startJTSTx(JavaEETransaction t, boolean isAssociatedTimeout) throws RollbackException, IllegalStateException, SystemException {
        throw new UnsupportedOperationException("startJTSTx");
    }

    @Override
    public boolean supportsXAResource() {
        return false;
    }

    @Override
    public void initRecovery(boolean force) {
        // No-op. Always called on server startup
    }

    @Override
    public void recover(XAResource[] resourceList) {
        throw new UnsupportedOperationException("recover");
    }

    @Override
    public XATerminator getXATerminator() {
        throw new UnsupportedOperationException("getXATerminator");
    }

    @Override
    public void release(Xid xid) throws WorkException {
        throw new UnsupportedOperationException("release");
    }

    @Override
    public void recreate(Xid xid, long timeout) throws WorkException {
        throw new UnsupportedOperationException("recreate");
    }

    @Override
    public boolean recoverIncompleteTx(boolean delegated, String logPath, XAResource[] xaresArray) throws Exception {
        throw new UnsupportedOperationException("recoverIncompleteTx");
    }

    @Override
    public XAResourceWrapper getXAResourceWrapper(String clName) {
        return null;
    }

    @Override
    public void handlePropertyUpdate(String name, Object value) {
    }

    @Override
    public Lock getReadLock() {
        return readLock;
    }

    @Override
    public boolean isWriteLocked() {
        return (writeLock.availablePermits() == 0);
    }

    @Override
    public void acquireWriteLock() {
        try {
            writeLock.acquire();
        } catch (InterruptedException ie) {
            _logger.log(Level.FINE, "Error in acquireReadLock", ie);
        }
    }

    @Override
    public void releaseWriteLock() {
        writeLock.release();
    }

    /**
     * Return false as this delegate doesn't support tx interop.
     */
    @Override
    public boolean isNullTransaction() {
        return false;
    }

    @Override
    public TransactionAdminBean getTransactionAdminBean(Transaction tran) throws jakarta.transaction.SystemException {
        return ((JavaEETransactionManagerSimplified) tm).getTransactionAdminBean(tran);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getTxLogLocation() {
        throw new UnsupportedOperationException("getTxLogLocation");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void registerRecoveryResourceHandler(XAResource xaResource) {
        throw new UnsupportedOperationException("registerRecoveryResourceHandler");
    }
}
