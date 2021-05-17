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

import java.lang.*;
import java.io.*;
import java.net.*;

import com.sun.ejte.ccl.reporter.*;

public class WebTest
{

    private static final String TEST_NAME = "form-based";
    private static final String JSESSIONID = "JSESSIONID";
    private static final String FILTER_REQUEST = "FILTER-REQUEST:";
    private static final String FILTER_FORWARD = "FILTER-FORWARD:";
    private static final String FILTER_INCLUDE = "FILTER-INCLUDE:";
    private static final String PASS = "PASS";

    private static SimpleReporterAdapter stat=
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private int port;
    private String contextRoot;
    private String userName;
    private String password;
    private String jsessionId;

    public WebTest(String args[]) {
        host = args[0];
        port = Integer.parseInt(args[1]);
        contextRoot = args[2];
        userName = args[3];
        password = args[4];
    }

    public static void main(String args[]) {

        // The stat reporter writes out the test info and results
        // into the top-level quicklook directory during a run.

        stat.addDescription("Standalone jsr115  war test");
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

        jsessionId = accessServlet();
        String redirect = accessLoginPage();
        followRedirect(new URL(redirect).getPath());
    }

    /*
     * Attempt to access servlet resource protected by FORM based login.
     */
    private String accessServlet() throws Exception {
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        String location = null;
        String cookie = null;

        try {
            sock = new Socket(host, port);
            os = sock.getOutputStream();
            String get = "GET " + contextRoot + "/ServletTest HTTP/1.0\n";
            System.out.print(get);
            os.write(get.getBytes());
            os.write("\r\n".getBytes());

            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("Location:")) {
                    location = line;
                } else if (line.startsWith("Set-Cookie")) {
                    cookie = line;
                }
            }
        } finally {
            close(sock);
            close(os);
            close(is);
            close(br);
        }

        if (cookie == null) {
            throw new Exception("Missing Set-Cookie response header");
        }

        return getSessionIdFromCookie(cookie, JSESSIONID);
    }

    /*
     * Access login.jsp.
     */
    private String accessLoginPage() throws Exception {
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        String location = null;

        try {
            sock = new Socket(host, port);
            os = sock.getOutputStream();
            String get = "GET " + contextRoot
                + "/j_security_check?j_username=" + userName
                + "&j_password=" + password
                + " HTTP/1.0\n";
            System.out.println(get);
            os.write(get.getBytes());
            String cookie = "Cookie: " + jsessionId + "\n";
            os.write(cookie.getBytes());
            os.write("\r\n".getBytes());

            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith("Location:")) {
                    location = line;
                }
            }
        } finally {
            close(sock);
            close(os);
            close(is);
            close(br);
        }

        if (location == null) {
            throw new Exception("Missing Location response header");
        }

        return location.substring("Location:".length()).trim();
    }

    /*
     * Follow redirect to original URL
     */
    private void followRedirect(String path) throws Exception {
        Socket sock = null;
        OutputStream os = null;
        InputStream is = null;
        BufferedReader br = null;
        String response = null;
        String cookie = null;
        int cookieCount = 0;

        try {
            sock = new Socket(host, port);
            os = sock.getOutputStream();
            String get = "GET " + path + " HTTP/1.0\n";
            System.out.print(get);
            os.write(get.getBytes());
            String sendCookie = "Cookie: " + jsessionId + "\n";
            System.out.println(sendCookie);
            os.write(sendCookie.getBytes());
            os.write("\r\n".getBytes());

            is = sock.getInputStream();
            br = new BufferedReader(new InputStreamReader(is));

            String line = null;
            boolean isFilterRequestOK = false;
            boolean isFilterForwardOK = false;
            boolean isFilterIncludeOK = false;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                if (line.startsWith(FILTER_REQUEST)) {
                    isFilterRequestOK = PASS.equals(
                        line.substring(FILTER_REQUEST.length()));
                } else if (line.startsWith(FILTER_FORWARD)) {
                    isFilterForwardOK = PASS.equals(
                        line.substring(FILTER_FORWARD.length()));
                } else if (line.startsWith(FILTER_INCLUDE)) {
                    isFilterIncludeOK = PASS.equals(
                        line.substring(FILTER_INCLUDE.length()));
                }
            }

            if (!(isFilterRequestOK && isFilterForwardOK && isFilterIncludeOK)) {
                throw new Exception("Incorrect response: " + isFilterRequestOK + ","
                    + isFilterForwardOK + ", " + isFilterIncludeOK);
            }
        } finally {
            close(sock);
            close(os);
            close(is);
            close(br);
        }
    }

    private String getSessionIdFromCookie(String cookie, String field) {

        String ret = null;

        int index = cookie.indexOf(field);
        if (index != -1) {
            int endIndex = cookie.indexOf(';', index);
            if (endIndex != -1) {
                ret = cookie.substring(index, endIndex);
            } else {
                ret = cookie.substring(index);
            }
            ret = ret.trim();
        }

        return ret;
    }

    private void close(Socket sock) {
        try {
            if (sock != null) {
                sock.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(InputStream is) {
        try {
            if (is != null) {
                is.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(OutputStream os) {
        try {
            if (os != null) {
                os.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }

    private void close(Reader reader) {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch(IOException ioe) {
            // ignore
        }
    }
}
