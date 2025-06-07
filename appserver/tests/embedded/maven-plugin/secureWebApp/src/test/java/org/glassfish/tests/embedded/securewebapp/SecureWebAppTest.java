/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation.
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
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.glassfish.main.jdke.security.KeyTool;
import org.glassfish.tests.utils.junit.JUnitSystem;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SecureWebAppTest {

    // FIXME: read certificate from truststore.
    private static final TrustManager[] naiveTrustManager = new TrustManager[]{new X509TrustManager() {
        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            return;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            return;
        }
    }};

    @BeforeAll
    public static void createKeyStore() throws Exception {
        File keystore = JUnitSystem.detectBasedir().resolve(Path.of("target", "keystore.jks")).toFile();
        KeyTool keyTool = new KeyTool(keystore, "changeit".toCharArray());
        keyTool.generateKeyPair("s1as", "CN=localhost", "RSA", 1);
    }

    @Test
    public void http() throws Exception {
        goGet(false, "test", "Hi from SecureWebAppTestServlet");
    }

    @Test
    public void https() throws Exception {
        goGet(true, "test", "Hi from SecureWebAppTestServlet");
    }


    private static void goGet(boolean secured, String contextPath, String expectedBody) throws Exception {
        final String protocol = secured ? "https" : "http";
        final int port = secured ? 8181 : 8080;
        URL servlet = new URI(protocol + "://localhost:" + port + "/test/SecureWebAppTestServlet").toURL();
        HttpURLConnection uc = openConnection(secured, servlet);
        System.out.println("URLConnection = " + uc);
        if (uc.getResponseCode() != 200) {
            throw new Exception("Servlet did not return 200 OK response code");
        }
        int count = 0;
        try (BufferedReader in = new BufferedReader(new InputStreamReader(uc.getInputStream()))) {
            String line;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                int index = line.indexOf(expectedBody);
                if (index == -1) {
                    continue;
                }
                index = line.indexOf(":");
                String status = line.substring(index + 1);
                if (!status.equalsIgnoreCase("PASS")) {
                    return;
                }
                count++;
            }
        }
        assertEquals(3, count, "Expected count of successful tests");
    }

    private static HttpURLConnection openConnection(boolean secured, URL endpoint)
        throws IOException, GeneralSecurityException {
        if (!secured) {
            return (HttpURLConnection) endpoint.openConnection();
        }
        HttpsURLConnection uc = (HttpsURLConnection) endpoint.openConnection();
        uc.setHostnameVerifier((hostname, session) -> true);
        uc.setSSLSocketFactory(createSocketFactory());
        return uc;
    }


    private static SSLSocketFactory createSocketFactory() throws GeneralSecurityException {
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, naiveTrustManager, new SecureRandom());
        return sc.getSocketFactory();
    }
}
