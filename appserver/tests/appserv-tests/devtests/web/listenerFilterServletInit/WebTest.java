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

    private static final String TEST_NAME = "Listener Filter Servlet init test";
    private static final String EXPECTED_RESPONSE = "LLiFFiSSi";

    private String host;
    private String portS;
    private String contextRoot;
    private Socket sock = null;

    public WebTest(String[] args) {
        host = args[0];
        portS = args[1];
        contextRoot = args[2];
    }

    public static void main(String args[]) {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.

        stat.addDescription("Unit test for IT 13555");

        WebTest webTest = new WebTest(args);

        int port = new Integer(webTest.portS).intValue();
        String name;

        try {
            webTest.goGet(webTest.host, port,
                webTest.contextRoot + "/test" );
        } catch (Throwable t) {
            System.out.println(t.getMessage());
            stat.addStatus(" Test " + TEST_NAME + " UNPREDICTED-FAILURE",
                stat.FAIL);
        } finally {
            try {
                if (webTest.sock != null) {
                    webTest.sock.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        stat.printSummary(TEST_NAME + " ---> PASS");
    }

    private void goGet(String host, int port, String contextPath)
            throws Exception {

        sock = new Socket(host, port);
        OutputStream os = sock.getOutputStream();

        System.out.println(("GET " + contextPath + " HTTP/1.0\n"));
        os.write(("GET " + contextPath + " HTTP/1.0\n").getBytes());
        os.write("\n".getBytes());

        InputStream is = null;
        BufferedReader bis = null;
        String line = null;
        boolean pass = false;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                // Check if the filter was invoked
                if (EXPECTED_RESPONSE.equals("LLiFFiSSi")) {
                    pass = true;
                    break;
                }
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        if (pass) {
            System.out.println("security constraint processed");
            stat.addStatus(TEST_NAME + " PASSED", stat.PASS);
        } else {
            System.out.println("security constraint NOT processed");
            stat.addStatus(TEST_NAME + " FAILED", stat.FAIL);
        }
   }

}
