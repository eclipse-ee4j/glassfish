/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.jdbcra.spi;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.LocalTransactionException;

/**
 * <code>LocalTransaction</code> implementation for Generic JDBC Connector.
 *
 * @version        1.0, 02/08/03
 * @author        Evani Sai Surya Kiran
 */
public class LocalTransaction implements jakarta.resource.spi.LocalTransaction {

    private final ManagedConnection mc;

    /**
     * Constructor for <code>LocalTransaction</code>.
     * @param        mc        <code>ManagedConnection</code> that returns
     *                        this <code>LocalTransaction</code> object as
     *                        a result of <code>getLocalTransaction</code>
     */
    public LocalTransaction(ManagedConnection mc) {
        this.mc = mc;
    }

    /**
     * Begin a local transaction.
     *
     * @throws        LocalTransactionException        if there is an error in changing
     *                                                the autocommit mode of the physical
     *                                                connection
     */
    @Override
    public void begin() throws ResourceException {
        //GJCINT
        mc.transactionStarted();
        try {
            mc.getActualConnection().setAutoCommit(false);
        } catch(java.sql.SQLException sqle) {
            throw new LocalTransactionException(sqle.getMessage());
        }
    }

    /**
     * Commit a local transaction.
     * @throws        LocalTransactionException        if there is an error in changing
     *                                                the autocommit mode of the physical
     *                                                connection or committing the transaction
     */
    @Override
    public void commit() throws ResourceException {
        try {
            mc.getActualConnection().commit();
            mc.getActualConnection().setAutoCommit(true);
        } catch(java.sql.SQLException sqle) {
            throw new LocalTransactionException(sqle.getMessage());
        }
        //GJCINT
        mc.transactionCompleted();
    }

    /**
     * Rollback a local transaction.
     *
     * @throws        LocalTransactionException        if there is an error in changing
     *                                                the autocommit mode of the physical
     *                                                connection or rolling back the transaction
     */
    @Override
    public void rollback() throws ResourceException {
        try {
            mc.getActualConnection().rollback();
            mc.getActualConnection().setAutoCommit(true);
        } catch(java.sql.SQLException sqle) {
            throw new LocalTransactionException(sqle.getMessage());
        }
        //GJCINT
        mc.transactionCompleted();
    }

}
