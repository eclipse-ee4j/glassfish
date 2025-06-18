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
import java.net.*;
import java.security.*;
import javax.net.ssl.*;
import com.sun.ejte.ccl.reporter.*;

/*
 * Unit test for https://glassfish.dev.java.net/issues/show_bug.cgi?id=11504
 * ("Glassfishv3 j_security_check causes No active contexts errors")
 */
public class WebTest {

    private static final String TEST_NAME = "weld-jsf-form-login-page";
    private static final SimpleReporterAdapter stat =
        new SimpleReporterAdapter("appserv-tests");

    private String host;
    private String httpPort;
    private String httpsPort;
    private String contextRoot;
    private String keyStorePath;
    private String trustStorePath;

    public WebTest(String[] args) {
        host = args[0];
        httpPort = args[1];
        httpsPort = args[2];
        contextRoot = args[3];
        keyStorePath = args[4];
        trustStorePath = args[5];
    }

    public static void main(String[] args) {
        stat.addDescription("Unit test for IT 11504");
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

    /*
     * Attempts to access resource protected by FORM based login.
     */
    private void run() throws Exception {
        URL url = new URL("http://" + host  + ":" + httpPort + contextRoot +
                "/protected.txt");
        System.out.println("Connecting to " + url.toString());
        URLConnection conn = url.openConnection();
        String redirectLocation = conn.getHeaderField("Location");
        System.out.println("Location: " + redirectLocation);

        String expectedRedirectLocation = "https://" + host + ":" + httpsPort +
                contextRoot + "/protected.txt";
        if (!expectedRedirectLocation.equals(redirectLocation)) {
            throw new Exception("Unexpected redirect location");
        }

        SSLSocketFactory ssf = getSSLSocketFactory(keyStorePath, trustStorePath);
        System.out.println("Connecting to " + redirectLocation);
        HttpsURLConnection connection = connect(redirectLocation, ssf);
        verifyResponse(connection);
    }

    private void verifyResponse(HttpsURLConnection connection)
            throws Exception {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new InputStreamReader(
                    connection.getInputStream()));
            String line = null;
            boolean found = false;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
                if (line.indexOf("j_security_check") != -1) {
                    found = true;
                }
            }
            if (!found) {
                throw new Exception("Expected j_security_check ACTION " +
                        "not found in response");
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }


    private SSLSocketFactory getSSLSocketFactory(String keyStorePath, String trustStorePath) throws Exception {
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


    private HttpsURLConnection connect(String urlAddress,
            SSLSocketFactory ssf) throws Exception {
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

        return connection;
    }
}

