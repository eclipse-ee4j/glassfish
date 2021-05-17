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
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=3374
 * (FORM authenticator should issue a redirect (instead of a request
 * dispatch "forward") to the login page).
 *
 * This unit test has been reworked in light of the fix for CR 6633257:
 * Rather than issuing a redirect over https to the login.jsp login page
 * (which is protected by a transport-guarantee of CONFIDENTIAL), the
 * redirect over https will be applied to the original request (that is,
 * to protected.jsp), followed by a FORWARD dispatch to login.jsp.
 *
 * This unit test verifies only that the target of the https redirect is as
 * expected, and does not perform the actual FORM-based login.
 */
public class WebTest {

    private static final String TEST_NAME =
        "form-login-transport-guarantee-confidential";

    private static final SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String httpPort;
    private String httpsPort;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        httpPort = args[1];
        httpsPort = args[2];
        contextRoot = args[3];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for GlassFish Issue 3374");
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

        URL url = new URL("http://" + host  + ":" + httpPort + contextRoot
                          + "/protected.jsp");
        System.out.println(url.toString());
        URLConnection conn = url.openConnection();
        java.util.Map fields = conn.getHeaderFields();
        for (Object header: fields.keySet().toArray()) {
            System.out.println("Header: "+header+" : "+conn.getHeaderField((String)header));
        }
        String redirectLocation = conn.getHeaderField("Location");
        System.out.println("Location: " + redirectLocation);

        String expectedRedirectLocation = "https://" + host + ":" + httpsPort
            + contextRoot + "/protected.jsp";
        if (!expectedRedirectLocation.equals(redirectLocation)) {
            throw new Exception("Unexpected redirect location");
        }
    }
}
