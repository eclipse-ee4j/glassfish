/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2020 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.cdi.transaction;

import jakarta.transaction.HeuristicMixedException;
import jakarta.transaction.HeuristicRollbackException;
import jakarta.transaction.InvalidTransactionException;
import jakarta.transaction.NotSupportedException;
import jakarta.transaction.RollbackException;
import jakarta.transaction.SystemException;

public class TransactionManager implements jakarta.transaction.TransactionManager {
    private ThreadLocal<Transaction> transactionThreadLocal = new ThreadLocal<>();

    @Override
    public void begin() throws NotSupportedException, SystemException {
        if (getTransaction() != null) {
            throw new NotSupportedException("attempt to start tx when one already exists");
        }
        transactionThreadLocal.set(new Transaction());
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException,
            IllegalStateException, SystemException {
        if (getTransaction().isMarkedRollback) {
            suspend();
            throw new RollbackException("test tx was marked for rollback");
        }
        suspend();
    }

    @Override
    public int getStatus() throws SystemException {
        return 0;
    }

    @Override
    public Transaction getTransaction() throws SystemException {
        return transactionThreadLocal.get();
    }

    @Override
    public void resume(jakarta.transaction.Transaction transaction)
        throws InvalidTransactionException, IllegalStateException, SystemException {
        transactionThreadLocal.set((Transaction) transaction);
    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        suspend();
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        Transaction transaction = getTransaction();
        if (transaction != null) {
            transaction.isMarkedRollback = true;
        }
    }

    @Override
    public void setTransactionTimeout(int seconds) throws SystemException {

    }

    @Override
    public Transaction suspend() throws SystemException {
        Transaction transaction = transactionThreadLocal.get();
        transactionThreadLocal.set(null);
        return transaction;
    }
}
