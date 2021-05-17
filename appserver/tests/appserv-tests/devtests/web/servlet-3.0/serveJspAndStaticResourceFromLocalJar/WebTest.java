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
        "serve-jsp-and-static-resource-from-local-jar";
    private static final String EXPECTED_RESPONSE = "Hello World!";
    private static final String EXPECTED_RESPONSE_2 = "Hello World folder!";
    private static final String EXPECTED_RESPONSE_3 = "2: /folder/def.txt, /folder/ghi.txt, ";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for serving JSP and static " +
                            "resources from JAR inside WEB-INF/lib");
        new WebTest(args).doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invokeJspServlet();
            invokeDefaultServlet("/abc.txt", 200, EXPECTED_RESPONSE);
            invokeDefaultServlet("/folder", new int[] { 301, 302 }, contextRoot + "/folder/");
            invokeDefaultServlet("/folder/", 404, null);
            invokeDefaultServlet("/folder/def.txt", 200, EXPECTED_RESPONSE_2);
            invokeTestServlet();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJspServlet() throws Exception {
        URL url = new URL("http://" + host  + ":" +
                          port + contextRoot + "/abc.jsp");
        System.out.println("Invoking URL: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
            throw new Exception("Unexpected response code: " + responseCode);
        }
        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String response = input.readLine();
        if (!EXPECTED_RESPONSE.equals(response)) {
            throw new Exception("Wrong response, expected: " +
                EXPECTED_RESPONSE + ", received: " + response);
        }
    }

    private void invokeDefaultServlet(String path, int expectedStatus,
            String expectedResponse) throws Exception {
        invokeDefaultServlet(path, new int[] { expectedStatus }, expectedResponse);
    }

    private void invokeDefaultServlet(String path, int[] expectedStatuses,
            String expectedResponse) throws Exception {
        URL url = new URL("http://" + host  + ":" +
                          port + contextRoot + path);
        System.out.println("Invoking URL: " + url.toString());
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.connect();
        int responseCode = conn.getResponseCode();
        boolean validStatus = false;
        for (int status : expectedStatuses) {
            if (status == responseCode) {
                validStatus = true;
            }
        }
        if (!validStatus) {
            throw new Exception("Unexpected response code: " + responseCode);
        }

        if (responseCode == HttpURLConnection.HTTP_OK && expectedResponse != null) {
            InputStream is = conn.getInputStream();
            BufferedReader input = new BufferedReader(new InputStreamReader(is));
            String response = input.readLine();
            if (!expectedResponse.equals(response)) {
                throw new Exception("Wrong response, expected: " +
                    expectedResponse + ", received: " + response);
            }
        }

        if (responseCode == HttpURLConnection.HTTP_MOVED_TEMP && expectedResponse != null) {
            String location = conn.getHeaderField("Location");
            System.out.println("Location: " + location);
            if (location == null || !location.endsWith(expectedResponse)) {
                throw new Exception("Wrong location: " + location +
                    " does not end with " + expectedResponse);
            }
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
        InputStream is = conn.getInputStream();
        BufferedReader input = new BufferedReader(new InputStreamReader(is));
        String response = input.readLine();
        if (!EXPECTED_RESPONSE_3.equals(response)) {
            throw new Exception("Wrong response, expected: " +
                EXPECTED_RESPONSE_3 + ", received: " + response);
        }
    }
}
