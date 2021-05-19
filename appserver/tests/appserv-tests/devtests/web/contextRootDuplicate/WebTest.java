/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for GLASSFISH-18861
 *
 * After setting context root of two wars which deployed on the server target with the same value,
 * both of the two wars accessed failed.
 *
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "context-root-duplicate";

    private String host;
    private String port;
    private String contextRoot1;
    private String contextRoot2;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot1 = args[2];
        contextRoot2 = args[3];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for GLASSFISH-18861");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invokeJsp();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJsp() throws Exception {

        URL url = new URL("http://" + host + ":" + port + "/"
                          + contextRoot1 + "/jsp/test.jsp");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            System.out.println("Wrong response code. Expected: 200, received: "
                           + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
            return;
        }

        url = new URL("http://" + host + ":" + port + "/"
                          + contextRoot2 + "/jsp/test.jsp");
        System.out.println("Connecting to: " + url.toString());
        conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.out.println("Wrong response code. Expected: 200, received: "
                           + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
