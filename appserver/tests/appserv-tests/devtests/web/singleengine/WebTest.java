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

import java.io.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/**
 * Unit test for 4929994 ("Admin virtual server should be treated as
 * special case").
 * See also https://glassfish.dev.java.net/issues/show_bug.cgi?id=7548
 *
 * Make sure the admin port has not been accessed up until the time this
 * test is run.
 */
public class WebTest{

    private static final String TEST_NAME = "single-engine";

    private static final SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String adminPort;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        adminPort = args[3];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for 4929994");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest(webTest.port, 200);
            webTest.doTest(webTest.adminPort, 202);
            // Sleep long enough for the admin console to have been
            // installed and deployed
            Thread.currentThread().sleep(60000);
            webTest.doTest(webTest.adminPort, 404);
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void doTest(String port, int expectedResponseStatus) throws Exception {

        URL url = new URL("http://" + host  + ":" + port +
            contextRoot + "/ServletTest");
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        int responseCode = conn.getResponseCode();
        System.out.println("Response code "+responseCode);
        if (responseCode != expectedResponseStatus) {
            throw new Exception("Unexpected return code: " + responseCode +
                ", expected: " + expectedResponseStatus);
        }
    }
}
