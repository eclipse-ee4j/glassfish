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

package org.glassfish.loadbalancer.admin.cli.connection;

import com.sun.enterprise.security.ssl.SSLUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.loadbalancer.admin.cli.LbLogUtil;

/**
 *
 * @author hr124446
 */
public class ConnectionManager {

    public static final String HTTPS_PROTOCOL = "https";
    public static final String HTTP_PROTOCOL = "http";
    public static final String TLS = "TLS";

    /** Creates a new instance of ConnectionManager */
    public ConnectionManager(String lbHost, String lbPort, String lbProxyHost,
            String lbProxyPort, String lbName, boolean isSecure) {
        _lbHost = lbHost;
        _lbPort = lbPort;
        _lbProxyHost = lbProxyHost;
        _lbProxyPort = lbProxyPort;
        _lbName = lbName;
        _isSecure = isSecure;
    }

    /**
     * creates a connection to the loadbalancer
     * @param contextRoot context root that will be used in constructing the URL
     * @throws java.io.IOException
     * @return either HTTP or HTTPS connection to the load balancer.
     */
    public HttpURLConnection getConnection(String contextRoot) throws IOException {
        if (_isSecure) {
            return getSecureConnection(contextRoot);
        } else {
            return getNonSecureConnection(contextRoot);
        }
    }

    /**
     * creates a connection to the loadbalancer
     * @param contextRoot context root that will be used in constructing the URL
     * @throws java.io.IOException
     * @return either HTTP or HTTPS connection to the load balancer.
     */
    private HttpURLConnection getNonSecureConnection(String contextRoot) throws IOException {
        if (_lbHost == null || _lbPort == null) {
            String msg = LbLogUtil.getStringManager().getString("LbDeviceNotConfigured", _lbName);
            throw new IOException(msg);
        }

        HttpURLConnection conn = null;
        URL url = null;
        try {

            //---------------------------------
            url = new URL(HTTP_PROTOCOL, _lbHost, Integer.parseInt(_lbPort), contextRoot);
            if (_lbProxyHost != null && _lbProxyPort != null) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP,
                        new InetSocketAddress(_lbProxyHost, Integer.parseInt(_lbProxyPort)));
                conn = (HttpURLConnection) url.openConnection(proxy);
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
        return conn;
    }

    /**
     * creates a connection to the loadbalancer
     * @param contextRoot context root that will be used in constructing the URL
     * @throws java.io.IOException
     * @return HTTPS connection to the load balancer.
     */
    private HttpsURLConnection getSecureConnection(String contextRoot) throws IOException {
        if (_lbHost == null || _lbPort == null) {
            String msg = LbLogUtil.getStringManager().getString("LbDeviceNotConfigured", _lbName);
            throw new IOException(msg);
        }

        HttpsURLConnection conn = null;
        URL url = null;
        try {
            //---------------------------------
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {

                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(
                            java.security.cert.X509Certificate[] certs, String authType) {
                    }
                }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance(TLS);
            ServiceLocator habitat = Globals.getDefaultHabitat();
            SSLUtils sslUtils = habitat.getService(SSLUtils.class);
            sc.init(sslUtils.getKeyManagers(), trustAllCerts, new java.security.SecureRandom());

            //---------------------------------
            url = new URL(HTTPS_PROTOCOL, _lbHost, Integer.parseInt(_lbPort), contextRoot);
            if (_lbProxyHost != null && _lbProxyPort != null) {
                Proxy proxy = new Proxy(Proxy.Type.HTTP,
                        new InetSocketAddress(_lbProxyHost, Integer.parseInt(_lbProxyPort)));
                conn = (HttpsURLConnection) url.openConnection(proxy);
            } else {
                conn = (HttpsURLConnection) url.openConnection();
            }
            conn.setSSLSocketFactory(sc.getSocketFactory());
            HostnameVerifier hnv = new SSLHostNameVerifier();
            conn.setDefaultHostnameVerifier(hnv);
        } catch (Exception e) {
            throw new IOException(e.getMessage(), e);
        }
        return conn;
    }
    private String _lbHost = null;
    private String _lbPort = null;
    private String _lbProxyHost = null;
    private String _lbProxyPort = null;
    private String _lbName = null;
    private boolean _isSecure = true;
}
