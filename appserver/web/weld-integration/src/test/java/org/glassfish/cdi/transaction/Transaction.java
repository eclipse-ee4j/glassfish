/*
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
import jakarta.transaction.RollbackException;
import jakarta.transaction.Synchronization;
import jakarta.transaction.SystemException;

import javax.transaction.xa.XAResource;

/**
 * User: paulparkinson Date: 12/18/12 Time: 11:50 AM
 */
public class Transaction implements jakarta.transaction.Transaction {
    private static int counter;
    private int txid;
    public boolean isMarkedRollback;

    public Transaction() {
        txid = counter++;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Transaction && ((Transaction) o).txid == this.txid;
    }

    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException,
            IllegalStateException, SystemException {

    }

    public boolean delistResource(XAResource xaRes, int flag) throws IllegalStateException, SystemException {
        return false;
    }

    public boolean enlistResource(XAResource xaRes) throws RollbackException, IllegalStateException, SystemException {
        return false;
    }

    public int getStatus() throws SystemException {
        return 0;
    }

    public void registerSynchronization(Synchronization sync) throws RollbackException, IllegalStateException, SystemException {

    }

    public void rollback() throws IllegalStateException, SystemException {

    }

    public void setRollbackOnly() throws IllegalStateException, SystemException {

    }
}
