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
 * Unit test for:
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=11417
 *  ("JspC is not able to locate Tag classes in referenced libraries")
 *
 * and
 *
 *  https://glassfish.dev.java.net/issues/show_bug.cgi?id=11419
 *  ("Should tag libraries be searched in referenced jars outside WEB-INF/lib")
 *
 * This unit test uses a slightly modified version of the test case
 * attached to the above issue: Its WEB-INF/lib/foo.jar references
 * bar.jar, which is located in the application's document root, via
 * a Class-Path Manifest attribute with value "../bar.jar".
 */
public class WebTest {

    private static final String TEST_NAME = "class-path-manifest-attribute-precompile";

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

        stat.addDescription("Unit test for GlassFish IT 11417 and IT 11419");
        WebTest webTest = new WebTest(args);

        try {
            webTest.doTest("/foo.jsp");
            webTest.doTest("/bar.jsp");
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void doTest(String jsp) throws Exception {

        URL url = new URL("http://" + host  + ":" + port + contextRoot + jsp);
        System.out.println("Connecting to: " + url.toString());

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new Exception("Unexpected return code: " + responseCode);
        }
    }
}
