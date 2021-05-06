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
 * Unit test for Inheritance @ServletSecurity
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-3.0-auth-annotation-inheritance";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");

    private String host;
    private int port;
    private String contextRoot;

    public WebTest(String[] args) {
        host = args[0];
        port = Integer.parseInt(args[1]);
        contextRoot = args[2];
    }

    public static void main(String[] args) {

        stat.addDescription("Unit test for @ServletSecurity Inheritance");
        WebTest webTest = new WebTest(args);

        try {
            webTest.run();
        } catch( Exception ex) {
            ex.printStackTrace();
            stat.addStatus(TEST_NAME, stat.FAIL);
        }

        stat.printSummary();
    }

    public void run() throws Exception {
        String contextPath = contextRoot + "/myurl";
        boolean ok = doWebMethod("POST", host, port, contextPath, false, 200, "p:Hello, null");
        ok = ok && doWebMethod("OPTIONS", host, port, contextPath, false, 200, "o:Hello");

        contextPath = contextRoot + "/myurl2";
        ok = ok && doWebMethod("PUT", host, port, contextPath, true, 200, "put:Hello, javaee");
        ok = ok && doWebMethod("GET", host, port, contextPath, true, 403, null);
        ok = ok && doWebMethod("POST", host, port, contextPath, true, 200, "p:Hello, javaee");
        ok = ok && doWebMethod("OPTIONS", host, port, contextPath, true, 403, null);

        contextPath = contextRoot + "/myurl2b";
        ok = ok && doWebMethod("GET", host, port, contextPath, true, 403, null);
        ok = ok && doWebMethod("POST", host, port, contextPath, true, 200, "p:Hello, javaee");
        ok = ok && doWebMethod("OPTIONS", host, port, contextPath, true, 403, null);

        stat.addStatus(TEST_NAME, ((ok)? stat.PASS : stat.FAIL));
    }

    private static boolean doWebMethod(String webMethod, String host, int port,
            String contextPath, boolean requireAuthenticate,
            int responseCode, String expected) throws Exception {

        String urlStr = "http://" + host + ":" + port + contextPath;
        System.out.println(webMethod + " " + urlStr);
        URL url = new URL(urlStr);
        HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
        urlConnection.setRequestMethod(webMethod);
        if (requireAuthenticate) {
            urlConnection.setRequestProperty("Authorization", "Basic amF2YWVlOmphdmFlZQ==");
        }
        urlConnection.connect();

        int code = urlConnection.getResponseCode();
        boolean ok = (code == responseCode);
        if (!ok) {
            System.out.println("Get response code: " + code + ", expected " + responseCode);
        }
        if (expected != null) {
            InputStream is = null;
            BufferedReader bis = null;
            String line = null;

            try{
                is = urlConnection.getInputStream();
                bis = new BufferedReader(new InputStreamReader(is));
                int lineNum = 1;
                while ((line = bis.readLine()) != null) {
                    System.out.println(lineNum + ":  " + line);
                    lineNum++;
                    ok = ok && expected.equals(line);
                }
            } catch( Exception ex){
                ex.printStackTrace();
                throw new Exception("Test UNPREDICTED-FAILURE");
            } finally {
                try {
                    if (is != null) {
                        is.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
                try {
                    if (bis != null) {
                        bis.close();
                    }
                } catch(IOException ioe) {
                    // ignore
                }
            }
        }

        return ok;
    }
}
