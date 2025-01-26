/*
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.appserv.connectors.internal.spi.BadConnectionEventListener;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.gjc.common.DataSourceObjectBuilder;
import com.sun.gjc.spi.base.CacheObjectKey;
import com.sun.gjc.spi.base.CallableStatementWrapper;
import com.sun.gjc.spi.base.ConnectionHolder;
import com.sun.gjc.spi.base.ConnectionWrapper;
import com.sun.gjc.spi.base.PreparedStatementWrapper;
import com.sun.gjc.spi.base.datastructure.Cache;
import com.sun.gjc.spi.base.datastructure.CacheFactory;
import com.sun.gjc.util.SQLTraceDelegator;
import com.sun.gjc.util.StatementLeakDetector;
import com.sun.logging.LogDomains;

import jakarta.resource.NotSupportedException;
import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionEvent;
import jakarta.resource.spi.ConnectionEventListener;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.DissociatableManagedConnection;
import jakarta.resource.spi.LazyEnlistableManagedConnection;
import jakarta.resource.spi.LocalTransaction;
import jakarta.resource.spi.ManagedConnection;
import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.resource.spi.ManagedConnectionMetaData;
import jakarta.resource.spi.security.PasswordCredential;

import java.io.PrintWriter;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.logging.Logger;

import javax.security.auth.Subject;
import javax.sql.PooledConnection;
import javax.sql.XAConnection;
import javax.transaction.xa.XAResource;

import org.glassfish.resourcebase.resources.api.PoolInfo;

import static jakarta.resource.spi.ConnectionEvent.CONNECTION_ERROR_OCCURRED;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

/**
 * <code>ManagedConnection</code> implementation for Generic JDBC Connector.
 *
 * @author Evani Sai Surya Kiran
 * @version 1.0, 02/07/22
 */
