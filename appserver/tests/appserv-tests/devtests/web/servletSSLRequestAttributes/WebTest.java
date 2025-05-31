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
import java.util.*;
import java.security.*;
import java.net.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test related to
 * https://glassfish.dev.java.net/issues/show_bug.cgi?id=562
 * ("HttpServletRequest does not return any attribute in JAX-WS web service"):
 *
 * Make sure ServletRequest.getAttributeNames() returns all SSL-related
 * request attributes mandated by the Servlet spec when the request is over
 * HTTPS with SSL client auth turned on, namely:
 *
 *   jakarta.servlet.request.cipher_suite
 *   jakarta.servlet.request.key_size
 *   jakarta.servlet.request.X509Certificate
 *
 * even if none of these attributes have been requested explicitly by a call
 * to ServletRequest.getAttribute().
 *
 * (SSL client auth is enforced by virtue of the HTTPS listener
 * having client-auth-enabled set to true.)
 */
public class WebTest {

    private static final String TEST_NAME = "servlet-ssl-request-attributes";

    private static final String SSL_CIPHER_SUITE
        = "jakarta.servlet.request.cipher_suite";
    private static final String SSL_KEY_SIZE
        = "jakarta.servlet.request.key_size";
    private static final String SSL_CERTIFICATE
        = "jakarta.servlet.request.X509Certificate";
    private static final String SSL_SESSION_ID
        = "jakarta.servlet.request.ssl_session_id";

    private static SimpleReporterAdapter stat
        = new SimpleReporterAdapter("appserv-tests");


    public static void main(String args[]) throws Exception{

        String host = args[0];
        String port = args[1];
        String contextRoot = args[2];
        String keyStorePath = args[3];
        String trustStorePath = args[4];

        try {
            SSLSocketFactory ssf = getSSLSocketFactory(keyStorePath,
                                                       trustStorePath);
            HttpsURLConnection connection = connect("https://" + host  + ":"
                                                    + port + contextRoot
                                                    + "/TestServlet",
                                                    ssf);

            parseResponse(connection);

        } catch (Throwable t) {
            stat.addStatus(TEST_NAME, stat.FAIL);
            t.printStackTrace();
        }
        stat.printSummary(TEST_NAME);
    }


    private static void parseResponse(HttpsURLConnection connection)
            throws Exception {

        BufferedReader in = null;

        try {
            in = new BufferedReader(new InputStreamReader(
                            connection.getInputStream()));
            String line = in.readLine();
            System.out.println("Response: " + line);
            if (line != null
                    && (line.indexOf(SSL_CIPHER_SUITE) >= 0)
                    && (line.indexOf(SSL_KEY_SIZE) >= 0)
                    && (line.indexOf(SSL_CERTIFICATE) >= 0)
                    && (line.indexOf(SSL_SESSION_ID) >= 0)) {
                stat.addStatus(TEST_NAME, stat.PASS);
            } else {
                System.err.println("Wrong response");
                stat.addStatus(TEST_NAME, stat.FAIL);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }


    private static SSLSocketFactory getSSLSocketFactory(String keyStorePath,
                                                        String trustStorePath)
            throws Exception {

        SSLContext ctx = SSLContext.getInstance("TLS");

        // Keystore
        char[] passphrase = "changeit".toCharArray();
        KeyStore ks = KeyStore.getInstance(new File(keyStorePath), passphrase);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, passphrase);

        // Truststore
        KeyStore trustStore = KeyStore.getInstance(new File(trustStorePath), passphrase);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);

        ctx.init(kmf.getKeyManagers(),tmf.getTrustManagers(), null);

        return ctx.getSocketFactory();
    }


    private static HttpsURLConnection connect(String urlAddress,
                                              SSLSocketFactory ssf)
            throws Exception {

        URL url = new URL(urlAddress);
        HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
        HttpsURLConnection connection = (HttpsURLConnection)
            url.openConnection();

        connection.setHostnameVerifier(
            new HostnameVerifier() {
                public boolean verify(String rserver, SSLSession sses) {
                    return true;
                }
        });

        connection.setDoOutput(true);

        return connection;
    }
}
