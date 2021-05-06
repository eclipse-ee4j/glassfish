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
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=871
 * ("[JSTL] Remove dependency of jakarta.servlet.jsp.jstl.fmt.LocaleSupport on
 * RI classes"):
 *
 * Make sure this API still functions after removing its dependencies on the
 * RI.
 */
public class WebTest {

    private static final String TEST_NAME = "jstl-locale-support-api";

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

        stat.addDescription("Unit test for GlassFish 871");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
        stat.printSummary();
    }

    public void doTest() throws Exception {

        // Access test1.jsp
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/test1.jsp" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line = null;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if ("Guten Morgen".equals(line)) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing Guten Morgen in response");
        }

        // Access test2.jsp
        sock = new Socket(host, new Integer(port).intValue());
        os = sock.getOutputStream();
        get = "GET " + contextRoot + "/test2.jsp" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        String acceptLanguage = "Accept-Language: de\n";
        os.write(acceptLanguage.getBytes());
        os.write("\n".getBytes());

        is = sock.getInputStream();
        br = new BufferedReader(new InputStreamReader(is));

        while ((line = br.readLine()) != null) {
            System.out.println(line);
            if ("Guten Abend".equals(line)) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Missing Guten Abend in response");
        }

        stat.addStatus(TEST_NAME, stat.PASS);
    }

}
