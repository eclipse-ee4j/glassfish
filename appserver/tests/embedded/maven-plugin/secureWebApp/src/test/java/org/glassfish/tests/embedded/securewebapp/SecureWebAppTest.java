/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
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

package org.glassfish.tests.embedded.securewebapp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SecureWebAppTest {

    private static int count = 0;
    private static int EXPECTED_COUNT = 3;

    private String contextPath = "test";

    @BeforeAll
    public static void setup() throws IOException {
    }

    @Test
    public void testWeb() throws Exception {
        goGet("localhost", 8080, "Hi from SecureWebAppTestServlet", contextPath);
    }

    private static void goGet(String host, int port,
                              String result, String contextPath) throws Exception {
        try {
            disableCertValidation();
            URL servlet = new URL("https://localhost:8181/test/SecureWebAppTestServlet");
            HttpsURLConnection uc = (HttpsURLConnection) servlet.openConnection();
            System.out.println("URLConnection = " + uc);
            if (uc.getResponseCode() != 200) {
                throw new Exception("Servlet did not return 200 OK response code");
            }
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    uc.getInputStream()));
            String line = null;
            int index;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                index = line.indexOf(result);
                if (index != -1) {
                    index = line.indexOf(":");
                    String status = line.substring(index + 1);

                    if (status.equalsIgnoreCase("PASS")) {
                        count++;
                    } else {
                        return;
                    }
                }
            }
            Assertions.assertTrue(count == 3);

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
