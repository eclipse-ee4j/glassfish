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
 * Unit test for Bugzilla 28380
 * (http://nagoya.apache.org/bugzilla/show_bug.cgi?id=28380)
 */
public class WebTest {

    private static final String TEST_NAME
        = "jsp-attribute-name-with-colon-or-dot";

    private static final String EXPECTED = "attr.1=value1,attr:2=value2";

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
        stat.addDescription("Unit test for Bugzilla 28380");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary();
    }

    public void doTest() {

        BufferedReader bis = null;
        try {
            URL url = new URL("http://" + host  + ":" + port
                              + contextRoot + "/jsp//test.jsp");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                System.err.println("Wrong response code. Expected: 200"
                                   + ", received: " + responseCode);
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {

                bis = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
                String line = null;
                String lastLine = null;
                while ((line = bis.readLine()) != null) {
                    lastLine = line;
                }

                if (EXPECTED.equals(lastLine)) {
                    stat.addStatus(TEST_NAME, stat.PASS);
                } else {
                    System.err.println("Wrong response: " + lastLine
                                       + ", expected: " + EXPECTED);
                    stat.addStatus(TEST_NAME, stat.FAIL);
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        } finally {
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }

}
