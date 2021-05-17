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

package com.sun.enterprise.security.auth.realm.ldap;

import com.sun.enterprise.security.SecurityLoggerInfo;
import com.sun.enterprise.security.SecurityServicesUtil;
import com.sun.enterprise.security.ssl.SSLUtils;
import com.sun.enterprise.util.i18n.StringManager;
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import java.net.InetAddress;

import org.glassfish.internal.api.Globals;
import org.glassfish.internal.api.SharedSecureRandom;
/**
 * Custom socket factory for ldaps (SSL).
 *
 * The comparator only works in JDK 1.6 onwards. Due to a bug in JDK 1.6
 * compare method invocation fails with a classcast exception. The caller is
 * trying to pass java.lang.String when it should have passed
 * javax.net.SocketFactory
 *
 * @see com.sun.enterprise.security.auth.realm.ldap.LDAPRealm
 *
 */
public class CustomSocketFactory extends SocketFactory implements Comparator<SocketFactory> {
    private SocketFactory socketFactory;

    public static final String SSL = "SSL";
    protected static final Logger _logger =
        SecurityLoggerInfo.getLogger();
    protected static final StringManager sm =
        StringManager.getManager(CustomSocketFactory.class);
    private static final  CustomSocketFactory customSocketFactory = new CustomSocketFactory();

    public  CustomSocketFactory() {
        SSLUtils sslUtils = Globals.getDefaultHabitat().getService(SSLUtils.class);
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance(SSL);
            sc.init(sslUtils.getKeyManagers(), sslUtils.getTrustManagers(), SharedSecureRandom.get());
            socketFactory = sc.getSocketFactory();
        } catch (Exception ex) {
            _logger.log(Level.WARNING, SecurityLoggerInfo.securityExceptionError, ex);
        }
    }

    /**
     * @see javax.net.SocketFactory#createSocket(java.lang.String, int)
     */
    public Socket createSocket(String arg0, int arg1) throws IOException,
            UnknownHostException {
        return socketFactory.createSocket(arg0, arg1);
    }

    /**
     * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int)
     */
    public Socket createSocket(InetAddress arg0, int arg1) throws IOException {
        return socketFactory.createSocket(arg0, arg1);
    }

    /**
     * @see javax.net.SocketFactory#createSocket(java.lang.String, int,
     *      java.net.InetAddress, int)
     */
    public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3)
    throws IOException, UnknownHostException {
        return socketFactory.createSocket(arg0, arg1, arg2, arg3);
    }

    /**
     * @see javax.net.SocketFactory#createSocket(java.net.InetAddress, int,
     *      java.net.InetAddress, int)
     */
    public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2,
            int arg3) throws IOException {
        return socketFactory.createSocket(arg0, arg1, arg2, arg3);
    }

    public int compare(SocketFactory s1, SocketFactory s2) {
        return s1.getClass().toString().compareTo(s2.getClass().toString());
    }

    public static SocketFactory getDefault() {
        return customSocketFactory;
    }


}
