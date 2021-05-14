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

package com.sun.jts.CosTransactions;

import com.sun.logging.LogDomains;

import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.InitialContext;
import javax.sql.DataSource;

/** The LogDBHelper class takes care of writing the transaction logs
  *  into database.
  * @author Sun Micro Systems, Inc
*/

class LogDBHelper {

    String resName = "jdbc/TxnDS";

    // serverName cannot be final - the instance is first requested
    // during auto-recovery when the server name is not yet final
    private String serverName;

    final private String instanceName;

    private DataSource ds = null;
    private Method getNonTxConnectionMethod = null;
    private static final String insertStatement =
             System.getProperty("com.sun.jts.dblogging.insertquery",
                 "insert into  txn_log_table values ( ? , ? , ? , ? )");
    private static final String deleteStatement =
             System.getProperty("com.sun.jts.dblogging.deletequery",
                 "delete from txn_log_table where localtid = ? and servername = ? ");
    private static final String selectStatement =
             System.getProperty("com.sun.jts.dblogging.selectquery",
                 "select * from txn_log_table where servername = ? ");
    private static final String selectServerNameStatement =
             System.getProperty("com.sun.jts.dblogging.selectservernamequery",
                 "select distinct servername from txn_log_table where instancename = ? ");
    private static final String createTableStatement =
                 "create table txn_log_table (localtid varchar(20), servername varchar(150), instancename varchar(150), gtrid blob)";
    private static final boolean useNonTxConnectionForAddRecord = Boolean.getBoolean("com.sun.jts.dblogging.use.nontx.connection.for.add");
    private static Logger _logger = LogDomains.getLogger(LogDBHelper.class, LogDomains.TRANSACTION_LOGGER);
    private static LogDBHelper _instance = new LogDBHelper();

    static LogDBHelper getInstance() {
        return _instance;
    }

    LogDBHelper() {
        instanceName = Configuration.getPropertyValue(Configuration.INSTANCE_NAME);
        if (Configuration.getPropertyValue(Configuration.DB_LOG_RESOURCE) != null) {
            resName = Configuration.getPropertyValue(Configuration.DB_LOG_RESOURCE);
        }
        try {
            InitialContext ctx = new InitialContext();
            ds = (DataSource)ctx.lookup(resName);
        Class cls = ds.getClass();
        getNonTxConnectionMethod = cls.getMethod("getNonTxConnection", null);

            createTable();

        } catch (Throwable t) {
            _logger.log(Level.SEVERE,"jts.unconfigured_db_log_resource",resName);
            _logger.log(Level.SEVERE,"",t);
        }
        if (_logger.isLoggable(Level.FINE)) {
            _logger.fine("LogDBHelper.resName: " + resName);
            _logger.fine("LogDBHelper.ds: " + ds);
            _logger.fine("LogDBHelper.getNonTxConnectionMethod: " + getNonTxConnectionMethod);
        }
    }

    void setServerName() {
        // Add a mapping between the serverName and the instanceName
        String serverName0 = Configuration.getServerName();
        if (serverName != null && serverName.equals(serverName0)) {
            //Nothing changed
            return;
        } else {
            serverName = serverName0;
        }

        if (_logger.isLoggable(Level.INFO)) {
            _logger.info("LogDBHelper.setServerName for serverName: " + serverName);
            _logger.info("LogDBHelper.setServerName for instanceName: " + instanceName);
        }
        String s = getServerNameForInstanceName(instanceName);
        if (s == null) {
            // Set the mapping
            if (_logger.isLoggable(Level.INFO)) {
                _logger.info("LogDBHelper.initTable adding marker record...");
            }
            addRecord(0, null);
        } else if (!s.equals(serverName)) {
            // Override the mapping
            _logger.log(Level.WARNING, "jts.exception_in_db_log_server_to_instance_mapping",
                    new Object[] {instanceName, s, serverName});
            deleteRecord(0, s);
            addRecord(0, null);
        }
    }

