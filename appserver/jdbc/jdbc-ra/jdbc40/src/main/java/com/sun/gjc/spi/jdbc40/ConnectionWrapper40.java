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

package com.sun.gjc.spi.jdbc40;

import com.sun.gjc.spi.ManagedConnectionImpl;
import com.sun.gjc.spi.base.ConnectionWrapper;

import java.sql.*;

/**
 * Wrapper class that aids to provide wrapper for the following JDBC objects : <br>
 * Statement, PreparedStatement, CallableStatement, DatabaseMetaData
 */
public class ConnectionWrapper40 extends ConnectionHolder40 implements ConnectionWrapper {

    /**
     * Instantiates connection wrapper to wrap JDBC objects.
     *
     * @param con           Connection that is wrapped
     * @param mc            Managed Connection
     * @param cxRequestInfo Connection Request Info
     */
    public ConnectionWrapper40(Connection con, ManagedConnectionImpl mc,
                               jakarta.resource.spi.ConnectionRequestInfo cxRequestInfo,
                               boolean jdbc30Connection) {
        super(con, mc, cxRequestInfo, jdbc30Connection);
    }

    /**
     * Creates a statement from the underlying Connection
     *
     * @return <code>Statement</code> object.
     * @throws java.sql.SQLException In case of a database error.
     */
    public Statement createStatement() throws SQLException {

        return new StatementWrapper40(this, super.createStatement());
    }

    /**
     * Creates a statement from the underlying Connection.
     *
     * @param resultSetType        Type of the ResultSet
     * @param resultSetConcurrency ResultSet Concurrency.
     * @return <code>Statement</code> object.
     * @throws SQLException In case of a database error.
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return new StatementWrapper40(this, super.createStatement(resultSetType,
                resultSetConcurrency));
    }

    /**
     * Creates a statement from the underlying Connection.
     *
     * @param resultSetType        Type of the ResultSet
     * @param resultSetConcurrency ResultSet Concurrency.
     * @param resultSetHoldability ResultSet Holdability.
     * @return <code>Statement</code> object.
     * @throws SQLException In case of a database error.
     */
    public Statement createStatement(int resultSetType, int resultSetConcurrency,
                                     int resultSetHoldability) throws SQLException {
        return new StatementWrapper40(this, super.createStatement(resultSetType,
                resultSetConcurrency, resultSetHoldability));
    }

    /**
     * Retrieves the <code>DatabaseMetaData</code>object from the underlying
     * <code> Connection </code> object.
     *
     * @return <code>DatabaseMetaData</code> object.
     * @throws SQLException In case of a database error.
     */
    public DatabaseMetaData getMetaData() throws SQLException {
        return new DatabaseMetaDataWrapper40(this, super.getMetaData());
    }


