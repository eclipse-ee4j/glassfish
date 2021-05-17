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
import java.util.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for Bugzilla 34113 ("setHeader( ) method in Response object does
 * not clear multiple values").
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "set-multi-valued-response-header";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugzilla 34113");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            String url = "http://" + host + ":" + port + contextRoot
                + "/SetHeadersServlet";
            HttpURLConnection conn = (HttpURLConnection)
                (new URL(url)).openConnection();

            int code = conn.getResponseCode();
            if (code != 200) {
                System.err.println("Unexpected return code: " + code);
                stat.addStatus(TEST_NAME, stat.FAIL);
                return;
            }

            Map headers = conn.getHeaderFields();
            if (headers == null) {
                System.err.println("No response headers");
                stat.addStatus(TEST_NAME, stat.FAIL);
                return;
            }

            List values = (List) headers.get("Cache-Control");

            //In case of WS7.0, the header is "Cache-control"
            if (values == null) {
                values = (List) headers.get("Cache-control");
            }
            if (values == null) {
                System.err.println("No Cache-Control response headers");
                stat.addStatus(TEST_NAME, stat.FAIL);
                return;
            }

            if (values.size() != 1) {
                System.err.println(
                    "Wrong number of Cache-Control response header values");
                stat.addStatus(TEST_NAME, stat.FAIL);
                return;
            }

            if ("public".equals(values.get(0))) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Wrong Cache-Control response header");
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }
}
