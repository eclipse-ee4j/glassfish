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
 * Unit test for IT:
 *     13129: Session is null in request with URL containing jsessionid parameter
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "jsessionid-parameter";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for IT 13129");
        WebTest webTest = new WebTest(args);
        try {
            String id = webTest.doTest("/test.jsp?a=1", "1");
            webTest.doTest("/test.jsp;jsessionid="+ id, "1");
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch(Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
        }
        stat.printSummary(TEST_NAME);
    }

    public String doTest(String url, String expected) throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        try {
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + url + " HTTP/1.0\n";
            System.out.print(get);
            os.write(get.getBytes());
            os.write("\n".getBytes());

            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));

            String line = null;
            String id = null;
            String a = null;
            while ((line = bis.readLine()) != null) {
                if (line.startsWith("id=")) {
                    id = line.substring(3);
                } else if (line.startsWith("a=")) {
                    a = line.substring(2);
                }
            }

            if (!expected.equals(a)) {
                throw new Exception("Unexpected result: " + a + ", expected = " + expected);
            }

            System.out.println("session id = " + id);
            return id;
            } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch(IOException ex) {
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch(IOException ex) {
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch(IOException ex) {
                }
            }
            if (sock != null) {
                try {
                    sock.close();
                } catch(IOException ex) {
                }
            }
        }
    }
}
