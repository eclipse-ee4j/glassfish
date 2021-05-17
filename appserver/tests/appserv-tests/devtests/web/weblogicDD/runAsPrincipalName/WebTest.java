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
 * Unit test for run-as-principal-name in weblogic.xml.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "wl-run-as-principal-name";
    private static final String EXPECTED_RESPONSE = "Hello";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for run-as-principal-name in weblogic.xml");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            boolean ok = runTest("/mytest", 200, EXPECTED_RESPONSE);
            stat.addStatus(TEST_NAME, ((ok)? stat.PASS : stat.FAIL));
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private boolean runTest(String urlPattern, int statusCode,
            String expectedResponse) throws Exception {

        String url = "http://" + host + ":" + port + contextRoot + urlPattern;
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();
        conn.setRequestProperty("Authorization", "Basic amF2YWVlOmphdmFlZQ==");
        conn.connect();

        int code = conn.getResponseCode();
        boolean testStatus = (code == statusCode);

        if (!testStatus) {
            System.out.println("Unexpected return code: " + code);
        }

        if (!testStatus || expectedResponse == null) {
            return testStatus;
        }

        InputStream is = null;
        BufferedReader input = null;
        String line = null;
        try {
            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            line = input.readLine();
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
        if (!expectedResponse.equals(line)) {
            System.out.println("Wrong response. Expected: " +
                expectedResponse + ", received: " + line);
            testStatus = false;
        }

        return testStatus;
    }
}
