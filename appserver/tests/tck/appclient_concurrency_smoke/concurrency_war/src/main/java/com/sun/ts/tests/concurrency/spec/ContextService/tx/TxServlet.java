/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation. All rights reserved.
 * Copyright (c) 2013, 2018, 2020 Oracle and/or its affiliates. All rights reserved.
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

import static com.sun.ts.tests.concurrency.spec.ContextService.tx.Constants.PASSWORD;
import static com.sun.ts.tests.concurrency.spec.ContextService.tx.Constants.SQL_TEMPLATE;
import static com.sun.ts.tests.concurrency.spec.ContextService.tx.Constants.TABLE_P;
import static com.sun.ts.tests.concurrency.spec.ContextService.tx.Constants.USERNAME;
import static com.sun.ts.tests.concurrency.spec.ContextService.tx.Util.getConnection;
import static com.sun.ts.tests.concurrency.spec.ContextService.tx.Util.getCount;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import jakarta.annotation.Resource;
import jakarta.enterprise.concurrent.ContextService;
import jakarta.enterprise.concurrent.ManagedTask;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.UserTransaction;

@WebServlet("/TxServlet")
public class TxServlet extends HttpServlet {
    private static final long serialVersionUID = -4483473120784959634L;

    private static final String METHOD_PARAM_NAME = "methodname";

    @Resource(lookup = "java:comp/DefaultContextService")
    private ContextService contextService;

    @Resource(lookup = "java:comp/UserTransaction")
    private UserTransaction userTransaction;

    private String tableName;
    private String username;
    private String password;
    private String sqlTemplate;

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String methodName = request.getParameter(METHOD_PARAM_NAME);
        Map<String, String> params = null;
        try {
            params = formatMap(request);
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

        username = params.get(USERNAME);
        password = params.get(PASSWORD);
        tableName = params.get(TABLE_P);
        sqlTemplate = params.get(SQL_TEMPLATE);

        String s = (String) invoke(this, methodName, new Class[] {}, new Object[] {});

        response.getWriter().write(s);
    }

