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

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.KeyStore;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

import com.sun.appserv.test.BaseDevTest;

/**
 * Bugster 6480567
 */
public class WebTest extends BaseDevTest {

    public WebTest(String host, String port, String contextRoot, String trustStorePath) {
        HttpsURLConnection connection = null;
        try {
            SSLSocketFactory ssf = getSSLSocketFactory(trustStorePath);
            connection = doSSLHandshake("https://" + host + ":" + port + "/", ssf);
            int count = 1;
            checkStatus(connection, count++);
            connection.disconnect();

            connection = doSSLHandshake("https://" + host  + ":" + port + "/" + contextRoot + "/ServletTest", ssf);
            checkStatus(connection, count++);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            if(connection != null) {
                connection.disconnect();
            }
        }

        stat.printSummary();
    }

    @Override
    protected String getTestName() {
        return "sslServerName";
    }

    @Override
    protected String getTestDescription() {
        return "serverName SSL";
    }

    public static void main(String args[]) throws Exception{
        new WebTest(args[0], args[1], args[2], args[3]);

    }

    private SSLSocketFactory getSSLSocketFactory(String trustStorePath) throws Exception {
        SSLContext sc = SSLContext.getInstance("TLS");
        sc.init(null, getTrustManagers(trustStorePath), null);
        return sc.getSocketFactory();
    }

    private HttpsURLConnection doSSLHandshake(String urlAddress, SSLSocketFactory ssf) throws Exception {
        URL url = new URL(urlAddress);
        HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();

        connection.setHostnameVerifier(new HostnameVerifier() {
                public boolean verify(String rserver, SSLSession sses) {
                    return true;
                }
        });
        connection.setDoOutput(true);
        return connection;
    }

    private void checkStatus(HttpsURLConnection connection, final int iteration) throws Exception {
        report("response-code-" + iteration, connection.getResponseCode() == 200);
    }

    private TrustManager[] getTrustManagers(String path) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(new File(path), passphrase);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf.getTrustManagers();
    }
}
