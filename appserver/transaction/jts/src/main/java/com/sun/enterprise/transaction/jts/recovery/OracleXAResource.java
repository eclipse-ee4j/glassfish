/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

//Source File Name:   OracleXAResource.java

package com.sun.enterprise.transaction.jts.recovery;

import com.sun.enterprise.transaction.JavaEETransactionManagerSimplified;
import com.sun.enterprise.transaction.api.XAResourceWrapper;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;

import static com.sun.logging.LogDomains.JTA_LOGGER;
import static java.util.logging.Level.FINEST;

/**
 * This implements workaround for Oracle XAResource. Oracle's 8.1.7 XAResource implementation doesn't work fine while
 * recovery. This class fires sql statements to achieve same.
 *
 * @author <a href="mailto:bala.dutt@sun.com">Bala Dutt</a>
 * @version 1.0
 */
public class OracleXAResource extends XAResourceWrapper {

    // Use superclass for Sting Manager
    private static final StringManager sm = StringManager.getManager(XAResourceWrapper.class);

    // Use JTA_LOGGER for backward compatibility, so use a class from
    // 'jta' bundle to load it.
    private static final Logger _logger = LogDomains.getLogger(JavaEETransactionManagerSimplified.class, JTA_LOGGER);

    @Override
    public XAResourceWrapper getInstance() {
        return new OracleXAResource();
    }

    /**
     * Recovers list of xids in transaction table. Recover on oracle ignores flags sent to it, this method takes care of
     * flags in addition to calling recoverList for xid list.
     *
     * @param flag an <code>int</code> value
     * @return a <code>Xid[]</code> value
     * @exception XAException if an error occurs
     */
    @Override
    public Xid[] recover(int flag) throws XAException {
        if (flag == XAResource.TMNOFLAGS) {
            return null;
        }

        return recoverList(flag);
    }

    /**
     * Fires a select statement so that transaction xids are updated and retrieve the xid list. Oracle doesn't update the
     * xid's for sometime. After this update, recover of real oracle xa resource is is used get xid list.
     *
     * @return a <code>Xid[]</code> value
     * @exception XAException if an error occurs
     */
    private Xid[] recoverList(int flag) throws XAException {
        Statement statement = null;
        ResultSet resultset = null;
        Connection connection = null;
        try {
            connection = (Connection) m_xacon.getConnection(subject, null);
            if (connection == null) {
                // throw new XAException("Oracle XA Resource wrapper : connection could not be got");
                throw new XAException(sm.getString("transaction.oracle_xa_wrapper_connection_failed"));
            }
            statement = connection.createStatement();
            resultset = statement.executeQuery("select pending.local_tran_id from SYS.PENDING_TRANS$ pending, SYS.DBA_2PC_NEIGHBORS");
            resultset.close();
            resultset = null;
            statement.close();
            statement = null;
            return m_xacon.getXAResource().recover(flag);
        } catch (SQLException sqlexception) {
            throw new XAException(sm.getString("transaction.oracle_sqlexception_occurred", sqlexception));
        } catch (XAException e) {
            throw e;
        } catch (Exception e) {
            throw new XAException(sm.getString("transaction.oracle_unknownexception_occurred", e));
        } finally {
            if (resultset != null) {
                try {
                    resultset.close();
                } catch (SQLException sqlexception1) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sqlexception2) {
                }
            }
        }
    }

    @Override
    public void commit(Xid xid, boolean flag) throws XAException {
        doRecovery(xid, true);
    }

    @Override
    public void rollback(Xid xid) throws XAException {
        doRecovery(xid, false);
    }

    /**
     * Does actual recovery depending on boolean argument - true for commmit.
     *
     * @param xid a <code>Xid</code> value
     * @param isCommit a <code>boolean</code> value
     * @exception XAException if an error occurs
     */
    private void doRecovery(Xid xid, boolean isCommit) throws XAException {
        try {
            if (isCommit) {
                m_xacon.getXAResource().commit(xid, true);
            } else {
                m_xacon.getXAResource().rollback(xid);
            }
        } catch (XAException ex) {
            _logger.log(FINEST, " An XAException occurred while calling XAResource method ", ex);
        } catch (Exception ex) {
            _logger.log(FINEST, " An Exception occurred while calling XAResource method ", ex);
        }

        Statement statement = null;
        ResultSet resultset = null;
        Connection connection = null;
        try {
            connection = (Connection) m_xacon.getConnection(subject, null);
            if (connection == null) {
                throw new XAException(sm.getString("transaction.oracle_xa_wrapper_connection_failed"));
            }

            statement = connection.createStatement();
            resultset = statement.executeQuery(
                    "select pending.local_tran_id from SYS.PENDING_TRANS$ pending, SYS.DBA_2PC_NEIGHBORS dba where pending.global_foreign_id = '"
                            + toHexString(xid.getGlobalTransactionId())
                            + "' and pending.local_tran_id = dba.local_tran_id and dba.branch = '" + toHexString(xid.getBranchQualifier())
                            + "' and pending.state = 'prepared'");
            if (resultset.next()) {
                String s = resultset.getString(1);
                resultset.close();
                resultset = null;
                statement.executeUpdate((isCommit ? "commit force '" : "rollback force '") + s + "'");
                statement.close();
                statement = null;
            }
        } catch (SQLException sqlexception) {
            _logger.log(Level.FINE, " An SQLException during recovery ", sqlexception);
            throw new XAException(sm.getString("transaction.oracle_sqlexception_occurred", sqlexception));
        } catch (Exception e) {
            _logger.log(Level.FINE, " An Exception during recovery ", e);
            throw new XAException(sm.getString("transaction.oracle_unknownexception_occurred", e));
        } finally {
            if (resultset != null) {
                try {
                    resultset.close();
                } catch (SQLException sqlexception1) {
                }
            }
            if (statement != null) {
                try {
                    statement.close();
                } catch (SQLException sqlexception2) {
                }
            }
        }
    }

    private static final char HEX_DIGITS[] = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

    /**
     * Converts Xids into string that can be used in sql statements for oracle.
     *
     * @param abyte0[] a <code>byte</code> value
     * @return a <code>String</code> value
     */
    private static String toHexString(byte abyte0[]) {
        StringBuffer stringbuffer = new StringBuffer();
        if (null != abyte0 && 0 < abyte0.length) {
            for (int i = 0; i < abyte0.length; i++) {
                stringbuffer.append(HEX_DIGITS[(abyte0[i] & 0xf0) >> 4]);
                stringbuffer.append(HEX_DIGITS[abyte0[i] & 0xf]);
            }
            return stringbuffer.toString();
        }

        return "";
    }
}
