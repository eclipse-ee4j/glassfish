/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * Unit test for customizing complete list of session tracking cookie
 * properties via web.xml
 */
public class WebTest {

    private static String TEST_NAME = "servlet-3.0-session-cookie-config-declarative";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for customizing complete list of " +
                            "session tracking cookie properties via " +
                            "web.xml");
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

        String url = "http://" + host + ":" + port + contextRoot
                     + "/CreateSession";
        HttpURLConnection conn = (HttpURLConnection)
            (new URL(url)).openConnection();
        int code = conn.getResponseCode();
        if (code != 200) {
            throw new Exception("Unexpected return code: " + code);
        }
        String cookie = conn.getHeaderField("Set-Cookie");
        System.out.println("Response Set-Cookie: " + cookie);
        if (cookie.contains("MYJSESSIONID")) {
            if (cookie.contains("Comment=myComment") &&
                    cookie.contains("Domain=mydomain") &&
                    cookie.contains("Path=/myPath") &&
                    cookie.contains("Secure") &&
                    cookie.contains("HttpOnly") &&
                    cookie.contains("Max-Age=123")
                    == false) {
                throw new Exception("Response cookie not HttpOnly");
            }
        }
    }
}
