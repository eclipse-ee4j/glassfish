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
 * Unit test for 6861682 ("GLASSFISH/redirect_n function is making
 * unwanted matches"):
 *
 * Virtual server "server" is configured with this property (all in a single
 * line):
 *
 *  <property name="redirect_1"
 *            value="from=/portal/directory
              url-prefix=http://tmpserver:1234/myportal/mydirectory"/>
 *
 * Make sure that a request whose URI is equal to "/portal/directory" will be
 * redirected to: http://tmpserver:1234/myportal/mydirectory,
 * whereas a request with an URI equal to "/portal/directorySearchPortlet"
 * will not be redirected at all (resulting in a 404), instead of being
 * redirected to http://tmpserver:1234/myportal/mydirectorySearchPortlet
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME =
        "virtual-server-redirect-property-no-substring-match";

    private static final String EXPECTED_LOCATION_RESPONSE_HEADER =
        "http://tmpserver:1234/myportal/mydirectory";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for 6861682");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invokeServlet("/portal/directory",
                EXPECTED_LOCATION_RESPONSE_HEADER);
            stat.addStatus(TEST_NAME, stat.PASS);
            invokeServlet("/portal/directorySearchPortlet", null);
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeServlet(String uri, String expectedRedirect)
            throws Exception {

        URL url = new URL("http://" + host  + ":" + port + uri);
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(false);
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (expectedRedirect != null) {
            if (responseCode != 302) {
                throw new Exception("Unexpected return code. " +
                    "Expected: 302, received: " + responseCode);
            }
            String actualRedirect = conn.getHeaderField("Location");
            System.out.println("Response Location header: " + actualRedirect);
            if (!expectedRedirect.equals(actualRedirect)) {
                throw new Exception("Wrong response Location header. " +
                    "Expected: " + expectedRedirect + ", received: " +
                    actualRedirect);
            }
        } else {
            if (responseCode != 404) {
                throw new Exception("Unexpected return code. " +
                    "Expected: 404, received: " + responseCode);
            }
        }
    }
}
