/*
 * Copyright (c) 2025 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.resource.pool.mock;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.LocalTransaction;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionMetaData;

import java.io.PrintWriter;

import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

/**
 * Mock class without any implementation
 */
public class ManagedConnectionMock implements ManagedConnection {

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        return null;
    }

    @Override
    public void destroy() throws ResourceException {
    }

    @Override
    public void cleanup() throws ResourceException {
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        return null;
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return null;
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return null;
    }
}
