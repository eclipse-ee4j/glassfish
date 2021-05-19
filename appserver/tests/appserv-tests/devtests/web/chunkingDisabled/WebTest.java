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
 * Unit test for 4861933 ("S1AS 8 (PE) removes the Content-Length
 * header (valid HTTP header)").
 *
 * This test ensures that if chunking has been disabled, a response whose
 * length exceeds the response buffer size still contains a Content-Length
 * header.
 *
 * This test is supposed to run on PE only (and will always pass on EE by
 * having the servlet print a response that is guaranteed to fit in the
 * response buffer), because the 'chunkingDisabled' http-listener property is
 * supported on PE only.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "chunked-encoding";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for 4861933");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invokeServlet();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeServlet() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();

        String get = "GET " + contextRoot + "/TestServlet" + " HTTP/1.1\n";
        os.write(get.getBytes());

        String hostHeader = "Host: " + host + ":" + port + "\n";
        os.write(hostHeader.getBytes());

        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        boolean responseOK = false;
        boolean contentLenFound = false;
        String line = null;
        while ((line = bis.readLine()) != null) {
            if (line.indexOf("HTTP/1.1 200 OK") != -1) {
                responseOK = true;
            } else if (line.toLowerCase().indexOf("content-length:") != -1) {
                System.out.println("Response Content-Length: " + line);
                contentLenFound = true;
            }
        }

        if (responseOK && contentLenFound) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else if (!responseOK) {
            System.out.println("Wrong response code, expected 200 OK");
            stat.addStatus(TEST_NAME, stat.FAIL);
        } else {
            System.out.println("Missing Content-Length response header");
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
