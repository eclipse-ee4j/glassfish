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

import com.sun.gjc.common.DataSourceSpec;
import com.sun.gjc.spi.stub.MyDataSource;
import com.sun.gjc.spi.stub.MyDriver;

import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ResourceAllocationException;

import java.sql.DriverManager;

import javax.security.auth.Subject;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DMManagedConnectionFactoryImplTest extends AbstractManagedConnectionFactoryImplTest {

    @Override
    public ManagedConnectionFactoryImpl getManagedConnectionFactory() throws Exception {
        // A database driver is required
        DriverManager.registerDriver(new MyDriver());

        ManagedConnectionFactoryImpl mcf = new DMManagedConnectionFactory();
        mcf.setClassName(MyDataSource.class.getCanonicalName());
        mcf.spec.setDetail(DataSourceSpec.URL, MyDriver.ACCEPTED_URL);
        return mcf;
    }

    @Test
    public void testCreateManagedConnection_SubjectCredentials() throws Exception {
        ManagedConnectionFactoryImpl mcf = getManagedConnectionFactory();

        // No credentials in ConnectionRequestInfo
        ConnectionRequestInfo connectionRequestInfo = null;

        // Test subject with matching password
        // DIFFERENCE: DMManagedConnectionFactory ignores subject credentials.
        // Change via mcf spec the password:
        Subject subject = createSubjectWithCredentials(mcf, PASSWORD);
        mcf.spec.setDetail(DataSourceSpec.PASSWORD, PASSWORD);
        ManagedConnection managedConnection = mcf.createManagedConnection(subject, connectionRequestInfo);
        assertNotNull(managedConnection);

        // Test subject without matching password, driver must now handle username/password
        // DIFFERENCE: DMManagedConnectionFactory ignores subject credentials.
        // Change via mcf spec the password:
        subject = createSubjectWithCredentials(mcf, ANOTHER_PASSWORD);
        mcf.spec.setDetail(DataSourceSpec.PASSWORD, ANOTHER_PASSWORD);
        managedConnection = mcf.createManagedConnection(subject, connectionRequestInfo);
        assertNotNull(managedConnection);

        // Test with driver not accepting username/password
        // DIFFERENCE: DMManagedConnectionFactory ignores subject credentials.
        // Change via mcf spec the password:
        mcf.spec.setDetail(DataSourceSpec.PASSWORD, MyDataSource.NOT_ALLOWED_PASSWORD);
        final Subject subjectNotAccepted = createSubjectWithCredentials(mcf, MyDataSource.NOT_ALLOWED_PASSWORD);
        assertThrows(ResourceAllocationException.class, () -> {
            mcf.createManagedConnection(subjectNotAccepted, connectionRequestInfo);
        });
    }

    @Test
    public void testGetDataSource() throws Exception {
        ManagedConnectionFactoryImpl mcf = getManagedConnectionFactory();
        // Method always returns null in DMManagedConnectionFactory
        assertNull(mcf.getDataSource());
    }
}
