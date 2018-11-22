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
 * Unit test for making sure that if the "enableCookies" session property in
 * sun-web.xml is set to FALSE, there will be no session cookie present in the
 * response, and the redirect URL will get rewritten with the session id
 * appended to it.
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "session-id-url-rewrite";

    private static final String EXPECTED = "MY_SESSION_ATTRIBUTE";

    private String host;
    private String port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
    }
    
    public static void main(String[] args) {
        stat.addDescription("URL rewriting with session id");
        new WebTest(args).doTest();
    }

    public void doTest() {
     
        try { 
            invokeServlet();
            stat.addStatus(TEST_NAME, stat.PASS);
        } catch (Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary(TEST_NAME);
    }

    private void invokeServlet() throws Exception {
         
        Socket sock = new Socket(host, new Integer(port).intValue());
        OutputStream os = sock.getOutputStream();
        String get = "GET " + contextRoot + "/redirectFrom" + " HTTP/1.0\n";
        System.out.println(get);
        os.write(get.getBytes());
        os.write("\n".getBytes());
        
        InputStream is = sock.getInputStream();
        BufferedReader bis = new BufferedReader(new InputStreamReader(is));

        String line = null;
        String redirectLine = null;
        String cookieLine = null;
        int i=0;
        while ((line = bis.readLine()) != null) {
            System.out.println(i++ + ": " + line);
            if (line.startsWith("Location:")) {
                redirectLine = line;
            } else if (line.startsWith("Set-Cookie:")) {
                cookieLine = line;
            }
        }

        if (cookieLine != null) {
            throw new Exception("Unexpected Set-Cookie response header");
        }
        
        if (redirectLine == null) {
            throw new Exception("Missing Location response header");
        }

        int index = redirectLine.indexOf("http");
        if (index == -1) {
            throw new Exception(
                "Missing http address in Location response header");
        }

        String redirectTo = redirectLine.substring(index);
        System.out.println("Redirect to: " + redirectTo);
        URL url = new URL(redirectTo);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.connect();
        int responseCode = conn.getResponseCode();
        if (responseCode != 200) { 
            throw new Exception("Wrong response code. Expected: 200" +
                                ", received: " + responseCode);
        }

        bis = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        while ((line = bis.readLine()) != null) {
            if (line.equals(EXPECTED)) {
                break;
            }
        }

        if (line == null) {
            throw new Exception("Did not receive expected response data: " +
                                EXPECTED);
        }
    }
}
