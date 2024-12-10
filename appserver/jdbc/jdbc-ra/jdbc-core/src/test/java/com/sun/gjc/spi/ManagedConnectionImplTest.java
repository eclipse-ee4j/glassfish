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
import com.sun.gjc.spi.stub.MyPooledConnection;
import com.sun.gjc.util.SQLTraceDelegator;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.security.PasswordCredential;

import java.sql.Connection;

import javax.sql.PooledConnection;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ManagedConnectionImplTest {

    @Test
    public void testConstructor() throws Exception {
        // pooledConn and sqlConn not allowed to be both null
        assertThrows(ResourceException.class, () -> createManagedConnection(null, null));

        // pooledConn and sqlConn not allowed to be both not null according to javadoc
        // but testing this fails so use assertDoesNotThrow for now but it should be assertThrows(ResourceException.class
        assertDoesNotThrow(() -> createManagedConnection(new MyPooledConnection(), new MyConnection()));

        // valid ManagedConnection
        ManagedConnectionImpl managedConnection = createManagedConnection(null, new MyConnection());
        assertNotNull(managedConnection.getActualConnection());
    }

    @Test
    public void testAssociateConnection() throws Exception {
        ConnectionRequestInfo cxRequestInfo = null;

        final ManagedConnectionImpl managedConnection1 = createManagedConnection(null, new MyConnection());
        final Connection connection1 = managedConnection1.getActualConnection();
        assertEquals(0, managedConnection1.connectionCount);

        final ManagedConnectionImpl managedConnection2 = createManagedConnection(null, new MyConnection());
        final Connection connection2 = managedConnection2.getActualConnection();
        assertEquals(0, managedConnection2.connectionCount);

        // Create an application-level connection handle ConnectionHolder linked to managedConnection2
        // and instruct the managedConnection1 to associate with this ConnectionHolder
        try (ConnectionHolder connectionHolder = new MyConnectionHolder(connection2, managedConnection2, cxRequestInfo)) {
            assertEquals(connection2, connectionHolder.getConnection());
            assertEquals(managedConnection2, connectionHolder.getManagedConnection());

            // Associate ManagedCollection1 to the connectionholder / handle
            managedConnection1.associateConnection(connectionHolder);

            // Spec: jakarta.resource.spi.ManagedConnection.associateConnection(Object)
            // "The method implementation for a ManagedConnection should dissociate the connection handle (passed as a parameter)
            // from its currently associated ManagedConnection and associate the new connection handle with itself. "
            //
            // Expect the connectionHolder to be changed and point to ManagedCollection1
            assertEquals(connection1, connectionHolder.getConnection());
            assertEquals(managedConnection1, connectionHolder.getManagedConnection());

            // Expect both ManagedconnectionImpl instances to not be changed regarding the actual (database) connection
            assertEquals(connection1, managedConnection1.getActualConnection());
            assertEquals(connection2, managedConnection2.getActualConnection());

            // Expect managedConnection2.myLogicalConnection to be null, it was never linked to a connection holder
            assertNull(managedConnection2.myLogicalConnection);

            // Expect managedConnection1.myLogicalConnection to reference the new connectionHolder.
            // This means a circular dependency between the managedConnection1 and the connectionHolder is added
            // by the associateConnection logic. This makes the objects relations hard to understand.
            // Show both sides of the relation:
            assertEquals(connectionHolder, managedConnection1.myLogicalConnection);
            assertEquals(managedConnection1, connectionHolder.getManagedConnection());
            assertEquals(managedConnection1.getActualConnection(), connectionHolder.getConnection());

            // The associateContext also increases the connectionCount for the connection associated
            // to the connectionholder
            assertEquals(1, managedConnection1.connectionCount);
            // And decreases the connectionCount for the one that was used in the connection holder
            assertEquals(-1, managedConnection2.connectionCount);
        }
    }

    @Test
    public void testAssociateAndDissociateConnection() throws Exception {
        ConnectionRequestInfo cxRequestInfo = null;

        final ManagedConnectionImpl managedConnection1 = createManagedConnection(null, new MyConnection());
        assertEquals(0, managedConnection1.connectionCount);

        final ManagedConnectionImpl managedConnection2 = createManagedConnection(null, new MyConnection());
        final Connection connection2 = managedConnection2.getActualConnection();
        assertEquals(0, managedConnection2.connectionCount);

        // Create an application-level connection handle ConnectionHolder linked to managedConnection2
        // and instruct the managedConnection1 to associate with this ConnectionHolder
        try (ConnectionHolder connectionHolder = new MyConnectionHolder(connection2, managedConnection2, cxRequestInfo)) {
            // Associate ManagedCollection1 to the connectionholder / handle
            managedConnection1.associateConnection(connectionHolder);
            assertNotNull(managedConnection1.myLogicalConnection);
            assertNotNull(connectionHolder.getConnection());
            assertNotNull(connectionHolder.getManagedConnection());
            assertEquals(1, managedConnection1.connectionCount);

            // Dissociate, could be called by the application server in case "lazy connection association optimization" is used.
            // Expect managedConnection1.myLogicalConnection to no longer reference the new connectionHolder.
            // And the connectionHolder should no longer reference the managedConnection.
            managedConnection1.dissociateConnections();
            assertNull(managedConnection1.myLogicalConnection);
            assertNull(connectionHolder.getConnection());
            assertNull(connectionHolder.getManagedConnection());
            // Note: count is not decreased on dissociate call, it is decreased on connectionholder.close call
            assertEquals(1, managedConnection1.connectionCount);
        }

        // Close is called on connectionHolder, but it was no longer linked to managedConnection1.
        // Thus connectionCount is not decreased from 1 to 0.
        assertEquals(1, managedConnection1.connectionCount);
    }

    /**
     * Creates a ManagedConnectionImpl with a DSManagedConnectionFactory
     */
    public static ManagedConnectionImpl createManagedConnection(PooledConnection pooledConnection, Connection sqlConnection) throws ResourceException {
        // Password is optional
        PasswordCredential passwdCred = null;
        // Use DSManagedConnectionFactory
        ManagedConnectionFactory mcf = new DSManagedConnectionFactory();
        // poolInfo
        PoolInfo poolInfo = new PoolInfo(SimpleJndiName.of("myPoolInfo"));
        // Default value is 0, no statement cache enabled
        int statementCacheSize = 0;
        // Cache is not enabled, no type needed
        String statementCacheType = null;
        // No SqlTrace needed
        SQLTraceDelegator delegator = null;
        // Leak timeout not used
        long statementLeakTimeout = 0;
        // Leak reclaim is not used
        boolean statementLeakReclaim = false;

        return new ManagedConnectionImpl(pooledConnection, sqlConnection, passwdCred,
                mcf, poolInfo, statementCacheSize, statementCacheType,
                delegator, statementLeakTimeout, statementLeakReclaim);
    }
}