    /**
     * Creates a <code> CallableStatement </code> object for calling database
     * stored procedures.
     *
     * @param sql SQL Statement
     * @return <code> CallableStatement</code> object.
     * @throws java.sql.SQLException In case of a database error.
     */
    public CallableStatement prepareCall(String sql) throws SQLException {
        return mc.prepareCachedCallableStatement(this, sql, ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * Creates a <code> CallableStatement </code> object for calling database
     * stored procedures.
     *
     * @param sql                  SQL Statement
     * @param resultSetType        Type of the ResultSet
     * @param resultSetConcurrency ResultSet Concurrency.
     * @return <code> CallableStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency) throws SQLException {
        return mc.prepareCachedCallableStatement(this, sql, resultSetType, resultSetConcurrency);
    }

    /**
     * Creates a <code> CallableStatement </code> object for calling database
     * stored procedures.
     *
     * @param sql                  SQL Statement
     * @param resultSetType        Type of the ResultSet
     * @param resultSetConcurrency ResultSet Concurrency.
     * @param resultSetHoldability ResultSet Holdability.
     * @return <code> CallableStatement</code> object.
     * @throws SQLException In case of a database error.
     */
     public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        return mc.prepareCachedCallableStatement(this, sql, resultSetType,
                resultSetConcurrency, resultSetHoldability);
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending
     * paramterized SQL statements to database
     *
     * @param sql SQL Statement
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return mc.prepareCachedStatement(this, sql, ResultSet.TYPE_FORWARD_ONLY,
                ResultSet.CONCUR_READ_ONLY);
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending
     * paramterized SQL statements to database
     *
     * @param sql               SQL Statement
     * @param autoGeneratedKeys a flag indicating AutoGeneratedKeys need to be returned.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
     public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return mc.prepareCachedStatement(this, sql, autoGeneratedKeys);
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending
     * paramterized SQL statements to database
     *
     * @param sql           SQL Statement
     * @param columnIndexes an array of column indexes indicating the columns that should be
     *                      returned from the inserted row or rows.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
     public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return mc.prepareCachedStatement(this, sql, columnIndexes);
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending
     * paramterized SQL statements to database
     *
     * @param sql                  SQL Statement
     * @param resultSetType        Type of the ResultSet
     * @param resultSetConcurrency ResultSet Concurrency.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
     public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency) throws SQLException {
        return mc.prepareCachedStatement(this, sql, resultSetType, resultSetConcurrency);
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending
     * paramterized SQL statements to database
     *
     * @param sql                  SQL Statement
     * @param resultSetType        Type of the ResultSet
     * @param resultSetConcurrency ResultSet Concurrency.
     * @param resultSetHoldability ResultSet Holdability.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
     public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency,
                                              int resultSetHoldability) throws SQLException {
        return mc.prepareCachedStatement(this, sql, resultSetType,
                resultSetConcurrency, resultSetHoldability);
    }

    /**
     * Creates a <code> PreparedStatement </code> object for sending
     * paramterized SQL statements to database
     *
     * @param sql         SQL Statement
     * @param columnNames Name of bound columns.
     * @return <code> PreparedStatement</code> object.
     * @throws SQLException In case of a database error.
     */
     public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        checkValidity();
        jdbcPreInvoke();
        return mc.prepareCachedStatement(this, sql, columnNames);
    }

      public PreparedStatementWrapper40 prepareCachedStatement(String sql,
              int resultSetType, int resultSetConcurrency, boolean enableCaching)
              throws SQLException {
           return new PreparedStatementWrapper40(this, super.prepareStatement(sql,
                   resultSetType, resultSetConcurrency), enableCaching);
       }

       public PreparedStatementWrapper40 prepareCachedStatement(String sql,
              String[] columnNames, boolean enableCaching)
              throws SQLException {
           return new PreparedStatementWrapper40(this,
                   super.prepareStatement(sql, columnNames), enableCaching);
       }

        public PreparedStatementWrapper40 prepareCachedStatement(String sql,
                int resultSetType, int resultSetConcurrency, int resultSetHoldability,
                boolean enableCaching) throws SQLException {
            return new PreparedStatementWrapper40(this,
                    super.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability),
                    enableCaching);
        }

        public PreparedStatementWrapper40 prepareCachedStatement(String sql,
              int[] columnIndexes, boolean enableCaching)
              throws SQLException {
           return new PreparedStatementWrapper40(this,
                   super.prepareStatement(sql, columnIndexes), enableCaching);
       }

        public PreparedStatementWrapper40 prepareCachedStatement(String sql,
              int autoGeneratedKeys, boolean enableCaching)
              throws SQLException {
           return new PreparedStatementWrapper40(this,
                   super.prepareStatement(sql, autoGeneratedKeys), enableCaching);
       }

        public CallableStatementWrapper40 callableCachedStatement(String sql,
               int resultSetType, int resultSetConcurrency, boolean enableCaching)
               throws SQLException {
           return new CallableStatementWrapper40(this, super.prepareCall(sql,
                   resultSetType, resultSetConcurrency), enableCaching);
       }

        public CallableStatementWrapper40 callableCachedStatement(String sql,
               int resultSetType, int resultSetConcurrency,
               int resultSetHoldability, boolean enableCaching)
               throws SQLException {
           return new CallableStatementWrapper40(this, super.prepareCall(sql,
                   resultSetType, resultSetConcurrency, resultSetHoldability),
                   enableCaching);
       }

}
