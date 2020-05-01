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

import jakarta.resource.ResourceException;
import jakarta.resource.spi.EISSystemException;
import jakarta.resource.spi.LocalTransaction;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author Tony Ng
 */
public class LocalTransactionImpl implements LocalTransaction {

    // XXX TODO: call sequence, tx.begin

    private JdbcManagedConnection mc;

    public LocalTransactionImpl(JdbcManagedConnection mc) {
        this.mc = mc;
    }

    public void begin() throws ResourceException {
        try {
            Connection con = mc.getJdbcConnection();
            con.setAutoCommit(false);
        } catch (SQLException ex) {
            ResourceException re = new EISSystemException(ex.getMessage());
            re.setLinkedException(ex);
            throw re;
        }
    }

    public void commit() throws ResourceException {
        Connection con = null;
        try {
            con = mc.getJdbcConnection();
            con.commit();
        } catch (SQLException ex) {
            ResourceException re = new EISSystemException(ex.getMessage());
            re.setLinkedException(ex);
            throw re;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception ex) {
            }
        }
    }

    public void rollback() throws ResourceException {
        Connection con = null;
        try {
            con = mc.getJdbcConnection();
            con.rollback();
        } catch (SQLException ex) {
            ResourceException re = new EISSystemException(ex.getMessage());
            re.setLinkedException(ex);
            throw re;
        } finally {
            try {
                con.setAutoCommit(true);
            } catch (Exception ex) {
            }
        }
    }
}
