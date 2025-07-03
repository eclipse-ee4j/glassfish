/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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
import java.util.Properties;
import java.net.*;
import java.security.KeyStore;
import javax.net.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

public class WebTest{

    static SimpleReporterAdapter stat=
           new SimpleReporterAdapter("appserv-tests");
    private static final String TEST_NAME = "servlet-3.0-transportProtectedAnnotation";

    public static void main(String args[]) throws Exception{
        String host = args[0];
        String httpPort = args[1];
        String httpsPort = args[2];
        String contextRoot = args[3];
        String trustStorePath = args[4];

        stat.addDescription("Testing @ServletSecurity transportGuarantee");

        try {
            SSLSocketFactory ssf = getSSLSocketFactory(trustStorePath);

            boolean ok = testURL("GET", "https://" + host + ":" + httpsPort + "/" + contextRoot + "/myurl",
                    ssf, false, "c:Hello:true");
            ok = ok && testURL("GET", "https://" + host + ":" + httpsPort + "/" + contextRoot + "/myurl2",
                    ssf, false, "m:Hello:true");
            ok = ok && testURL("TRACE", "http://" + host + ":" + httpPort + "/" + contextRoot + "/myurl2",
                    null, true, "mfr:Hello:javaee:false");
            ok = ok && testURL("GET", "https://" + host + ":" + httpsPort + "/" + contextRoot + "/myurl3",
                    ssf, true, "g:Hello:javaee:true");

            ok = ok && testURL("TRACE", "http://" + host + ":" + httpPort + "/" + contextRoot + "/myurl3",
                    null, true, "t:Hello:javaee:false");
            stat.addStatus(TEST_NAME, ((ok)? stat.PASS : stat.FAIL));
        } catch (Throwable t) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            t.printStackTrace();
        }
        stat.printSummary();
    }

    private static boolean testURL(String httpMethod, String url, SSLSocketFactory ssf,
            boolean needAuthenticate, String expected)
            throws Exception {

        System.out.println("Accessing " + httpMethod + " " + url);
        HttpURLConnection connection = doHandshake(httpMethod, url, ssf, needAuthenticate);
        boolean status = false;
        if (checkStatus(connection)) {
            status = parseResponse(connection, expected);
        }
        return status;
    }

    private static SSLSocketFactory getSSLSocketFactory(String trustStorePath)
                    throws Exception {
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, getTrustManagers(trustStorePath), null);
        return sc.getSocketFactory();
    }

    private static HttpURLConnection doHandshake(String httpMethod,
            String urlAddress, SSLSocketFactory ssf,
            boolean needAuthenticate) throws Exception{

        URL url = new URL(urlAddress);
        HttpURLConnection connection = null;

        if (ssf != null) {
            HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
            HttpsURLConnection conn = (HttpsURLConnection) url.openConnection();

            conn.setHostnameVerifier(
                new HostnameVerifier() {
                    public boolean verify(String rserver, SSLSession sses) {
                        return true;
                    }
            });

            connection = conn;
        } else {
            connection = (HttpURLConnection) url.openConnection();
        }
        connection.setRequestMethod(httpMethod);
        connection.setDoOutput(true);
        if (needAuthenticate) {
            connection.setRequestProperty("Authorization", "Basic amF2YWVlOmphdmFlZQ==");
        }
        return connection;
    }

    private static boolean checkStatus(HttpURLConnection connection)
            throws Exception{

        int responseCode =  connection.getResponseCode();
        System.out.println("Response code: " + responseCode + " Expected code: 200");
        return (connection.getResponseCode() == 200);
    }

    private static boolean parseResponse(HttpURLConnection connection,
            String expected) throws Exception {

        BufferedReader in = null;
        boolean ok = false;
        try {
            in = new BufferedReader(
                new InputStreamReader(connection.getInputStream()));

            String line = "";
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                if (line.equals(expected)) {
                    ok = true;
                    break;
                }
            }
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch(IOException ioe) {
                // ignore
            }
        }

        return ok;
    }

    private TrustManager[] getTrustManagers(String path) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(new File(path), passphrase);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf.getTrustManagers();
    }
}
