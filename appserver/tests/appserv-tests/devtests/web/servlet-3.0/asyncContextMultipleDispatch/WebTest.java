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
 * Unit test for repeated calls to AsyncContext#dispatch with one
 * intervening ServletRequest#startAsync. This unit test also checks
 * to make sure that the intervening ServletRequest#startAsync notifies
 * the registered AsyncListener at its onStartAsync method, and provides
 * the AsyncListener with an opportunity to register itself again so that
 * it will receive the onComplete event when AsyncContext#complete is
 * called.
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-3.0-async-context-multiple-dispatch";

    private static final String EXPECTED_RESPONSE = "onStartAsync,onStartAsync,onComplete";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) throws Exception {

        stat.addDescription("Unit test for AsyncContext#dispatch");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void doTest() throws Exception {

        HttpURLConnection conn = getHttpURLConnection("/TestServlet");
        conn.disconnect();

        conn = getHttpURLConnection("/TestServlet?result=1");

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

        if (!EXPECTED_RESPONSE.equals(line)) {
            throw new Exception("Unexpected response body, expected: " +
                EXPECTED_RESPONSE + ", received: " + line);
        }
    }

    private HttpURLConnection getHttpURLConnection(String path) throws Exception {
        URL url = new URL("http://" + host  + ":" + port + contextRoot + path);
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.connect();
        if (conn.getResponseCode() != 200) {
            throw new Exception("Unexpected return code: " +
                                conn.getResponseCode());
        }

        return conn;
    }
}
