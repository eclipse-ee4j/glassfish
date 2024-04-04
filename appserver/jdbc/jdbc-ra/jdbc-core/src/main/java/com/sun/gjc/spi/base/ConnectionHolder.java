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

package com.sun.gjc.spi.base;

import com.sun.enterprise.util.i18n.StringManager;
import com.sun.gjc.common.DataSourceObjectBuilder;
import com.sun.gjc.spi.ManagedConnectionImpl;
import com.sun.gjc.util.MethodExecutor;
import com.sun.logging.LogDomains;

import jakarta.resource.ResourceException;
import jakarta.resource.spi.ConnectionRequestInfo;
import jakarta.resource.spi.LazyAssociatableConnectionManager;
import jakarta.resource.spi.LazyEnlistableConnectionManager;
import jakarta.resource.spi.ManagedConnectionFactory;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.sql.Statement;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Holds the java.sql.Connection object, which is to be passed to the
 * application program.
 *
 * @author Binod P.G
 * @version 1.0, 02/07/23
 */
public abstract class ConnectionHolder implements Connection {

    protected final static Logger _logger = LogDomains.getLogger(ManagedConnectionImpl.class, LogDomains.RSR_LOGGER);
    protected final static StringManager sm = StringManager.getManager(DataSourceObjectBuilder.class);

    protected Connection connection;
    protected ManagedConnectionImpl managedConnectionImpl;

    protected boolean wrappedAlready;
    protected boolean isClosed;
    protected boolean valid = true;
    protected boolean active;

    private LazyAssociatableConnectionManager lazyAssocCm_;
    private LazyEnlistableConnectionManager lazyEnlistCm_;

    private ConnectionRequestInfo connectionRequestInfo;
    private ManagedConnectionFactory managedConnectionFactory;

    protected int statementTimeout;
    protected boolean statementTimeoutEnabled;

    private MethodExecutor executor;

    public static enum ConnectionType {
        LAZY_ENLISTABLE, LAZY_ASSOCIATABLE, STANDARD
    }

    private ConnectionType myType_ = ConnectionType.STANDARD;

    /**
     * The active flag is false when the connection handle is created. When a method
     * is invoked on this object, it asks the ManagedConnection if it can be the
     * active connection handle out of the multiple connection handles. If the
     * ManagedConnection reports that this connection handle can be active by
     * setting this flag to true via the setActive function, the above method
     * invocation succeeds; otherwise an exception is thrown.
     */



    /**
     * Constructs a Connection holder.
     *
     * @param con <code>java.sql.Connection</code> object.
     */
    public ConnectionHolder(Connection con, ManagedConnectionImpl mc, ConnectionRequestInfo cxRequestInfo) {
        this.connection = con;
        this.managedConnectionImpl = mc;
        managedConnectionFactory = mc.getMcf();
        connectionRequestInfo = cxRequestInfo;
        statementTimeout = mc.getStatementTimeout();
        executor = new MethodExecutor();
        if (statementTimeout > 0) {
            statementTimeoutEnabled = true;
        }
    }

    /**
     * Returns the actual connection in this holder object.
     *
     * @return Connection object.
     */
    public Connection getConnection() {
        return connection;
    }

    /**
     * Sets the flag to indicate that, the connection is wrapped already or not.
     *
     * @param wrapFlag
     */
    public void wrapped(boolean wrapFlag) {
        this.wrappedAlready = wrapFlag;
    }

    /**
     * Returns whether it is wrapped already or not.
     *
     * @return wrapped flag.
     */
    public boolean isWrapped() {
        return wrappedAlready;
    }

    /**
     * Returns the <code>ManagedConnection</code> instance responsible for this
     * connection.
     *
     * @return <code>ManagedConnection</code> instance.
     */
    public ManagedConnectionImpl getManagedConnection() {
        return managedConnectionImpl;
    }

    /**
     * Replace the actual <code>java.sql.Connection</code> object with the one
     * supplied. Also replace <code>ManagedConnection</code> link.
     *
     * @param con <code>Connection</code> object.
     * @param mc <code> ManagedConnection</code> object.
     */
    public void associateConnection(Connection con, ManagedConnectionImpl mc) {
        this.managedConnectionImpl = mc;
        this.connection = con;
    }

    /**
     * Dis-associate ManagedConnection and actual-connection from this user
     * connection. Used when lazy-connection-association is ON.
     */
    public void dissociateConnection() {
        this.managedConnectionImpl = null;
        this.connection = null;
    }

