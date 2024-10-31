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

import com.sun.gjc.spi.stub.MyDataSource;

import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ResourceAllocationException;
import jakarta.resource.spi.security.PasswordCredential;

import java.util.HashSet;
import java.util.Set;

import javax.security.auth.Subject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public abstract class AbstractManagedConnectionFactoryImplTest {

    protected static final String USERNAME = "myUsername";
    protected static final String PASSWORD = "myPassword";
    protected static final String ANOTHER_PASSWORD = "anotherPassword";

    public abstract ManagedConnectionFactoryImpl getManagedConnectionFactory() throws Exception;

    @Test
    public void testCreateManagedConnection_NoCredentials() throws Exception {
        ManagedConnectionFactoryImpl mcf = getManagedConnectionFactory();

        // No credentials at all
        Subject subject = null;
        ConnectionRequestInfo connectionRequestInfo = null;

        // Credentials 'both null', should receive a connection in current code.
        ManagedConnection managedConnection = mcf.createManagedConnection(subject, connectionRequestInfo);
        assertNotNull(managedConnection);
    }

    @Test
    public void testCreateManagedConnection_SubjectCredentials() throws Exception {
        ManagedConnectionFactoryImpl mcf = getManagedConnectionFactory();

        // No credentials in ConnectionRequestInfo
        ConnectionRequestInfo connectionRequestInfo = null;

        // Test subject with matching password
        Subject subject = createSubjectWithCredentials(mcf, PASSWORD);
        ManagedConnection managedConnection = mcf.createManagedConnection(subject, connectionRequestInfo);
        assertNotNull(managedConnection);

        // Test subject without matching password, driver must now handle username/password
        subject = createSubjectWithCredentials(mcf, ANOTHER_PASSWORD);
        managedConnection = mcf.createManagedConnection(subject, connectionRequestInfo);
        assertNotNull(managedConnection);

        // Test with driver not accepting username/password
        final Subject subjectNotAccepted = createSubjectWithCredentials(mcf, MyDataSource.NOT_ALLOWED_PASSWORD);
        assertThrows(ResourceAllocationException.class, () -> {
            mcf.createManagedConnection(subjectNotAccepted, connectionRequestInfo);
        });
    }

    @Test
    public void testCreateManagedConnection_ConnectionRequestInfoCredentials() throws Exception {
        ManagedConnectionFactoryImpl mcf = getManagedConnectionFactory();

        // No credentials in subject
        Subject subject = null;

        // Test with matching password
        ConnectionRequestInfo connectionRequestInfo = new ConnectionRequestInfoImpl(USERNAME, PASSWORD.toCharArray());
        ManagedConnection managedConnection = mcf.createManagedConnection(subject, connectionRequestInfo);
        assertNotNull(managedConnection);

        // Test without matching password, driver must now handle username/password
        connectionRequestInfo = new ConnectionRequestInfoImpl(USERNAME, ANOTHER_PASSWORD.toCharArray());
        managedConnection = mcf.createManagedConnection(subject, connectionRequestInfo);
        assertNotNull(managedConnection);

        // Test with driver not accepting username/password
        assertThrows(ResourceAllocationException.class, () -> {
            ConnectionRequestInfoImpl cr = new ConnectionRequestInfoImpl(USERNAME, MyDataSource.NOT_ALLOWED_PASSWORD.toCharArray());
            mcf.createManagedConnection(subject, cr);
        });
    }

    @Test
    public void testCreateManagedConnection_AnotherPassword() throws Exception {
        ManagedConnectionFactoryImpl mcf = getManagedConnectionFactory();

        // No credentials in subject
        Subject subject = null;
        ConnectionRequestInfo connectionRequestInfo = new ConnectionRequestInfoImpl(USERNAME, ANOTHER_PASSWORD.toCharArray());

        ManagedConnection managedConnection = mcf.createManagedConnection(subject, connectionRequestInfo);
        assertNotNull(managedConnection);
    }

    @Test
    public void testGetDataSource() throws Exception {
        ManagedConnectionFactoryImpl mcf = getManagedConnectionFactory();
        assertNotNull(mcf.getDataSource());
    }

    /**
     * This is about connection 'matching' and not in transaction matching as used in connector-runtime
     */
    @Test
    public void testMatchManagedConnections() throws Exception {
        ManagedConnectionFactoryImpl mcf = getManagedConnectionFactory();

        // connectionSet null, expect null
        Set<ManagedConnection> connectionSet = null;
        assertNull(mcf.matchManagedConnections(connectionSet, null, null));

        // Create a managed connection
        Subject subject = createSubjectWithCredentials(mcf, PASSWORD);
        ConnectionRequestInfo connectionRequestInfo = null;
        ManagedConnection managedConnection = mcf.createManagedConnection(subject, connectionRequestInfo);

        // Match it
        connectionSet = new HashSet<>();
        connectionSet.add(managedConnection);
        assertEquals(managedConnection, mcf.matchManagedConnections(connectionSet, null, null));

        // Could test some more complex matching variations
    }

    protected Subject createSubjectWithCredentials(ManagedConnectionFactory mcf, String password) {
        Subject subject = new Subject();
        PasswordCredential passwordCredential = new PasswordCredential(USERNAME, password.toCharArray());
        subject.getPrivateCredentials().add(passwordCredential);
        passwordCredential.setManagedConnectionFactory(mcf);
        return subject;
    }

}
