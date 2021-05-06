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
 * Unit test for puggability
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "servlet-3.0-pluggable-sharedlibraries-war";
    private static final String[] EXPECTED_RESPONSE = {"none","CALLED SHAREDLIB-1;CALLED SHAREDLIB-2;CALLED SHAREDLIB-3;CALLED SHAREDLIB-4;CALLED APPLIB-1;null","CALLED SHAREDLIB-1;CALLED SHAREDLIB-2;CALLED SHAREDLIB-3;CALLED SHAREDLIB-4;null;CALLED APPLIB-2"};

    private String host;
    private String port;
    private String testNumber;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        testNumber = args[3];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for pluggable shared libraries in WAR");
        WebTest webTest = new WebTest(args);
        if("1".equals(args[3])) {
            webTest.doTest("/mytest1");
            stat.printSummary(TEST_NAME);
        }
        if("2".equals(args[3])) {
            webTest.doTest("/mytest2");
            stat.printSummary(TEST_NAME);
        }
    }

    public void doTest(String root) {
        try {
            invoke(root);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke(String root) throws Exception {

        String url = "http://" + host + ":" + port + contextRoot
                     + root;
        System.out.println("Invoking " + url);
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            System.out.println("Unexpected return code: " + code);
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
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
            if (EXPECTED_RESPONSE[(new Integer(testNumber)).intValue()].equals(line)) {
                System.out.println("RESPONSE : " + line);
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.out.println("Wrong response. Expected: " +
                        EXPECTED_RESPONSE[(new Integer(testNumber)).intValue()] + ", received: " + line);
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        }
    }
}
