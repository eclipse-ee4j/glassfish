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

import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest
{

    static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "Security Constraint Mapping test";

    public static void main(String args[])
    {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.

        stat.addDescription("Unit test for 4903209");

        String host = args[0];
        String portS = args[1];
        String contextRoot = args[2];

        int port = new Integer(portS).intValue();
        String name;

        try {
            //Check if it strips out uri parameters (";.*") before mapping to webapps
            goGet(host, port, contextRoot + "/ServletTest;test=aaa" );
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary(TEST_NAME + " ---> PASS");
    }

    private static void goGet(String host, int port,
                              String contextPath)
         throws Exception
    {

        URL url = new URL("http://" + host  + ":" + port + contextPath);
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        System.out.println("Response code: " + responseCode);
        if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
            url = new URL(conn.getHeaderField("Location"));
            System.out.println("Redirected to: " + url.toString());
            conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            responseCode = conn.getResponseCode();
        }

        boolean pass = false;

        if (responseCode == HttpURLConnection.HTTP_OK) {
            InputStream is = null;
            BufferedReader input = null;
            String line = null;
            try {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                // Check if login page gets displayed
                while ((line = input.readLine()) != null) {
                    System.out.println(line);
                    if (line.startsWith("login.jsp")) {
                        pass = true;
                        break;
                    }
                }
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
            }
        }

        if (!pass) {
            throw new Exception("security constraint NOT processed");
        }
    }
}
