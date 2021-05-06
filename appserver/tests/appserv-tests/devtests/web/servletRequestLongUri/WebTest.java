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
 * Unit test for testing very long request URIs.
 *
 * There was a claim that an URI of length 3427 was correctly mapped to
 * its target servlet, whereas an URI of length 3428 was somehow "swallowed"
 * by the web container and no longer mapped to its target servlet.
 *
 * The purpose of this unit test is to repute this claim.
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-request-long-uri";

    private static final SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for testing very long request URI");
        WebTest webTest = new WebTest(args);
        try {
            // Connect with request URI of length 3427
            webTest.doTest("/entries12345/3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30b", "3427");
            // Connect with request URI of length 3428
            webTest.doTest("/entries12345/3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30e86c-a339-3390-a5c5-2e4074036145,3d30bb", "3428");
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
        stat.printSummary();
    }

    public void doTest(String pathInfo, String expected) throws Exception {
        URL url = new URL("http://" + host  + ":" +
            port + contextRoot + "/TestServlet" + pathInfo);
        System.out.println("Connecting to: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Wrong response code. Expected: 200" +
                ", received: " + responseCode);
        }

        InputStream is = null;
        BufferedReader input = null;
        try {
            is = conn.getInputStream();
            input = new BufferedReader(new InputStreamReader(is));
            String line = input.readLine();
            if (!expected.equals(line)) {
                throw new Exception("Wrong response. Expected: " +
                    expected + ", received: " + line);
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

}
