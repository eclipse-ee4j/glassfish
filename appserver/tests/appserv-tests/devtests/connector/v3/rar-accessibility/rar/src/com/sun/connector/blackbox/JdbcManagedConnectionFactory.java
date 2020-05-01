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

import com.sun.logging.LogDomains;

import javax.naming.Context;
import javax.naming.InitialContext;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import jakarta.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.sql.XAConnection;
import javax.sql.XADataSource;
import java.io.PrintWriter;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Logger;

/**
 * @author Tony Ng
 */
public class JdbcManagedConnectionFactory
        implements ManagedConnectionFactory, Serializable{

    private String XADataSourceName;
    private ResourceAdapter ra;
    private int count = 0;
    transient private Context ic;
    static Logger _logger;

    static {
        Logger _logger = LogDomains.getLogger( JdbcManagedConnectionFactory.class, LogDomains.RSR_LOGGER);
    }

    public JdbcManagedConnectionFactory() {
    }

    public Object createConnectionFactory(ConnectionManager cxManager)
            throws ResourceException {

        return new JdbcDataSource_XA(this, cxManager);
    }

    public Object createConnectionFactory() throws ResourceException {
        return new JdbcDataSource_XA(this, null);
    }

    public ManagedConnection
    createManagedConnection(Subject subject,
                            ConnectionRequestInfo info)
            throws ResourceException {

        try {
            XAConnection xacon = null;
            String userName = null;
            PasswordCredential pc =
                    Util.getPasswordCredential(this, subject, info);
            if (pc == null) {
                xacon = getXADataSource().getXAConnection();
            } else {
                userName = pc.getUserName();
                xacon = getXADataSource().
                        getXAConnection(userName,
                                new String(pc.getPassword()));
            }
            Connection con = xacon.getConnection();
            return new JdbcManagedConnection
                    (this, pc, xacon, con, true, true);
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
        _logger.severe(" In matchManagedConnections ");
        PasswordCredential pc =
                Util.getPasswordCredential(this, subject, info);
        Iterator it = connectionSet.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            count++;
            if (count == 3) {
                _logger.severe("@@@@@@ Sending ResourceErrorOccured event");
                count = 0;
                ((JdbcManagedConnection) obj).sendEvent(
                        jakarta.resource.spi.ConnectionEvent.CONNECTION_ERROR_OCCURRED,
                        null);
            }
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

        try {
            getXADataSource().setLogWriter(out);
        } catch (SQLException ex) {
            // XXX I18N
            ResourceException rex = new ResourceException("SQLException");
            rex.setLinkedException(ex);
            throw rex;
        }
    }

    public PrintWriter getLogWriter() throws ResourceException {
        try {
            return getXADataSource().getLogWriter();
        } catch (SQLException ex) {
            // XXX I18N
            ResourceException rex = new ResourceException("SQLException");
            rex.setLinkedException(ex);
            throw rex;
        }
    }

    public String getXADataSourceName() {
        return XADataSourceName;
    }

    public void setXADataSourceName(String XADataSourceName) {
        this.XADataSourceName = XADataSourceName;
    }

    private XADataSource getXADataSource() throws ResourceException {
        try {
            if (ic == null) ic = new InitialContext();
            XADataSource ds = (XADataSource) ic.lookup(XADataSourceName);
            return ds;
        } catch (Exception ex) {
            ResourceException rex = new ResourceException("");
            rex.setLinkedException(ex);
            throw rex;
        }
    }

    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj instanceof JdbcManagedConnectionFactory) {
            String v1 = ((JdbcManagedConnectionFactory) obj).
                    XADataSourceName;
            String v2 = this.XADataSourceName;
            return (v1 == null) ? (v2 == null) : (v1.equals(v2));
        } else {
            return false;
        }
    }

    public int hashCode() {
        if (XADataSourceName == null) {
            return (new String("")).hashCode();
        } else {
            return XADataSourceName.hashCode();
        }
    }

    public ResourceAdapter getResourceAdapter() {
        return this.ra;
    }

    public void setResourceAdapter(ResourceAdapter ra) {
        this.ra = ra;
    }
}
