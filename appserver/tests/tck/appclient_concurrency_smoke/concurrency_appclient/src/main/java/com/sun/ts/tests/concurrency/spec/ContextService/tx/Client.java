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

import static com.sun.ts.tests.concurrency.spec.ContextService.tx.Constants.DS_JNDI_NAME;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.sql.DataSource;

import com.sun.javatest.Status;
import com.sun.ts.lib.harness.EETest;
import com.sun.ts.lib.util.TestUtil;

import jakarta.annotation.Resource;

public class Client extends EETest {

    private static final long serialVersionUID = 1L;

    private String host = null;

    private int port;

    private Properties props = null;

    private String appendedURL = "";

    private String username, password;

    private String tablename;

    @Resource(lookup = DS_JNDI_NAME)
    private static DataSource dataSource;

    private Connection conn;

    private static final String CONTEXT_PATH = "/concurrency_spec_ContextService_tx_web";

    private String testURL;

    public static void main(String[] args) {
        Client theTests = new Client();
        Status s = theTests.run(args, System.out, System.err);
        s.exit();
    }

    /*
     * @class.setup_props: webServerHost; webServerPort; ts_home; Driver, the Driver name; db1, the database name with url;
     * user1, the database user name; password1, the database password; db2, the database name with url; user2, the database
     * user name; password2, the database password; DriverManager, flag for DriverManager; ptable, the primary table;
     * ftable, the foreign table; cofSize, the initial size of the ptable; cofTypeSize, the initial size of the ftable;
     * binarySize, size of binary data type; varbinarySize, size of varbinary data type; longvarbinarySize, size of
     * longvarbinary data type;
     *
     * @class.testArgs: -ap tssql.stmt
     */
    public void setup(String[] args, Properties p) throws Fault {
        TestUtil.logMsg("setup");
        try {
            // get props
            port = Integer.parseInt(p.getProperty("webServerPort"));
            host = p.getProperty("webServerHost");

            // check props for errors
            if (port < 1) {
                throw new Exception("'port' in ts.jte must be > 0");
            }
            if (host == null) {
                throw new Exception("'host' in ts.jte must not be null ");
            }

            props = new Properties(p);
            tablename = (String) p.get(Constants.TABLE_P);
            appendedURL = appendedURL(p);
            username = p.getProperty(Constants.USERNAME);
            password = p.getProperty(Constants.PASSWORD);
            testURL = "http://" + host + ":" + port + CONTEXT_PATH + "/TxServlet" + appendedURL;
            removeTestData();
        } catch (Exception e) {
            TestUtil.printStackTrace(e);
            throw new Fault("Setup failed!", e);
        }
    }

    /*
     * @testName: testTransactionOfExecuteThreadAndCommit
     *
     * @assertion_ids: CONCURRENCY:SPEC:86; CONCURRENCY:SPEC:87; CONCURRENCY:SPEC:88; CONCURRENCY:SPEC:89;
     * CONCURRENCY:SPEC:90; CONCURRENCY:SPEC:31.2; CONCURRENCY:SPEC:32; CONCURRENCY:SPEC:34; CONCURRENCY:SPEC:8.1;
     * CONCURRENCY:SPEC:9;
     *
     * @test_Strategy: Get UserTransaction in Servlet and insert 2 row data. Create a proxy with Transaction property
     * "TransactionOfExecuteThread". Invoke proxy in Servlet. In proxy, insert 1 row data commit in Servlet. Expect insert
     * actions in servlet and in proxy will be committed.
     */

