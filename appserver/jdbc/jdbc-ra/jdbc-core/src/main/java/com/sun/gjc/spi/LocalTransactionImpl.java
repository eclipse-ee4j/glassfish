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
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLRecoverableException;
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

            notifyConnectionErrorIfFatal(sqle);
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

            notifyConnectionErrorIfFatal(sqle);
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

            notifyConnectionErrorIfFatal(sqle);
            throw new LocalTransactionException(sqle.getMessage(), sqle);
        } finally {
            managedConnectionImpl.transactionCompleted();
        }
    }

    /**
     * Signals a connection error to the <code>ManagedConnection</code> when the given
     * <code>SQLException</code> indicates that the underlying physical connection is broken.
     * <p>
     * Local transaction operations (begin/commit/rollback) are the place where a connection
     * that died while in the pool is first detected (for example after a request timeout
     * interrupts a statement and leaves the connection closed). Without raising a
     * CONNECTION_ERROR_OCCURRED event the broken connection is never removed from the pool and,
     * when "validate-atmost-once-period-in-seconds" is greater than zero, it is also never
     * re-validated on checkout, so it stays broken forever. Raising the event lets the pool
     * discard the connection, mirroring how the XA path reacts in
     * {@link ManagedConnectionImpl#XAStartOccurred()} / {@link ManagedConnectionImpl#XAEndOccurred()}.
     *
     * @param sqle the exception thrown by the physical connection
     */
    private void notifyConnectionErrorIfFatal(SQLException sqle) {
        if (isConnectionError(sqle)) {
            managedConnectionImpl.connectionErrorOccurred(sqle, null);
        }
    }

    /**
     * Determines whether the given <code>SQLException</code> (or any exception in its
     * {@link SQLException#getNextException() next} or {@link Throwable#getCause() cause} chain)
     * represents a fatal connection error, as opposed to a transient or data related failure that
     * leaves the connection usable. Detection is driver agnostic: it relies on the standard
     * connection-exception subclasses and on SQLState class "08" (connection exception) defined by
     * the SQL standard. For example Oracle reports ORA-17008 ("Closed connection") with SQLState
     * "08003" and a socket read interrupted by a request timeout as a
     * <code>SQLRecoverableException</code>.
     *
     * @param sqle the exception to inspect
     * @return true if the exception indicates the physical connection is no longer usable
     */
    static boolean isConnectionError(SQLException sqle) {
        for (SQLException current = sqle; current != null; current = current.getNextException()) {
            for (Throwable t = current; t != null; t = t.getCause()) {
                if (t instanceof SQLRecoverableException || t instanceof SQLNonTransientConnectionException) {
                    return true;
                }
                if (t instanceof SQLException) {
                    String sqlState = ((SQLException) t).getSQLState();
                    if (sqlState != null && sqlState.startsWith("08")) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

}
