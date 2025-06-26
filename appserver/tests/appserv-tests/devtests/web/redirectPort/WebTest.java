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
import java.security.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Test case for 4946739 ("Tomcat default SSL port used when redirect-port not
 * specified on http-listener").
 */
public class WebTest {

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "redirect-port";

    private String host;
    private String port;
    private String contextRoot;
    private String trustStorePath;

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
        contextRoot = args[2];
        trustStorePath = args[3];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for Bugtraq 4946739");
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary(TEST_NAME);
    }

    public void doTest() {

        URL url = null;
        int responseCode;
        boolean fail = false;

        try {

            url = new URL("http://" + host  + ":" + port + contextRoot
                    + "/jsp/test.jsp");
            System.out.println("Connecting to: " + url.toString());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.connect();
            responseCode = conn.getResponseCode();
            System.out.println("Response code: " + responseCode);
            if (responseCode != HttpURLConnection.HTTP_MOVED_TEMP) {
                fail = true;
            } else {
                url = new URL(conn.getHeaderField("Location"));
                System.out.println("Redirected to: " + url.toString());
                SSLContext ctx = SSLContext.getInstance("TLS");
                ctx.init(null, getTrustManagers(trustStorePath), null);
                HttpsURLConnection httpsConn = (HttpsURLConnection) url.openConnection();
                httpsConn.setSSLSocketFactory(ctx.getSocketFactory());
                httpsConn.setHostnameVerifier(new MyHostnameVerifier());
                httpsConn.connect();
                responseCode = httpsConn.getResponseCode();
                System.out.println("Response code: " + responseCode);
                if (responseCode != HttpURLConnection.HTTP_OK) {
                    fail = true;
                }
            }

            if (fail) {
                stat.addStatus(TEST_NAME, stat.FAIL);
            } else {
                stat.addStatus(TEST_NAME, stat.PASS);
            }

        } catch (Exception ex) {
            System.out.println(TEST_NAME + " test failed");
            stat.addStatus(TEST_NAME, stat.FAIL);
            ex.printStackTrace();
        }
    }

    private TrustManager[] getTrustManagers(String path) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(new File(path), passphrase);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf.getTrustManagers();
    }

    private static class MyHostnameVerifier implements HostnameVerifier {

        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }
}
