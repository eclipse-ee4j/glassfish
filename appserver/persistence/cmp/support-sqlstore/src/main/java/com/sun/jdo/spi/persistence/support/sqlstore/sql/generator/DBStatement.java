/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

/*
 * DBStatement.java
 *
 * Created on March 3, 2000
 *
 */

package com.sun.jdo.spi.persistence.support.sqlstore.sql.generator;

import com.sun.jdo.spi.persistence.support.sqlstore.LogHelperSQLStore;
import com.sun.jdo.spi.persistence.support.sqlstore.database.DBVendorType;
import com.sun.jdo.spi.persistence.support.sqlstore.model.LocalFieldDesc;
import com.sun.jdo.spi.persistence.utility.logging.Logger;

import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;

import org.netbeans.modules.dbschema.ColumnElement;

/**
 */
public class DBStatement extends Object {

    /** Name of the batch threshold property. */
    public static final String BATCH_THRESHOLD_PROPERTY =
        "com.sun.jdo.spi.persistence.support.sqlstore.BATCH_THRESHOLD";

    /**
     * Batch threshold. Set the value from the system property named by
     * this class followed by "BATCH_THRESHOLD". Default is 100.
     */
    private static final int BATCH_THRESHOLD =
        Integer.getInteger(BATCH_THRESHOLD_PROPERTY, 100).intValue();

    /** The wrapped PreparedStatement. */
    private PreparedStatement preparedStmt;

    /** Current number of batched commands. */
    private int batchCounter = 0;

    /** The SQL text. */
    private String statementText;

    /** The logger */
    private static Logger logger = LogHelperSQLStore.getLogger();

    /**
     * This constructor is used for batched updates.
     * @param conn the connection
     * @param statementText the statement text
     * @param timeout the query timeout
     */
    public DBStatement(Connection conn, String statementText, int timeout)
        throws SQLException
    {
        this.statementText = statementText;
        preparedStmt = conn.prepareStatement(statementText);
        // Set SELECT/INSERT/UPDATE/DELETE Statement timeout
        if(timeout != -1) {
            // Avoid calling setQueryTimeOut when user has specified it as -1
            // This can be used as a mechanism to prevent calling setQueryTimeOut
            // for drivers that do not support this call
            // for example, look at bug 6561160
            preparedStmt.setQueryTimeout(timeout);
        }
    }

    /** Returns the SQL text. */
    public String getStatementText()
    {
        return statementText;
    }

    /** Returns the wrapped PreparedStatement. */
    public PreparedStatement getPreparedStatement()
    {
        return preparedStmt;
    }

    /**
     * Checks whether the current number of batched commands exceeds the
     * batch threshold defined by {@link #BATCH_THRESHOLD}.
     */
    public boolean exceedsBatchThreshold()
    {
        return batchCounter >= BATCH_THRESHOLD;
    }

    /**
     * Increases the batch counter and delegates the addBatch call to the
     * PreparedStatement wrapped by this DBStatement.
     */
    public void addBatch()
        throws SQLException
    {
        batchCounter++;
        if (logger.isLoggable(Logger.FINER)) {
            logger.finer("sqlstore.sql.generator.dbstatement.addbatch", // NOI18N
                         new Integer(batchCounter));
        }
        preparedStmt.addBatch();
    }

    /**
     * Delegates the executeBatch call to the PreparedStatement wrapped by
     * this DBStatement and resets the batch counter.
     */
    public int[] executeBatch()
        throws SQLException
    {
        if (logger.isLoggable(Logger.FINER)) {
            logger.finer("sqlstore.sql.generator.dbstatement.executebatch", // NOI18N
                         new Integer(batchCounter));
        }
        batchCounter = 0;
        return preparedStmt.executeBatch();
    }

    /**
     * Delegates the executeUpdate call to the PreparedStatement wrapped by
     * this DBStatement.
     */
    public int executeUpdate()
        throws SQLException
    {
        return preparedStmt.executeUpdate();
    }

    /**
     * Delegates the executeQuery call to the PreparedStatement wrapped by
     * this DBStatement.
     */
    public ResultSet executeQuery()
        throws SQLException
    {
        return preparedStmt.executeQuery();
    }

    /**
     * Delegates the close call to the PreparedStatement wrapped by
     * this DBStatement.
     */
    public void close()
        throws SQLException
    {
        if (preparedStmt != null) {
            preparedStmt.close();
    }
    }

