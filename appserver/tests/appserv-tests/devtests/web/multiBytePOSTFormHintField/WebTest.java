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

import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

/**
 * Make sure that form-hint-field specified in sun-web.xml is applied
 * to request's form data. This is verified by the fact that the target
 * servlet does not need to call ServletRequest.setCharacterEncoding()
 * before retrieving the request param via ServletRequest.getParameter().
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME =
        "multi-byte-post-form-hint-field";
    private static final String EXPECTED_RESPONSE = "true";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for POST form data with form-hint-field");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
        stat.printSummary(TEST_NAME);
    }

    public void doTest() throws Exception {

        // POST body
        String body = "japaneseName="
            + URLEncoder.encode("\u3068\u4eba\u6587", "Shift_JIS")
            + "&requestCharset=Shift_JIS";

        // Create a socket to the host
        Socket sock = new Socket(host, Integer.parseInt(port));
        OutputStream os = sock.getOutputStream();

        // Send header
        BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(
                                    sock.getOutputStream()));
        wr.write("POST " + contextRoot + "/TestServlet HTTP/1.0\r\n");
        wr.write("Content-Length: " + body.length() + "\r\n");
        wr.write("Content-Type: application/x-www-form-urlencoded\r\n");
        wr.write("\r\n");

        // Send body
        wr.write(body);
        wr.flush();

        // Read response
        InputStream is = sock.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String line = null;
        String lastLine = null;
        while ((line = input.readLine()) != null) {
            lastLine = line;
        }
        if (EXPECTED_RESPONSE.equals(lastLine)) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            System.err.println("Wrong response body. Expected: "
                               + EXPECTED_RESPONSE + ", received: "
                               + lastLine);
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
    }
}
