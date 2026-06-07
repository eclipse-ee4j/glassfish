/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
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

import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLRecoverableException;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the connection-error detection used by {@link LocalTransactionImpl} to decide whether a
 * failed local transaction operation should remove the connection from the pool (see issue #25930).
 */
public class LocalTransactionImplTest {

    @Test
    public void recoverableExceptionIsConnectionError() {
        // e.g. Oracle "ORA-18730: Interrupted IO error" after a request timeout interrupts a read
        assertTrue(LocalTransactionImpl.isConnectionError(new SQLRecoverableException("Socket read interrupted")));
    }

    @Test
    public void nonTransientConnectionExceptionIsConnectionError() {
        assertTrue(LocalTransactionImpl.isConnectionError(new SQLNonTransientConnectionException("gone")));
    }

    @Test
    public void sqlState08IsConnectionError() {
        // e.g. Oracle "ORA-17008: Closed connection" is reported with SQLState 08003
        assertTrue(LocalTransactionImpl.isConnectionError(new SQLException("Closed connection", "08003")));
    }

    @Test
    public void dataExceptionIsNotConnectionError() {
        // A constraint violation / data error must NOT discard a still usable connection
        assertFalse(LocalTransactionImpl.isConnectionError(new SQLDataException("bad value", "22003")));
        assertFalse(LocalTransactionImpl.isConnectionError(new SQLException("constraint violated", "23000")));
    }

    @Test
    public void connectionErrorDetectedInNextExceptionChain() {
        SQLException head = new SQLException("wrapper", "HY000");
        head.setNextException(new SQLException("Closed connection", "08003"));
        assertTrue(LocalTransactionImpl.isConnectionError(head));
    }

    @Test
    public void connectionErrorDetectedInCauseChain() {
        SQLException head = new SQLException("wrapper", "HY000");
        head.initCause(new SQLRecoverableException("Socket read interrupted"));
        assertTrue(LocalTransactionImpl.isConnectionError(head));
    }

    @Test
    public void nullSqlStateIsNotConnectionError() {
        assertFalse(LocalTransactionImpl.isConnectionError(new SQLException("no state")));
    }
}
