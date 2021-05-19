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
 * Unit test for <servlet-mapping> with multiple <url-pattern> subelements
 * (Servlet 2.5 feature).
 */
public class WebTest {

    private static final String TEST_NAME =
        "servlet-mapping-with-multiple-url-patterns";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for servlet-mapping with "
                            + "multiple url-pattern subelements");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest();
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void doTest() throws Exception {

        boolean fail = false;
        URL url = null;
        HttpURLConnection conn = null;
        int responseCode = -1;

        url = new URL("http://" + host  + ":" + port
                      + contextRoot + "/Servlet_1");
        System.out.println("Connecting to: " + url.toString());
        conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            fail = true;
        }

        url = new URL("http://" + host  + ":" + port
                      + contextRoot + "/Servlet_2");
        System.out.println("Connecting to: " + url.toString());
        conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            fail = true;
        }

        if (fail) {
            System.err.println("Unexpected return code: " + responseCode);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            stat.addStatus(TEST_NAME, stat.PASS);
        }
    }
}
