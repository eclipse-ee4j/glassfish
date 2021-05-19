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
 * Unit test for 6175642 ("Cache-control: Feature regression in 8.1 SE/EE")
 * and 4953220 ("S1AS7 Http Listener needs to be compliant with RFC 2616 and
 * allow multiple cache-control directives").
 *
 * This unit test sets a setCacheControl property with multiple values
 * ("must-revalidate,no-store") on the virtual server with id 'server', stops
 * and restarts the instance, and then verifies that the response corresponding
 * to a request to this unit test's web module (deployed on 'server') contains
 * the expected Cache-Control response headers.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "cache-control-response-headers";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for 6175642");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invokeJsp();
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed.");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invokeJsp() throws Exception {

        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/jsp/test.jsp" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());

        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        boolean found1 = false;
        boolean found2 = false;
        boolean found12 = false;
        String line = null;
        while ((line = bis.readLine()) != null) {
            System.out.println(line);
            // Take into account the different case in the response header
            // name: Cache-Control vs Cache-control, between PE and SE/EE
            if ("Cache-Control: must-revalidate".equals(line)
                    || "Cache-control: must-revalidate".equals(line)) {
                found1 = true;
            } else if ("Cache-Control: no-store".equals(line)
                    || "Cache-control: no-store".equals(line)) {
                found2 = true;
            } else if ("Cache-Control: must-revalidate,no-store".equals(line) ||
                       "Cache-control: must-revalidate,no-store".equals(line)) {
                found12 = true;
            }
            if ((found1 && found2) || found12) {
                break;
            }
        }

        if ((found1 && found2) || found12) {
            stat.addStatus(TEST_NAME, stat.PASS);
        } else {
            stat.addStatus("Missing Cache-Control response header(s)",
                           stat.FAIL);
        }
    }
}