    boolean addRecord(long localTID, byte[] data) {
        if (ds != null) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("LogDBHelper.addRecord for localTID: " + localTID);
                _logger.fine("LogDBHelper.addRecord for serverName: " + serverName);
                _logger.fine("LogDBHelper.addRecord for instanceName: " + instanceName);
            }
            Connection conn = null;
            PreparedStatement prepStmt1 = null;
            try {
                if (useNonTxConnectionForAddRecord)
            conn = (Connection)(getNonTxConnectionMethod.invoke(ds, null));
                else
                    conn = ds.getConnection();
                prepStmt1 = conn.prepareStatement(insertStatement);
                prepStmt1.setString(1,Long.toString(localTID));
                prepStmt1.setString(2,serverName);
                prepStmt1.setString(3,instanceName);
                prepStmt1.setBytes(4,data);
                prepStmt1 .executeUpdate();
                return true;
            } catch (Throwable ex) {
                _logger.log(Level.SEVERE,"jts.exception_in_db_log_resource",ex);
                return false;
            } finally {
                try {
                    if (prepStmt1 != null)
                        prepStmt1.close();
                } catch (Exception ex1) {
                    _logger.log(Level.SEVERE,"jts.exception_in_db_log_resource",ex1);
                }
                try {
                    if (conn != null)
                        conn.close();
                } catch (Exception ex1) {
                    _logger.log(Level.SEVERE,"jts.exception_in_db_log_resource",ex1);
                }
            }
        }
        return false;
    }

    boolean deleteRecord(long localTID) {
        return deleteRecord(localTID, serverName);
    }

    boolean deleteRecord(long localTID, String serverName0) {
        if (ds != null) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("LogDBHelper.deleteRecord for localTID: " + localTID + " and serverName: " + serverName0);
            }
            Connection conn = null;
            PreparedStatement prepStmt1 = null;
            try {
                // To avoid compile time dependency to get NonTxConnection
                conn = (Connection)(getNonTxConnectionMethod.invoke(ds, null));
                prepStmt1 = conn.prepareStatement(deleteStatement);
                prepStmt1.setString(1,Long.toString(localTID));
                prepStmt1.setString(2,serverName0); //Configuration.getServerName());
                prepStmt1 .executeUpdate();
                return true;
            } catch (Exception ex) {
                _logger.log(Level.SEVERE,"jts.exception_in_db_log_resource",ex);
                return false;
            } finally {
                try {
                if (prepStmt1 != null)
                    prepStmt1.close();
                if (conn != null)
                    conn.close();
                } catch (Exception ex1) {
                    _logger.log(Level.SEVERE,"jts.exception_in_db_log_resource",ex1);
                }
            }
        }
        return false;
    }


    Map getGlobalTIDMap() {
        return getGlobalTIDMap(serverName);
    }


    Map getGlobalTIDMap(String serverName0) {
        Map gtidMap = new HashMap();
        if (ds != null) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("LogDBHelper get records for serverName: " + serverName0);
            }
            Connection conn = null;
            PreparedStatement prepStmt1 = null;
            ResultSet rs = null;
            try {
                //conn = ds.getConnection();
                conn = (Connection)(getNonTxConnectionMethod.invoke(ds, null));
                prepStmt1 = conn.prepareStatement(selectStatement);
                prepStmt1.setString(1,serverName0); //Configuration.getServerName());
                rs = prepStmt1.executeQuery();
                while (rs.next()) {
                    Long localTID = Long.valueOf(rs.getString(1));
                    byte[] gtridbytes = rs.getBytes(4);
                    if (gtridbytes != null) {
                        // Skip mapping record
                        if (_logger.isLoggable(Level.FINE)) {
                            _logger.fine("LogDBHelper found record for localTID: " + localTID + " and serverName: " + serverName0);
                            _logger.fine("LogDBHelper GlobalTID for localTID: " + localTID + " : " + GlobalTID.fromTIDBytes(gtridbytes));
                        }
                        gtidMap.put(GlobalTID.fromTIDBytes(gtridbytes), localTID);
                    }
                }
            } catch (Exception ex) {
                _logger.log(Level.SEVERE,"jts.exception_in_db_log_resource",ex);
            } finally {
                try {
                    if (rs != null)
                        rs.close();
                } catch (Exception ex1) {
                    _logger.log(Level.SEVERE,"jts.exception_in_db_log_resource",ex1);
                }
                try {
                    if (prepStmt1 != null)
                        prepStmt1.close();
                } catch (Exception ex1) {
                    _logger.log(Level.SEVERE,"jts.exception_in_db_log_resource",ex1);
                }
                try {
                    if (conn != null)
                        conn.close();
                } catch (Exception ex1) {
                    _logger.log(Level.SEVERE,"jts.exception_in_db_log_resource",ex1);
                }
            }
        }
        return gtidMap;
    }


    String getServerNameForInstanceName(String instanceName0) {
        String serverName0 = null;
        if (ds != null) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("LogDBHelper get serverName for instanceName: " + instanceName0);
            }
            Connection conn = null;
            PreparedStatement prepStmt1 = null;
            ResultSet rs = null;
            try {
                //conn = ds.getConnection();
                conn = (Connection)(getNonTxConnectionMethod.invoke(ds, null));
                prepStmt1 = conn.prepareStatement(selectServerNameStatement);
                prepStmt1.setString(1,instanceName0);
                rs = prepStmt1.executeQuery();
                if (rs.next()) {
                    serverName0 = rs.getString(1);
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.fine("LogDBHelper found serverName: " + serverName0 + " for instanceName: " + instanceName0);
                    }
                }
            } catch (Exception ex) {
                _logger.log(Level.SEVERE,"jts.exception_in_db_log_resource",ex);
            } finally {
                try {
                    if (rs != null)
                        rs.close();
                } catch (Exception ex1) {
                    _logger.log(Level.SEVERE,"jts.exception_in_db_log_resource",ex1);
                }
                try {
                    if (prepStmt1 != null)
                        prepStmt1.close();
                } catch (Exception ex1) {
                    _logger.log(Level.SEVERE,"jts.exception_in_db_log_resource",ex1);
                }
                try {
                    if (conn != null)
                        conn.close();
                } catch (Exception ex1) {
                    _logger.log(Level.SEVERE,"jts.exception_in_db_log_resource",ex1);
                }
            }
        }
        return serverName0;
    }

    private void createTable() {
        if (ds != null) {
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("LogDBHelper.createTable for instanceName: " + instanceName);
            }
            Connection conn = null;
            Statement stmt1 = null;
            try {
                conn = (Connection)(getNonTxConnectionMethod.invoke(ds, null));
                stmt1 = conn.createStatement();
                stmt1.execute(createTableStatement);
                _logger.fine("=== table created ===");
            } catch (Exception ex) {
                _logger.log(Level.INFO,"jts.exception_in_db_log_resource_create");
                _logger.log(Level.FINE,"jts.exception_in_db_log_table_create_error", ex);
            } finally {
                try {
                if (stmt1 != null)
                    stmt1.close();
                if (conn != null)
                    conn.close();
                } catch (Exception ex1) {
                    _logger.log(Level.SEVERE,"jts.exception_in_db_log_resource",ex1);
                }
            }
        }
    }
}
