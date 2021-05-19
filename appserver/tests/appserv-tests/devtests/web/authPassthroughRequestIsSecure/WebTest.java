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
 * Unit test for 6397218 ("Servlet request.isSecure() value is not propagated
 * when authPassthroughEnabled is set to true"):
 *
 * This test sets the HTTP listener's authPassthroughEnabled property to TRUE
 * and includes a 'Proxy-keysize' header in the request. The test therefore
 * expects that ServletRequest.isSecure() return TRUE.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "auth-passthrough-request-is-secure";

    private static final String EXPECTED_RESPONSE = "isSecure=true";

    private String host;
    private String port;
    private String contextRoot;
    private Socket sock = null;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for 6397218");
        WebTest test = new WebTest(args);
        try {
            test.invokeJsp();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception e) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            e.printStackTrace();
        } finally {
            try {
                if (test.sock != null) {
                    test.sock.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        stat.printSummary(TEST_NAME);
    }

    private void invokeJsp() throws Exception {

        sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/jsp/test.jsp" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("Proxy-keysize: 512\n".getBytes());
        os.write("Proxy-ip: 123.456.789\n".getBytes());
        os.write("\n".getBytes());

        InputStream is = null;
        BufferedReader bis = null;
        String line = null;
        String lastLine = null;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                lastLine = line;
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        if (!EXPECTED_RESPONSE.equals(lastLine)) {
            throw new Exception("Unexpected response: Expected: " +
                                EXPECTED_RESPONSE + ", received: " + lastLine);
        }
    }
}
