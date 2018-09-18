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

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

/*
 * Unit test for 6346226 ("SessionLockingStandardPipeline.hasFailoverOccurred
 * only supports jroute-id from cookie, not URL").
 *
 * This test requires that security manager be disabled (see build.xml),
 * because the target servlet performs a security-checked operation.
 */
public class WebTest {
    private static final String TEST_NAME = "jroute-id-in-cookie-or-url";
    private static final String EXPECTED_RESPONSE = "jrouteId=1234";
    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", TEST_NAME);
    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for 6346226");
        WebTest webTest = new WebTest(args);
        try {
            boolean success = webTest.doTestURL();
            if (success) {
                webTest.doTestCookie();
            }
        } catch (Exception ex) {
            stat.addStatus("exception found", SimpleReporterAdapter.FAIL);
            ex.printStackTrace();
        }
        stat.printSummary();
    }

    /*
     * @return true on success, false on failure
     */
    public boolean doTestURL() throws Exception {
        URL url = new URL("http://" + host + ":" + port
            + contextRoot + "/TestServlet"
            + ";jsessionid=CFE28BD89B33B59CD7249ACBDA5B479D"
            + ":1234");
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        final String testName = "test url";
        if (responseCode != 200) {
            System.err.println("Wrong response code. Expected: 200"
                + ", received: " + responseCode);
            stat.addStatus(testName, SimpleReporterAdapter.FAIL);
            return false;
        } else {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
            if (EXPECTED_RESPONSE.equals(line)) {
                stat.addStatus(testName, SimpleReporterAdapter.PASS);
                return true;
            } else {
                System.err.println("Wrong response. Expected: "
                    + EXPECTED_RESPONSE
                    + ", received: " + line);
                stat.addStatus(testName, SimpleReporterAdapter.FAIL);
                return false;
            }
        }
    }

    public void doTestCookie() throws Exception {
        Socket socket = new Socket(host, Integer.parseInt(port));
        OutputStream os = socket.getOutputStream();
        os.write(("GET " + contextRoot + "/TestServlet HTTP/1.0\n").getBytes());
        os.write("Cookie: JROUTE=1234\n".getBytes());
        os.write("\n".getBytes());
        InputStream is = socket.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));
        String line = null;
        String lastLine = null;
        while ((line = bis.readLine()) != null) {
            lastLine = line;
        }
        final String testName = "test cookie";
        if (EXPECTED_RESPONSE.equals(lastLine)) {
            stat.addStatus(testName, SimpleReporterAdapter.PASS);
        } else {
            System.err.printf("Wrong response. Expected: %s, received: %s\n", EXPECTED_RESPONSE, line);
            stat.addStatus(testName, SimpleReporterAdapter.FAIL);
        }
    }
}
