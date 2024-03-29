/*
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.ejte.j2ee.connector.securitymapweb;

import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Base64;

import com.sun.ejte.ccl.reporter.*;

public class WebTest {

    static String host = "localhost";
    static int port;

    private static SimpleReporterAdapter status = new SimpleReporterAdapter();

    private static String driver = null;
    private static String[] dbUsers = { "dbmap1", "dbmap1", "dbmap4", "dbmap3",
        "dbmap4", "dbmap2", "dbmap2", "dbmap4", "dbmap4" };

    public static void main(String[] args) {

        driver = args[0];
        port = Integer.parseInt(args[1]);
        status.addDescription("SecurityMap WebTest");
        try {
            runTests();
        } finally {
            status.printSummary("Securitymap tests");
        }
    }

    public static void runTests() {
        String[] users = { "foo1", "foo2", "foo3", "bar1", "bar2", "foobar",
            "barfoo", "foofoo" , "barbar" };

        for (int i = 0; i <users.length; i++) {
            sendRequest(users[i]);

            if (query(dbUsers[i], users[i]))
                status.addStatus("connector.securitymapweb.test" + (1 + i),
                                 status.PASS);
            else
                status.addStatus("connector.securitymapweb.test" + (1 + i),
                                 status.FAIL);
        }
    }

    public static boolean query(String dbUser, String userName) {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            Class.forName(driver);
            String url = "jdbc:derby://localhost:1527/testdb";

            conn = DriverManager.getConnection(url, dbUser, dbUser);

            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from securitymapwebdb " +
                                   "where name = '" + userName + "'");

            int count  = 0;

            while (rs.next())
                count++;

            rs.close();

            System.out.println("No of records : " + count);

            if (count == 1)
                return true;

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (stmt != null)
                try {
                    stmt.close();
                } catch (Exception e) { }

            if (conn != null)
                try {
                    conn.close();
                } catch (Exception e) { }
        }

        return false;
    }

    /**
     * Connect to host:port and issue GET with given auth info.
     * This is hardcoded to expect the output that is generated
     * by the Test.jsp used in this test case.
     *
     */
    private static void sendRequest(String userName) {
        Socket socket = null;
        OutputStream os = null;
        InputStream is = null;
        try {
            String userpass = encode(userName + ":" + userName);

            String auth = "Authorization: Basic " + userpass + "\n";

            socket = new Socket(host, port);
            os = socket.getOutputStream();

            os.write("GET /security-map-web/ HTTP/1.0\n".getBytes());
            os.write(auth.getBytes());
            os.write("\n".getBytes());

            System.out.println("Send request");

            is = socket.getInputStream();
            BufferedReader bis = new BufferedReader(new InputStreamReader(is));

            String line = null;

            while ((line = bis.readLine()) != null) {
                System.out.println(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (os != null)
                try {
                    os.close();
                } catch (Exception e) { }

            if (is != null)
                try {
                    is.close();
                } catch (Exception e) { }

            if (socket != null)
                try {
                    socket.close();
                } catch (Exception e) { }
        }
    }

    public static String encode(String userpass) {
        return new String(Base64.getEncoder().encodeToString(userpass.getBytes()));
    }

}
