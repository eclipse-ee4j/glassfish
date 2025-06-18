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

public class HttpRedirectElement extends BaseDevTest {
    private String targetUrl;
    private String httpPort;
    private String httpsPort;
    private boolean secureRedirect;
    private boolean samePort;
    private SSLSocketFactory ssf;

    public HttpRedirectElement(final String host, final String port, final String securePort, boolean secureRedirect,
        final boolean samePort, final String path) {
        httpPort = port;
        httpsPort = securePort;
        this.secureRedirect = secureRedirect;
        this.samePort = samePort;
        stat.getSuite().setName(getTestName());
        stat.getSuite().setDescription(getTestDescription());
        createPUElements();
        try {
            String url;
            if (secureRedirect) {
                targetUrl = String.format("https://%s:%s/", host, samePort ? port : securePort);
                url = String.format("http://%s:%s/", host, port);
            } else {
                targetUrl = String.format("http://%s:%s/", host, samePort ? securePort : port);
                url = String.format("https://%s:%s/", host, securePort);
                ssf = getSSLSocketFactory(path);
            }
            HttpURLConnection connection = getConnection(url);
            connection.setInstanceFollowRedirects(true);
            checkStatus(connection);
            parseResponse(connection);
        } catch (Throwable t) {
            report("exception found: " + t.getMessage(), false);
            t.printStackTrace();
        } finally {
            deletePUElements();
        }
        stat.printSummary();
    }

    private HttpURLConnection getConnection(final String url) throws Exception {
        return secureRedirect
            ? (HttpURLConnection) new URL(url).openConnection()
            : doSSLHandshake(url, ssf);
    }

    private TrustManager[] getTrustManagers(String path) throws Exception {
        KeyStore trustStore = KeyStore.getInstance(new File(path), null);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(trustStore);
        return tmf.getTrustManagers();
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

    @Override
    protected String getTestName() {
        return String.format("%sRedirectOn%sPort",
            secureRedirect ? "HttpToHttps" : "HttpsToHttp",
            samePort ? "Same" : "Different");
    }

    @Override
    protected String getTestDescription() {
        return String.format("%s redirection on %s port using http-redirect elements",
            secureRedirect ? "HTTP to HTTPS" : "HTTPS to HTTP",
            samePort ? "the same" : "different");
    }

    public static void main(String args[]) throws Exception {
        for(boolean secure : new boolean[] {true, false}) {
            for(boolean same : new boolean[] {true, false}) {
                new HttpRedirectElement(args[0], args[1], args[2], secure, same, args[3]);
            }
        }
    }

    private void createPUElements() {
        final String redirectProtocol = "http-redirect";
        // http-redirect
        report("create-http-redirect-protocol", asadmin("create-protocol",
            "--securityenabled", String.valueOf(!secureRedirect),
            redirectProtocol));
        String port;
        if(samePort) {
            port = "-1";
        } else {
            port = secureRedirect ? httpsPort : httpPort;
        }
        report("create-http-redirect", asadmin("create-http-redirect",
            "--redirect-port", port,
            "--secure-redirect", String.valueOf(secureRedirect),
            redirectProtocol));
        if (!secureRedirect) {
            report("create-ssl", asadmin("create-ssl",
                "--ssl3enabled", "false",
                "--ssl2enabled", "false",
                "--certname", "s1as",
                "--clientauthenabled", "false",
                "--type", "network-listener",
                 redirectProtocol));
        }
        //  pu-protocol
        report("create-pu-protocol", asadmin("create-protocol", "pu-protocol"));
        report("create-protocol-finder-http-finder", asadmin("create-protocol-finder",
            "--protocol", "pu-protocol",
            "--targetprotocol", secureRedirect ? "http-listener-2" : "http-listener-1",
            "--classname", HttpProtocolFinder.class.getName(),
            "http-finder"));
        report("create-protocol-finder-http-redirect", asadmin("create-protocol-finder",
            "--protocol", "pu-protocol",
            "--targetprotocol", redirectProtocol,
            "--classname", HttpProtocolFinder.class.getName(),
            redirectProtocol));
        // reset listener
        if (secureRedirect) {
            report("set-http-listener-protocol", asadmin("set",
                "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.protocol=pu-protocol"));
        } else {
            report("set-http-listener-protocol", asadmin("set",
                "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-2.protocol=pu-protocol"));
        }
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    private void deletePUElements() {
        // reset listener
        if (secureRedirect) {
            report("reset-http-listener-protocol", asadmin("set",
                "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.protocol=http-listener-1"));
        } else {
            report("reset-http-listener-protocol", asadmin("set",
                "configs.config.server-config.network-config.network-listeners.network-listener.http-listener-2.protocol=http-listener-2"));
        }
        report("delete-pu-protocol", asadmin("delete-protocol",
            "pu-protocol"));
        report("delete-http-redirect", asadmin("delete-protocol",
            "http-redirect"));
    }

    private void checkStatus(HttpURLConnection connection) throws Exception {
        int responseCode = connection.getResponseCode();
        String location = connection.getHeaderField("location");
        report("response-code", responseCode == 302);
        report("returned-location", targetUrl.equals(location));
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
