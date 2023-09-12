/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.tests.embedded.jsftest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class JSFTest {

    @Test
    public void testWeb() throws Exception {

        disableCertValidation();

        goGet("http://localhost:8080/test/JSFTestServlet", "Created viewRoot");

        // test non secure access.
        goGet("http://localhost:8080/test", "BHAVANI", "SHANKAR", "Mr. X");

        // test secure access.
        goGet("https://localhost:8181/test", "BHAVANI", "SHANKAR", "Mr. X");
    }

    private static void goGet(String url, String... match) throws Exception {
        try {

            URL servlet = new URL(url);
            HttpURLConnection uc = (HttpURLConnection) servlet.openConnection();
            System.out.println("\nURLConnection = " + uc + " : ");
            if (uc.getResponseCode() != 200) {
                throw new Exception("Servlet did not return 200 OK response code");
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    uc.getInputStream()));
            String line = null;
            boolean[] found = new boolean[match.length];

            int count = 0;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                for (String m : match) {
                    int index = line.indexOf(m);
                    if (index != -1 && count < match.length) {
                        found[count++] = true;
                        System.out.println("Found [" + m + "] in the response, index = " + count);
                        break;
                    }
                }
            }

            for (boolean f : found) {
                Assertions.assertTrue(f);
            }
            System.out.println("\n***** SUCCESS **** Found all matches in the response.*****\n");
            in.close();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public static void disableCertValidation() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
                return;
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
                return;
            }
        }};

        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            return;
        }
    }


}
