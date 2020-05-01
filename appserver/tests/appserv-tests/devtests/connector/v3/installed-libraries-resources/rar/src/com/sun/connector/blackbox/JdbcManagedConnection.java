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

import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.*;
import jakarta.resource.spi.IllegalStateException;
import jakarta.resource.spi.SecurityException;
import jakarta.resource.spi.security.PasswordCredential;
import javax.security.auth.Subject;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Tony Ng
 */
public class JdbcManagedConnection implements ManagedConnection {

    private XAConnection xacon;
    private Connection con;
    private JdbcConnectionEventListener jdbcListener;
    private PasswordCredential passCred;
    private ManagedConnectionFactory mcf;
    private PrintWriter logWriter;
    private boolean supportsXA;
    private boolean supportsLocalTx;
    private boolean destroyed;
    private Set connectionSet;  // set of JdbcConnection

    JdbcManagedConnection(ManagedConnectionFactory mcf,
                          PasswordCredential passCred,
                          XAConnection xacon,
                          Connection con,
                          boolean supportsXA,
                          boolean supportsLocalTx) {
        this.mcf = mcf;
        this.passCred = passCred;
        this.xacon = xacon;
        this.con = con;
        this.supportsXA = supportsXA;
        this.supportsLocalTx = supportsLocalTx;
        connectionSet = new HashSet();
        jdbcListener = new JdbcConnectionEventListener(this);
        if (xacon != null) {
            xacon.addConnectionEventListener(jdbcListener);
        }

    }

    // XXX should throw better exception
    private void throwResourceException(SQLException ex)
            throws ResourceException {

        ResourceException re =
                new ResourceException("SQLException: " + ex.getMessage());
        re.setLinkedException(ex);
        throw re;
    }

    public Object getConnection(Subject subject,
                                ConnectionRequestInfo connectionRequestInfo)
            throws ResourceException {

        PasswordCredential pc =
                Util.getPasswordCredential(mcf, subject, connectionRequestInfo);
        if (!Util.isPasswordCredentialEqual(pc, passCred)) {
            throw new SecurityException("Principal does not match. Reauthentication not supported");
        }
        checkIfDestroyed();
        JdbcConnection jdbcCon =
                new JdbcConnection(this, this.supportsLocalTx);
        addJdbcConnection(jdbcCon);
        return jdbcCon;
    }

    public void destroy() throws ResourceException {
        try {
            if (destroyed) return;
            destroyed = true;
            Iterator it = connectionSet.iterator();
            while (it.hasNext()) {
                JdbcConnection jdbcCon = (JdbcConnection) it.next();
                jdbcCon.invalidate();
            }
            connectionSet.clear();
            con.close();
            if (xacon != null) xacon.close();
        } catch (SQLException ex) {
            throwResourceException(ex);
        }
    }

    public void cleanup() throws ResourceException {
        try {
            checkIfDestroyed();
            Iterator it = connectionSet.iterator();
            while (it.hasNext()) {
                JdbcConnection jdbcCon = (JdbcConnection) it.next();
                // Dont invalidate during cleanup, invalidate during destroy
                //jdbcCon.invalidate();
            }
            connectionSet.clear();
            if (xacon != null) {
                con.close();
                con = xacon.getConnection();
            } else {
                con.setAutoCommit(true);
            }
        } catch (SQLException ex) {
            throwResourceException(ex);
        }
    }


    public void associateConnection(Object connection)
            throws ResourceException {

        checkIfDestroyed();
        if (connection instanceof JdbcConnection) {
            JdbcConnection jdbcCon = (JdbcConnection) connection;
            jdbcCon.associateConnection(this);
        } else {
            throw new IllegalStateException("Invalid connection object: " +
                    connection);
        }
    }


    public void addConnectionEventListener(ConnectionEventListener listener) {
        jdbcListener.addConnectorListener(listener);
    }


    public void removeConnectionEventListener
            (ConnectionEventListener listener) {

        jdbcListener.removeConnectorListener(listener);
    }


    public XAResource getXAResource() throws ResourceException {
        if (!supportsXA) {
            throw new NotSupportedException("XA transaction not supported");
        }
        try {
            checkIfDestroyed();
            return xacon.getXAResource();
        } catch (SQLException ex) {
            throwResourceException(ex);
            return null;
        }
    }

    public LocalTransaction getLocalTransaction() throws ResourceException {
        if (!supportsLocalTx) {
            throw new NotSupportedException("Local transaction not supported");
        } else {
            checkIfDestroyed();
            return new LocalTransactionImpl(this);
        }
    }

    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        checkIfDestroyed();
        return new MetaDataImpl(this);
    }

    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
    }

    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    Connection getJdbcConnection() throws ResourceException {
        checkIfDestroyed();
        return con;
    }

    boolean isDestroyed() {
        return destroyed;
    }

    PasswordCredential getPasswordCredential() {
        return passCred;
    }

    void sendEvent(int eventType, Exception ex) {
        jdbcListener.sendEvent(eventType, ex, null);
    }

    void sendEvent(int eventType, Exception ex, Object connectionHandle) {
        jdbcListener.sendEvent(eventType, ex, connectionHandle);
    }

    void removeJdbcConnection(JdbcConnection jdbcCon) {
        connectionSet.remove(jdbcCon);
    }

    void addJdbcConnection(JdbcConnection jdbcCon) {
        connectionSet.add(jdbcCon);
    }

    private void checkIfDestroyed() throws ResourceException {
        if (destroyed) {
            throw new IllegalStateException("Managed connection is closed");
        }
    }

    ManagedConnectionFactory getManagedConnectionFactory() {
        return mcf;
    }
}
