/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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
import org.apache.catalina.startup.SimpleHttpClient;

/*
 * Unit test for https://issues.apache.org/bugzilla/show_bug.cgi?id=49779
 * 501 Method not implemented with successive POST requests
 */
public class WebTest {

    private static final String TEST_NAME =
        "test-form-authenticator";

    private static final SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private int port;
    private String contextRoot;
    private String adminUser;
    private String adminPassword;

    public WebTest(String[] args) {
        host = args[0];
        port = Integer.parseInt(args[1]);
        contextRoot = args[2];
        adminUser = args[3];
        adminPassword = args[4];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for Tomcat bug 49779");
        WebTest webTest = new WebTest(args);

        try {
            webTest.run();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void run() throws Exception {
        testGet();
        testPostNoContinue();
        testPostWithContinue();
        testPostNoContinuePostRedirect();
        testPostWithContinuePostRedirect();
    }

    public void testGet() throws Exception {
        doTest("GET", "GET", false);
    }

    public void testPostNoContinue() throws Exception {
        doTest("POST", "GET", false);
    }

    public void testPostWithContinue() throws Exception {
        doTest("POST", "GET", true);
    }

    // Bugzilla Bug 49779
    public void testPostNoContinuePostRedirect() throws Exception {
        doTest("POST", "POST", false);
    }

    // Bugzilla Bug 49779
    public void testPostWithContinuePostRedirect() throws Exception {
        doTest("POST", "POST", true);
    }

    public void doTest(String resourceMethod, String redirectMethod,
            boolean useContinue) throws Exception {
        FormAuthClient client = new FormAuthClient();

        // First request for authenticated resource
        client.setUseContinue(useContinue);
        client.doResourceRequest(resourceMethod);
        assertTrue(client.isResponse200());
        assertTrue(client.isResponseBodyOK());
        client.reset();

        // Second request for the login page
        client.setUseContinue(useContinue);
        client.doLoginRequest();
        assertTrue(client.isResponse302());
        assertTrue(client.isResponseBodyOK());
        client.reset();

        // Third request - follow the redirect
        client.doResourceRequest(redirectMethod);
        if ("POST".equals(redirectMethod)) {
            client.setUseContinue(useContinue);
        }
        assertTrue(client.isResponse200());
        assertTrue(client.isResponseBodyOK());
        client.reset();

        // Subsequent requests - direct to the resource
        for (int i = 0; i < 5; i++) {
            client.setUseContinue(useContinue);
            client.doResourceRequest(resourceMethod);
            assertTrue(client.isResponse200());
            assertTrue(client.isResponseBodyOK());
            client.reset();
        }


    }

    private static void assertTrue(boolean status) {
        if (!status) {
            throw new RuntimeException();
        }
    }

    private final class FormAuthClient extends SimpleHttpClient {

        private static final String LOGIN_PAGE = "j_security_check";

        private String protectedPage = "index.jsp";
        private String protectedLocation = contextRoot;
        private int requestCount = 0;
        private String sessionId = null;

        private FormAuthClient() {
            setHost(host);
            setPort(port);
        }

        private void doResourceRequest(String method) throws Exception {
            StringBuilder requestHead = new StringBuilder(128);
            String requestTail;
            requestHead.append(method).append(" ").append(protectedLocation)
                    .append("/").append(protectedPage);
            if ("GET".equals(method)) {
                requestHead.append("?role=bar");
            }
            requestHead.append(" HTTP/1.1").append(CRLF);
            requestHead.append("Host: " + host).append(CRLF);
            requestHead.append("Connection: close").append(CRLF);
            if (getUseContinue()) {
                requestHead.append("Expect: 100-continue").append(CRLF);
            }
            if (sessionId != null) {
                requestHead.append("Cookie: JSESSIONID=").append(sessionId)
                        .append(CRLF);
            }
            if ("POST".equals(method)) {
                requestHead.append(
                        "Content-Type: application/x-www-form-urlencoded")
                        .append(CRLF);
                requestHead.append("Content-length: 8").append(CRLF);
                requestHead.append(CRLF);
                requestTail = "role=bar";
            } else {
                requestTail = CRLF;
            }
            String request[] = new String[2];
            request[0] = requestHead.toString();
            request[1] = requestTail;
            doRequest(request);
        }

        private void doLoginRequest() throws Exception {
            StringBuilder requestHead = new StringBuilder(128);
            requestHead.append("POST ").append(protectedLocation)
                    .append("/").append(LOGIN_PAGE).append(" HTTP/1.1").append(CRLF);
            requestHead.append("Host: " + host).append(CRLF);
            requestHead.append("Connection: close").append(CRLF);
            if (getUseContinue()) {
                requestHead.append("Expect: 100-continue").append(CRLF);
            }
            if (sessionId != null) {
                requestHead.append("Cookie: JSESSIONID=").append(sessionId)
                        .append(CRLF);
            }
            requestHead.append(
                    "Content-Type: application/x-www-form-urlencoded").append(
                    CRLF);
            requestHead.append("Content-length: 35").append(CRLF);
            requestHead.append(CRLF);
            String request[] = new String[2];
            request[0] = requestHead.toString();
            request[1] = "j_username=" + adminUser + "&j_password=" + adminPassword;

            doRequest(request);
        }

        private void doRequest(String request[]) throws Exception {
            setRequest(request);

            try {
                connect();
                processRequest();
                String newSessionId = getSessionId();
                if (newSessionId != null) {
                    sessionId = newSessionId;
                }
            } finally {
                disconnect();
            }

            requestCount++;
        }

        @Override
        public boolean isResponseBodyOK() {
            if (requestCount == 1) {
                // First request should result in the login page
                assertContains(getResponseBody(), "A JSP Login Page");
                return true;
            } else if (requestCount == 2) {
                // Second request should result in a redirect
                return true;
            } else {
                // Subsequent requests should result in the protected page
                // The role parameter should have reached the page
                String body = getResponseBody();
                assertContains(body, "Hello javaee, bar");
                /*
                assertContains(body,
                        "<input type=\"text\" name=\"role\" value=\"bar\"");
                        */
                return true;
            }
        }

        private void assertContains(String body, String expected) {
            if (!body.contains(expected)) {
                throw new RuntimeException("Response body check failure.\n"
                        + "Expected to contain substring: [" + expected
                        + "]\nActual: [" + body + "]");
            }
        }
    }
}
