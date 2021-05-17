/*
 * Copyright (c) 2020 Fujitsu Limited and/or its affiliates. All rights
 * reserved.
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
 * Unit test for request-character-encoding, response-character-encoding in web.xml and parameter-encoding in glassfish-web.xml
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "servlet-4.0-req-res-encoding-gfwebxml";
    private static final String JCHARSET_REQUEST = "Shift_JIS";
    private static final String JCHARSET_RESPONSE = "Shift_JIS";
    private static final String JSTR = "\u3053\u3093\u306b\u3061\u306f";
    private static final String EXPECTED_RESPONSE = "true:" + JSTR;

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
        stat.addDescription("Unit test for request-character-encoding, response-character-encoding in web.xml and parameter-encoding in glassfish-web.xml");
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
        // Static Content
        try (HttpClient httpClient = HttpClient.builder().
                host(host).port(port).build()) {
            httpClient.request().path(contextRoot + "/index.html")
                .method("GET")
                .build().send();
            HttpResponse httpResponse = httpClient.getHttpResponse();
            int code = httpResponse.getStatus();
            if (code != 200) {
                throw new Exception("Unexpected return code: " + code);
            }
            String contentType = httpResponse.getHeader("Content-Type");
            int ind = contentType.indexOf(";charset=");
            String charset = null;
            if (ind > 0) {
                charset = contentType.substring(ind + 9).trim();
            }
            System.out.println("--> headers = " + httpResponse.getHeaders());
            String line = httpResponse.getBody(charset).trim();
            System.out.println("--> line = " + line);
            if (!JCHARSET_RESPONSE.equals(charset)) {
                throw new Exception("Wrong Content-Type charset. Expected: " +
                    JCHARSET_RESPONSE + ", received: " + charset);
            }

            if  (!EXPECTED_RESPONSE.equals(line)) {
                throw new Exception("Wrong response. Expected: " +
                    EXPECTED_RESPONSE + ", received: " + line);
            }
        }

        // Servlet
        try (HttpClient httpClient = HttpClient.builder().
                host(host).port(port).build()) {
            httpClient.request().path(contextRoot + "/TestServlet")
                .method("POST").contentType("application/x-www-form-urlencoded")
                .characterEncoding(JCHARSET_REQUEST)
                .content("japaneseName=" + JSTR)
                .build().send();
            HttpResponse httpResponse = httpClient.getHttpResponse();
            int code = httpResponse.getStatus();
            if (code != 200) {
                throw new Exception("Unexpected return code: " + code);
            }
            String contentType = httpResponse.getHeader("Content-Type");
            int ind = contentType.indexOf(";charset=");
            String charset = null;
            if (ind > 0) {
                charset = contentType.substring(ind + 9).trim();
            }
            System.out.println("--> headers = " + httpResponse.getHeaders());
            String line = httpResponse.getBody(charset).trim();
            System.out.println("--> line = " + line);
            if (!JCHARSET_RESPONSE.equals(charset)) {
                throw new Exception("Wrong Content-Type charset. Expected: " +
                    JCHARSET_RESPONSE + ", received: " + charset);
            }

            if  (!EXPECTED_RESPONSE.equals(line)) {
                throw new Exception("Wrong response. Expected: " +
                    EXPECTED_RESPONSE + ", received: " + line);
            }
        }
    }
}
