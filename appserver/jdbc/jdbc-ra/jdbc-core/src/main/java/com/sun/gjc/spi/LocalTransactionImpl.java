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

package com.sun.gjc.spi;

import com.sun.logging.LogDomains;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.LocalTransactionException;

import java.sql.SQLException;
import java.util.logging.Logger;

import static java.util.logging.Level.FINEST;

/**
 * <code>LocalTransactionImpl</code> implementation for Generic JDBC Connector.
 *
 * @author Evani Sai Surya Kiran
 * @version 1.0, 02/08/03
 */
public class LocalTransactionImpl implements jakarta.resource.spi.LocalTransaction {

    protected final static Logger _logger = LogDomains.getLogger(LocalTransactionImpl.class, LogDomains.RSR_LOGGER);

    private ManagedConnectionImpl managedConnectionImpl;

    /**
     * Constructor for <code>LocalTransactionImpl</code>.
     *
     * @param managedConnectionImpl <code>ManagedConnection</code> that returns this
     * <code>LocalTransactionImpl</code> object as a result of
     * <code>getLocalTransaction</code>
     */
    public LocalTransactionImpl(ManagedConnectionImpl managedConnectionImpl) {
        this.managedConnectionImpl = managedConnectionImpl;
    }

    /**
     * Begin a local transaction.
     *
     * @throws LocalTransactionException if there is an error in changing the
     * autocommit mode of the physical connection
     */
    public void begin() throws ResourceException {
        managedConnectionImpl.transactionStarted();

        try {
            managedConnectionImpl.getActualConnection().setAutoCommit(false);
        } catch (SQLException sqle) {
            if (_logger.isLoggable(FINEST)) {
                _logger.finest("Exception during begin() : " + sqle);
            }

            throw new LocalTransactionException(sqle.getMessage(), sqle);
        }
    }

    /**
     * Commit a local transaction.
     *
     * @throws LocalTransactionException if there is an error in changing the
     * autocommit mode of the physical connection or committing the transaction
     */
    public void commit() throws ResourceException {
        try {
            managedConnectionImpl.getActualConnection().commit();
            managedConnectionImpl.getActualConnection().setAutoCommit(true);
        } catch (SQLException sqle) {
            if (_logger.isLoggable(FINEST)) {
                _logger.finest("Exception during commit() : " + sqle);
            }

            throw new LocalTransactionException(sqle.getMessage(), sqle);
        } finally {
            managedConnectionImpl.transactionCompleted();
        }
    }

    /**
     * Rollback a local transaction.
     *
     * @throws LocalTransactionException if there is an error in changing the
     * autocommit mode of the physical connection or rolling back the transaction
     */
    public void rollback() throws ResourceException {
        try {
            managedConnectionImpl.getActualConnection().rollback();
            managedConnectionImpl.getActualConnection().setAutoCommit(true);
        } catch (SQLException sqle) {
            if (_logger.isLoggable(FINEST)) {
                _logger.finest("Exception during rollback() : " + sqle);
            }

            throw new LocalTransactionException(sqle.getMessage(), sqle);
        } finally {
            managedConnectionImpl.transactionCompleted();
        }
    }

}
