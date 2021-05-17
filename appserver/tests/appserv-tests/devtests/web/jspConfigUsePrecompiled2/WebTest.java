/*
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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
 * Unit test for 6181923 ("Add support for 'use-precompiled' JspServlet param
 * introduced by WS 6.0"):
 *
 * Ensure that a JSP that was precompiled and whose servlet class file has
 * been bundled in a JAR in WEB-INF/lib, may still be accessed even if the JSP
 * file itself is not present in the webapp, provided that usePrecompiled
 * has been set to TRUE.
 */
public class WebTest {

    private static SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "jsp-precompiled-bundled-in-jar";
    private static final String EXPECTED_RESPONSE = "This is my UPDATED output";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for 6273340");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invokeJsp();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    public void invokeJsp() throws Exception {
        String url = "http://" + host + ":" + port + contextRoot + "/jsp/test.jsp";
        HttpURLConnection conn = (HttpURLConnection) (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            System.err.println("Unexpected return code: " + code);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            try (InputStream is = conn.getInputStream(); BufferedReader input = new BufferedReader(new InputStreamReader(is))) {
                String line = input.readLine();
                if (!EXPECTED_RESPONSE.equals(line)) {
                    System.err.println("Wrong response. " + "Expected: " + EXPECTED_RESPONSE + ", received: " + line);
                    stat.addStatus(TEST_NAME, stat.FAIL);
                } else {
                    stat.addStatus(TEST_NAME, stat.PASS);
                }
            }
        }
    }
}