    /**
     * Binds the specified value to the column corresponding with
     * the specified index reference.
     * @param index the index
     * @param val the value
     * @param columnElement the columnElement corresponding to the parameter
     * marker at specified index. This parameter will always contain correct
     * value when called for sql statements corresponding to insert and update
     * For select statements this parameter can be null if query compiler is not
     * able to detect java field for a parameter or value passed to the query.
     * Please see RetrieveDescImpl#addValueConstraint for more information
     *
     * @param vendorType the vendor type
     * @throws SQLException thrown by setter methods on java.sql.PreparedStatement
     * @see com.sun.jdo.spi.persistence.support.sqlstore.sql.RetrieveDescImpl#addValueConstraint
     */
    public void bindInputColumn(int index, Object val, ColumnElement columnElement, DBVendorType vendorType)
        throws SQLException
    {
        int sqlType = getSqlType(columnElement);
        if (logger.isLoggable(Logger.FINER)) {
            Object[] items = {new Integer(index),val,new Integer(sqlType)};
            logger.finer("sqlstore.sql.generator.dbstatement.bindinputcolumn", items); // NOI18N
        }

        if (val == null) {
            //setNull is called only for insert and update statement to set a column
            //to null value. We will always have valid sqlType in this case
            preparedStmt.setNull(index, sqlType);
        } else {
            if (val instanceof Number) {
                Number number = (Number) val;
                if (number instanceof Integer) {
                    preparedStmt.setInt(index, number.intValue());
                } else if (number instanceof Long) {
                    preparedStmt.setLong(index, number.longValue());
                } else if (number instanceof Short) {
                    preparedStmt.setShort(index, number.shortValue());
                } else if (number instanceof Byte) {
                    preparedStmt.setByte(index, number.byteValue());
                } else if (number instanceof Double) {
                    preparedStmt.setDouble(index, number.doubleValue());
                } else if (number instanceof Float) {
                    preparedStmt.setFloat(index, number.floatValue());
                } else if (number instanceof BigDecimal) {
                    preparedStmt.setBigDecimal(index, (BigDecimal) number);
                } else if (number instanceof BigInteger) {
                    preparedStmt.setBigDecimal(index, new BigDecimal((BigInteger) number));
                }
            } else if (val instanceof String) {
                bindStringValue(index, (String)val, columnElement, vendorType);
            } else if (val instanceof Boolean) {
                preparedStmt.setBoolean(index, ((Boolean) val).booleanValue());
            } else if (val instanceof java.util.Date) {
                if (val instanceof java.sql.Date) {
                    preparedStmt.setDate(index, (java.sql.Date) val);
                } else if (val instanceof Time) {
                    preparedStmt.setTime(index, (Time) val);
                } else if (val instanceof Timestamp) {
                    preparedStmt.setTimestamp(index, (Timestamp) val);
                } else {
                    Timestamp timestamp = new Timestamp(((java.util.Date) val).getTime());
                    preparedStmt.setTimestamp(index, timestamp);
                }
            } else if (val instanceof Character) {
                bindStringValue(index, val.toString(), columnElement, vendorType);
            } else if (val instanceof byte[]) {
                //
                // We use setBinaryStream() because of a limit on the maximum
                // array size that can be bound using the
                // PreparedStatement class setBytes() method on Oracle.
                //
                //preparedStmt.setBytes(index, (byte[]) val);
                byte[] ba = (byte[]) val;
                preparedStmt.setBinaryStream(index, new ByteArrayInputStream(ba), ba.length);
            } else if (val instanceof Blob) {
                preparedStmt.setBlob(index, (Blob) val);
            } else if (val instanceof Clob) {
                preparedStmt.setClob(index, (Clob) val);
            } else {
                preparedStmt.setObject(index, val);
            }
        }
    }

    /**
     * Binds the specified value to the column corresponding with
     * the specified index reference.
     * @param index the index
     * @param strVal the value
     * @param columnElement Descripion of the database column.
     * @param vendorType the vendor type
     * @throws SQLException thrown by setter methods on java.sql.PreparedStatement
     */
    private void bindStringValue(int index, String strVal, ColumnElement columnElement, DBVendorType vendorType)
        throws SQLException {

        int sqlType = getSqlType(columnElement);
        if(LocalFieldDesc.isCharLobType(sqlType) ) {
            //Correct sqlType is passed for parameter markers which do not belong to where clause
            //So for insert and update statement we can safely detect binding to character LOB here.
            //
            //For parameter markers belonging to where clause, we do not always receive correct sqlType.
            //It is not allowed by any db to have a Character LOB type in where clause except
            //for null comparison. Let the db report an error if user puts a field mapped
            //to character LOB column in where clause.
            preparedStmt.setCharacterStream(index, new StringReader(strVal), strVal.length());
        } else if(LocalFieldDesc.isFixedCharType(sqlType) ) {
            vendorType.getSpecialDBOperation().bindFixedCharColumn(preparedStmt, index,
                    strVal, getLength(columnElement) );
        } else {
            preparedStmt.setString(index, strVal);
        }
    }

    private static int getSqlType(ColumnElement columnElement) {
        return (columnElement != null) ? columnElement.getType() : Types.OTHER;
    }

    private static int getLength(ColumnElement columnElement) {
        int length = -1;
        if(columnElement != null) {
            Integer l = columnElement.getLength();
            if(l != null) {
                length = l.intValue();
            }
        }
        return length;
    }
}
