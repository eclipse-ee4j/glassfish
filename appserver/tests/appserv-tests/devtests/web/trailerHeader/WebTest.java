/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.*;
import java.util.concurrent.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Port Tomcat unit test
 *     test/org/apache/coyote/http11/filters/TestChunkedInputFilter.java.
 * Unit test for GLASSFISH-17857.
 */
public class WebTest {

    private static final String TEST_NAME =
        "trailer-header";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String CRLF = "\r\n";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for IT GLASSFISH-16768");
        final WebTest webTest = new WebTest(args);

        try {
            String[] req = null;
            // trailing headers
            req = new String[] {
                "POST /" + webTest.contextRoot + " HTTP/1.1" + CRLF +
                "Host: any" + CRLF +
                "Transfer-encoding: chunked" + CRLF +
                "Content-Type: application/x-www-form-urlencoded" + CRLF +
                "Connection: close" + CRLF +
                CRLF +
                "3" + CRLF +
                "a=0" + CRLF +
                "4" + CRLF +
                "&b=1" + CRLF +
                "0" + CRLF +
                "x-trailer: Test",
                "TestTest0123456789abcdefghijABCDEFGHIJopqrstuvwxyz" + CRLF +
                CRLF };
            webTest.doTest(req, "null7TestTestTest0123456789abcdefghijABCDEFGHIJopqrstuvwxyz", true);

            /* enable this when maxTrailerSize is configurable dynamically to 10
            // trailing headers size limit
            // need to set maxTrailerSize = 10
            String dummy = "01234567890";

            req = new String[] {
                "POST /" + webTest.contextRoot + " HTTP/1.1" + CRLF +
                "Host: any" + CRLF +
                "Transfer-encoding: chunked" + CRLF +
                "Content-Type: application/x-www-form-urlencoded" + CRLF +
                "Connection: close" + CRLF +
                CRLF +
                "3" + CRLF +
                "a=0" + CRLF +
                "4" + CRLF +
                "&b=1" + CRLF +
                "0" + CRLF +
                "x-trailer: Test" + dummy + CRLF +
                CRLF };
            webTest.doTest(req, "Http/1.1 500", false);
            */

            // no trailing headers
            req = new String[] {
                "POST /" + webTest.contextRoot + " HTTP/1.1" + CRLF +
                "Host: any" + CRLF +
                "Transfer-encoding: chunked" + CRLF +
                "Content-Type: application/x-www-form-urlencoded" +
                    CRLF +
                "Connection: close" + CRLF +
                CRLF +
                "3" + CRLF +
                "a=0" + CRLF +
                "4" + CRLF +
                "&b=1" + CRLF +
                "0" + CRLF +
                CRLF };
            webTest.doTest(req, "null7null", true);
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void doTest(String[] req, String expectedResult, boolean exact) throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        String cookie = null;

        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            for (String r : req) {
                System.out.print(r);
                os.write(r.getBytes());
            }

            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            boolean found = false;
            // there is no Location header here anymore
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if ((exact && line.equals(expectedResult)) ||
                        ((!exact) && line.startsWith(expectedResult))) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new Exception("Do not find " + expectedResult);
            }
        } finally {
            close(sock);
            close(os);
            close(is);
            close(br);
        }
    }

    private void close(Socket sock) {
        try {
            if (sock != null) {
                sock.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }
}
