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
import java.util.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for cookie-based session persistence.
 * See https://glassfish.dev.java.net/issues/show_bug.cgi?id=11648 for
 * details.
 *
 * This test deploys an application which has cookie-based session
 * persistence configured in its sun-web.xml descriptor, with a cookie
 * name equal to ABC.
 *
 * This test accesses a resource that creates a session and checks to make
 * sure that the session is persisted in a response cookie with name ABC.
 * The test then submits this cookie (along with the JSESSIONID cookie
 * that was also received as part of the response) and checks to make sure
 * that the session may be resumed.
 */
public class WebTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

   private static final String TEST_NAME = "session-with-cookie-persistence-type";

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
        stat.addDescription("Unit test for cookie-based session persistence");
        new WebTest(args).doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invoke();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        } finally {
            try {
                if (sock != null) {
                    sock.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }

    private void invoke() throws Exception {

        /*
         * Create session
         */
        String url = "http://" + host + ":" + port + contextRoot + "/CreateSession";
        System.out.println("Connecting to: " + url);
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected return code: " + code);
        }

        Map <String, List<String>> headers = conn.getHeaderFields();
        List<String> cookieHeaders = headers.get("Set-Cookie");
        if (cookieHeaders.size() != 2) {
            throw new Exception("Wrong number of Set-Cookie response " +
                "headers. Expected: 2, received: " + cookieHeaders.size());
        }
        String jsessionIdCookie = null;
        String persistedSessionCookie = null;
        for (String cookieHeader : cookieHeaders) {
            System.out.println("Response cookie: " + cookieHeader);
            if (cookieHeader.indexOf("JSESSIONID=") != -1) {
                jsessionIdCookie = cookieHeader;
            } else if (cookieHeader.indexOf("ABC=") != -1) {
                persistedSessionCookie = cookieHeader;
            }
        }
        if (jsessionIdCookie == null) {
            throw new Exception("Missing JSESSIONID cookie response header");
        }
        if (persistedSessionCookie == null) {
            throw new Exception("Missing persisted session cookie response header");
        }

        /*
         * Resume session
         */
        sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String getRequestLine = "GET " + contextRoot + "/CheckSession" +
            " HTTP/1.0\n";
        System.out.print("\nConnecting to: " + getRequestLine);
        os.write(getRequestLine.getBytes());
        String cookieHeaderLine = "Cookie: " + jsessionIdCookie + "\n";
        System.out.print(cookieHeaderLine);
        os.write(cookieHeaderLine.getBytes());
        cookieHeaderLine = "Cookie: " + persistedSessionCookie + "\n";
        System.out.print(cookieHeaderLine);
        os.write(cookieHeaderLine.getBytes());
        os.write("\n".getBytes());

        InputStream is = null;
        BufferedReader bis = null;
        String line = null;
        boolean okStatus = false;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                if (line.equals("HTTP/1.1 200 OK") || line.equals("HTTP/1.0 200 OK")) {
                    okStatus = true;
                    break;
                }
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

        if (!okStatus) {
            throw new Exception("Unable to resume session");
        }
    }
}