    /**
     * Clears all warnings reported for the underlying connection object.
     *
     * @throws SQLException In case of a database error.
     */
    public void clearWarnings() throws SQLException {
        checkValidity();
        connection.clearWarnings();
    }

    /**
     * Closes the logical connection.
     *
     * @throws SQLException In case of a database error.
     */
    public void close() throws SQLException {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "ConnectionHolder.close() START managedConnectionImpl=" + managedConnectionImpl);
        }

        if (isClosed) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.log(Level.FINE, "jdbc.duplicate_close_connection", this);
            }
            return;
        }

        isClosed = true;
        if (managedConnectionImpl != null) {
            // mc might be null if this is a lazyAssociatable connection
            // and has not been associated yet or has been disassociated
            managedConnectionImpl.connectionClosed(null, this);
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "ConnectionHolder.close() END managedConnectionImpl=" + managedConnectionImpl);
        }
    }

    /**
     * Invalidates this object.
     */
    public void invalidate() {
        valid = false;
    }

    /**
     * Closes the physical connection involved in this.
     *
     * @throws SQLException In case of a database error.
     */
    void actualClose() throws SQLException {
        connection.close();
    }

    /**
     * Commit the changes in the underlying Connection.
     *
     * @throws SQLException In case of a database error.
     */
    public void commit() throws SQLException {
        checkValidity();
        connection.commit();
    }

    /**
     * Creates a statement from the underlying Connection
     *
     * @return <code>Statement</code> object.
     * @throws SQLException In case of a database error.
     */
    public Statement createStatement() throws SQLException {
        checkValidity();
        jdbcPreInvoke();

        Statement statement = connection.createStatement();
        if (statementTimeoutEnabled) {
            try {
                statement.setQueryTimeout(statementTimeout);
            } catch (SQLException ex) {
                statement.close();
            }
        }

        return statement;
    }

    /**
     * Creates a statement from the underlying Connection.
     *
     * @param resultSetType Type of the ResultSet
     * @param resultSetConcurrency ResultSet Concurrency.
     * @return <code>Statement</code> object.
     * @throws SQLException In case of a database error.
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        checkValidity();
        jdbcPreInvoke();

        Statement statement = connection.createStatement(resultSetType, resultSetConcurrency);
        if (statementTimeoutEnabled) {
            try {
                statement.setQueryTimeout(statementTimeout);
            } catch (SQLException ex) {
                statement.close();
            }
        }

        return statement;
    }

    /**
     * Creates a statement from the underlying Connection.
     *
     * @param resultSetType Type of the ResultSet
     * @param resultSetConcurrency ResultSet Concurrency.
     * @param resultSetHoldability ResultSet Holdability.
     * @return <code>Statement</code> object.
     * @throws SQLException In case of a database error.
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        checkValidity();
        jdbcPreInvoke();

        Statement statement = connection.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
        if (statementTimeoutEnabled) {
            try {
                statement.setQueryTimeout(statementTimeout);
            } catch (SQLException ex) {
                statement.close();
            }
        }

        return statement;
    }

    /**
     * Retrieves the current auto-commit mode for the underlying
     * <code> Connection</code>.
     *
     * @return The current state of connection's auto-commit mode.
     * @throws SQLException In case of a database error.
     */
    public boolean getAutoCommit() throws SQLException {
        checkValidity();
        return connection.getAutoCommit();
    }

    /**
     * Retrieves the underlying <code>Connection</code> object's catalog name.
     *
     * @return Catalog Name.
     * @throws SQLException In case of a database error.
     */
    public String getCatalog() throws SQLException {
        checkValidity();
        return connection.getCatalog();
    }

    /**
     * Retrieves the current holdability of <code>ResultSet</code> objects created
     * using this connection object.
     *
     * @return holdability value.
     * @throws SQLException In case of a database error.
     */
    public int getHoldability() throws SQLException {
        checkValidity();
        return connection.getHoldability();
    }

    /**
     * Retrieves the <code>DatabaseMetaData</code>object from the underlying
     * <code> Connection </code> object.
     *
     * @return <code>DatabaseMetaData</code> object.
     * @throws SQLException In case of a database error.
     */
    public DatabaseMetaData getMetaData() throws SQLException {
        checkValidity();
        return connection.getMetaData();
    }

    /**
     * Retrieves this <code>Connection</code> object's current transaction isolation
     * level.
     *
     * @return Transaction level
     * @throws SQLException In case of a database error.
     */
    public int getTransactionIsolation() throws SQLException {
        checkValidity();
        return connection.getTransactionIsolation();
    }

    /**
     * Retrieves the <code>Map</code> object associated with
     * <code> Connection</code> Object.
     *
     * @return TypeMap set in this object.
     * @throws SQLException In case of a database error.
     */
    public Map<String, Class<?>> getTypeMap() throws SQLException {
        checkValidity();
        return connection.getTypeMap();
    }

    /**
     * Retrieves the the first warning reported by calls on the underlying
     * <code>Connection</code> object.
     *
     * @return First <code> SQLWarning</code> Object or null.
     * @throws SQLException In case of a database error.
     */
    public SQLWarning getWarnings() throws SQLException {
        checkValidity();
        return connection.getWarnings();
    }

    /**
     * Retrieves whether underlying <code>Connection</code> object is closed.
     *
     * @return true if <code>Connection</code> object is closed, false if it is
     * closed.
     * @throws SQLException In case of a database error.
     */
    public boolean isClosed() throws SQLException {
        return isClosed;
    }

    /**
     * Set the isClosed flag based on whether the underlying <code>Connection</code>
     * object is closed.
     *
     * @param flag true if <code>Connection</code> object is closed, false if its
     * not closed.
     */
    public void setClosed(boolean flag) {
        isClosed = flag;
    }

    /**
     * Retrieves whether this <code>Connection</code> object is read-only.
     *
     * @return true if <code> Connection </code> is read-only, false other-wise
     * @throws SQLException In case of a database error.
     */
    public boolean isReadOnly() throws SQLException {
        checkValidity();
        return connection.isReadOnly();
    }

    /**
     * Converts the given SQL statement into the system's native SQL grammer.
     *
     * @param sql SQL statement , to be converted.
     * @return Converted SQL string.
     * @throws SQLException In case of a database error.
     */
    public String nativeSQL(String sql) throws SQLException {
        checkValidity();
        return connection.nativeSQL(sql);
    }

    /**
     * Creates a <code> CallableStatement </code> object for calling database stored
     * procedures.
     *
     * @param sql SQL Statement
     * @return <code> CallableStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    public CallableStatement prepareCall(String sql) throws SQLException {
        checkValidity();
        jdbcPreInvoke();
        CallableStatement stmt = connection.prepareCall(sql);
        if (statementTimeoutEnabled) {
            stmt.setQueryTimeout(statementTimeout);
        }
        return stmt;
    }

    /**
     * Creates a <code> CallableStatement </code> object for calling database stored
     * procedures.
     *
     * @param sql SQL Statement
     * @param resultSetType Type of the ResultSet
     * @param resultSetConcurrency ResultSet Concurrency.
     * @return <code> CallableStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        checkValidity();
        jdbcPreInvoke();

        CallableStatement callableStatement = connection.prepareCall(sql, resultSetType, resultSetConcurrency);
        if (statementTimeoutEnabled) {
            callableStatement.setQueryTimeout(statementTimeout);
        }

        return callableStatement;
    }

    /**
     * Creates a <code> CallableStatement </code> object for calling database stored
     * procedures.
     *
     * @param sql SQL Statement
     * @param resultSetType Type of the ResultSet
     * @param resultSetConcurrency ResultSet Concurrency.
     * @param resultSetHoldability ResultSet Holdability.
     * @return <code> CallableStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        checkValidity();
        jdbcPreInvoke();

        CallableStatement callableStatement = connection.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        if (statementTimeoutEnabled) {
            callableStatement.setQueryTimeout(statementTimeout);
        }

        return callableStatement;
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending paramterized
     * SQL statements to database
     *
     * @param sql SQL Statement
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    public PreparedStatement prepareStatement(final String sql) throws SQLException {
        checkValidity();
        jdbcPreInvoke();

        PreparedStatement preparedStatement = connection.prepareStatement(sql);
        if (statementTimeoutEnabled) {
            try {
                preparedStatement.setQueryTimeout(statementTimeout);
            } catch (SQLException ex) {
                preparedStatement.close();
            }
        }

        return preparedStatement;
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending paramterized
     * SQL statements to database
     *
     * @param sql SQL Statement
     * @param autoGeneratedKeys a flag indicating AutoGeneratedKeys need to be
     * returned.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    public PreparedStatement prepareStatement(final String sql, int autoGeneratedKeys) throws SQLException {
        checkValidity();
        jdbcPreInvoke();

        PreparedStatement preparedStatement = connection.prepareStatement(sql, autoGeneratedKeys);
        if (statementTimeoutEnabled) {
            try {
                preparedStatement.setQueryTimeout(statementTimeout);
            } catch (SQLException ex) {
                preparedStatement.close();
            }
        }

        return preparedStatement;
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending paramterized
     * SQL statements to database
     *
     * @param sql SQL Statement
     * @param columnIndexes an array of column indexes indicating the columns that
     * should be returned from the inserted row or rows.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    public PreparedStatement prepareStatement(final String sql, int[] columnIndexes) throws SQLException {
        checkValidity();
        jdbcPreInvoke();

        PreparedStatement preparedStatement = connection.prepareStatement(sql, columnIndexes);
        if (statementTimeoutEnabled) {
            try {
                preparedStatement.setQueryTimeout(statementTimeout);
            } catch (SQLException ex) {
                preparedStatement.close();
            }
        }

        return preparedStatement;
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending paramterized
     * SQL statements to database
     *
     * @param sql SQL Statement
     * @param resultSetType Type of the ResultSet
     * @param resultSetConcurrency ResultSet Concurrency.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    public PreparedStatement prepareStatement(final String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        checkValidity();
        jdbcPreInvoke();

        PreparedStatement preparedStatement = connection.prepareStatement(sql, resultSetType, resultSetConcurrency);
        if (statementTimeoutEnabled) {
            try {
                preparedStatement.setQueryTimeout(statementTimeout);
            } catch (SQLException ex) {
                preparedStatement.close();
            }
        }

        return preparedStatement;
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending paramterized
     * SQL statements to database
     *
     * @param sql SQL Statement
     * @param resultSetType Type of the ResultSet
     * @param resultSetConcurrency ResultSet Concurrency.
     * @param resultSetHoldability ResultSet Holdability.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    public PreparedStatement prepareStatement(final String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        checkValidity();
        jdbcPreInvoke();

        PreparedStatement preparedStatement = connection.prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        if (statementTimeoutEnabled) {
            try {
                preparedStatement.setQueryTimeout(statementTimeout);
            } catch (SQLException ex) {
                preparedStatement.close();
            }
        }

        return preparedStatement;
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending paramterized
     * SQL statements to database
     *
     * @param sql SQL Statement
     * @param columnNames Name of bound columns.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    public PreparedStatement prepareStatement(final String sql, String[] columnNames) throws SQLException {
        checkValidity();
        jdbcPreInvoke();

        PreparedStatement preparedStatement = connection.prepareStatement(sql, columnNames);
        if (statementTimeoutEnabled) {
            try {
                preparedStatement.setQueryTimeout(statementTimeout);
            } catch (SQLException ex) {
                preparedStatement.close();
            }
        }

        return preparedStatement;
    }

    /**
     * Removes the given <code>Savepoint</code> object from the current transaction.
     *
     * @param savepoint <code>Savepoint</code> object
     * @throws SQLException In case of a database error.
     */
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        checkValidity();
        connection.releaseSavepoint(savepoint);
    }

    /**
     * Rolls back the changes made in the current transaction.
     *
     * @throws SQLException In case of a database error.
     */
    public void rollback() throws SQLException {
        checkValidity();
        connection.rollback();
    }

    /**
     * Rolls back the changes made after the savepoint.
     *
     * @throws SQLException In case of a database error.
     */
    public void rollback(Savepoint savepoint) throws SQLException {
        checkValidity();
        connection.rollback(savepoint);
    }

    /**
     * Sets the auto-commmit mode of the <code>Connection</code> object.
     *
     * @param autoCommit boolean value indicating the auto-commit mode.
     * @throws SQLException In case of a database error.
     */
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        checkValidity();
        connection.setAutoCommit(autoCommit);
        managedConnectionImpl.setLastAutoCommitValue(autoCommit);
    }

    /**
     * Sets the catalog name to the <code>Connection</code> object
     *
     * @param catalog Catalog name.
     * @throws SQLException In case of a database error.
     */
    public void setCatalog(String catalog) throws SQLException {
        checkValidity();
        connection.setCatalog(catalog);
    }

    /**
     * Sets the holdability of <code>ResultSet</code> objects created using this
     * <code>Connection</code> object.
     *
     * @param holdability A <code>ResultSet</code> holdability constant
     * @throws SQLException In case of a database error.
     */
    public void setHoldability(int holdability) throws SQLException {
        checkValidity();
        connection.setHoldability(holdability);
    }

    /**
     * Puts the connection in read-only mode as a hint to the driver to perform
     * database optimizations.
     *
     * @param readOnly true enables read-only mode, false disables it.
     * @throws SQLException In case of a database error.
     */
    public void setReadOnly(boolean readOnly) throws SQLException {
        checkValidity();
        connection.setReadOnly(readOnly);
    }

    /**
     * Creates and unnamed savepoint and returns an object corresponding to that.
     *
     * @return <code>Savepoint</code> object.
     * @throws SQLException In case of a database error.
     */
    public Savepoint setSavepoint() throws SQLException {
        checkValidity();
        return connection.setSavepoint();
    }

    /**
     * Creates a savepoint with the name and returns an object corresponding to
     * that.
     *
     * @param name Name of the savepoint.
     * @return <code>Savepoint</code> object.
     * @throws SQLException In case of a database error.
     */
    public Savepoint setSavepoint(String name) throws SQLException {
        checkValidity();
        return connection.setSavepoint(name);
    }

    /**
     * Creates the transaction isolation level.
     *
     * @param level transaction isolation level.
     * @throws SQLException In case of a database error.
     */
    public void setTransactionIsolation(int level) throws SQLException {
        checkValidity();
        connection.setTransactionIsolation(level);
        managedConnectionImpl.setLastTransactionIsolationLevel(level);
    }

    /**
     * Checks the validity of this object
     */
    protected void checkValidity() throws SQLException {
        if (isClosed) {
            throw new SQLException("Connection closed");
        }

        if (!valid) {
            throw new SQLException("Invalid Connection");
        }

        if (active == false) {
            managedConnectionImpl.checkIfActive(this);
        }
    }

    /**
     * Sets the active flag to true
     *
     * @param actv boolean
     */
    public void setActive(boolean actv) {
        active = actv;
    }

    /*
     * Here this is a no-op. In the LazyEnlistableConnectionHolder, it will actually
     * fire the lazyEnlist method of LazyEnlistableManagedConnection
     */
    protected void jdbcPreInvoke() throws SQLException {
        if (myType_ == ConnectionType.LAZY_ASSOCIATABLE) {
            performLazyAssociation();
        } else if (myType_ == ConnectionType.LAZY_ENLISTABLE) {
            performLazyEnlistment();
        }

    }

    protected void performLazyEnlistment() throws SQLException {
        try {
            if (lazyEnlistCm_ != null) {
                lazyEnlistCm_.lazyEnlist(managedConnectionImpl);
            }
        } catch (ResourceException re) {
            String msg = sm.getString("jdbc.cannot_enlist", re.getMessage() + " Cannnot Enlist ManagedConnection");

            SQLException sqle = new SQLException(msg);
            sqle.initCause(re);
            throw sqle;
        }

    }

    protected void performLazyAssociation() throws SQLException {
        if (managedConnectionImpl == null) {
            try {
                if (lazyAssocCm_ != null) {
                    lazyAssocCm_.associateConnection(this, managedConnectionFactory, connectionRequestInfo);
                }
            } catch (ResourceException re) {
                String msg = sm.getString("jdbc.cannot_assoc",
                        re.getMessage() + " Cannnot Associate ManagedConnection");

                SQLException sqle = new SQLException(msg);
                sqle.initCause(re);
                throw sqle;
            }
        }
    }

    public void setConnectionType(ConnectionType type) {
        myType_ = type;
    }

    public ConnectionType getConnectionType() {
        return myType_;
    }

    public void setLazyAssociatableConnectionManager(jakarta.resource.spi.LazyAssociatableConnectionManager cm) {
        lazyAssocCm_ = cm;
    }

    public void setLazyEnlistableConnectionManager(jakarta.resource.spi.LazyEnlistableConnectionManager cm) {
        lazyEnlistCm_ = cm;
    }

    /**
     * Installs the given <code>Map</code> object as the tyoe map for this
     * <code> Connection </code> object.
     *
     * @param map <code>Map</code> a Map object to install.
     * @throws SQLException In case of a database error.
     */
    public void setTypeMap(java.util.Map<String, Class<?>> map) throws SQLException {
        checkValidity();
        connection.setTypeMap(map);
    }

    protected MethodExecutor getMethodExecutor() {
        return executor;
    }

}
