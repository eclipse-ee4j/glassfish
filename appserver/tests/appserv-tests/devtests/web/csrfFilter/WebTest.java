/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * Unit test for IT GLASSFISH-16768: CSRF Prevention Filter
 *
 */
public class WebTest {

    private static final String TEST_NAME =
        "csrf-filter";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private String sessionId;
    private String csrfParam = null;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for IT GLASSFISH-16768");
        final WebTest webTest = new WebTest(args);

        try {
            webTest.doTest("/resource.jsp", null, false, 403);
            webTest.doTest("/index.jsp", null, true, 200);
            webTest.doTest("/resource.jsp", webTest.csrfParam, false, 200);
            webTest.doTest("/resource.jsp", webTest.csrfParam + "__XXX", false, 403);
            webTest.doTest("/resource.jsp", null, false, 403);
            webTest.doTest("/resource.jsp", webTest.csrfParam + "__XXX", false, 403);
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public int doTest(String page, String param,
            boolean processSessionCookieHeader,
            int expectedCode) throws Exception {

        StringBuilder sb = new StringBuilder("http://");
        sb.append(host).append(":").append(port).append(contextRoot).append(page);
        if (sessionId != null) {
            sb.append(";jsessionid=").append(sessionId);
        }

        if (param != null) {
            sb.append("?").append(param);
        }
        URL url = new URL(sb.toString());

        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != expectedCode) {
            throw new Exception("Unexpected response code: " + responseCode +
                    ", expected: " + expectedCode);
        }

        if (responseCode == 200) {
            if (processSessionCookieHeader) {
                List<String> tempList = conn.getHeaderFields().get("Set-Cookie");
                if (tempList != null && tempList.size() > 0) {
                    String temp = tempList.get(0).split(";")[0];
                    int ind = temp.indexOf("=");
                    if (ind > 0) {
                        sessionId = temp.substring(ind + 1);
                    }
                }
            }

            InputStream is = null;
            BufferedReader bis = null;
            String line = null;
            String sid = null;

            try {
                is = conn.getInputStream();
                bis = new BufferedReader(new InputStreamReader(is));
                while ((line = bis.readLine()) != null) {
                    System.out.println(line);
                    if (line.startsWith("url=")) {
                        csrfParam = line.substring(6); // url=/?
                    } else if (line.startsWith("sid=")) {
                        sid = line.substring(4);
                        if (!sid.equals(sessionId)) {
                            throw new Exception("Session id mismatch. Got: "
                                    + sid + ". Expected: " + sessionId);
                        }
                    }
                }
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
            }
        }

        return responseCode;
    }
}
