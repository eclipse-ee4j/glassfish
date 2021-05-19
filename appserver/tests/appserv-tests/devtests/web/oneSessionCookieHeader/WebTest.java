/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
 * Unit test for https://issues.apache.org/bugzilla/show_bug.cgi?id=49158.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME = "one-session-cookie-header";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for one session cookie header");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            Socket sock = new Socket(host, new Integer(port).intValue());
            OutputStream os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/index.jsp HTTP/1.0\n";
            System.out.print(get);
            os.write(get.getBytes());
            os.write("\n".getBytes());

            InputStream is = sock.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            int cookieCount = 0;
            String cookie = null;
            String value = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("Set-Cookie: ")) {
                    cookieCount += 1;
                    int start = line.indexOf("=");
                    int end = line.indexOf(";");
                    if (end != -1) {
                        cookie = line.substring(start + 1, end);
                    } else {
                        cookie = line.substring(start + 1);
                    }
                } else if (line.startsWith("result=")) {
                    value = line.substring("result=".length());
                }
            }

            boolean status = (cookieCount == 1) && cookie != null && cookie.equals(value);
            stat.addStatus(TEST_NAME, ((status) ? stat.PASS : stat.FAIL));
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }

        return;
    }
}
