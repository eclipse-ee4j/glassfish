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

package com.sun.enterprise.transaction.api;

import jakarta.resource.spi.XATerminator;
import jakarta.transaction.SystemException;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

/**
 * Transaction Manager extensions to support transaction inflow w/o resource adapter.
 */
public interface TransactionImport {
  /**
     * Recreate a transaction based on the Xid. This call causes the calling
     * thread to be associated with the specified transaction.
     *
     * <p>
     * This method imports a transactional context controlled by an external transaction manager.
     *
     * @param xid the Xid object representing a transaction.
     */
    public void recreate(Xid xid, long timeout);

    /**
     * Release a transaction. This call causes the calling thread to be
     * dissociated from the specified transaction.
     *
     * <p>
     * This call releases transactional context imported by recreate method.
     *
     * @param xid the Xid object representing a transaction.
     */
    public void release(Xid xid);

    /**
     * Provides a handle to a <code>XATerminator</code> instance.
     *
     * <p> The XATerminator exports 2PC protocol control to an external root transaction coordinator.
     *
     * @return a <code>XATerminator</code> instance.
     */
    public XATerminator getXATerminator();

    /**
     * Return duration before current transaction would timeout.
     *
     * @return Returns the duration in seconds before current transaction would
     *         timeout.
     *         Returns zero if transaction has no timeout set and returns
     *         negative value if transaction already timed out.
     *
     * @exception IllegalStateException Thrown if the current thread is
     *    not associated with a transaction.
     *
     * @exception SystemException Thrown if the transaction manager
     *    encounters an unexpected error condition.
     */
    public int getTransactionRemainingTimeout() throws SystemException;

    /**
     * Allows an arbitrary XAResource to register for recovery
     *
     * @param xaResource XAResource to register for recovery
     */
    public void registerRecoveryResourceHandler(XAResource xaResource);
}
