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

import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;
import org.glassfish.grizzly.test.http2.*;

/**
 * Unit test for trailer.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "servlet-4.0-trailer";
    private static final String EXPECTED_RESPONSE = "hello124|A|B";

    private String host;
    private int port;
    private String contextRoot;
    private Socket sock = null;

    public WebTest(String[] args) {
        host = args[0];
        port = Integer.parseInt(args[1]);
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for trailer");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        } finally {
            try {
                if (webTest.sock != null) {
                    webTest.sock.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }

        stat.printSummary(TEST_NAME);
    }

    public void doTest() throws Exception {
        try (HttpClient httpClient = HttpClient.builder().
                host(host).port(port).build()) {
            httpClient.request().path(contextRoot + "/TestServlet")
                .method("POST")
                .chunked(true)
                .header("trailer", "foo1, foo2, foo3, foo4")
                .content("hello")
                .trailerField("foo1", "1")
                .trailerField("foo2", "2")
                .trailerField("foo3", "3")
                .trailerField("foo4", "4")
                .build().send();
            HttpResponse httpResponse = httpClient.getHttpResponse();
            int code = httpResponse.getStatus();
            if (code != 200) {
                throw new Exception("Unexpected return code: " + code);
            }
            String contentType = httpResponse.getHeader("Content-Type");
            System.out.println("--> headers = " + httpResponse.getHeaders());

            String result = new StringBuilder(httpResponse.getBody().trim())
                .append('|')
                .append(httpResponse.getTrailerFields().get("bar1")) // trailer field
                .append('|')
                .append(httpResponse.getTrailerFields().get("bar2")) // trailer field
                .toString();

            System.out.println("--> result = " + result);

            if  (!EXPECTED_RESPONSE.equals(result)) {
                throw new Exception("Wrong response. Expected: " +
                    EXPECTED_RESPONSE + ", received: " + result);
            }
        }
    }
}