    public String TransactionOfExecuteThreadAndCommitTest() throws ServletException {
        PreparedStatement statement = null;
        Connection connection = null;

        try {
            int originCount = getCount(tableName, username, password);
            userTransaction.begin();
            connection = getConnection(false, username, password);
            statement = connection.prepareStatement(sqlTemplate);
            statement.setInt(1, 99);
            statement.setString(2, "Type-99");
            statement.addBatch();
            statement.setInt(1, 100);
            statement.setString(2, "Type-100");
            statement.addBatch();
            statement.executeBatch();

            TestWorkInterface work = new TestTxWork();
            work.setUserName(username);
            work.setPassword(password);
            work.setSQLTemplate(sqlTemplate);
            Map<String, String> m = new HashMap();
            m.put(ManagedTask.TRANSACTION, ManagedTask.USE_TRANSACTION_OF_EXECUTION_THREAD);
            TestWorkInterface proxy = contextService.createContextualProxy(work, m, TestWorkInterface.class);
            proxy.doSomeWork();
            userTransaction.commit();
            int afterTransacted = getCount(tableName, username, password);

            return String.valueOf(afterTransacted - originCount);
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            try {
                if (statement != null)
                    statement.close();
                if (connection != null)
                    connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String TransactionOfExecuteThreadAndRollbackTest() throws ServletException {
        PreparedStatement pStmt = null;
        Connection conn = null;

        try {
            int originCount = getCount(tableName, username, password);
            userTransaction.begin();
            conn = getConnection(false, username, password);
            pStmt = conn.prepareStatement(sqlTemplate);
            pStmt.setInt(1, 99);
            pStmt.setString(2, "Type-99");
            pStmt.addBatch();
            pStmt.setInt(1, 100);
            pStmt.setString(2, "Type-100");
            pStmt.addBatch();
            pStmt.executeBatch();

            TestWorkInterface work = new TestTxWork();
            work.setUserName(username);
            work.setPassword(password);
            work.setSQLTemplate(sqlTemplate);
            Map<String, String> m = new HashMap();
            m.put(ManagedTask.TRANSACTION, ManagedTask.USE_TRANSACTION_OF_EXECUTION_THREAD);
            TestWorkInterface proxy = contextService.createContextualProxy(work, m, TestWorkInterface.class);
            proxy.doSomeWork();
            userTransaction.rollback();
            int afterTransacted = getCount(tableName, username, password);

            return String.valueOf(afterTransacted - originCount);
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            try {
                if (pStmt != null)
                    pStmt.close();
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String SuspendAndCommitTest() throws ServletException {
        PreparedStatement pStmt = null;
        Connection conn = null;
        Connection conn2 = null;

        try {
            int originCount = getCount(tableName, username, password);
            userTransaction.begin();
            conn = getConnection(false, username, password);
            pStmt = conn.prepareStatement(sqlTemplate);
            pStmt.setInt(1, 99);
            pStmt.setString(2, "Type-99");
            pStmt.addBatch();
            pStmt.setInt(1, 100);
            pStmt.setString(2, "Type-100");
            pStmt.addBatch();
            pStmt.executeBatch();
            TestWorkInterface work = new TestTxWork();
            work.setUserName(username);
            work.setPassword(password);
            work.setSQLTemplate(sqlTemplate);
            work.needBeginTx(true);
            work.needCommit(true);
            Map<String, String> m = new HashMap();
            m.put(ManagedTask.TRANSACTION, ManagedTask.SUSPEND);
            TestWorkInterface proxy = contextService.createContextualProxy(work, m, TestWorkInterface.class);
            proxy.doSomeWork();
            userTransaction.rollback();
            int afterTransacted = getCount(tableName, username, password);

            return String.valueOf(afterTransacted - originCount);
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            try {
                if (pStmt != null)
                    pStmt.close();
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String SuspendAndRollbackTest() throws ServletException {
        PreparedStatement pStmt = null;
        Connection conn = null;
        Connection conn2 = null;

        try {
            int originCount = getCount(tableName, username, password);
            userTransaction.begin();
            conn = getConnection(false, username, password);
            pStmt = conn.prepareStatement(sqlTemplate);
            pStmt.setInt(1, 99);
            pStmt.setString(2, "Type-99");
            pStmt.addBatch();
            pStmt.setInt(1, 100);
            pStmt.setString(2, "Type-100");
            pStmt.addBatch();
            pStmt.executeBatch();
            TestWorkInterface work = new TestTxWork();
            work.setUserName(username);
            work.setPassword(password);
            work.setSQLTemplate(sqlTemplate);
            work.needBeginTx(true);
            work.needRollback(true);
            Map<String, String> m = new HashMap();
            m.put(ManagedTask.TRANSACTION, ManagedTask.SUSPEND);
            TestWorkInterface proxy = contextService.createContextualProxy(work, m, TestWorkInterface.class);
            proxy.doSomeWork();
            userTransaction.commit();
            int afterTransacted = getCount(tableName, username, password);

            return String.valueOf(afterTransacted - originCount);
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            try {
                if (pStmt != null)
                    pStmt.close();
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String DefaultAndCommitTest() throws ServletException {
        PreparedStatement pStmt = null;
        Connection conn = null;
        Connection conn2 = null;

        try {
            int originCount = getCount(tableName, username, password);
            userTransaction.begin();
            conn = getConnection(false, username, password);
            pStmt = conn.prepareStatement(sqlTemplate);
            pStmt.setInt(1, 99);
            pStmt.setString(2, "Type-99");
            pStmt.addBatch();
            pStmt.setInt(1, 100);
            pStmt.setString(2, "Type-100");
            pStmt.addBatch();
            pStmt.executeBatch();
            TestWorkInterface work = new TestTxWork();
            work.setUserName(username);
            work.setPassword(password);
            work.setSQLTemplate(sqlTemplate);
            work.needBeginTx(true);
            work.needCommit(true);
            TestWorkInterface proxy = contextService.createContextualProxy(work, TestWorkInterface.class);
            proxy.doSomeWork();
            userTransaction.rollback();
            // int afterTransacted = Util.getCount(tableName, conn);
            int afterTransacted = getCount(tableName, username, password);

            return String.valueOf(afterTransacted - originCount);
        } catch (Exception e) {
            throw new ServletException(e);
        } finally {
            try {
                if (pStmt != null)
                    pStmt.close();
                if (conn != null)
                    conn.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Object invoke(Object o, String methodName, Class[] paramTypes, Object[] args) throws ServletException {

        try {
            if (o == null || methodName == null || "".equals(methodName.trim())) {
                throw new IllegalArgumentException("Object and methodName must not be null");
            }
            Method method = null;
            if (paramTypes != null && paramTypes.length > 0) {
                method = o.getClass().getMethod(methodName, paramTypes);
            } else {
                method = o.getClass().getMethod(methodName);
            }

            Object result = null;
            if (method != null) {
                if (args != null && args.length > 0) {
                    result = method.invoke(o, args);
                } else {
                    result = method.invoke(o);
                }
            }

            return result;

        } catch (ReflectiveOperationException | IllegalArgumentException e) {
            throw new ServletException(e);
        }
    }

    private Map<String, String> formatMap(HttpServletRequest request) throws UnsupportedEncodingException {
        Map<String, String> props = new HashMap<String, String>();
        Enumeration<String> params = request.getParameterNames();
        while (params.hasMoreElements()) {
            String name = params.nextElement();
            String value = request.getParameter(name);
            props.put(name, URLDecoder.decode(value, "utf8"));
        }
        return props;
    }
}
