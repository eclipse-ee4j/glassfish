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

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.SQLClientInfoException;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * Holds the java.sql.Connection object, which is to be
 * passed to the application program.
 *
 * @version        1.0, 02/07/23
 * @author        Binod P.G
 */
public class ConnectionHolder implements Connection{

    private Connection con;

    private ManagedConnection mc;

    private boolean wrappedAlready = false;

    private boolean isClosed = false;

    private boolean valid = true;

    private boolean active = false;
    /**
     * The active flag is false when the connection handle is
     * created. When a method is invoked on this object, it asks
     * the ManagedConnection if it can be the active connection
     * handle out of the multiple connection handles. If the
     * ManagedConnection reports that this connection handle
     * can be active by setting this flag to true via the setActive
     * function, the above method invocation succeeds; otherwise
     * an exception is thrown.
     */

    /**
     * Constructs a Connection holder.
     *
     * @param        con        <code>java.sql.Connection</code> object.
     */
    public ConnectionHolder(Connection con, ManagedConnection mc) {
        this.con = con;
        this.mc  = mc;
    }

    /**
     * Returns the actual connection in this holder object.
     *
     * @return        Connection object.
     */
    Connection getConnection() {
            return con;
    }

    /**
     * Sets the flag to indicate that, the connection is wrapped already or not.
     *
     * @param        wrapFlag
     */
    void wrapped(boolean wrapFlag){
        this.wrappedAlready = wrapFlag;
    }

    /**
     * Returns whether it is wrapped already or not.
     *
     * @return        wrapped flag.
     */
    boolean isWrapped(){
        return wrappedAlready;
    }

    /**
     * Returns the <code>ManagedConnection</code> instance responsible
     * for this connection.
     *
     * @return        <code>ManagedConnection</code> instance.
     */
    ManagedConnection getManagedConnection() {
        return mc;
    }

    /**
     * Replace the actual <code>java.sql.Connection</code> object with the one
     * supplied. Also replace <code>ManagedConnection</code> link.
     *
     * @param        con <code>Connection</code> object.
     * @param        mc  <code> ManagedConnection</code> object.
     */
    void associateConnection(Connection con, ManagedConnection mc) {
            this.mc = mc;
            this.con = con;
    }

    /**
     * Clears all warnings reported for the underlying connection  object.
     *
     * @throws SQLException In case of a database error.
     */
    @Override
    public void clearWarnings() throws SQLException{
        checkValidity();
        con.clearWarnings();
    }

    /**
     * Closes the logical connection.
     *
     * @throws SQLException In case of a database error.
     */
    @Override
    public void close() throws SQLException{
        isClosed = true;
        mc.connectionClosed(null, this);
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
    void actualClose() throws SQLException{
        con.close();
    }

    /**
     * Commit the changes in the underlying Connection.
     *
     * @throws SQLException In case of a database error.
     */
    @Override
    public void commit() throws SQLException {
        checkValidity();
            con.commit();
    }

    /**
     * Creates a statement from the underlying Connection
     *
     * @return        <code>Statement</code> object.
     * @throws SQLException In case of a database error.
     */
    @Override
    public Statement createStatement() throws SQLException {
        checkValidity();
        return con.createStatement();
    }

    /**
     * Creates a statement from the underlying Connection.
     *
     * @param        resultSetType        Type of the ResultSet
     * @param        resultSetConcurrency        ResultSet Concurrency.
     * @return        <code>Statement</code> object.
     * @throws SQLException In case of a database error.
     */
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        checkValidity();
        return con.createStatement(resultSetType, resultSetConcurrency);
    }

    /**
     * Creates a statement from the underlying Connection.
     *
     * @param        resultSetType        Type of the ResultSet
     * @param        resultSetConcurrency        ResultSet Concurrency.
     * @param        resultSetHoldability        ResultSet Holdability.
     * @return        <code>Statement</code> object.
     * @throws SQLException In case of a database error.
     */
    @Override
    public Statement createStatement(int resultSetType, int resultSetConcurrency,
                                         int resultSetHoldabilty) throws SQLException {
        checkValidity();
        return con.createStatement(resultSetType, resultSetConcurrency,
                                   resultSetHoldabilty);
    }