public class ManagedConnectionImpl
        implements ManagedConnection, LazyEnlistableManagedConnection, DissociatableManagedConnection {

    protected static final Logger _logger = LogDomains.getLogger(ManagedConnectionImpl.class, LogDomains.RSR_LOGGER);
    protected static final StringManager localStrings = StringManager.getManager(DataSourceObjectBuilder.class);

    public static final int ISNOTAPOOLEDCONNECTION = 0;
    public static final int ISPOOLEDCONNECTION = 1;
    public static final int ISXACONNECTION = 2;

    protected boolean isDestroyed;
    protected boolean isUsable = true;
    protected boolean initSqlExecuted;
    protected int connectionCount;

    protected int connectionType = ISNOTAPOOLEDCONNECTION;
    protected PooledConnection pooledConnection;
    protected java.sql.Connection actualConnection;
    protected Hashtable connectionHandles;
    protected PrintWriter logWriter;
    protected PasswordCredential passwdCredential;
    private ManagedConnectionFactory managedConnectionFactory;
    protected XAResource xaResource;

    protected ConnectionHolder myLogicalConnection;

    protected int lastTransactionIsolationLevel;
    protected boolean isClean = true;

    protected boolean transactionInProgress;

    protected ConnectionEventListener listener;
    protected ConnectionEvent connectionEvent;

    private boolean defaultAutoCommitValue = true;
    private boolean lastAutoCommitValue = defaultAutoCommitValue;

    private boolean markedForRemoval;
    private int statementTimeout;

    private Cache statementCache;
    private int cacheSize;
    private String cacheType;
    private boolean statementCaching;
    private long stmtLeakTimeout;
    private boolean stmtLeakReclaim;
    private boolean statementLeakTracing;
    protected StatementLeakDetector leakDetector;

    private SQLTraceDelegator sqlTraceDelegator;

    private boolean aborted;

    private DatabaseMetaData cachedDatabaseMetaData;
    private Boolean isClientInfoSupported;

    /**
     * Constructor for <code>ManagedConnectionImpl</code>. The pooledConn parameter is expected to be null and sqlConn
     * parameter is the actual connection in case where the actual connection is got from a non pooled datasource object.
     * The pooledConn parameter is expected to be non null and sqlConn parameter is expected to be null in the case where
     * the datasource object is a connection pool datasource or an xa datasource.
     *
     * @param pooledConn <code>PooledConnection</code> object in case the physical connection is to be obtained from a
     * pooled <code>DataSource</code>; null otherwise
     * @param sqlConn <code>java.sql.Connection</code> object in case the physical connection is to be obtained from a non
     * pooled <code>DataSource</code>; null otherwise
     * @param passwdCred object containing the user and password for allocating the connection, value is allowed to be null.
     * @param mcf the reference to the ManagedConnectionFactory instance that created this ManagedConnectionImpl instance.
     * @param poolInfo Name of the pool
     * @param statementCacheSize Statement caching is usually a feature of the JDBC driver. The GlassFish Server provides
     * caching for drivers that do not support caching. To enable this feature, set the Statement Cache Size. By default,
     * this attribute is set to zero and the statement caching is turned off. To enable statement caching, you can set any
     * positive nonzero value. The built-in cache eviction strategy is LRU-based (Least Recently Used). When a connection
     * pool is flushed, the connections in the statement cache are recreated.<br>
     * Configured via create-jdbc-connection-pool --statementcachesize
     * @param statementCacheType In case statementCacheSize is not 0 this defines the statement cache type to be used. Valid
     * values are defined in com.sun.gjc.spi.base.datastructure.CacheFactory. Value null or "" uses an LRU Cache
     * implementation. Value FIXED uses FIXED size cache implementation. Any other values are expected to be a className for
     * a cache implementation.
     * @param delegator optional SqlTraceDelegator, value is allowed to be null.
     * @param statementLeakTimeout statement leak timeout in seconds.<br>
     * Configured via create-jdbc-connection-pool --statementleaktimeout
     * @param statementLeakReclaim true if statements need to be reclaimed.<br>
     * Configured via create-jdbc-connection-pool --statementleakreclaim
     * @throws ResourceException if the <code>ManagedConnectionFactory</code> object that created this
     * <code>ManagedConnectionImpl</code> object is not the same as returned by <code>PasswordCredential</code> object
     * passed. And throws ResourceException in case both pooledConn and sqlConn are null.
     */
    public ManagedConnectionImpl(PooledConnection pooledConn, Connection sqlConn, PasswordCredential passwdCred,
            ManagedConnectionFactory mcf, PoolInfo poolInfo, int statementCacheSize, String statementCacheType,
            SQLTraceDelegator delegator, long statementLeakTimeout, boolean statementLeakReclaim)
            throws ResourceException {

        if (pooledConn == null && sqlConn == null) {
            String i18nMsg = localStrings.getString("jdbc.conn_obj_null");
            throw new ResourceException(i18nMsg);
        }

        if (connectionType == ISNOTAPOOLEDCONNECTION) {
            actualConnection = sqlConn;
        }

        pooledConnection = pooledConn;
        connectionHandles = new Hashtable();
        passwdCredential = passwdCred;
        sqlTraceDelegator = delegator;

        this.managedConnectionFactory = mcf;
        if (passwdCredential != null && this.managedConnectionFactory.equals(passwdCredential.getManagedConnectionFactory()) == false) {
            String i18nMsg = localStrings.getString("jdbc.mc_construct_err");
            throw new ResourceException(i18nMsg);
        }
        logWriter = mcf.getLogWriter();
        connectionEvent = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
        tuneStatementCaching(poolInfo, statementCacheSize, statementCacheType);
        tuneStatementLeakTracing(poolInfo, statementLeakTimeout, statementLeakReclaim);
    }

    public StatementLeakDetector getLeakDetector() {
        return leakDetector;
    }

    private void executeInitSql(final String initSql) {
        _logger.log(FINE, "jdbc.execute_init_sql_start");
        PreparedStatement statement = null;

        if (initSql != null && !initSql.equalsIgnoreCase("null") && !initSql.equals("")) {
            try {
                statement = actualConnection.prepareStatement(initSql);
                _logger.log(FINE, "jdbc.executing_init_sql", initSql);
                statement.execute();
            } catch (SQLException sqle) {
                _logger.log(WARNING, "jdbc.exc_init_sql_error", initSql);
                initSqlExecuted = false;
            } finally {
                try {
                    if (statement != null) {
                        statement.close();
                    }
                } catch (Exception e) {
                    if (_logger.isLoggable(FINE)) {
                        _logger.log(FINE, "jdbc.exc_init_sql_error_stmt_close", e.getMessage());
                    }
                }
            }
            initSqlExecuted = true;
        }
        _logger.log(FINE, "jdbc.execute_init_sql_end");
    }

    private void tuneStatementCaching(PoolInfo poolInfo, int statementCacheSize, String statementCacheType) {
        cacheSize = statementCacheSize;
        cacheType = statementCacheType;
        if (cacheSize > 0) {
            try {
                statementCache = CacheFactory.getDataStructure(poolInfo, cacheType, cacheSize);
                statementCaching = true;
            } catch (ResourceException ex) {
                _logger.severe(ex.getMessage());
            }
        }
    }

    private void tuneStatementLeakTracing(PoolInfo poolInfo, long statementLeakTimeout, boolean statementLeakReclaim) {
        stmtLeakTimeout = statementLeakTimeout;
        stmtLeakReclaim = statementLeakReclaim;

        if (stmtLeakTimeout > 0) {
            ManagedConnectionFactoryImpl managedConnectionFactoryImpl = (ManagedConnectionFactoryImpl) managedConnectionFactory;
            statementLeakTracing = true;
            if (leakDetector == null) {
                leakDetector =
                    new StatementLeakDetector(
                        poolInfo, statementLeakTracing, stmtLeakTimeout,
                        stmtLeakReclaim,
                        ((ResourceAdapterImpl) managedConnectionFactoryImpl.getResourceAdapter()).getTimer());
            }
        }
    }

    /**
     * Adds a connection event listener to the ManagedConnectionImpl instance.
     *
     * @param listener <code>ConnectionEventListener</code>
     * @see <code>removeConnectionEventListener</code>
     */
    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        this.listener = listener;
    }

    /**
     * Used by the container to change the association of an application-level
     * connection handle with a <code>ManagedConnectionImpl</code> instance.
     *
     * @param connection <code>ConnectionHolder</code> to be associated with this
     * <code>ManagedConnectionImpl</code> instance
     * @throws ResourceException if the physical connection is no more valid or the
     * connection handle passed is null
     */
    @Override
    public void associateConnection(Object connection) throws ResourceException {
        logFine("In associateConnection");
        checkIfValid();
        if (connection == null) {
            throw new ResourceException(localStrings.getString("jdbc.conn_handle_null"));
        }

        ConnectionHolder connectionHolder = (ConnectionHolder) connection;
        ManagedConnectionImpl managedConnectionImpl = connectionHolder.getManagedConnection();
        isClean = false;

        connectionHolder.associateConnection(actualConnection, this);

        /**
         * The expectation from the above method is that the connection holder replaces
         * the actual sql connection it holds with the sql connection handle being
         * passed in this method call. Also, it replaces the reference to the
         * ManagedConnectionImpl instance with this ManagedConnectionImpl instance. Any
         * previous statements and result sets also need to be removed.
         */

        connectionHolder.setActive();
        incrementCount();

        // associate the MC to the supplied logical connection similar to associating
        // the logical connection
        // with this MC and actual-connection.
        myLogicalConnection = connectionHolder;

        // managedConnection will be null in case we are lazily associating
        if (managedConnectionImpl != null) {
            managedConnectionImpl.decrementCount();
        }
    }

    /**
     * Application server calls this method to force any cleanup on the
     * <code>ManagedConnectionImpl</code> instance. This method calls the invalidate
     * method on all ConnectionHandles associated with this
     * <code>ManagedConnectionImpl</code>.
     *
     * @throws ResourceException if the physical connection is no more valid
     */
    @Override
    public void cleanup() throws ResourceException {
        logFine("In cleanup");

        /**
         * may need to set the autocommit to true for the non-pooled case.
         */
        isClean = true;

        resetConnectionProperties((ManagedConnectionFactoryImpl) managedConnectionFactory);
    }

    /**
     * This method removes all the connection handles from the table of connection
     * handles and invalidates all of them so that any operation on those connection
     * handles throws an exception.
     *
     * @throws ResourceException if there is a problem in retrieving the connection
     * handles
     */
    protected void invalidateAllConnectionHandles() throws ResourceException {
        Set handles = connectionHandles.keySet();
        Iterator iter = handles.iterator();
        try {
            while (iter.hasNext()) {
                ConnectionHolder ch = (ConnectionHolder) iter.next();
                ch.invalidate();
            }
        } catch (NoSuchElementException nsee) {
            throw new ResourceException("Could not find the connection handle: " + nsee.getMessage(), nsee);
        }
        connectionHandles.clear();
    }

    private void clearStatementCache() {
        if (statementCache != null) {
            _logger.fine("Closing statements in statement cache");
            statementCache.flushCache();
            statementCache.clearCache();
        }
    }

    /**
     * Destroys the physical connection to the underlying resource manager.
     *
     * @throws ResourceException if there is an error in closing the physical
     * connection
     */
    @Override
    public void destroy() throws ResourceException {
        logFine("In destroy");
        if (isDestroyed) {
            return;
        }
        clearStatementCache();

        // Connection could be closed even before statement is closed. Connection
        // close need not call statement close() method.
        // When application uses a statement, leakDetector could be started.At
        // this point, there is a need to clear the statement leak tasks
        if (leakDetector != null) {
            leakDetector.clearAllStatementLeakTasks();
        }

        try {
            if (connectionType == ISXACONNECTION || connectionType == ISPOOLEDCONNECTION) {
                pooledConnection.close();
                pooledConnection = null;
                actualConnection = null;
            } else {
                actualConnection.close();
                actualConnection = null;
            }
        } catch (SQLException sqle) {
            isDestroyed = true;
            passwdCredential = null;
            connectionHandles = null;
            throw new ResourceException(localStrings.getString("jdbc.error_in_destroy") + sqle.getMessage(), sqle);
        }

        isDestroyed = true;
        passwdCredential = null;
        connectionHandles = null;
    }

    /**
     * Creates a new connection handle for the underlying physical connection
     * represented by the <code>ManagedConnectionImpl</code> instance.
     *
     * @param sub <code>Subject</code> parameter needed for authentication
     * @param cxReqInfo <code>ConnectionRequestInfo</code> carries the user and
     * password required for getting this connection.
     * @return Connection the connection handle <code>Object</code>
     * @throws ResourceException if there is an error in allocating the physical
     * connection from the pooled connection
     * @throws jakarta.resource.spi.SecurityException if there is a mismatch between
     * the password credentials or reauthentication is requested
     */
    @Override
    public Object getConnection(Subject sub, ConnectionRequestInfo cxReqInfo) throws ResourceException {
        logFine("In getConnection");
        checkIfValid();

        getActualConnection();
        ManagedConnectionFactoryImpl managedConnectionFactoryImpl = (ManagedConnectionFactoryImpl) managedConnectionFactory;

        String statementTimeoutString = managedConnectionFactoryImpl.getStatementTimeout();
        if (statementTimeoutString != null) {
            int timeoutValue = Integer.parseInt(statementTimeoutString);
            if (timeoutValue >= 0) {
                statementTimeout = timeoutValue;
            }
        }

        myLogicalConnection =
            managedConnectionFactoryImpl.getJdbcObjectsFactory()
                                        .getConnection(
                                            actualConnection, this, cxReqInfo,
                                            managedConnectionFactoryImpl.isStatementWrappingEnabled(),
                                            sqlTraceDelegator);

        // TODO : need to see if this should be executed for every getConnection
        if (!initSqlExecuted) {
            // Check if Initsql is set and execute it
            String initSql = managedConnectionFactoryImpl.getInitSql();
            executeInitSql(initSql);
        }
        incrementCount();
        isClean = false;

        myLogicalConnection.setActive();

        return myLogicalConnection;
    }

    /**
     * Resett connection properties as connections are pooled by application
     * server<br>
     *
     * @param managedConnectionFactoryImpl
     * @throws ResourceException
     */
    private void resetConnectionProperties(ManagedConnectionFactoryImpl managedConnectionFactoryImpl) throws ResourceException {
        if (isClean) {

            // If the ManagedConnection is clean, reset the transaction isolation level to
            // what it was when it was when this ManagedConnection was cleaned up depending on
            // the ConnectionRequestInfo passed.
            managedConnectionFactoryImpl.resetIsolation(this, lastTransactionIsolationLevel);

            // Reset the autocommit value of the connection if application has modified it.
            resetAutoCommit();
        }
    }

    /**
     * To reset AutoCommit of actual connection. If the last-auto-commit value is
     * different from default-auto-commit value, reset will happen. If there is a
     * transaction in progress (because of connection sharing), reset will not
     * happen.
     *
     * @throws ResourceException
     */
    private void resetAutoCommit() throws ResourceException {
        if (defaultAutoCommitValue != getLastAutoCommitValue() && !(isTransactionInProgress())) {
            try {
                actualConnection.setAutoCommit(defaultAutoCommitValue);
            } catch (SQLException sqle) {
                throw new ResourceException(localStrings.getString("jdbc.error_during_setAutoCommit") + sqle.getMessage(), sqle);
            }

            setLastAutoCommitValue(defaultAutoCommitValue);
        }
    }

    /**
     * Returns an <code>LocalTransactionImpl</code> instance. The
     * <code>LocalTransactionImpl</code> interface is used by the container to
     * manage local transactions for a RM instance.
     *
     * @return <code>LocalTransactionImpl</code> instance
     * @throws ResourceException if the physical connection is not valid
     */
    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        logFine("In getLocalTransaction");
        checkIfValid();
        return new LocalTransactionImpl(this);
    }

    /**
     * Gets the log writer for this <code>ManagedConnectionImpl</code> instance.
     *
     * @return <code>PrintWriter</code> instance associated with this
     * <code>ManagedConnectionImpl</code> instance
     * @throws ResourceException if the physical connection is not valid
     * @see <code>setLogWriter</code>
     */
    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        logFine("In getLogWriter");
        checkIfValid();

        return logWriter;
    }

    /**
     * Gets the metadata information for this connection's underlying EIS resource
     * manager instance.
     *
     * @return <code>ManagedConnectionMetaData</code> instance
     * @throws ResourceException if the physical connection is not valid
     */
    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        logFine("In getMetaData");
        checkIfValid();

        return new ManagedConnectionMetaDataImpl(this);
    }

    /**
     * Returns an <code>XAResource</code> instance.
     *
     * @return <code>XAResource</code> instance
     * @throws ResourceException if the physical connection is not valid or there is
     * an error in allocating the <code>XAResource</code> instance
     * @throws NotSupportedException if underlying datasource is not an
     * <code>XADataSource</code>
     */
    @Override
    public XAResource getXAResource() throws ResourceException {
        logFine("In getXAResource");
        checkIfValid();

        if (connectionType == ISXACONNECTION) {
            try {
                if (xaResource == null) {
                    /**
                     * Using the wrapper XAResource.
                     */
                    xaResource = new XAResourceImpl(((XAConnection) pooledConnection).getXAResource(), this);
                }
                return xaResource;
            } catch (SQLException sqle) {
                throw new ResourceException(sqle.getMessage(), sqle);
            }
        } else {
            throw new NotSupportedException("Cannot get an XAResource from a non XA connection");
        }
    }

    /**
     * Removes an already registered connection event listener from the
     * <code>ManagedConnectionImpl</code> instance.
     *
     * @param listener <code>ConnectionEventListener</code> to be removed
     * @see <code>addConnectionEventListener</code>
     */
    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        if (this.listener == listener) {
            this.listener = null;
        }
    }

    /**
     * This method is called from XAResource wrapper object when its
     * XAResource.start() has been called or from LocalTransactionImpl object when
     * its begin() method is called.
     */
    void transactionStarted() {
        transactionInProgress = true;
    }

    /**
     * This method is called from XAResource wrapper object when its
     * XAResource.end() has been called or from LocalTransactionImpl object when its
     * end() method is called.
     */
    void transactionCompleted() {
        try {
            transactionInProgress = false;
            if (connectionType == ISPOOLEDCONNECTION || connectionType == ISXACONNECTION) {
                if (connectionCount <= 0) {
                    try {
                        actualConnection.close();
                        actualConnection = null;
                    } catch (SQLException sqle) {
                        actualConnection = null;
                    }
                }
            }
        } catch (java.lang.NullPointerException e) {
            _logger.log(FINE, "jdbc.duplicateTxCompleted");
        }

        if (markedForRemoval) {
            if (aborted) {
                BadConnectionEventListener badConnectionEventListener = (BadConnectionEventListener) listener;
                badConnectionEventListener.connectionAbortOccurred(connectionEvent);
                _logger.log(INFO, "jdbc.markedForRemoval_conAborted");
                markedForRemoval = false;
                myLogicalConnection.setClosed(true);
            } else {
                connectionErrorOccurred(null, null);
                _logger.log(INFO, "jdbc.markedForRemoval_txCompleted");
                markedForRemoval = false;
            }
        }

        isClean = true;
    }

    /**
     * Checks if a this ManagedConnection is involved in a transaction or not.
     */
    public boolean isTransactionInProgress() {
        return transactionInProgress;
    }

    /**
     * Sets the log writer for this <code>ManagedConnectionImpl</code> instance.
     *
     * @param out <code>PrintWriter</code> to be associated with this
     * <code>ManagedConnectionImpl</code> instance
     * @throws ResourceException if the physical connection is not valid
     * @see <code>getLogWriter</code>
     */
    public void setLogWriter(PrintWriter out) throws ResourceException {
        checkIfValid();
        logWriter = out;
    }

    /**
     * This method determines the type of the connection being held in this
     * <code>ManagedConnectionImpl</code>.
     *
     * @param pooledConn <code>PooledConnection</code>
     * @return connection type
     */
    protected int getConnectionType(PooledConnection pooledConn) {
        if (pooledConn == null) {
            return ISNOTAPOOLEDCONNECTION;
        }

        if (pooledConn instanceof XAConnection) {
            return ISXACONNECTION;
        }

        return ISPOOLEDCONNECTION;
    }

    /**
     * Returns the <code>ManagedConnectionFactory</code> instance that created this
     * <code>ManagedConnection</code> instance.
     *
     * @return <code>ManagedConnectionFactory</code> instance that created this
     * <code>ManagedConnection</code> instance
     */
    public ManagedConnectionFactoryImpl getManagedConnectionFactory() {
        return (ManagedConnectionFactoryImpl) managedConnectionFactory;
    }

    /**
     * Returns the actual sql connection for this <code>ManagedConnection</code>.
     *
     * @return the physical <code>java.sql.Connection</code>
     */
    Connection getActualConnection() throws ResourceException {
        if (connectionType == ISXACONNECTION || connectionType == ISPOOLEDCONNECTION) {
            try {
                if (actualConnection == null) {
                    actualConnection = pooledConnection.getConnection();

                    // re-initialize lastAutoCommitValue such that resetAutoCommit() wont
                    // affect autoCommit of actualConnection
                    setLastAutoCommitValue(defaultAutoCommitValue);
                }

            } catch (SQLException sqle) {
                throw new ResourceException(sqle.getMessage(), sqle);
            }
        }

        return actualConnection;
    }

    /**
     * Returns the <code>PasswordCredential</code> object associated with this
     * <code>ManagedConnection</code>.
     *
     * @return <code>PasswordCredential</code> associated with this
     * <code>ManagedConnection</code> instance
     */
    PasswordCredential getPasswordCredential() {
        return passwdCredential;
    }

    /**
     * Checks if this <code>ManagedConnection</code> is valid or not and throws an
     * exception if it is not valid. A <code>ManagedConnection</code> is not valid
     * if destroy has not been called and no physical connection error has occurred
     * rendering the physical connection unusable.
     *
     * @throws ResourceException if <code>destroy</code> has been called on this
     * <code>ManagedConnection</code> instance or if a physical connection error
     * occurred rendering it unusable
     */
    void checkIfValid() throws ResourceException {
        if (isDestroyed || !isUsable) {
            throw new ResourceException(localStrings.getString("jdbc.mc_not_usable"));
        }
    }

    /**
     * This method is called by the <code>ConnectionHolder</code> when its close
     * method is called. This <code>ManagedConnection</code> instance invalidates
     * the connection handle and sends a CONNECTION_CLOSED event to all the
     * registered event listeners.
     *
     * @param e Exception that may have occurred while closing the connection handle
     * @param connectionHolder <code>ConnectionHolder</code> that has been
     * closed
     * @throws SQLException in case closing the sql connection got out of
     * <code>getConnection</code> on the underlying <code>PooledConnection</code>
     * throws an exception
     */
    public void connectionClosed(Exception e, ConnectionHolder connectionHolder) throws SQLException {
        connectionHolder.invalidate();
        decrementCount();
        connectionEvent.setConnectionHandle(connectionHolder);

        if (markedForRemoval && !transactionInProgress) {
            BadConnectionEventListener badConnectionEventListener = (BadConnectionEventListener) listener;
            badConnectionEventListener.badConnectionClosed(connectionEvent);
            _logger.log(INFO, "jdbc.markedForRemoval_conClosed");
            markedForRemoval = false;
        } else {
            if (listener != null) {
                listener.connectionClosed(connectionEvent);
            }
        }
    }

    /**
     * This method is called by the <code>ConnectionHolder</code> when it detects
     * a connection related error.
     *
     * @param e Exception that has occurred during an operation on the physical
     * connection
     * @param connectionHolder <code>ConnectionHolder</code> that detected the
     * physical connection error
     */
    void connectionErrorOccurred(Exception e, ConnectionHolder connectionHolder) {
        ConnectionEventListener connectionEventListener = this.listener;

        ConnectionEvent connectionEvent = e == null ?
            new ConnectionEvent(this, CONNECTION_ERROR_OCCURRED) :
            new ConnectionEvent(this, CONNECTION_ERROR_OCCURRED, e);

        if (connectionHolder != null) {
            connectionEvent.setConnectionHandle(connectionHolder);
        }

        connectionEventListener.connectionErrorOccurred(connectionEvent);
        isUsable = false;
    }

    /**
     * This method is called by the <code>XAResource</code> object when its start
     * method has been invoked.
     */
    void XAStartOccurred() {
        try {
            actualConnection.setAutoCommit(false);
        } catch (Exception e) {
            _logger.log(WARNING, "XA Start [ setAutoCommit ] failure ", e);
            connectionErrorOccurred(e, null);
        }
    }

    /**
     * This method is called by the <code>XAResource</code> object when its end
     * method has been invoked.
     */
    void XAEndOccurred() {
        try {
            actualConnection.setAutoCommit(true);
        } catch (Exception e) {
            _logger.log(WARNING, "XA End [ setAutoCommit ] failure ", e);
            connectionErrorOccurred(e, null);
        }
    }

    /**
     * This method is called by a Connection Handle to check if it is the active
     * Connection Handle. If it is not the active Connection Handle, this method
     * throws an SQLException. Else, it returns setting the active Connection Handle
     * to the calling Connection Handle object to this object if the active
     * Connection Handle is null.
     *
     * @param connectionHolder <code>ConnectionHolder</code> that requests this
     * <code>ManagedConnection</code> instance whether it can be active or not
     * @throws SQLException in case the physical is not valid or there is already an
     * active connection handle
     */
    public void checkIfActive(ConnectionHolder connectionHolder) throws SQLException {
        if (isDestroyed || !isUsable) {
            throw new SQLException(localStrings.getString("jdbc.conn_not_usable"));
        }
    }

    /**
     * sets the connection type of this connection. This method is called by the MCF
     * while creating this ManagedConnection. Saves us a costly instanceof operation
     * in the getConnectionType
     */
    public void initializeConnectionType(int _connectionType) {
        connectionType = _connectionType;
    }

    public void incrementCount() {
        connectionCount++;
    }

    public void decrementCount() {
        connectionCount--;
    }

    @Override
    public void dissociateConnections() {
        if (myLogicalConnection != null) {
            myLogicalConnection.dissociateConnection();
            myLogicalConnection = null;
        }
    }

    public boolean getLastAutoCommitValue() {
        return lastAutoCommitValue;
    }

    /**
     * To keep track of last auto commit value. Helps to reset the auto-commit-value
     * while giving new connection-handle.
     *
     * @param lastAutoCommitValue
     */
    public void setLastAutoCommitValue(boolean lastAutoCommitValue) {
        this.lastAutoCommitValue = lastAutoCommitValue;
    }

    public void markForRemoval(boolean flag) {
        markedForRemoval = flag;
    }

    public ManagedConnectionFactory getMcf() {
        return managedConnectionFactory;
    }

    /*
     * public boolean getStatementWrapping(){ return statemntWrapping; }
     */

    public int getStatementTimeout() {
        return statementTimeout;
    }

    public void setLastTransactionIsolationLevel(int isolationLevel) {
        lastTransactionIsolationLevel = isolationLevel;
    }

    /**
     * Returns the cached <code>DatabaseMetaData</code>.
     *
     * @return <code>DatabaseMetaData</code>
     */
    public DatabaseMetaData getCachedDatabaseMetaData() throws ResourceException {
        if (cachedDatabaseMetaData == null) {
            try {
                cachedDatabaseMetaData = getActualConnection().getMetaData();
            } catch (SQLException sqle) {
                throw new ResourceException(sqle.getMessage(), sqle);
            }
        }
        return cachedDatabaseMetaData;
    }

    public Boolean isClientInfoSupported() {
        return isClientInfoSupported;
    }

    public void setClientInfoSupported(Boolean isClientInfoSupported) {
        this.isClientInfoSupported = isClientInfoSupported;
    }

    private void logFine(String logMessage) {
        _logger.log(FINE, logMessage);
    }

    public PreparedStatement prepareCachedStatement(ConnectionWrapper connection, String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        if (statementCaching) {
            CacheObjectKey key =
                new CacheObjectKey(sql, CacheObjectKey.PREPARED_STATEMENT, resultSetType, resultSetConcurrency);

            // TODO-SC should a null check be done for statementCache?
            // TODO-SC refactor this method.
            PreparedStatementWrapper preparedStatement = (PreparedStatementWrapper) statementCache.checkAndUpdateCache(key);

            // TODO-SC-DEFER can the usability (isFree()) check be done by the cache itself
            // and make sure that only a free stmt is returned
            if (preparedStatement != null) {
                if (isFree(preparedStatement)) {
                    // Find if this preparedStatement is a valid one. If invalid, remove it
                    // from the cache and prepare a new stmt & add it to cache
                    if (!preparedStatement.isValid()) {
                        statementCache.purge(preparedStatement);
                        preparedStatement = connection.prepareCachedStatement(sql, resultSetType, resultSetConcurrency, true);
                        preparedStatement.setBusy(true);
                        statementCache.addToCache(key, preparedStatement, false);
                    } else {
                        // Valid preparedStatement
                        preparedStatement.setBusy(true);
                    }
                } else {
                    return connection.prepareCachedStatement(sql, resultSetType, resultSetConcurrency, false);
                }
            } else {
                preparedStatement = connection.prepareCachedStatement(sql, resultSetType, resultSetConcurrency, true);

                preparedStatement.setBusy(true);
                statementCache.addToCache(key, preparedStatement, false);
            }

            return preparedStatement;
        }

        return connection.prepareCachedStatement(sql, resultSetType, resultSetConcurrency, false);
    }

    public PreparedStatement prepareCachedStatement(ConnectionWrapper connection, String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (statementCaching) {
            CacheObjectKey key =
                new CacheObjectKey(sql, CacheObjectKey.PREPARED_STATEMENT, resultSetType, resultSetConcurrency, resultSetHoldability);

            // TODO-SC should a null check be done for statementCache?
            PreparedStatementWrapper preparedStatement = (PreparedStatementWrapper) statementCache.checkAndUpdateCache(key);

            // TODO-SC-DEFER can the usability (isFree()) check be done by the cache itself
            // and make sure that only a free stmt is returned
            if (preparedStatement != null) {
                if (isFree(preparedStatement)) {
                    // Find if this preparedStatement is a valid one. If invalid, remove it
                    // from the cache and prepare a new stmt & add it to cache
                    if (!preparedStatement.isValid()) {
                        statementCache.purge(preparedStatement);
                        preparedStatement = connection.prepareCachedStatement(sql, resultSetType, resultSetConcurrency,
                                resultSetHoldability, true);
                        preparedStatement.setBusy(true);
                        statementCache.addToCache(key, preparedStatement, false);
                    } else {
                        // Valid preparedStatement
                        preparedStatement.setBusy(true);
                    }

                } else {
                    return connection.prepareCachedStatement(sql, resultSetType, resultSetConcurrency,
                            resultSetHoldability, false);
                }
            } else {
                preparedStatement = connection.prepareCachedStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability,
                        true);

                statementCache.addToCache(key, preparedStatement, false);
                preparedStatement.setBusy(true);
            }
            return preparedStatement;
        }

        return connection.prepareCachedStatement(
                   sql, resultSetType, resultSetConcurrency, resultSetHoldability, false);
    }

    public PreparedStatement prepareCachedStatement(ConnectionWrapper connection, String sql, String[] columnNames) throws SQLException {
        if (statementCaching) {
            CacheObjectKey key = new CacheObjectKey(sql, CacheObjectKey.PREPARED_STATEMENT, columnNames);

            // TODO-SC should a null check be done for statementCache?
            PreparedStatementWrapper preparedStatement = (PreparedStatementWrapper) statementCache.checkAndUpdateCache(key);

            // TODO-SC-DEFER can the usability (isFree()) check be done by the cache itself
            // and make sure that only a free stmt is returned
            if (preparedStatement != null) {
                if (isFree(preparedStatement)) {
                    // Find if this preparedStatement is a valid one. If invalid, remove it
                    // from the cache and prepare a new stmt & add it to cache
                    if (!preparedStatement.isValid()) {
                        statementCache.purge(preparedStatement);
                        preparedStatement = connection.prepareCachedStatement(sql, columnNames, true);
                        preparedStatement.setBusy(true);
                        statementCache.addToCache(key, preparedStatement, false);
                    } else {
                        // Valid preparedStatement
                        preparedStatement.setBusy(true);
                    }

                } else {
                    return connection.prepareCachedStatement(sql, columnNames, false);
                }
            } else {
                preparedStatement = connection.prepareCachedStatement(sql, columnNames, true);

                statementCache.addToCache(key, preparedStatement, false);
                preparedStatement.setBusy(true);
            }

            return preparedStatement;
        }

        return connection.prepareCachedStatement(sql, columnNames, false);
    }

    public PreparedStatement prepareCachedStatement(ConnectionWrapper connection, String sql, int[] columnIndexes) throws SQLException {
        if (statementCaching) {
            CacheObjectKey key = new CacheObjectKey(sql, CacheObjectKey.PREPARED_STATEMENT, columnIndexes);

            // TODO-SC should a null check be done for statementCache?
            PreparedStatementWrapper preparedStatement = (PreparedStatementWrapper) statementCache.checkAndUpdateCache(key);

            // TODO-SC-DEFER can the usability (isFree()) check be done by the cache itself
            // and make sure that only a free stmt is returned
            if (preparedStatement != null) {
                if (isFree(preparedStatement)) {
                    // Find if this preparedStatement is a valid one. If invalid, remove it
                    // from the cache and prepare a new stmt & add it to cache
                    if (!preparedStatement.isValid()) {
                        statementCache.purge(preparedStatement);
                        preparedStatement = connection.prepareCachedStatement(sql, columnIndexes, true);
                        preparedStatement.setBusy(true);
                        statementCache.addToCache(key, preparedStatement, false);
                    } else {
                        // Valid preparedStatement
                        preparedStatement.setBusy(true);
                    }

                } else {
                    return connection.prepareCachedStatement(sql, columnIndexes, false);
                }
            } else {
                preparedStatement = connection.prepareCachedStatement(sql, columnIndexes, true);

                statementCache.addToCache(key, preparedStatement, false);
                preparedStatement.setBusy(true);
            }

            return preparedStatement;
        }

        return connection.prepareCachedStatement(sql, columnIndexes, false);
    }

    public PreparedStatement prepareCachedStatement(ConnectionWrapper connection, String sql, int autoGeneratedKeys) throws SQLException {
        if (statementCaching) {
            CacheObjectKey key = new CacheObjectKey(sql, CacheObjectKey.PREPARED_STATEMENT, autoGeneratedKeys);

            // TODO-SC should a null check be done for statementCache?
            PreparedStatementWrapper preparedStatement = (PreparedStatementWrapper) statementCache.checkAndUpdateCache(key);

            // TODO-SC-DEFER can the usability (isFree()) check be done by the cache itself
            // and make sure that only a free stmt is returned
            if (preparedStatement != null) {
                if (isFree(preparedStatement)) {
                    // Find if this preparedStatement is a valid one. If invalid, remove it
                    // from the cache and prepare a new stmt & add it to cache
                    if (!preparedStatement.isValid()) {
                        statementCache.purge(preparedStatement);
                        preparedStatement = connection.prepareCachedStatement(sql, autoGeneratedKeys, true);
                        preparedStatement.setBusy(true);
                        statementCache.addToCache(key, preparedStatement, false);
                    } else {
                        // Valid preparedStatement
                        preparedStatement.setBusy(true);
                    }

                } else {
                    return connection.prepareCachedStatement(sql, autoGeneratedKeys, false);
                }
            } else {
                preparedStatement = connection.prepareCachedStatement(sql, autoGeneratedKeys, true);

                statementCache.addToCache(key, preparedStatement, false);
                preparedStatement.setBusy(true);
            }

            return preparedStatement;
        }

        return connection.prepareCachedStatement(sql, autoGeneratedKeys, false);
    }

    public CallableStatement prepareCachedCallableStatement(ConnectionWrapper connection, String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        if (statementCaching) {
            // Adding the sql as well as the Statement type "CS" to the CacheObjectKey
            // object
            CacheObjectKey key = new CacheObjectKey(sql, CacheObjectKey.CALLABLE_STATEMENT, resultSetType, resultSetConcurrency);
            CallableStatementWrapper callableStatement = (CallableStatementWrapper) statementCache.checkAndUpdateCache(key);

            // TODO-SC-DEFER can the usability (isFree()) check be done by the cache
            // itself and make sure that only a free stmt is returned
            if (callableStatement != null) {
                if (isFree(callableStatement)) {
                    // Find if this callableStatement is a valid one. If invalid, remove it
                    // from the cache and prepare a new stmt & add it to cache
                    if (!callableStatement.isValid()) {
                        statementCache.purge(callableStatement);
                        callableStatement = connection.callableCachedStatement(sql, resultSetType, resultSetConcurrency, true);
                        callableStatement.setBusy(true);
                        statementCache.addToCache(key, callableStatement, false);
                    } else {
                        // Valid callableStatement
                        callableStatement.setBusy(true);
                    }

                } else {
                    return connection.callableCachedStatement(sql, resultSetType, resultSetConcurrency, false);
                }
            } else {
                callableStatement = connection.callableCachedStatement(sql, resultSetType, resultSetConcurrency, true);

                statementCache.addToCache(key, callableStatement, false);
                callableStatement.setBusy(true);
            }
            return callableStatement;
        }

        return connection.callableCachedStatement(sql, resultSetType, resultSetConcurrency, false);
    }

    public CallableStatement prepareCachedCallableStatement(ConnectionWrapper connection, String sql, int resultSetType,
            int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        if (statementCaching) {

            // Adding the sql as well as the Statement type "CS" to the CacheObjectKey object
            CacheObjectKey key = new CacheObjectKey(sql, CacheObjectKey.CALLABLE_STATEMENT, resultSetType,
                    resultSetConcurrency, resultSetHoldability);
            CallableStatementWrapper callableStatement = (CallableStatementWrapper) statementCache.checkAndUpdateCache(key);

            // TODO-SC-DEFER can the usability (isFree()) check be done by the cache
            // itself and make sure that only a free stmt is returned
            if (callableStatement != null) {
                if (isFree(callableStatement)) {
                    // Find if this cs is a valid one. If invalid, remove it
                    // from the cache and prepare a new stmt & add it to cache
                    if (!callableStatement.isValid()) {
                        statementCache.purge(callableStatement);
                        callableStatement = connection.callableCachedStatement(sql, resultSetType, resultSetConcurrency,
                                resultSetHoldability, true);
                        callableStatement.setBusy(true);
                        statementCache.addToCache(key, callableStatement, false);
                    } else {
                        // Valid ps
                        callableStatement.setBusy(true);
                    }

                } else {
                    return connection.callableCachedStatement(sql, resultSetType, resultSetConcurrency,
                            resultSetHoldability, false);
                }
            } else {
                callableStatement = connection.callableCachedStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability,
                        true);

                statementCache.addToCache(key, callableStatement, false);
                callableStatement.setBusy(true);
            }

            return callableStatement;
        }

        return connection.callableCachedStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability, false);
    }

    boolean isFree(PreparedStatementWrapper cachedps) {
        return !cachedps.isBusy();
    }

    public void setAborted(boolean flag) {
        aborted = flag;
    }

    public boolean isAborted() {
        return aborted;
    }

    public void purgeStatementFromCache(PreparedStatement preparedStatement) {
        // TODO isValid check for preparedStatement?
        statementCache.purge(preparedStatement);
    }
}
