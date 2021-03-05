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

package com.sun.web.security;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509KeyManager;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.glassfish.security.common.SharedSecureRandomImpl;

//V3:Commented import com.sun.enterprise.ServerConfiguration;
//V3:Commented import com.sun.web.server.*;
//V3:Commented import com.sun.enterprise.server.J2EEServer;
import com.sun.enterprise.security.ssl.J2EEKeyManager;
import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.logging.LogDomains;

/**
 * SSL server socket factory.
 *
 * @author Harish Prabandham
 * @author Vivek Nagar
 * @author Harpreet Singh
 */
// TODO: this should become a HK2 component
public class SSLSocketFactory implements org.apache.catalina.net.ServerSocketFactory {

    static Logger _logger = LogDomains.getLogger(SSLSocketFactory.class, LogDomains.WEB_LOGGER);

    private static final boolean clientAuth = false;

    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(SSLSocketFactory.class);

    private SSLContext context = null;
    private javax.net.ssl.SSLServerSocketFactory factory = null;
    private String cipherSuites[];

    private static KeyManager[] keyManagers = null;
    private static TrustManager[] trustManagers = null;

    // XXX initStoresAtStartup may call more than once, should clean up later
    // copied from SSLUtils : V3 to break dependency of this SSLUtils on this Class.
    private static boolean initialized = false;

    /**
     * Create the SSL socket factory. Initialize the key managers and trust managers which are passed to the SSL context.
     */
    public SSLSocketFactory() {
        try {
            if (keyManagers == null || trustManagers == null) {
                initStoresAtStartup();
            }
            context = SSLContext.getInstance("TLS");
            context.init(keyManagers, trustManagers, SharedSecureRandomImpl.get());

            factory = context.getServerSocketFactory();
            cipherSuites = factory.getSupportedCipherSuites();

            for (String cipherSuite : cipherSuites) {
                if (_logger.isLoggable(Level.FINEST)) {
                    _logger.log(Level.FINEST, "Suite: " + cipherSuite);
                }
            }

        } catch (Exception e) {
            _logger.log(Level.SEVERE, "web_security.excep_sslsockfact", e.getMessage());
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
     * Specify whether the server will require client authentication.
     *
     * @param socket the SSL server socket.
     */
    private void init(SSLServerSocket socket) {
        // Some initialization goes here.....
        // socket.setEnabledCipherSuites(cipherSuites);
        socket.setNeedClientAuth(clientAuth);
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
        ServiceLocator habitat = Globals.getDefaultHabitat();
        SSLUtils sslUtils = habitat.getService(SSLUtils.class);

        keyManagers = sslUtils.getKeyManagers();
        trustManagers = sslUtils.getTrustManagers();

        // Creating a default SSLContext and HttpsURLConnection for clients
        // that use Https
        SSLContext ctx = SSLContext.getInstance("TLS");
        String keyAlias = System.getProperty(SSLUtils.HTTPS_OUTBOUND_KEY_ALIAS);
        KeyManager[] kMgrs = sslUtils.getKeyManagers();
        if (keyAlias != null && keyAlias.length() > 0 && kMgrs != null) {
            for (int i = 0; i < kMgrs.length; i++) {
                kMgrs[i] = new J2EEKeyManager((X509KeyManager) kMgrs[i], keyAlias);
            }
        }
        ctx.init(kMgrs, sslUtils.getTrustManagers(), null);
        HttpsURLConnection.setDefaultSSLSocketFactory(ctx.getSocketFactory());
        initialized = true;
    }
}
