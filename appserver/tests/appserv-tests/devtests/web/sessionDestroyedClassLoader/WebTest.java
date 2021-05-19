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
 *
 *
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "session-destroyed-classloader";
    private static final String JSESSIONID = "JSESSIONID";
    private static final String EXPECTED_RESPONSE = "test.MyObject";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for classloader in"
                            + "HttpSessionListener.sessionDestroyed()");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
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
        }
    }

    private void invoke() throws Exception {

        String baseUrl = "http://" + host + ":" + port + contextRoot + "/test";

        HttpURLConnection conn = (HttpURLConnection)(new URL(baseUrl + "?timeout=1")).openConnection();
        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected return code: " + code);
        }
        String sessionId1 = getSessionId(conn);
        System.out.println("sessionId1 =  " + sessionId1);

        Thread.sleep(4 * 1000);

        conn = (HttpURLConnection) (new URL(baseUrl)).openConnection();
        conn.addRequestProperty("Cookie", JSESSIONID + "=" + sessionId1);
        code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected return code: " + code);
        }
        String sessionId2 = getSessionId(conn);
        System.out.println("sessionId2 =  " + sessionId2);

        InputStream is = null;
        BufferedReader input = null;
        try {
            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
            if (!EXPECTED_RESPONSE.equals(line)) {
                throw new Exception("Wrong response. Expected: " +
                    EXPECTED_RESPONSE + ", received: " + line);
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
                if (input != null) {
                    input.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }

    private String getSessionId(HttpURLConnection conn) {
        String sessionId = null;
        String cookieString = conn.getHeaderField("Set-Cookie");
        if (cookieString != null) {
            String[] cookies = cookieString.split(";");
            for (String c: cookies) {
                String[] tokens = c.trim().split("=");
                if (JSESSIONID.equals(tokens[0])) {
                    sessionId = tokens[1];
                    break;
                }
            }
        }

        return sessionId;
    }
}
