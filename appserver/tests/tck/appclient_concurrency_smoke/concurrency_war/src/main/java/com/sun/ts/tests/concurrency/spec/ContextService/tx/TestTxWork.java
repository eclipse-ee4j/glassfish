/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2013, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ts.tests.concurrency.spec.ContextService.tx;

import static com.sun.ts.tests.concurrency.spec.ContextService.tx.Util.getConnection;
import static com.sun.ts.tests.concurrency.spec.ContextService.tx.Util.lookup;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import jakarta.transaction.UserTransaction;

public class TestTxWork implements TestWorkInterface, Serializable {

    private static final long serialVersionUID = 1L;

    protected String result;
    protected Connection con;
    protected String sqlTemplate;
    protected boolean beginTransaction = false;
    protected boolean commit = false;
    protected boolean rollback = false;
    protected String userName;
    protected String password;

    @Override
    public void doSomeWork() {
        UserTransaction userTransaction = null;

        if (beginTransaction) {
            try {
                userTransaction = lookup("java:comp/UserTransaction");
                userTransaction.begin();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        PreparedStatement statement = null;
        try {
            con = getConnection(false, userName, password);
            statement = con.prepareStatement(sqlTemplate);
            String sTypeDesc = "Type-98";
            int newType = 98;
            statement.setInt(1, newType);
            statement.setString(2, sTypeDesc);
            statement.executeUpdate();

            if (commit) {
                userTransaction.commit();
            }

            if (rollback) {
                userTransaction.rollback();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (statement != null)
                    statement.close();
                if (con != null)
                    con.close();
            } catch (SQLException se) {
            }
        }
    }

    @Override
    public String getResult() {
        return result;
    }

    @Override
    public void setConnection(Connection con) {
        this.con = con;
    }

    @Override
    public void setSQLTemplate(String sqlTemplate) {
        this.sqlTemplate = sqlTemplate;
    }

    @Override
    public void needBeginTx(boolean beginTx) {
        this.beginTransaction = beginTx;
    }

    @Override
    public void needCommit(boolean commit) {
        this.commit = commit;
    }

    @Override
    public void needRollback(boolean rollback) {
        this.rollback = rollback;
    }

    @Override
    public void run() {
        doSomeWork();
    }

    @Override
    public void setUserName(String name) {
        this.userName = name;
    }

    @Override
    public void setPassword(String pass) {
        this.password = pass;
    }
}
