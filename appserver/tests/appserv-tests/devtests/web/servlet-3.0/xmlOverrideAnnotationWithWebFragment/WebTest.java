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
 * Unit test for Issue 9816
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "servlet-3.0-xml-override-annotation-with-web-fragment";
    private static final String EXPECTED_RESPONSE = "filterMessage=hello, initParams: n1=v2, ";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for Issue 9816");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            boolean ok = invoke("/mytest2", 200, EXPECTED_RESPONSE);
            stat.addStatus(TEST_NAME, ((ok)? stat.PASS : stat.FAIL));
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private boolean invoke(String urlPattern,
            int expectedCode, String expectedResponse) throws Exception {

        String url = "http://" + host + ":" + port + contextRoot
                     + urlPattern;
        System.out.println("Accessing " + url);
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != expectedCode) {
            System.out.println("Unexpected return code: " + code);
            return false;
        } else {
            if (expectedResponse == null) { // don't check body
                return true;
            }

            InputStream is = null;
            BufferedReader input = null;
            String line = null;
            try {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                line = input.readLine();
                //System.out.println(line);
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
            if (expectedResponse.equals(line)) {
                return true;
            } else {
                System.out.println("Wrong response. Expected: " +
                        expectedResponse + ", received: " + line);
                return false;
            }
        }
    }
}
