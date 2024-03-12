/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.admin.mbeanserver;

import org.glassfish.hk2.api.ServiceLocator;

import javax.management.MBeanServer;
import javax.management.remote.JMXAuthenticator;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.security.auth.Subject;
import java.io.IOException;
import java.net.UnknownHostException;

/**
Start and stop JMX connectors, base class.
 */
abstract class ConnectorStarter {

    protected static void debug(final String s) {
        System.out.println(s);
    }
    protected final MBeanServer mMBeanServer;
    protected final String mHostName;
    protected final int mPort;
    protected final boolean mSecurityEnabled;
    private final ServiceLocator mHabitat;
    protected final BootAMXListener mBootListener;
    protected volatile JMXServiceURL mJMXServiceURL = null;
    protected volatile JMXConnectorServer mConnectorServer = null;

    public JMXServiceURL getJMXServiceURL() {
        return mJMXServiceURL;
    }

    public String hostname() throws UnknownHostException {
        if (mHostName.equals("") || mHostName.equals("0.0.0.0")) {
            return Util.localhost();
        } else if (mHostName.contains(":") && !mHostName.startsWith("[")) {
            return "["+mHostName+"]";
        }
        return mHostName;
    }

    ConnectorStarter(
            final MBeanServer mbeanServer,
            final String host,
            final int port,
            final boolean securityEnabled,
            final ServiceLocator habitat,
            final BootAMXListener bootListener) {
        mMBeanServer = mbeanServer;
        mHostName = host;
        mPort = port;
        mSecurityEnabled = securityEnabled;
        mHabitat = habitat;
        mBootListener = bootListener;
    }

    abstract JMXConnectorServer start() throws Exception;

    public JMXAuthenticator getAccessController() {

        // we return a proxy to avoid instantiating the jmx authenticator until it is actually
        // needed by the system.
        return new JMXAuthenticator() {

            /**
             * We actually wait for the first authentication request to delegate/
             * @param credentials
             * @return
             */
            public Subject authenticate(Object credentials) {
                // lazy init...
                // todo : lloyd, if this becomes a performance bottleneck, we should cache
                // on first access.
                JMXAuthenticator controller = mHabitat.getService(JMXAuthenticator.class);
                return controller.authenticate(credentials);
            }
        };
    }

    public synchronized void stop() {
        try {
            if (mConnectorServer != null) {
                mConnectorServer.stop();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static protected void ignore(Throwable t) {
        // ignore
    }

    protected boolean isSecurityEnabled() {
        return mSecurityEnabled;
    }
}







