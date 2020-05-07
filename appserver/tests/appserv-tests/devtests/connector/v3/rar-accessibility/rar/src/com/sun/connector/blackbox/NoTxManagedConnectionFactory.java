/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.connector.blackbox;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import jakarta.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Tony Ng
 */
public class NoTxManagedConnectionFactory
        implements ManagedConnectionFactory, Serializable{

    private String url;
    private ResourceAdapter ra;

    public NoTxManagedConnectionFactory() {
    }

    public Object createConnectionFactory(ConnectionManager cxManager)
            throws ResourceException {

        return new JdbcDataSource_NoTx(this, cxManager);
    }

    public Object createConnectionFactory() throws ResourceException {
        return new JdbcDataSource_NoTx(this, null);
    }

    public ManagedConnection
    createManagedConnection(Subject subject,
                            ConnectionRequestInfo info)
            throws ResourceException {

        try {
            Connection con = null;
            String userName = null;
            PasswordCredential pc =
                    Util.getPasswordCredential(this, subject, info);
            if (pc == null) {
                con = DriverManager.getConnection(url);
            } else {
                userName = pc.getUserName();
                con = DriverManager.getConnection
                        (url, userName, new String(pc.getPassword()));
            }
            return new JdbcManagedConnection
                    (this, pc, null, con, false, false);
        } catch (SQLException ex) {
            ResourceException re =
                    new EISSystemException("SQLException: " + ex.getMessage());
            re.setLinkedException(ex);
            throw re;
        }

    }

    public ManagedConnection
    matchManagedConnections(Set connectionSet,
                            Subject subject,
                            ConnectionRequestInfo info)
            throws ResourceException {

        PasswordCredential pc =
                Util.getPasswordCredential(this, subject, info);
        Iterator it = connectionSet.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (obj instanceof JdbcManagedConnection) {
                JdbcManagedConnection mc = (JdbcManagedConnection) obj;
                ManagedConnectionFactory mcf =
                        mc.getManagedConnectionFactory();
                if (Util.isPasswordCredentialEqual
                        (mc.getPasswordCredential(), pc) &&
                        mcf.equals(this)) {
                    return mc;
                }
            }
        }
        return null;
    }

    public void setLogWriter(PrintWriter out)
            throws ResourceException {

        DriverManager.setLogWriter(out);
    }

    public PrintWriter getLogWriter() throws ResourceException {
        return DriverManager.getLogWriter();
    }

    public String getConnectionURL() {
        return url;
    }

    public void setConnectionURL(String url) {
        this.url = url;
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof NoTxManagedConnectionFactory) {
            String v1 = ((NoTxManagedConnectionFactory) obj).url;
            String v2 = this.url;
            return (v1 == null) ? (v2 == null) : (v1.equals(v2));
        } else {
            return false;
        }
    }

    public int hashCode() {
        if (url == null) {
            return (new String("")).hashCode();
        } else {
            return url.hashCode();
        }
    }

    public ResourceAdapter getResourceAdapter() {
        return this.ra;
    }

    public void setResourceAdapter(ResourceAdapter ra) {
        this.ra = ra;
    }
}
