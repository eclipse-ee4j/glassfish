/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package org.glassfish.concurrent.runtime;

import com.sun.enterprise.transaction.api.JavaEETransactionManager;

import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.SystemException;
import jakarta.transaction.Transaction;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.enterprise.concurrent.spi.TransactionHandle;
import org.glassfish.enterprise.concurrent.spi.TransactionSetupProvider;

public class TransactionSetupProviderImpl implements TransactionSetupProvider {

    private static final long serialVersionUID = 2278968807330359713L;

    private transient JavaEETransactionManager transactionManager;
    private final boolean keepTransactionUnchanged;
    private final boolean clearTransaction;

    public TransactionSetupProviderImpl(
        JavaEETransactionManager transactionManager,
        boolean keepTransactionUnchanged,
        boolean clearTransaction
    ) {
        this.transactionManager = transactionManager;
        this.keepTransactionUnchanged = keepTransactionUnchanged;
        this.clearTransaction = clearTransaction;
    }

    @Override
    public TransactionHandle beforeProxyMethod(String transactionExecutionProperty) {
        if (keepTransactionUnchanged) {
            return null;
        }
        if (!clearTransaction && ManagedTask.USE_TRANSACTION_OF_EXECUTION_THREAD.equals(transactionExecutionProperty)) {
            return null;
        }
        try {
            Transaction suspendedTxn = transactionManager.suspend();
            return new TransactionHandleImpl(suspendedTxn);
        } catch (SystemException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, "", e);
            return null;
        }
    }

    @Override
    public void afterProxyMethod(TransactionHandle handle, String transactionExecutionProperty) {
        // resume transaction if any
        if (handle instanceof TransactionHandleImpl) {
            Transaction suspendedTxn = ((TransactionHandleImpl)handle).getTransaction();
            if (suspendedTxn != null) {
                try {
                    transactionManager.resume(suspendedTxn);
                } catch (InvalidTransactionException | SystemException e) {
                    Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.toString());
                }
            }
        }
    }

    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
    }

    private void readObject(java.io.ObjectInputStream in) throws ClassNotFoundException, IOException {
        in.defaultReadObject();
        // re-initialize these fields
        ConcurrentRuntime concurrentRuntime = ConcurrentRuntime.getRuntime();
        transactionManager = concurrentRuntime.getTransactionManager();
    }

}