    public void testTransactionOfExecuteThreadAndCommit() throws Fault {
        URL url;
        String resp = null;
        try {
            url = new URL(testURL + "&methodname=TransactionOfExecuteThreadAndCommitTest");
            resp = TestUtil.getResponse(url.openConnection());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("The result is : " + resp);
        if (resp == null || !"3".equals(resp.trim())) {
            throw new Fault("testTransactionOfExecuteThreadAndCommit fail to get successful result.");
        }
    }

    /*
     * @testName: testTransactionOfExecuteThreadAndRollback
     *
     * @assertion_ids: CONCURRENCY:SPEC:86; CONCURRENCY:SPEC:87; CONCURRENCY:SPEC:88; CONCURRENCY:SPEC:89;
     * CONCURRENCY:SPEC:90; CONCURRENCY:SPEC:31.2; CONCURRENCY:SPEC:32; CONCURRENCY:SPEC:34; CONCURRENCY:SPEC:8.1;
     * CONCURRENCY:SPEC:9;
     *
     * @test_Strategy: Get UserTransaction in Servlet and insert 2 row data. Create a proxy with Transaction property
     * "TransactionOfExecuteThread". Invoke proxy in Servlet. In proxy, insert 1 row data rollback in Servlet. Expect insert
     * actions in servlet and in proxy will be roll backed.
     */

    public void testTransactionOfExecuteThreadAndRollback() throws Fault {
        URL url;
        String resp = null;
        try {
            url = new URL(testURL + "&methodname=TransactionOfExecuteThreadAndRollbackTest");
            resp = TestUtil.getResponse(url.openConnection());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("The result is : " + resp);
        if (resp == null || !"0".equals(resp.trim())) {
            throw new Fault("testTransactionOfExecuteThreadAndRollback fail to get successful result.");
        }
    }

    /*
     * @testName: testSuspendAndCommit
     *
     * @assertion_ids: CONCURRENCY:SPEC:86; CONCURRENCY:SPEC:87; CONCURRENCY:SPEC:88; CONCURRENCY:SPEC:89;
     * CONCURRENCY:SPEC:90; CONCURRENCY:SPEC:31.2; CONCURRENCY:SPEC:32; CONCURRENCY:SPEC:34; CONCURRENCY:SPEC:8.1;
     * CONCURRENCY:SPEC:9;
     *
     * @test_Strategy: Get UserTransaction in Servlet and insert 2 row data. Create a proxy with Transaction property
     * "SUSPEND". Invoke proxy in Servlet. In proxy, get UserTransaction then insert 1 row data and commit Rollback in
     * Servlet. Expect insert action in servlet will be roll backed and insert action in proxy will be committed.
     */

    public void testSuspendAndCommit() throws Fault {
        URL url;
        String resp = null;
        try {
            url = new URL(testURL + "&methodname=SuspendAndCommitTest");
            resp = TestUtil.getResponse(url.openConnection());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("The result is : " + resp);
        if (resp == null || !"1".equals(resp.trim())) {
            throw new Fault("testSuspendAndCommit fail to get successful result.");
        }
    }

    /*
     * @testName: testSuspendAndRollback
     *
     * @assertion_ids: CONCURRENCY:SPEC:86; CONCURRENCY:SPEC:87; CONCURRENCY:SPEC:88; CONCURRENCY:SPEC:89;
     * CONCURRENCY:SPEC:90; CONCURRENCY:SPEC:31.2; CONCURRENCY:SPEC:32; CONCURRENCY:SPEC:34; CONCURRENCY:SPEC:8.1;
     * CONCURRENCY:SPEC:9;
     *
     * @test_Strategy: Get UserTransaction in Servlet and insert 2 row data. Create a proxy with Transaction property
     * "SUSPEND". Invoke proxy in Servlet. In proxy, get UserTransaction then insert 1 row data and rollback Commit in
     * Servlet. Expect insert action in servlet will be committed and insert action in proxy will be roll backed.
     */

    public void testSuspendAndRollback() throws Fault {
        URL url;
        String resp = null;
        try {
            url = new URL(testURL + "&methodname=SuspendAndRollbackTest");
            resp = TestUtil.getResponse(url.openConnection());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("The result is : " + resp);
        if (resp == null || !"2".equals(resp.trim())) {
            throw new Fault("testSuspendAndRollback fail to get successful result.");
        }
    }

    /*
     * @testName: testDefaultAndCommit
     *
     * @assertion_ids: CONCURRENCY:SPEC:86; CONCURRENCY:SPEC:87; CONCURRENCY:SPEC:88; CONCURRENCY:SPEC:89;
     * CONCURRENCY:SPEC:90; CONCURRENCY:SPEC:91; CONCURRENCY:SPEC:31.2; CONCURRENCY:SPEC:32; CONCURRENCY:SPEC:34;
     * CONCURRENCY:SPEC:8.1; CONCURRENCY:SPEC:9;
     *
     * @test_Strategy: Get UserTransaction in Servlet and insert 2 row data. Create a proxy with default Transaction
     * property. Invoke proxy in Servlet. In proxy, get UserTransaction then insert 1 row data and commit Rollback in
     * Servlet. Expect insert action in servlet will be roll backed and insert action in proxy will be committed.
     */

    public void testDefaultAndCommit() throws Fault {
        URL url;
        String resp = null;
        try {
            url = new URL(testURL + "&methodname=DefaultAndCommitTest");
            resp = TestUtil.getResponse(url.openConnection());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("The result is : " + resp);
        if (resp == null || !"1".equals(resp.trim())) {
            throw new Fault("testDefaultAndCommit fail to get successful result.");
        }
    }

    private String appendedURL(Properties p) throws UnsupportedEncodingException {
        StringBuffer sb = new StringBuffer("?");
        sb.append(Constants.USERNAME + "=" + p.get(Constants.USERNAME));
        sb.append("&");
        sb.append(Constants.PASSWORD + "=" + p.get(Constants.PASSWORD));
        sb.append("&");
        sb.append(Constants.TABLE_P + "=" + Constants.TABLE_P);
        sb.append("&");
        sb.append(Constants.SQL_TEMPLATE + "=" + URLEncoder.encode(p.get(Constants.SQL_TEMPLATE).toString(), "utf8"));
        return sb.toString();
    }

    public void cleanup() throws Fault {
        try {
            removeTestData();
        } catch (Exception e) {
            e.printStackTrace();
            throw new Fault("cleanup failed.", e);
        }
        TestUtil.logTrace("test cleanup ok");
    }

    private void removeTestData() throws RemoteException {
        TestUtil.logTrace("removeTestData");

        // init connection.
        conn = Util.getConnection(dataSource, username, password, true);
        String removeString = props.getProperty("Dbschema_Concur_Delete", "");
        try {
            Statement stmt = conn.createStatement();
            stmt.executeUpdate(removeString);
            stmt.close();
        } catch (Exception e) {
            TestUtil.printStackTrace(e);

            throw new RemoteException(e.getMessage());
        } finally {
            try {
                conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }
}
