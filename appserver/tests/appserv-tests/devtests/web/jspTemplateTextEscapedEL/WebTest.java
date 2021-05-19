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
 * Unit test for escaping EL expressions in template text:
 * In the target JSP page, neither EL expression must be evaluated, because
 * both are escaped. There used to be a problem with the first EL expression
 * in the page being evaluated, even though it was escaped.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "jsp-template-text-escaped-el";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for escaping EL expressions in template text");
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

    public void invokeJsp() throws Exception {

        InputStream is = null;
        BufferedReader input = null;
        try {
            String url = "http://" + host + ":" + port + "/" + contextRoot
                + "/jsp/test.jsp";
            HttpURLConnection conn = (HttpURLConnection)
                (new URL(url)).openConnection();

            int code = conn.getResponseCode();
            if (code != 200) {
                System.err.println("Unexpected return code: " + code);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }

            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            String line = null;
            boolean found1 = false;
            boolean found2 = false;

            while ((line = input.readLine()) != null) {
                if (line.equals("${4+3}")) {
                    found1 = true;
                } else if (line.equals("${2+2}")) {
                    found2 = true;
            }
            }

            if (found1 && found2) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Wrong response body");
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }
    }
}
