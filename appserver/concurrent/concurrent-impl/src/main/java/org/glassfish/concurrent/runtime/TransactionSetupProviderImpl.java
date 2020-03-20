/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.enterprise.concurrent.spi.TransactionHandle;
import org.glassfish.enterprise.concurrent.spi.TransactionSetupProvider;

import jakarta.enterprise.concurrent.ManagedTask;
import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionSetupProviderImpl implements TransactionSetupProvider {

    private transient JavaEETransactionManager transactionManager;

    static final long serialVersionUID = -856400645253308289L;

    public TransactionSetupProviderImpl(JavaEETransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public TransactionHandle beforeProxyMethod(String transactionExecutionProperty) {
        // suspend current transaction if not using transaction of execution thread
        if (! ManagedTask.USE_TRANSACTION_OF_EXECUTION_THREAD.equals(transactionExecutionProperty)) {
            try {
                Transaction suspendedTxn = transactionManager.suspend();
                return new TransactionHandleImpl(suspendedTxn);
            } catch (SystemException e) {
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, e.toString());
            }
        }
        return null;
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

    private void writeObject(java.io.ObjectOutputStream out) {
        // no field to be written
    }

    private void readObject(java.io.ObjectInputStream in) {
        // re-initialize these fields
        ConcurrentRuntime concurrentRuntime = ConcurrentRuntime.getRuntime();
        transactionManager = concurrentRuntime.getTransactionManager();
    }

}
