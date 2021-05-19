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
 * Unit test for Issus 8585: RequestDispatcher directory traversal vulnerability
 *
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private static final String TEST_NAME
        = "request-dispatcher-directory-traversal";
    private static final String TEST_NAME2
        = "request-dispatcher-directory-traversal-type2";
    private static final String TEST_NAME3
        = "request-dispatcher-directory-traversal-type3";

    private static final String EXPECTED = "This is OK.";

    private String host;
    private String port;
    private String contextRoot;
    private String appserverTestPath;
    private String adminPort;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        appserverTestPath = args[3];
        adminPort = args[4];
    }

    public static void main(String[] args) {
        stat.addDescription("Security Vulnerability test for RequestDispatcher directory traversal");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {
        try {
            invoke();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
        try {
            invokeValidationTestForDoubleDot();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME2, stat.FAIL);
            ex.printStackTrace();
        }
        try {
            invokeValidationTestForColon();
        } catch (Exception ex) {
            stat.addStatus(TEST_NAME3, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private void invoke() throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        try {
            System.out.println("Host=" + host + ", port=" + port);
            sock = new Socket(host, new Integer(port).intValue());
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/page.jsp?blah=/../WEB-INF/web.xml HTTP/1.1\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write("Host: localhost\n".getBytes());
            os.write("\n".getBytes());
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = null;
            boolean isExpected = false;
            while ((line = bis.readLine()) != null) {
                System.out.println(line);
                if (line.equals(EXPECTED)) {
                    isExpected = true;
                    break;
                }
            }
            if (isExpected) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Missing expected response: " + EXPECTED);
            }

        } finally {
            try {
                if (os != null) os.close();
            } catch (IOException ex) {}
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (sock != null) sock.close();
            } catch (IOException ex) {}
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }

    private void invokeValidationTestForDoubleDot() throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        try {
            // Validating the ".." file traversal check
            sock = new Socket(host, Integer.valueOf(adminPort));
            os = sock.getOutputStream();
            String get = "GET " + "/theme/META-INF/%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af"
                    + "%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af"
                    + "%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af"
                    + "%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af"
                    + "%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af%c0%ae%c0%ae%c0%af%c0%ae%c0%ae"
                    + "%c0" + appserverTestPath + "/domains/domain1/config/local-password HTTP/1.1\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write("Host: localhost\n".getBytes());
            os.write("\n".getBytes());
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = bis.readLine();
            if (line != null && line.contains("200")) {
                stat.addStatus(TEST_NAME2, stat.FAIL);
            } else {
                stat.addStatus(TEST_NAME2, stat.PASS);
            }
        }
        finally {
            try {
                if (os != null) os.close();
            } catch (IOException ex) {}
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (sock != null) sock.close();
            } catch (IOException ex) {}
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }

    private void invokeValidationTestForColon() throws Exception {

        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader bis = null;
        try {
            // Validating the ":" file traversal check
            sock = new Socket(host, Integer.valueOf(adminPort));
            os = sock.getOutputStream();
            String get = "GET " + "/resource/file%3a///etc/passwd/ HTTP/1.1\n";
            System.out.println(get);
            os.write(get.getBytes());
            os.write("Host: localhost\n".getBytes());
            os.write("\n".getBytes());
            is = sock.getInputStream();
            bis = new BufferedReader(new InputStreamReader(is));
            String line = bis.readLine();
            if (line != null && line.contains("200")) {
                stat.addStatus(TEST_NAME3, stat.FAIL);
            } else {
                stat.addStatus(TEST_NAME3, stat.PASS);
            }
        }
        finally {
            try {
                if (os != null) os.close();
            } catch (IOException ex) {}
            try {
                if (is != null) is.close();
            } catch (IOException ex) {}
            try {
                if (sock != null) sock.close();
            } catch (IOException ex) {}
            try {
                if (bis != null) bis.close();
            } catch (IOException ex) {}
        }
    }
}
