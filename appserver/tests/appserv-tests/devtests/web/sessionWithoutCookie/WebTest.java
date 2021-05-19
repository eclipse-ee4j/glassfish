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
 * Unit test for IT 13129, session without cookie.
 *
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "session-without-cookie";

    private String host;
    private String port;
    private String contextRoot;

    private String sessionId;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for IT 13129, session without cookie");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            int a = invoke("/index.jsp");
            int b = invoke("/index.jsp;jsessionid=" + sessionId);
            int c = invoke("/;jsessionid=" + sessionId);
            int d = invoke(";jsessionid=" + sessionId);
            boolean status = ((a + 1 == b) && (b + 1 == c) && (c + 1 == d));
            stat.addStatus(TEST_NAME, ((status) ? stat.PASS : stat.FAIL));
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    /*
     * @param uri The URI to connect to
     * @param expected The string that must be present in the returned contents
     * in order for the test to pass
     */
    private int invoke(String uri)
            throws Exception {

        Integer result = null;
        URL url = new URL("http://" + host  + ":" + port + contextRoot + uri);
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Wrong response code. Expected: 200"
                                + ", received: " + responseCode);
        }

        InputStream is = null;
        BufferedReader input = null;
        try {
            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = input.readLine()) != null) {
                //System.out.println(line);
                if (line.startsWith("Id: ")) {
                    String sId = line.substring(4);
                    if (sessionId != null) {
                        if (!sessionId.equals(sId)) {
                            throw new Exception("Session id change");
                        }
                    } else {
                        sessionId = sId;
                    }
                } else if (line.startsWith("A: ")) {
                    result = Integer.valueOf(line.substring(3));

                }
            }

            if (result == null) {
                throw new Exception("Missing count for " + uri);
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

        return result;
    }
}
