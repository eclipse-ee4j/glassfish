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
 * Unit test for 6324911 ("can not migrate all virtual server functions
 * from 7.1 to 8.1. (eg: custom error page)").
 *
 * The supporting build.xml assigns the following property to the virtual
 * server named "server":
 *
 *   send-error="path=default-web.xml reason=MY404 code=404"
 *
 * As a result of this setting, any 404 response must have a reason string of
 * MY404, and must provide the contents of the
 * domains/domain1/config/default-web.xml file in its body.
 *
 * The code below does not check the entire response body. Instead, it only
 * checks for the presence of a line that starts with "<web-app xmlns=",
 * which is contained in default-web.xml, and uses its presence as an
 * indication that the response contains the expected body.
 */
public class WebTest {

    private static SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME =
        "virtual-server-send-error-property";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for 6324911");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest("/nonexistent", "HTTP/1.1 404 MY404", "MY404");
            webTest.doTest(webTest.contextRoot + "/test500?sendError=true",
                "HTTP/1.1 500 MY500", "<web-app xmlns=");
            webTest.doTest(webTest.contextRoot + "/test500?sendError=false",
                "HTTP/1.1 500 MY500", "<web-app xmlns=");
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        stat.printSummary(TEST_NAME);
    }

    private void doTest(String target, String status, String body)
            throws Exception {
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + target + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());

        boolean statusHeaderFound = false;
        boolean bodyLineFound = false;
        BufferedReader br = null;
        try {
            InputStream is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));
            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith(status)) {
                    statusHeaderFound = true;
                }
                if (line.contains(body)) {
                    bodyLineFound = true;
                }
                if (statusHeaderFound && bodyLineFound) {
                    break;
                }
            }
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ioe) {
                    // Ignore
                }
            }
        }

        if (!statusHeaderFound || !bodyLineFound) {
            throw new Exception("Missing response status or body line");
        }
    }
}