    /**
     * Retrieves the current auto-commit mode for the underlying <code> Connection</code>.
     *
     * @return The current state of connection's auto-commit mode.
     * @throws SQLException In case of a database error.
     */
    @Override
    public boolean getAutoCommit() throws SQLException {
        checkValidity();
            return con.getAutoCommit();
    }

    /**
     * Retrieves the underlying <code>Connection</code> object's catalog name.
     *
     * @return        Catalog Name.
     * @throws SQLException In case of a database error.
     */
    @Override
    public String getCatalog() throws SQLException {
        checkValidity();
        return con.getCatalog();
    }

    /**
     * Retrieves the current holdability of <code>ResultSet</code> objects created
     * using this connection object.
     *
     * @return        holdability value.
     * @throws SQLException In case of a database error.
     */
    @Override
    public int getHoldability() throws SQLException {
        checkValidity();
            return        con.getHoldability();
    }

    /**
     * Retrieves the <code>DatabaseMetaData</code>object from the underlying
     * <code> Connection </code> object.
     *
     * @return <code>DatabaseMetaData</code> object.
     * @throws SQLException In case of a database error.
     */
    @Override
    public DatabaseMetaData getMetaData() throws SQLException {
        checkValidity();
            return con.getMetaData();
    }

    /**
     * Retrieves this <code>Connection</code> object's current transaction isolation level.
     *
     * @return Transaction level
     * @throws SQLException In case of a database error.
     */
    @Override
    public int getTransactionIsolation() throws SQLException {
        checkValidity();
        return con.getTransactionIsolation();
    }

    /**
     * Retrieves the <code>Map</code> object associated with
     * <code> Connection</code> Object.
     *
     * @return        TypeMap set in this object.
     * @throws SQLException In case of a database error.
     */
    @Override
    public Map getTypeMap() throws SQLException {
        checkValidity();
            return con.getTypeMap();
    }

    /**
     * Retrieves the the first warning reported by calls on the underlying
     * <code>Connection</code> object.
     *
     * @return First <code> SQLWarning</code> Object or null.
     * @throws SQLException In case of a database error.
     */
    @Override
    public SQLWarning getWarnings() throws SQLException {
        checkValidity();
            return con.getWarnings();
    }

    /**
     * Retrieves whether underlying <code>Connection</code> object is closed.
     *
     * @return        true if <code>Connection</code> object is closed, false
     *                 if it is closed.
     * @throws SQLException In case of a database error.
     */
    @Override
    public boolean isClosed() throws SQLException {
            return isClosed;
    }

    /**
     * Retrieves whether this <code>Connection</code> object is read-only.
     *
     * @return        true if <code> Connection </code> is read-only, false other-wise
     * @throws SQLException In case of a database error.
     */
    @Override
    public boolean isReadOnly() throws SQLException {
        checkValidity();
            return con.isReadOnly();
    }

    /**
     * Converts the given SQL statement into the system's native SQL grammer.
     *
     * @param        sql        SQL statement , to be converted.
     * @return        Converted SQL string.
     * @throws SQLException In case of a database error.
     */
    @Override
    public String nativeSQL(String sql) throws SQLException {
        checkValidity();
            return con.nativeSQL(sql);
    }

