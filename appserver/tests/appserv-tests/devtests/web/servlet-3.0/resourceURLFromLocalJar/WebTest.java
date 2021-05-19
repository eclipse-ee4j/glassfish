/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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
import java.util.*;
import java.net.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for serving JSP and static resources from
 * WEB-INF/lib/[*.jar]/META-INF/resources
 *
 * In this unit test, the client makes a request for
 *   http://localhost:8080/abc.jsp
 * and
 *   http://localhost:8080/abc.txt
 * and the requested resource is supposed to be served from
 *   WEB-INF/lib/nested.jar!META-INF/resources/abc.jsp
 * (by the JspServlet) and
 *   WEB-INF/lib/nested.jar!META-INF/resources/abc.txt
 * (by the DefaultServlet), respectively.
 *
 * Add unit test for IT 11835.
 */
public class WebTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME =
        "resource-url-from-local-jar";
    private static final List<String> EXPECTED_RESPONSE = Arrays.asList("Hello", "World");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for resource URL from JAR inside WEB-INF/lib");
        new WebTest(args).doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invokeTestServlet();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeTestServlet() throws Exception {
        URL url = new URL("http://" + host  + ":" +
                          port + contextRoot + "/test");
        System.out.println("Invoking URL: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Unexpected response code: " + responseCode);
        }

        List<String> responses = new ArrayList<String>();
        try (BufferedReader input = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line = null;
            while ((line = input.readLine()) != null) {
                responses.add(line);
            }
        }

        if (!EXPECTED_RESPONSE.equals(responses)) {
            throw new Exception("Wrong response, expected: " +
                EXPECTED_RESPONSE + ", received: " + responses);
        }
    }
}
