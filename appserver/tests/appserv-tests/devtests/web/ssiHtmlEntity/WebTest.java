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

/**
 * Unit test for:
 *
 *  https://issues.apache.org/bugzilla/show_bug.cgi?id=44392
 *  ("HTML entities not resolved in SSI filter")
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "ssi-html-entity";

    private static final String EXPECTED_CONTENT_TYPE = "content-type: text/html";
    private static final String EXPECTED1 = "<!-- testvalue \"x\" -->";
    private static final String EXPECTED2 = "&lt;!-- testvalue &quot;x&quot; --&gt;";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for SSI Html Entity");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invoke();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {

        System.out.println("Host=" + host + ", port=" + port);
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/index.shtml HTTP/1.1\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Host: localhost\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        boolean hasExpectedContentType = false;
        boolean hasExpectedLine1 = false;
        boolean hasExpectedLine2 = false;
        String expected = EXPECTED1;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            if (!hasExpectedContentType) {
                if (line.toLowerCase().startsWith(EXPECTED_CONTENT_TYPE)) {
                    hasExpectedContentType = true;
                }
            } else if (line.equals(expected)) {
                if (expected.equals(EXPECTED1)) {
                    hasExpectedLine1 = true;
                    expected = EXPECTED2;
                } else {
                    hasExpectedLine2 = true;
                    break;
                }
            }
        }

        if (hasExpectedContentType && hasExpectedLine1 && hasExpectedLine2) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            if (!hasExpectedContentType) {
                System.err.println("Missing expected content type: " + EXPECTED_CONTENT_TYPE);
            }
            if (!hasExpectedLine1) {
                System.err.println("Missing expected response: " + EXPECTED1);
            }
            if (!hasExpectedLine2) {
                System.err.println("Missing expected response: " + EXPECTED2);
            }
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
