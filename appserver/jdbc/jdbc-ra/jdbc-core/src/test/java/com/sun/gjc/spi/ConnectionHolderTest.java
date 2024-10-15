/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import com.sun.gjc.spi.base.ConnectionHolder;
import com.sun.gjc.spi.stub.MyConnection;
import com.sun.gjc.spi.stub.MyConnectionHolder;

import jakarta.resource.spi.ConnectionRequestInfo;

import java.sql.Connection;

import org.junit.jupiter.api.Test;

import static com.sun.gjc.spi.ManagedConnectionImplTest.createManagedConnection;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ConnectionHolderTest {

    @Test
    public void testAssociateAndDissociateConnection() throws Exception {
        ConnectionRequestInfo cxRequestInfo = null;

        Connection connection1 = new MyConnection();
        ManagedConnectionImpl managedConnection1 = createManagedConnection(null, connection1);

        Connection connection2 = new MyConnection();
        ManagedConnectionImpl managedConnection2 = createManagedConnection(null, connection2);

        try (ConnectionHolder connectionHolder = new MyConnectionHolder(connection1, managedConnection1, cxRequestInfo)) {
            assertEquals(connectionHolder.getConnection(), connection1);
            assertEquals(connectionHolder.getManagedConnection(), managedConnection1);

            // Associate a new connection and managed connection
            connectionHolder.associateConnection(connection2, managedConnection2);
            assertEquals(connectionHolder.getConnection(), connection2);
            assertEquals(connectionHolder.getManagedConnection(), managedConnection2);

            // Disassociate connection and managed connection
            connectionHolder.dissociateConnection();
            assertNull(connectionHolder.getConnection());
            assertNull(connectionHolder.getManagedConnection());
        }
    }
}
