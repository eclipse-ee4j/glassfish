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

package wrongtransport;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
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
import org.glassfish.grizzly.config.portunif.HttpProtocolFinder;

public class WrongTransportSSL extends BaseDevTest {
    private static final String TEST_NAME = "wrongTransportSSL";
    private String redirectURL;

    public WrongTransportSSL(final String host, final String port, final String path) {
        redirectURL = "http://" + host + ":" + port + "/";
        createPUElements();
        try {
            SSLSocketFactory ssf = getSSLSocketFactory(path);
            final String url = "https://" + host + ":" + port + "/";
            HttpURLConnection connection = doSSLHandshake(url, ssf);
            checkStatus(connection);
            parseResponse(connection);
        } catch (Throwable t) {
            t.printStackTrace();
        } finally {
            deletePUElements();
        }
        stat.printSummary();
    }

    @Override
    protected String getTestName() {
        return TEST_NAME;
    }

    @Override
    protected String getTestDescription() {
        return "Wrong Protocol SSL test";
    }

    public static void main(String args[]) throws Exception {
        new WrongTransportSSL(args[0], args[1], args[2]);
    }

    private void createPUElements() {
        // https-redirect
        report("create-https-redirect-protocol", asadmin("create-protocol",
            "--securityenabled=true",
            "https-redirect"));
        report("create-protocol-filter-redirect", asadmin("create-protocol-filter",
            "--protocol", "https-redirect",
            "--classname", "org.glassfish.grizzly.config.portunif.HttpRedirectFilter",
            "redirect-filter"));
        report("create-https-redirect-ssl", asadmin("create-ssl",
            "--certname", "s1as",
            "--type", "network-listener",
            "--ssl2enabled", "false",
            "--ssl3enabled", "false",
            "--clientauthenabled", "false",
            "https-redirect"));
        //  pu-protocol
        report("create-pu-protocol", asadmin("create-protocol",
            "pu-protocol"));
        report("create-protocol-finder-http-finder", asadmin("create-protocol-finder",
            "--protocol", "pu-protocol",
            "--targetprotocol", "http-listener-1",
            "--classname", HttpProtocolFinder.class.getName(),
            "http-finder"));
        report("create-protocol-finder-https-redirect", asadmin("create-protocol-finder",
            "--protocol", "pu-protocol",
            "--targetprotocol", "https-redirect",
            "--classname", HttpProtocolFinder.class.getName(),
            "https-redirect"));
        // reset listener
        report("set-https-listener-protocol", asadmin("set",
            "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-2.protocol=pu-protocol"));
    }

    private void deletePUElements() {
        // reset listener
        report("reset-https-listener-protocol", asadmin("set",
            "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-2.protocol=http-listener-2"));
        report("delete-pu-protocol", asadmin("delete-protocol",
            "pu-protocol"));
        report("delete-https-redirect", asadmin("delete-protocol",
            "https-redirect"));
    }

    private void checkStatus(HttpURLConnection connection) throws Exception {
        report("response-code", connection.getResponseCode() == 302);
        report("returned-location", redirectURL.equals(connection.getHeaderField("location")));
    }

    private SSLSocketFactory getSSLSocketFactory(String trustStorePath)
        throws Exception {
        SSLContext sc = SSLContext.getInstance("SSL");
        sc.init(null, getTrustManagers(trustStorePath), null);
        return sc.getSocketFactory();
    }

    private HttpsURLConnection doSSLHandshake(String urlAddress, SSLSocketFactory ssf)
        throws Exception {
        URL url = new URL(urlAddress);
        HttpsURLConnection.setDefaultSSLSocketFactory(ssf);
        HttpsURLConnection.setFollowRedirects(true);
        HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setHostnameVerifier(
            new HostnameVerifier() {
                public boolean verify(String server, SSLSession session) {
                    return true;
                }
            });
        connection.setDoOutput(true);
        return connection;
    }

    private TrustManager[] getTrustManagers(String path) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(new File(path), passphrase);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf.getTrustManagers();
    }

    private void parseResponse(HttpURLConnection connection) throws Exception {
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        try {
            while (in.readLine() != null) {
            }
        } finally {
            in.close();
        }
    }
}
