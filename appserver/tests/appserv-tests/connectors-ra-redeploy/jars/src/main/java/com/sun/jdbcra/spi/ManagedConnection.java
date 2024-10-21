/*
 * Copyright (c) 2003, 2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.jdbcra.spi;

import com.sun.jdbcra.util.SecurityUtils;

import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.security.PasswordCredential;

import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

/**
 * <code>ManagedConnection</code> implementation for Generic JDBC Connector.
 *
 * @version        1.0, 02/07/22
 * @author        Evani Sai Surya Kiran
 */
public class ManagedConnection implements jakarta.resource.spi.ManagedConnection {

    public static final int ISNOTAPOOLEDCONNECTION = 0;
    public static final int ISPOOLEDCONNECTION = 1;
    public static final int ISXACONNECTION = 2;

    private boolean isDestroyed = false;
    private boolean isUsable = true;

    private final int connectionType;
    private PooledConnection pc = null;
    private java.sql.Connection actualConnection = null;
    private Hashtable connectionHandles;
    private PrintWriter logWriter;
    private PasswordCredential passwdCredential;
    private jakarta.resource.spi.ManagedConnectionFactory mcf = null;
    private XAResource xar = null;
    public ConnectionHolder activeConnectionHandle;

    //GJCINT
    private int isolationLevelWhenCleaned;
    private boolean isClean = false;

    private boolean transactionInProgress = false;

    private ConnectionEventListener listener = null;

    private ConnectionEvent ce = null;

    private static Logger _logger;
    static {
        _logger = Logger.getAnonymousLogger();
    }

    /**
     * Constructor for <code>ManagedConnection</code>. The pooledConn parameter is expected
     * to be null and sqlConn parameter is the actual connection in case where
     * the actual connection is got from a non pooled datasource object. The
     * pooledConn parameter is expected to be non null and sqlConn parameter
     * is expected to be null in the case where the datasource object is a
     * connection pool datasource or an xa datasource.
     *
     * @param        pooledConn        <code>PooledConnection</code> object in case the
     *                                physical connection is to be obtained from a pooled
     *                                <code>DataSource</code>; null otherwise
     * @param        sqlConn        <code>java.sql.Connection</code> object in case the physical
     *                        connection is to be obtained from a non pooled <code>DataSource</code>;
     *                        null otherwise
     * @param        passwdCred        object conatining the
     *                                user and password for allocating the connection
     * @throws        ResourceException        if the <code>ManagedConnectionFactory</code> object
     *                                        that created this <code>ManagedConnection</code> object
     *                                        is not the same as returned by <code>PasswordCredential</code>
     *                                        object passed
     */
    public ManagedConnection(PooledConnection pooledConn, java.sql.Connection sqlConn,
        PasswordCredential passwdCred, jakarta.resource.spi.ManagedConnectionFactory mcf) throws ResourceException {
        if(pooledConn == null && sqlConn == null) {
            throw new ResourceException("Connection object cannot be null");
        }

        connectionType = getConnectionType(pooledConn);
        if (connectionType == ISNOTAPOOLEDCONNECTION ) {
            actualConnection = sqlConn;
        }

        pc = pooledConn;
        connectionHandles = new Hashtable();
        passwdCredential = passwdCred;
        this.mcf = mcf;
        if(passwdCredential != null &&
            this.mcf.equals(passwdCredential.getManagedConnectionFactory()) == false) {
            throw new ResourceException("The ManagedConnectionFactory that has created this " +
                "ManagedConnection is not the same as the ManagedConnectionFactory returned by" +
                    " the PasswordCredential for this ManagedConnection");
        }
        logWriter = mcf.getLogWriter();
        activeConnectionHandle = null;
        ce = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
    }

