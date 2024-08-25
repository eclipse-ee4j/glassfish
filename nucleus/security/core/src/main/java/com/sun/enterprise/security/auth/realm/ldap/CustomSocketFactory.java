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

package com.sun.enterprise.security.auth.realm.ldap;

import com.sun.enterprise.security.SecurityLoggerInfo;
import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.enterprise.util.i18n.StringManager;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.logging.Logger;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;

import org.glassfish.internal.api.Globals;
import org.glassfish.security.common.SharedSecureRandom;

import static com.sun.enterprise.security.SecurityLoggerInfo.securityExceptionError;
import static java.util.logging.Level.WARNING;

/**
 * Custom socket factory for ldaps (SSL).
 *
 * The comparator only works in JDK 1.6 onwards. Due to a bug in JDK 1.6 compare method invocation fails with a
 * classcast exception. The caller is trying to pass java.lang.String when it should have passed javax.net.SocketFactory
 *
 * @see com.sun.enterprise.security.auth.realm.ldap.LDAPRealm
 *
 */
public class CustomSocketFactory extends SocketFactory implements Comparator<SocketFactory> {

    public static final String SSL = "SSL";

    protected static final Logger _logger = SecurityLoggerInfo.getLogger();
    protected static final StringManager sm = StringManager.getManager(CustomSocketFactory.class);

    private static final CustomSocketFactory customSocketFactory = new CustomSocketFactory();

    private SocketFactory socketFactory;

    public CustomSocketFactory() {
        SSLUtils sslUtils = Globals.get(SSLUtils.class);

        try {
            SSLContext sslContext = SSLContext.getInstance(SSL);
            sslContext.init(sslUtils.getKeyManagers(), sslUtils.getTrustManagers(), SharedSecureRandom.get());
            socketFactory = sslContext.getSocketFactory();
        } catch (Exception ex) {
            _logger.log(WARNING, securityExceptionError, ex);
        }
    }

    @Override
    public Socket createSocket() throws IOException {
        return socketFactory.createSocket();
    }

    @Override
    public Socket createSocket(String host, int port) throws IOException, UnknownHostException {
        return socketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(InetAddress host, int port) throws IOException {
        return socketFactory.createSocket(host, port);
    }

    @Override
    public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException {
        return socketFactory.createSocket(host, port, localHost, localPort);
    }

    @Override
    public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException {
        return socketFactory.createSocket(address, port, localAddress, localPort);
    }

    @Override
    public int compare(SocketFactory s1, SocketFactory s2) {
        return s1.getClass().toString().compareTo(s2.getClass().toString());
    }

    public static SocketFactory getDefault() {
        return customSocketFactory;
    }

}
