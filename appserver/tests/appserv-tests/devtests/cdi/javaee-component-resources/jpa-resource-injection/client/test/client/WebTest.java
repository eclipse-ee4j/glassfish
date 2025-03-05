/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.sun.ejte.ccl.reporter.SimpleReporterAdapter;

/**
 * Unit test for @WebServlet
 */
public class WebTest {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "jpa-resource-injection";

    private static final String EXPECTED_RESPONSE = "Hello from Servlet 3.0.";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for Jakarta EE resource injection");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invoke("llinit");
            invoke("llquery");
            invoke("llfind");
            invoke("llinj");
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke(String testCase) throws Exception {

        String url = "http://" + host + ":" + port + contextRoot + "/myurl" + "?testcase=" + testCase;
        System.out.println("opening connection to " + url);
        HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            System.out.println("Unexpected return code: " + code);
            stat.addStatus(TEST_NAME + testCase, stat.FAIL);
        } else {
            InputStream is = null;
            BufferedReader input = null;
            String line = null;
            try {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                line = input.readLine();
                System.out.println("line = " + line);
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch (IOException ioe) {
                    // ignore
                }
                try {
                    if (input != null) {
                        input.close();
                    }
                } catch (IOException ioe) {
                    // ignore
                }
            }
            
            if (EXPECTED_RESPONSE.equals(line)) {
                stat.addStatus(TEST_NAME + testCase, stat.PASS);
            } else {
                System.out.println("Wrong response. Expected: " + EXPECTED_RESPONSE + ", received: " + line);
                stat.addStatus(TEST_NAME + testCase, stat.FAIL);
            }
        }
    }
}
