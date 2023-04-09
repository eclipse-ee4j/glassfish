/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.connector.cciblackbox;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionManager;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.EISSystemException;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.security.PasswordCredential;

import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

import javax.security.auth.Subject;

/**
 * @author Sheetal Vartak
 */
public class CciLocalTxManagedConnectionFactory implements ManagedConnectionFactory, Serializable {

    private String url;

    public CciLocalTxManagedConnectionFactory() {
    }


    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        return new CciConnectionFactory(this, cxManager);
    }


    @Override
    public Object createConnectionFactory() throws ResourceException {
        return new CciConnectionFactory(this, null);
    }


    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo info)
        throws ResourceException {
        try {
            Connection con = null;
            String userName = null;

            PasswordCredential pc = Util.getPasswordCredential(this, subject, info);
            if (pc == null) {
                con = DriverManager.getConnection(url);
            } else {
                userName = pc.getUserName();
                con = DriverManager.getConnection(url, userName, new String(pc.getPassword()));
            }
            return new CciManagedConnection(this, pc, null, con, false, true);
        } catch (SQLException ex) {
            throw new EISSystemException("SQLException: " + ex.getMessage(), ex);
        }

    }


    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo info)
        throws ResourceException {
        PasswordCredential pc = Util.getPasswordCredential(this, subject, info);
        Iterator it = connectionSet.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof CciManagedConnection) {
                CciManagedConnection mc = (CciManagedConnection) obj;
                ManagedConnectionFactory mcf = mc.getManagedConnectionFactory();
                if (Util.isPasswordCredentialEqual(mc.getPasswordCredential(), pc) && mcf.equals(this)) {
                    return mc;
                }
            }
        }
        return null;
    }


    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        DriverManager.setLogWriter(out);
    }


    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return DriverManager.getLogWriter();
    }


    public String getConnectionURL() {
        return url;
    }


    public void setConnectionURL(String url) {
        this.url = url;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof CciLocalTxManagedConnectionFactory) {
            String v1 = ((CciLocalTxManagedConnectionFactory) obj).url;
            String v2 = this.url;
            return v1 == null ? v2 == null : v1.equals(v2);
        }
        return false;
    }


    @Override
    public int hashCode() {
        return Objects.hashCode(url);
    }
}
