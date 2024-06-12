/*
 * Copyright (c) 2024 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.pool.mock;

import com.sun.enterprise.transaction.api.JavaEETransaction;
import com.sun.enterprise.transaction.api.XAResourceWrapper;
import com.sun.enterprise.transaction.spi.JavaEETransactionManagerDelegate;
import com.sun.enterprise.transaction.spi.TransactionalResource;
import jakarta.resource.spi.XATerminator;
import jakarta.resource.spi.work.WorkException;
import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.api.invocation.InvocationException;
import org.glassfish.api.invocation.ResourceHandler;

/**
 * Mock class without any implementation
 */
public class JavaEETransactionManagerMock implements com.sun.enterprise.transaction.api.JavaEETransactionManager {

    @Override
    public void begin() throws NotSupportedException, SystemException {
    }

    @Override
    public void commit()
            throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
    }

    @Override
    public int getStatus() throws SystemException {
        return 0;
    }

    @Override
    public Transaction getTransaction() throws SystemException {
        return null;
    }

    @Override
    public void resume(Transaction tobj) throws InvalidTransactionException, IllegalStateException, SystemException {
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {
    }

    @Override
    public Transaction suspend() throws SystemException {
        return null;
    }

    @Override
    public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException, SystemException {
    }

    @Override
    public boolean enlistResource(Transaction tran, TransactionalResource h) throws RollbackException, IllegalStateException, SystemException {
        return false;
    }

    @Override
    public boolean delistResource(Transaction tran, TransactionalResource h, int flag) throws IllegalStateException, SystemException {
        return false;
    }

    @Override
    public void enlistComponentResources() throws RemoteException {
    }

    @Override
    public void delistComponentResources(boolean suspend) throws RemoteException {
    }

    @Override
    public void componentDestroyed(Object instance, ComponentInvocation inv) {
    }

    @Override
    public void componentDestroyed(Object instance) {
    }

    @Override
    public void componentDestroyed(ResourceHandler rh) {
    }

    @Override
    public void preInvoke(ComponentInvocation prev) throws InvocationException {
    }

    @Override
    public void postInvoke(ComponentInvocation curr, ComponentInvocation prev) throws InvocationException {
    }

    @Override
    public void setDefaultTransactionTimeout(int seconds) {
    }

    @Override
    public void cleanTxnTimeout() {
    }

    @Override
    public List getExistingResourceList(Object instance, ComponentInvocation inv) {
        return null;
    }

    @Override
    public void registerComponentResource(TransactionalResource h) {
    }

    @Override
    public void unregisterComponentResource(TransactionalResource h) {
    }

    @Override
    public void recover(XAResource[] resourceList) {
    }

    @Override
    public void initRecovery(boolean force) {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void begin(int timeout) throws NotSupportedException, SystemException {
    }

    @Override
    public boolean isNullTransaction() {
        return false;
    }

    @Override
    public void checkTransactionExport(boolean isLocal) {
    }

    @Override
    public void checkTransactionImport() {
    }

    @Override
    public boolean isTimedOut() {
        return false;
    }

    @Override
    public ArrayList getActiveTransactions() {
        return null;
    }

    @Override
    public void forceRollback(String txnId) throws IllegalStateException, SystemException {
    }

    @Override
    public void setMonitoringEnabled(boolean enabled) {
    }

    @Override
    public void freeze() {
    }

    @Override
    public void unfreeze() {
    }

    @Override
    public boolean isFrozen() {
        return false;
    }

    @Override
    public void recreate(Xid xid, long timeout) throws WorkException {
    }

    @Override
    public void release(Xid xid) throws WorkException {
    }

    @Override
    public XATerminator getXATerminator() {
        return null;
    }

    @Override
    public void setDelegate(JavaEETransactionManagerDelegate delegate) {
    }

    @Override
    public JavaEETransaction getCurrentTransaction() {
        return null;
    }

    @Override
    public void setCurrentTransaction(JavaEETransaction tx) {
    }

    @Override
    public XAResourceWrapper getXAResourceWrapper(String clName) {
        return null;
    }

    @Override
    public void handlePropertyUpdate(String name, Object value) {
    }

    @Override
    public boolean recoverIncompleteTx(boolean delegated, String logPath, XAResource[] xaresArray) throws Exception {
        return false;
    }

    @Override
    public List getResourceList(Object instance, ComponentInvocation inv) {
        return null;
    }

    @Override
    public void clearThreadTx() {
    }

    @Override
    public String getTxLogLocation() {
        return null;
    }

    @Override
    public void registerRecoveryResourceHandler(XAResource xaResource) {
    }

    @Override
    public int getPurgeCancelledTtransactionsAfter() {
        return 0;
    }

    @Override
    public void setPurgeCancelledTtransactionsAfter(int value) {
    }
}
