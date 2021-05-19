/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package connector;

import java.io.PrintWriter;
import java.util.Set;

import jakarta.resource.ResourceException;
import jakarta.resource.cci.Connection;
import jakarta.resource.cci.ConnectionFactory;
import jakarta.resource.spi.ConfigProperty;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ConnectionDefinition;
import jakarta.resource.spi.TransactionSupport;
import javax.security.auth.Subject;

@ConnectionDefinition(
        connectionFactory=ConnectionFactory.class,
        connectionFactoryImpl=MyConnectionFactory.class,
        connection=Connection.class,
        connectionImpl=MyConnection.class)
public class MyManagedConnectionFactory implements ManagedConnectionFactory, TransactionSupport {

    private static final long serialVersionUID = 8394689502759459536L;
    private String testName;
    private ConnectionManager cm;
    private PrintWriter writer;
    private TransactionSupportLevel transactionSupport = TransactionSupportLevel.LocalTransaction;;

    public String getTestName() {
        return testName;
    }

    @ConfigProperty(
            defaultValue = "ConfigPropertyForRA",
            type = java.lang.String.class
    )
    public void setTestName(String name) {
        testName = name;
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        return new MyConnectionFactory(this, null);
    }

    @Override
    public Object createConnectionFactory(ConnectionManager cm)  throws ResourceException {
        this.cm = cm;
        return new MyConnectionFactory(this, cm);
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject,  ConnectionRequestInfo reqInfo)
            throws ResourceException {
        return null;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return writer;
    }

    @Override
    public ManagedConnection matchManagedConnections(Set candidates, Subject sub,  ConnectionRequestInfo reqInfo)
            throws ResourceException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter writer) throws ResourceException {
        this.writer = writer;
    }

    @Override
    public TransactionSupportLevel getTransactionSupport() {
        return transactionSupport;
    }

    public void setTransactionSupport(TransactionSupportLevel transactionSupport) {
        this.transactionSupport = transactionSupport;
    }

}
