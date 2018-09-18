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

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.sun.appserv.test.util.results.SimpleReporterAdapter;

public class WebTest {
    private static final String TEST_NAME = "keepAliveTimeoutSSL";
    private static final SimpleReporterAdapter stat = new SimpleReporterAdapter("appserv-tests", TEST_NAME);
    private String host;
    private String port;
    public static final String TLS = "TLS";

    public WebTest(String[] args) {
        host = args[0];
        port = args[1];
    }

    public static void main(String[] args) throws Throwable {
        stat.addDescription(TEST_NAME);
        WebTest webTest = new WebTest(args);
        webTest.doTest();
        stat.printSummary();
    }

    private void doTest() throws Throwable {
        BufferedWriter writer = null;
        BufferedReader reader = null;
        try {
            SSLSocketFactory sslsocketfactory = getSSLSocketFactory();
            SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(host, Integer.parseInt(port));
            writer = new BufferedWriter(new OutputStreamWriter(sslsocket.getOutputStream()));
            writer.write("GET / HTTP/1.1" + '\n');
            writer.write("Host: localhost" + '\n' + '\n');
            writer.flush();
            reader = new BufferedReader(new InputStreamReader(sslsocket.getInputStream()));
            long start = System.currentTimeMillis();
            try {
                while (reader.readLine() != null) {
                }
            } catch (IOException e) {
            }
            long end = System.currentTimeMillis();
            System.out.println("WebTest.invoke: end - start = " + (end - start));
            stat.addStatus(TEST_NAME, end - start >= 10000 ? SimpleReporterAdapter.PASS : SimpleReporterAdapter.FAIL);
        } finally {
            if (writer != null) {
                writer.close();
            }
            if (reader != null) {
                reader.close();
            }
        }
    }

    public SSLSocketFactory getSSLSocketFactory() throws IOException {
        if (host == null || port == null) {
            throw new IOException("null");
        }
        try {
            //---------------------------------
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    public void checkClientTrusted(
                        X509Certificate[] certs, String authType) {
                    }

                    public void checkServerTrusted(
                        X509Certificate[] certs, String authType) {
                    }
                }
            };
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance(TLS);
            sc.init(null, trustAllCerts, new SecureRandom());
            //---------------------------------
            return sc.getSocketFactory();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e.getMessage());
        } finally {
        }
    }
}