    /**
     * Creates a <code> CallableStatement </code> object for calling database
     * stored procedures.
     *
     * @param        sql        SQL Statement
     * @return <code> CallableStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    @Override
    public CallableStatement prepareCall(String sql) throws SQLException {
        checkValidity();
            return con.prepareCall(sql);
    }

    /**
     * Creates a <code> CallableStatement </code> object for calling database
     * stored procedures.
     *
     * @param        sql        SQL Statement
     * @param        resultSetType        Type of the ResultSet
     * @param        resultSetConcurrency        ResultSet Concurrency.
     * @return <code> CallableStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    @Override
    public CallableStatement prepareCall(String sql,int resultSetType,
                                            int resultSetConcurrency) throws SQLException{
        checkValidity();
            return con.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    /**
     * Creates a <code> CallableStatement </code> object for calling database
     * stored procedures.
     *
     * @param        sql        SQL Statement
     * @param        resultSetType        Type of the ResultSet
     * @param        resultSetConcurrency        ResultSet Concurrency.
     * @param        resultSetHoldability        ResultSet Holdability.
     * @return <code> CallableStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    @Override
    public CallableStatement prepareCall(String sql, int resultSetType,
                                             int resultSetConcurrency,
                                             int resultSetHoldabilty) throws SQLException{
        checkValidity();
            return con.prepareCall(sql, resultSetType, resultSetConcurrency,
                                   resultSetHoldabilty);
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending
     * paramterized SQL statements to database
     *
     * @param        sql        SQL Statement
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    @Override
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        checkValidity();
            return con.prepareStatement(sql);
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending
     * paramterized SQL statements to database
     *
     * @param        sql        SQL Statement
     * @param        autoGeneratedKeys a flag indicating AutoGeneratedKeys need to be returned.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    @Override
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        checkValidity();
            return con.prepareStatement(sql,autoGeneratedKeys);
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending
     * paramterized SQL statements to database
     *
     * @param        sql        SQL Statement
     * @param        columnIndexes an array of column indexes indicating the columns that should be
     *                returned from the inserted row or rows.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    @Override
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        checkValidity();
            return con.prepareStatement(sql,columnIndexes);
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending
     * paramterized SQL statements to database
     *
     * @param        sql        SQL Statement
     * @param        resultSetType        Type of the ResultSet
     * @param        resultSetConcurrency        ResultSet Concurrency.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    @Override
    public PreparedStatement prepareStatement(String sql,int resultSetType,
                                            int resultSetConcurrency) throws SQLException{
        checkValidity();
            return con.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending
     * paramterized SQL statements to database
     *
     * @param        sql        SQL Statement
     * @param        resultSetType        Type of the ResultSet
     * @param        resultSetConcurrency        ResultSet Concurrency.
     * @param        resultSetHoldability        ResultSet Holdability.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    @Override
    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                             int resultSetConcurrency,
                                             int resultSetHoldabilty) throws SQLException {
        checkValidity();
            return con.prepareStatement(sql, resultSetType, resultSetConcurrency,
                                        resultSetHoldabilty);
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending
     * paramterized SQL statements to database
     *
     * @param        sql        SQL Statement
     * @param        columnNames Name of bound columns.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    @Override
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        checkValidity();
            return con.prepareStatement(sql,columnNames);
    }

    @Override
    public Clob createClob() throws SQLException {
        return con.createClob();
    }

    @Override
    public Blob createBlob() throws SQLException {
        return con.createBlob();
    }

    @Override
    public NClob createNClob() throws SQLException {
        return con.createNClob();
    }

    @Override
    public SQLXML createSQLXML() throws SQLException {
        return con.createSQLXML();
    }

    @Override
    public boolean isValid(int timeout) throws SQLException {
        return con.isValid(timeout);
    }

    @Override
    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        con.setClientInfo(name, value);
    }

    @Override
    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        con.setClientInfo(properties);
    }

    @Override
    public String getClientInfo(String name) throws SQLException {
        return con.getClientInfo(name);
    }

    @Override
    public Properties getClientInfo() throws SQLException {
        return getClientInfo();
    }

    @Override
    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return createArrayOf(typeName, elements);
    }

    @Override
    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return createStruct(typeName, attributes);
    }

    /**
     * Removes the given <code>Savepoint</code> object from the current transaction.
     *
     * @param        savepoint        <code>Savepoint</code> object
     * @throws SQLException In case of a database error.
     */
    @Override
    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        checkValidity();
            con.releaseSavepoint(savepoint);
    }

    /**
     * Rolls back the changes made in the current transaction.
     *
     * @throws SQLException In case of a database error.
     */
    @Override
    public void rollback() throws SQLException {
        checkValidity();
            con.rollback();
    }

    /**
     * Rolls back the changes made after the savepoint.
     *
     * @throws SQLException In case of a database error.
     */
    @Override
    public void rollback(Savepoint savepoint) throws SQLException {
        checkValidity();
            con.rollback(savepoint);
    }

    /**
     * Sets the auto-commmit mode of the <code>Connection</code> object.
     *
     * @param        autoCommit boolean value indicating the auto-commit mode.
     * @throws SQLException In case of a database error.
     */
    @Override
    public void setAutoCommit(boolean autoCommit) throws SQLException {
        checkValidity();
            con.setAutoCommit(autoCommit);
    }

    /**
     * Sets the catalog name to the <code>Connection</code> object
     *
     * @param        catalog        Catalog name.
     * @throws SQLException In case of a database error.
     */
    @Override
    public void setCatalog(String catalog) throws SQLException {
        checkValidity();
            con.setCatalog(catalog);
    }

    /**
     * Sets the holdability of <code>ResultSet</code> objects created
     * using this <code>Connection</code> object.
     *
     * @param        holdability        A <code>ResultSet</code> holdability constant
     * @throws SQLException In case of a database error.
     */
    @Override
    public void setHoldability(int holdability) throws SQLException {
        checkValidity();
             con.setHoldability(holdability);
    }

    /**
     * Puts the connection in read-only mode as a hint to the driver to
     * perform database optimizations.
     *
     * @param        readOnly  true enables read-only mode, false disables it.
     * @throws SQLException In case of a database error.
     */
    @Override
    public void setReadOnly(boolean readOnly) throws SQLException {
        checkValidity();
            con.setReadOnly(readOnly);
    }

    /**
     * Creates and unnamed savepoint and returns an object corresponding to that.
     *
     * @return        <code>Savepoint</code> object.
     * @throws SQLException In case of a database error.
     */
    @Override
    public Savepoint setSavepoint() throws SQLException {
        checkValidity();
            return con.setSavepoint();
    }

    /**
     * Creates a savepoint with the name and returns an object corresponding to that.
     *
     * @param        name        Name of the savepoint.
     * @return        <code>Savepoint</code> object.
     * @throws SQLException In case of a database error.
     */
    @Override
    public Savepoint setSavepoint(String name) throws SQLException {
        checkValidity();
            return con.setSavepoint(name);
    }

    /**
     * Creates the transaction isolation level.
     *
     * @param        level transaction isolation level.
     * @throws SQLException In case of a database error.
     */
    @Override
    public void setTransactionIsolation(int level) throws SQLException {
        checkValidity();
            con.setTransactionIsolation(level);
    }

    /**
     * Installs the given <code>Map</code> object as the tyoe map for this
     * <code> Connection </code> object.
     *
     * @param        map        <code>Map</code> a Map object to install.
     * @throws SQLException In case of a database error.
     */
    @Override
    public void setTypeMap(Map map) throws SQLException {
        checkValidity();
            con.setTypeMap(map);
    }

    @Override
    public int getNetworkTimeout() throws SQLException {
      throw new SQLFeatureNotSupportedException("Do not support Java 7 new feature.");
    }

    @Override
    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
      throw new SQLFeatureNotSupportedException("Do not support Java 7 new feature.");
    }

    @Override
    public void abort(Executor executor)  throws SQLException{
      throw new SQLFeatureNotSupportedException("Do not support Java 7 new feature.");
    }

    @Override
    public String getSchema() throws SQLException{
      throw new SQLFeatureNotSupportedException("Do not support Java 7 new feature.");
    }

    @Override
    public void setSchema(String schema) throws SQLException{
      throw new SQLFeatureNotSupportedException("Do not support Java 7 new feature.");
    }


    /**
     * Checks the validity of this object
     */
    private void checkValidity() throws SQLException {
            if (isClosed) {
                throw new SQLException ("Connection closed");
            }
            if (!valid) {
                throw new SQLException ("Invalid Connection");
            }
            if(active == false) {
                mc.checkIfActive(this);
            }
    }

    /**
     * Sets the active flag to true
     *
     * @param        actv        boolean
     */
    void setActive(boolean actv) {
        active = actv;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return con.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return con.isWrapperFor(iface);
    }
}
