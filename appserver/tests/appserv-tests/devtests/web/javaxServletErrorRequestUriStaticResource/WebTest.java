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
 * Unit test for CR 4882996
 * (request.getAttribute("javax.servlet.error.request_uri") is not working).
 *
 * The following response body lines must be returned in order for this unit
 * test to succeed:
 *
 *  /web-javax-servlet-error-request-uri-static-resource/junk
 *  404
 *  /web-javax-servlet-error-request-uri-static-resource/404handler.jsp
 *  http://<host>:<port>/web-javax-servlet-error-request-uri-static-resource/404handler.jsp
 */
public class WebTest {

    private static final String TEST_NAME = "javax-servlet-error-request-uri-static-resource";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String port;
    private String contextRoot;
    private Socket sock = null;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("Unit test for 4882996");
        WebTest webTest = new WebTest(args);
        try {
            webTest.doTest();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
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

	stat.printSummary();
    }

    public void doTest() throws Exception {

        sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/junk HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());

        InputStream is = null;
        BufferedReader bis = null;
        String line = null;
        try {
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                if (line.equals(contextRoot + "/junk")) {
                    break;
                }
            }
            if (line == null) {
                throw new Exception("Unexpected response");
            }
            line = bis.readLine();
            System.out.println(line);
            if (line == null || !line.equals("404")) {
                throw new Exception("Unexpected response");
            }
            line = bis.readLine();
            System.out.println(line);
            if (line == null || !line.equals(
                    contextRoot + "/404handler.jsp")) {
                throw new Exception("Unexpected response");
            }
            line = bis.readLine();
            System.out.println(line);
            if (line == null ||
                    (!line.equals("http://" + host + ":" + port +
                            contextRoot + "/404handler.jsp") &&
                    !line.equals("http://" + 
                            InetAddress.getLocalHost().getHostName() + 
                            ":" + port + contextRoot +
                            "/404handler.jsp"))) {
                throw new Exception("Unexpected response");
            }
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
            try {
                if (bis != null) {
                    bis.close();
                }
            } catch (IOException ioe) {
                // ignore
            }
        }
    }
}
