/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.web.security;

import com.sun.enterprise.security.ssl.J2EEKeyManager;
import com.sun.enterprise.security.ssl.SSLUtils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;

import org.apache.catalina.net.ServerSocketFactory;
import org.glassfish.internal.api.Globals;
import org.glassfish.security.common.SharedSecureRandom;

import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.SEVERE;

/**
 * SSL server socket factory.
 *
 * @author Harish Prabandham
 * @author Vivek Nagar
 * @author Harpreet Singh
 */
public class SSLSocketFactory implements ServerSocketFactory {

    static Logger _logger = Logger.getLogger(SSLSocketFactory.class.getName(), WebSecurityResourceBundle.BUNDLE_NAME);

    private static final boolean clientAuth = false;

    private SSLContext context;
    private javax.net.ssl.SSLServerSocketFactory factory;
    private String cipherSuites[];

    private static KeyManager[] keyManagers;
    private static TrustManager[] trustManagers;

    // XXX initStoresAtStartup may call more than once, should clean up later
    // copied from SSLUtils : V3 to break dependency of this SSLUtils on this Class.
    private static boolean initialized;

    /**
     * Create the SSL socket factory. Initialize the key managers and trust managers which are passed to the SSL context.
     */
    public SSLSocketFactory() {
        try {
            if (keyManagers == null || trustManagers == null) {
                initStoresAtStartup();
            }

            context = SSLContext.getInstance("TLS");
            context.init(keyManagers, trustManagers, SharedSecureRandom.get());

            factory = context.getServerSocketFactory();
            cipherSuites = factory.getSupportedCipherSuites();

            for (String cipherSuite : cipherSuites) {
                _logger.log(FINEST, "Suite: {0}", cipherSuite);
            }

        } catch (Exception e) {
            _logger.log(SEVERE, "Could not initialize the SSL Socket Factory!", e);
        }
    }

    /**
     * Create the socket at the specified port.
     *
     * @param port the port number.
     * @return the SSL server socket.
     */
    @Override
    public ServerSocket createSocket(int port) throws IOException {
        SSLServerSocket socket = (SSLServerSocket) factory.createServerSocket(port);
        init(socket);

        return socket;
    }

    /**
     * Create the socket at the specified port.
     *
     * @param port the port number.
     * @return the SSL server socket.
     */
    @Override
    public ServerSocket createSocket(int port, int backlog) throws IOException {
        SSLServerSocket socket = (SSLServerSocket) factory.createServerSocket(port, backlog);
        init(socket);

        return socket;
    }

    /**
     * Create the socket at the specified port.
     *
     * @param port the port number.
     * @return the SSL server socket.
     */
    @Override
    public ServerSocket createSocket(int port, int backlog, InetAddress ifAddress) throws IOException {
        SSLServerSocket socket = (SSLServerSocket) factory.createServerSocket(port, backlog, ifAddress);
        init(socket);

        return socket;
    }

    // V3: to break dependency of SSLUtils on this class
    //    public static void setManagers(KeyManager[] kmgrs, TrustManager[] tmgrs) {
    //        keyManagers = kmgrs;
    //        trustManagers = tmgrs;
    //    }
    // V3: Copied from SSLUtils to break dependency of SSLUtils on this class
    public static synchronized void initStoresAtStartup() throws Exception {
        if (initialized) {
            return;
        }

        SSLUtils sslUtils = Globals.getDefaultHabitat().getService(SSLUtils.class);

        keyManagers = sslUtils.getKeyManagers();
        trustManagers = sslUtils.getTrustManagers();

        // Creating a default SSLContext and HttpsURLConnection for clients
        // that use Https
        SSLContext sslContext = SSLContext.getInstance("TLS");
        String keyAlias = System.getProperty(SSLUtils.HTTPS_OUTBOUND_KEY_ALIAS);
        KeyManager[] keyManagers = sslUtils.getKeyManagers();
        if (keyAlias != null && keyAlias.length() > 0 && keyManagers != null) {
            for (int i = 0; i < keyManagers.length; i++) {
                keyManagers[i] = new J2EEKeyManager((X509KeyManager) keyManagers[i], keyAlias);
            }
        }

        sslContext.init(keyManagers, sslUtils.getTrustManagers(), null);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());
        initialized = true;
    }

    /**
     * Specify whether the server will require client authentication.
     *
     * @param socket the SSL server socket.
     */
    private void init(SSLServerSocket socket) {
        socket.setNeedClientAuth(clientAuth);
    }
}
