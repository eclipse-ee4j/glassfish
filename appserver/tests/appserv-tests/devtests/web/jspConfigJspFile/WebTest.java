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
 * Verifies that <jsp-config> properties are propagated as init parameters
 * to <jsp-file> servlets (as well as to the JspServlet itself), so that
 * sun-web.xml <jsp-config> properties such as reload-interval, httpMethods,
 * etc. can be applied to JSPs configured as <jsp-file>s also.
 */
public class WebTest {

    private static final String TEST_NAME = "jsp-config-jsp-file";
    private static final String EXPECTED_RESPONSE = "myValue";

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
        stat.addDescription("Unit test for init param propagation to "
                            + "<jsp-file> servlets");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
        stat.printSummary();
    }

    public void doTest() throws Exception {

        InputStream is = null;
        BufferedReader input = null;
        try {
            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/TestServlet");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                is = conn.getInputStream();
                input = new BufferedReader(new InputStreamReader(is));
                String line = input.readLine();
                if (!EXPECTED_RESPONSE.equals(line)) {
                    System.err.println("Wrong response. Expected: "
                                       + EXPECTED_RESPONSE
                                       + ", received: " + line);
                    stat.addStatus(TEST_NAME, stat.FAIL);
                } else {
                    stat.addStatus(TEST_NAME, stat.PASS);
                }
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
