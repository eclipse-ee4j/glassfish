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
 * Unit test for "empty string" URL pattern (Servlet 3.0 feature).
 *
 * See also https://glassfish.dev.java.net/issues/show_bug.cgi?id=10601
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-3.0-servlet-mapping-empty-string";

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String EXPECTED_RESPONSE_1 =
        "Hello World from empty string URL pattern";

    private static final String EXPECTED_RESPONSE_2 =
        "Hello World from default URL pattern";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for empty string URL pattern");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest("/", EXPECTED_RESPONSE_1);
            webTest.doTest("/dispatch", EXPECTED_RESPONSE_1);
            webTest.doTest("/helloWorld", EXPECTED_RESPONSE_2);
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void doTest(String path, String expectedResponse) throws Exception {

        InputStream is = null;
        BufferedReader input = null;

        try {
            URL url = new URL("http://" + host  + ":" + port + contextRoot +
                path);
            System.out.println("Connecting to: " + url.toString());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();

            int responseCode = conn.getResponseCode();
            if (responseCode != 200) {
                throw new Exception("Unexpected response code: " +
                    responseCode);
            }

            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            String response = input.readLine();
            if (!expectedResponse.equals(response)) {
                throw new Exception("Missing or wrong response. Expected: " +
                    expectedResponse + ", received: " + response);
            }
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (input != null) input.close();
            } catch (IOException ex) {}
        }

    }
}
