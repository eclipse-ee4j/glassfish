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
import java.util.List;
import java.util.Map;
import com.sun.ejte.ccl.reporter.*;
import org.apache.catalina.startup.SimpleHttpClient;

/*
 * Unit test for HttpResponse.sendRedirect for network path reference.
 */
public class WebTest {

    private static String TEST_NAME = "servlet-3.1-network-path-reference";
    private static String LOCATION_PREFIX = "Location:";
    private static String EXPECTED_RESPONSE = "redirect a";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    public static void main(String[] args) {
        String host = args[0];
        int port = Integer.parseInt(args[1]);
        String contextRoot = args[2];
        stat.addDescription("Unit test for HttpResponse.sendRedirect for network path reference.");

        try {
            HttpClient client = new HttpClient(host, port);
            client.get(contextRoot + "/index.jsp");
            String location = client.getLocationHeader();
            if (location == null) {
                throw new Exception();
            }


            int ds = location.indexOf("//");
            int c = location.indexOf(":", ds);
            int ss = location.indexOf("/", c);
            host = location.substring(ds + 2, c);
            port = Integer.parseInt(location.substring(c + 1, ss));
            String path = location.substring(ss);

            HttpClient client2 = new HttpClient(host, port);
            client2.get(path);
            client2.setExpectedResponse(EXPECTED_RESPONSE);
            stat.addStatus(TEST_NAME, ((client2.isResponseBodyOK())? stat.PASS : stat.FAIL));
        } catch(Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }


    private static final class HttpClient extends SimpleHttpClient {
        private String expectedResponse;

        private HttpClient(String host, int port) {
            setHost(host);
            setPort(port);
        }

        private void setExpectedResponse(String expRes) {
            expectedResponse = expRes;
        }

        private void get(String path) throws Exception {
            String[] req = { "GET " + path + " HTTP/1.0" + CRLF + CRLF };
            setRequest(req);
            try {
                connect();
                processRequest();
            } finally {
                disconnect();
            }
        }

        private String getLocationHeader() {
            List<String> responseHeaders = getResponseHeaders();
            for (String header : responseHeaders) {
                if (header.startsWith(LOCATION_PREFIX)) {
                    header = header.substring(LOCATION_PREFIX.length());
                    header = header.trim();
                    return header;
                }
            }
            return null;
        }

        @Override
        public boolean isResponseBodyOK() {
            if (expectedResponse == null) {
                return true;
            }

            String body = getResponseBody();
            boolean valid = body.contains(expectedResponse);
            if (!valid) {
                System.out.println("Expected to contain: " + expectedResponse
                        + "\nActual: " + body);
            }
            return valid;
        }
    }
}
