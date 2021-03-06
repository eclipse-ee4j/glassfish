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

/*
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=896
 * ("Do not include cookie for invalidated HTTP session in the response").
 */
public class WebTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String EXPECTED_COOKIE_HEADER =
        " myCookieHeader=myCookieValue";

    private static final String TEST_NAME =
        "session-invalidate-no-cookie-in-response";

    private String host;
    private String port;
    private String contextRoot;
    private boolean fail;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for GlassFish 896");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {

        try {
            run();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        return;
    }

    private void run() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/TestServlet HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        String cookieHeader = null;
        int cookieCount = 0;
        while ((line = bis.readLine()) != null) {
            if (line.toLowerCase().startsWith("set-cookie:")) {
                int colon = line.indexOf(':');
                cookieHeader = line.substring(colon+1);
                cookieCount++;
            }
        }

        if (cookieCount != 1) {
            System.err.println("Wrong number of cookie response headers. ");
            System.err.println("Expected 1, got " + cookieCount);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else if (!EXPECTED_COOKIE_HEADER.equals(cookieHeader)) {
            System.err.println("Wrong cookie response header. ");
            System.err.println("Expected: " + EXPECTED_COOKIE_HEADER
                               + ", got: " + cookieHeader);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            stat.addStatus(TEST_NAME, stat.PASS);
        }
    }
}
