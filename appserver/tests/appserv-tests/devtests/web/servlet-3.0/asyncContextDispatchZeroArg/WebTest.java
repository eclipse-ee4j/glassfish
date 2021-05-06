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
 * Unit test for zero-argument AsyncContext#dispatch
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-3.0-async-context-zero-arg-dispatch";

    private static final String EXPECTED_RESPONSE = "Hello world";

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

    public static void main(String[] args) throws Exception {

        stat.addDescription("Unit test for AsyncContext#dispatch");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest("/TestServlet");
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void doTest(String target) throws Exception {

        URL url = new URL("http://" + host  + ":" + port
            + contextRoot + target);
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.connect();
        if (conn.getResponseCode() != 200) {
            throw new Exception("Unexpected return code: " +
                                conn.getResponseCode());
        }

        InputStream is = null;
        BufferedReader input = null;
        String line = null;
        try {
            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            while ((line = input.readLine()) != null) {
                System.out.println(line);
                if (line.equals(EXPECTED_RESPONSE)) {
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

        if (line == null) {
            throw new Exception("Missing or unexpected response body, " +
                                "expected: " + EXPECTED_RESPONSE);
        }
    }
}