    /**
     * Adds a connection event listener to the ManagedConnection instance.
     *
     * @param        listener        <code>ConnectionEventListener</code>
     * @see <code>removeConnectionEventListener</code>
     */
    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        this.listener = listener;
    }

    /**
     * Used by the container to change the association of an application-level
     * connection handle with a <code>ManagedConnection</code> instance.
     *
     * @param        connection        <code>ConnectionHolder</code> to be associated with
     *                                this <code>ManagedConnection</code> instance
     * @throws        ResourceException        if the physical connection is no more
     *                                        valid or the connection handle passed is null
     */
    @Override
    public void associateConnection(Object connection) throws ResourceException {
        if(logWriter != null) {
            logWriter.println("In associateConnection");
        }
        checkIfValid();
        if(connection == null) {
            throw new ResourceException("Connection handle cannot be null");
        }
        ConnectionHolder ch = (ConnectionHolder) connection;

        com.sun.jdbcra.spi.ManagedConnection mc = ch.getManagedConnection();
        mc.activeConnectionHandle = null;
        isClean = false;

        ch.associateConnection(actualConnection, this);
        /**
         * The expectation from the above method is that the connection holder
         * replaces the actual sql connection it holds with the sql connection
         * handle being passed in this method call. Also, it replaces the reference
         * to the ManagedConnection instance with this ManagedConnection instance.
         * Any previous statements and result sets also need to be removed.
         */

         if(activeConnectionHandle != null) {
            activeConnectionHandle.setActive(false);
        }

        ch.setActive(true);
        activeConnectionHandle = ch;
    }

    /**
     * Application server calls this method to force any cleanup on the
     * <code>ManagedConnection</code> instance. This method calls the invalidate
     * method on all ConnectionHandles associated with this <code>ManagedConnection</code>.
     *
     * @throws        ResourceException        if the physical connection is no more valid
     */
    @Override
    public void cleanup() throws ResourceException {
        if(logWriter != null) {
                logWriter.println("In cleanup");
        }
        checkIfValid();

        /**
         * may need to set the autocommit to true for the non-pooled case.
         */
        //GJCINT
        //if (actualConnection != null) {
        if (connectionType == ISNOTAPOOLEDCONNECTION ) {
        try {
            isolationLevelWhenCleaned = actualConnection.getTransactionIsolation();
        } catch(SQLException sqle) {
            throw new ResourceException("The isolation level for the physical connection "
                + "could not be retrieved");
        }
        }
        isClean = true;

        activeConnectionHandle = null;
    }

    /**
     * This method removes all the connection handles from the table
     * of connection handles and invalidates all of them so that any
     * operation on those connection handles throws an exception.
     *
     * @throws        ResourceException        if there is a problem in retrieving
     *                                         the connection handles
     */
    private void invalidateAllConnectionHandles() throws ResourceException {
        Set handles = connectionHandles.keySet();
        Iterator iter = handles.iterator();
        try {
            while(iter.hasNext()) {
                ConnectionHolder ch = (ConnectionHolder)iter.next();
                ch.invalidate();
            }
        } catch(java.util.NoSuchElementException nsee) {
            throw new ResourceException("Could not find the connection handle: "+ nsee.getMessage());
        }
        connectionHandles.clear();
    }

    /**
     * Destroys the physical connection to the underlying resource manager.
     *
     * @throws        ResourceException        if there is an error in closing the physical connection
     */
    @Override
    public void destroy() throws ResourceException{
        if(logWriter != null) {
            logWriter.println("In destroy");
        }
        //GJCINT
        if(isDestroyed == true) {
            return;
        }

        activeConnectionHandle = null;
        try {
            if(connectionType == ISXACONNECTION || connectionType == ISPOOLEDCONNECTION) {
                pc.close();
                pc = null;
                actualConnection = null;
            } else {
                actualConnection.close();
                actualConnection = null;
            }
        } catch(SQLException sqle) {
            isDestroyed = true;
            passwdCredential = null;
            connectionHandles = null;
            throw new ResourceException("The following exception has occured during destroy: "
                + sqle.getMessage());
        }
        isDestroyed = true;
        passwdCredential = null;
        connectionHandles = null;
    }

    /**
     * Creates a new connection handle for the underlying physical
     * connection represented by the <code>ManagedConnection</code> instance.
     *
     * @param        subject        <code>Subject</code> parameter needed for authentication
     * @param        cxReqInfo        <code>ConnectionRequestInfo</code> carries the user
     *                                and password required for getting this connection.
     * @return        Connection        the connection handle <code>Object</code>
     * @throws        ResourceException        if there is an error in allocating the
     *                                         physical connection from the pooled connection
     * @throws        SecurityException        if there is a mismatch between the
     *                                         password credentials or reauthentication is requested
     */
    @Override
    public Object getConnection(Subject sub, jakarta.resource.spi.ConnectionRequestInfo cxReqInfo)
        throws ResourceException {
        if(logWriter != null) {
            logWriter.println("In getConnection");
        }
        checkIfValid();
        com.sun.jdbcra.spi.ConnectionRequestInfo cxRequestInfo = (com.sun.jdbcra.spi.ConnectionRequestInfo) cxReqInfo;
        PasswordCredential passwdCred = SecurityUtils.getPasswordCredential(this.mcf, sub, cxRequestInfo);

        if(SecurityUtils.isPasswordCredentialEqual(this.passwdCredential, passwdCred) == false) {
            throw new jakarta.resource.spi.SecurityException("Re-authentication not supported");
        }

        //GJCINT
        getActualConnection();

        /**
         * The following code in the if statement first checks if this ManagedConnection
         * is clean or not. If it is, it resets the transaction isolation level to what
         * it was when it was when this ManagedConnection was cleaned up depending on the
         * ConnectionRequestInfo passed.
         */
        if(isClean) {
            ((com.sun.jdbcra.spi.ManagedConnectionFactory)mcf).resetIsolation(this, isolationLevelWhenCleaned);
        }


        ConnectionHolder connHolderObject = new ConnectionHolder(actualConnection, this);
        isClean=false;

        if(activeConnectionHandle != null) {
            activeConnectionHandle.setActive(false);
        }

        connHolderObject.setActive(true);
        activeConnectionHandle = connHolderObject;

        return connHolderObject;

    }

    /**
     * Returns an <code>LocalTransaction</code> instance. The <code>LocalTransaction</code> interface
     * is used by the container to manage local transactions for a RM instance.
     *
     * @return        <code>LocalTransaction</code> instance
     * @throws        ResourceException        if the physical connection is not valid
     */
    @Override
    public jakarta.resource.spi.LocalTransaction getLocalTransaction() throws ResourceException {
        if(logWriter != null) {
            logWriter.println("In getLocalTransaction");
        }
        checkIfValid();
        return new com.sun.jdbcra.spi.LocalTransaction(this);
    }

    /**
     * Gets the log writer for this <code>ManagedConnection</code> instance.
     *
     * @return        <code>PrintWriter</code> instance associated with this
     *                <code>ManagedConnection</code> instance
     * @throws        ResourceException        if the physical connection is not valid
     * @see <code>setLogWriter</code>
     */
    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        if(logWriter != null) {
                logWriter.println("In getLogWriter");
        }
        checkIfValid();

        return logWriter;
    }

    /**
     * Gets the metadata information for this connection's underlying EIS
     * resource manager instance.
     *
     * @return        <code>ManagedConnectionMetaData</code> instance
     * @throws        ResourceException        if the physical connection is not valid
     */
    @Override
    public jakarta.resource.spi.ManagedConnectionMetaData getMetaData() throws ResourceException {
        if(logWriter != null) {
                logWriter.println("In getMetaData");
        }
        checkIfValid();

        return new com.sun.jdbcra.spi.ManagedConnectionMetaData(this);
    }

    /**
     * Returns an <code>XAResource</code> instance.
     *
     * @return        <code>XAResource</code> instance
     * @throws        ResourceException        if the physical connection is not valid or
     *                                        there is an error in allocating the
     *                                        <code>XAResource</code> instance
     * @throws        NotSupportedException        if underlying datasource is not an
     *                                        <code>XADataSource</code>
     */
    @Override
    public XAResource getXAResource() throws ResourceException {
        if (logWriter != null) {
            logWriter.println("In getXAResource");
        }
        checkIfValid();

        if (connectionType != ISXACONNECTION) {
            throw new NotSupportedException("Cannot get an XAResource from a non XA connection");
        }
        try {
            if(xar == null) {
                xar = new com.sun.jdbcra.spi.XAResourceImpl(((XAConnection)pc).getXAResource(), this);
            }
            return xar;
        } catch(SQLException sqle) {
            throw new ResourceException(sqle.getMessage());
        }
    }


    /**
     * Removes an already registered connection event listener from the
     * <code>ManagedConnection</code> instance.
     *
     * @param listener <code>ConnectionEventListener</code> to be removed
     */
    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        if (this.listener == listener) {
            this.listener = null;
        }
    }

    /**
     * This method is called from XAResource wrapper object
     * when its XAResource.start() has been called or from
     * LocalTransaction object when its begin() method is called.
     */
    void transactionStarted() {
        transactionInProgress = true;
    }

    /**
     * This method is called from XAResource wrapper object
     * when its XAResource.end() has been called or from
     * LocalTransaction object when its end() method is called.
     */
    void transactionCompleted() {
        transactionInProgress = false;
        if(connectionType == ISPOOLEDCONNECTION || connectionType == ISXACONNECTION) {
            try {
                isolationLevelWhenCleaned = actualConnection.getTransactionIsolation();
            } catch(SQLException sqle) {
                //check what to do in this case!!
                _logger.log(Level.WARNING, "jdbc.notgot_tx_isolvl");
            }

            try {
                actualConnection.close();
                actualConnection = null;
            } catch(SQLException sqle) {
                actualConnection = null;
            }
        }


        isClean = true;

        activeConnectionHandle = null;

    }

    /**
     * Checks if a this ManagedConnection is involved in a transaction
     * or not.
     */
    public boolean isTransactionInProgress() {
        return transactionInProgress;
    }

    /**
     * Sets the log writer for this <code>ManagedConnection</code> instance.
     *
     * @param        out        <code>PrintWriter</code> to be associated with this
     *                        <code>ManagedConnection</code> instance
     * @throws        ResourceException        if the physical connection is not valid
     * @see <code>getLogWriter</code>
     */
    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        checkIfValid();
        logWriter = out;
    }

    /**
     * This method determines the type of the connection being held
     * in this <code>ManagedConnection</code>.
     *
     * @param        pooledConn        <code>PooledConnection</code>
     * @return        connection type
     */
    private static int getConnectionType(PooledConnection pooledConn) {
        if (pooledConn == null) {
            return ISNOTAPOOLEDCONNECTION;
        } else if (pooledConn instanceof XAConnection) {
            return ISXACONNECTION;
        } else {
            return ISPOOLEDCONNECTION;
        }
    }

    /**
     * Returns the <code>ManagedConnectionFactory</code> instance that
     * created this <code>ManagedConnection</code> instance.
     *
     * @return        <code>ManagedConnectionFactory</code> instance that created this
     *                <code>ManagedConnection</code> instance
     */
    ManagedConnectionFactory getManagedConnectionFactory() {
        return (com.sun.jdbcra.spi.ManagedConnectionFactory)mcf;
    }

    /**
     * Returns the actual sql connection for this <code>ManagedConnection</code>.
     *
     * @return        the physical <code>java.sql.Connection</code>
     */
    //GJCINT
    java.sql.Connection getActualConnection() throws ResourceException {
        //GJCINT
        if(connectionType == ISXACONNECTION || connectionType == ISPOOLEDCONNECTION) {
            try {
                if(actualConnection == null) {
                    actualConnection = pc.getConnection();
                }

            } catch(SQLException sqle) {
                sqle.printStackTrace();
                throw new ResourceException(sqle.getMessage());
            }
        }
        return actualConnection;
    }

    /**
     * Returns the <code>PasswordCredential</code> object associated with this <code>ManagedConnection</code>.
     *
     * @return        <code>PasswordCredential</code> associated with this
     *                <code>ManagedConnection</code> instance
     */
    PasswordCredential getPasswordCredential() {
        return passwdCredential;
    }

    /**
     * Checks if this <code>ManagedConnection</code> is valid or not and throws an
     * exception if it is not valid. A <code>ManagedConnection</code> is not valid if
     * destroy has not been called and no physical connection error has
     * occurred rendering the physical connection unusable.
     *
     * @throws        ResourceException        if <code>destroy</code> has been called on this
     *                                        <code>ManagedConnection</code> instance or if a
     *                                         physical connection error occurred rendering it unusable
     */
    //GJCINT
    void checkIfValid() throws ResourceException {
        if(isDestroyed == true || isUsable == false) {
            throw new ResourceException("This ManagedConnection is not valid as the physical " +
                "connection is not usable.");
        }
    }

    /**
     * This method is called by the <code>ConnectionHolder</code> when its close method is
     * called. This <code>ManagedConnection</code> instance  invalidates the connection handle
     * and sends a CONNECTION_CLOSED event to all the registered event listeners.
     *
     * @param        e        Exception that may have occured while closing the connection handle
     * @param        connHolderObject        <code>ConnectionHolder</code> that has been closed
     * @throws        SQLException        in case closing the sql connection got out of
     *                                     <code>getConnection</code> on the underlying
     *                                <code>PooledConnection</code> throws an exception
     */
    void connectionClosed(Exception e, ConnectionHolder connHolderObject) throws SQLException {
        connHolderObject.invalidate();

        activeConnectionHandle = null;

        ce.setConnectionHandle(connHolderObject);
        if (listener != null) {
            listener.connectionClosed(ce);
        }
    }

    /**
     * This method is called by the <code>ConnectionHolder</code> when it detects a connection
     * related error.
     *
     * @param        e        Exception that has occurred during an operation on the physical connection
     * @param        connHolderObject        <code>ConnectionHolder</code> that detected the physical
     *                                        connection error
     */
    void connectionErrorOccurred(Exception e,
            com.sun.jdbcra.spi.ConnectionHolder connHolderObject) {

         ConnectionEvent ce = null;
         ce = e == null ? new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED)
                    : new ConnectionEvent(this, ConnectionEvent.CONNECTION_ERROR_OCCURRED, e);
         if (connHolderObject != null) {
             ce.setConnectionHandle(connHolderObject);
         }

         if (listener != null) {
             listener.connectionErrorOccurred(ce);
         }
         isUsable = false;
    }

    /**
     * This method is called by the <code>XAResource</code> object when its start method
     * has been invoked.
     *
     */
    void XAStartOccurred() {
        try {
            actualConnection.setAutoCommit(false);
        } catch(Exception e) {
            e.printStackTrace();
            connectionErrorOccurred(e, null);
        }
    }

    /**
     * This method is called by the <code>XAResource</code> object when its end method
     * has been invoked.
     *
     */
    void XAEndOccurred() {
        try {
            actualConnection.setAutoCommit(true);
        } catch(Exception e) {
            e.printStackTrace();
            connectionErrorOccurred(e, null);
        }
    }

    /**
     * This method is called by a Connection Handle to check if it is
     * the active Connection Handle. If it is not the active Connection
     * Handle, this method throws an SQLException. Else, it
     * returns setting the active Connection Handle to the calling
     * Connection Handle object to this object if the active Connection
     * Handle is null.
     *
     * @param        ch        <code>ConnectionHolder</code> that requests this
     *                        <code>ManagedConnection</code> instance whether
     *                        it can be active or not
     * @throws        SQLException        in case the physical is not valid or
     *                                there is already an active connection handle
     */

    void checkIfActive(ConnectionHolder ch) throws SQLException {
        if(isDestroyed == true || isUsable == false) {
            throw new SQLException("The physical connection is not usable");
        }

        if(activeConnectionHandle == null) {
            activeConnectionHandle = ch;
            ch.setActive(true);
            return;
        }

        if(activeConnectionHandle != ch) {
            throw new SQLException("The connection handle cannot be used as another connection is currently active");
        }
    }
}
