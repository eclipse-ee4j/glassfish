/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.appserv.management.client.AppserverConnectionSource;
import com.sun.appserv.management.client.TLSParams;
import com.sun.appserv.management.client.TrustAnyTrustManager;
import javax.management.MBeanServerConnection;
import javax.net.ssl.X509TrustManager;

/** A class to create an @{link MBeanServerConnection} to appserver.
 */
public class MBeanServerConnectionFactory {

    /** Creates a new instance of MBeanServerConnectionFactory */
    private MBeanServerConnectionFactory() {
    }

    public static final MBeanServerConnection getMBeanServerConnectionHTTPOrHTTPS(final String adminUser, final String
            adminPassword, final String adminHost, final String adminPort, final String isSecure) throws RuntimeException {

        MBeanServerConnection mbsc = null;
        try {
            final String protocol = AppserverConnectionSource.PROTOCOL_HTTP;
            final int port = Integer.parseInt(adminPort);
            final boolean sec = Boolean.parseBoolean(isSecure);
            final TLSParams tlsp = sec ? getDummyTLSParams() : null;
            final AppserverConnectionSource acs = new AppserverConnectionSource(protocol, adminHost, port, adminUser, adminPassword, tlsp, null);
            mbsc = acs.getMBeanServerConnection(true);
            return ( mbsc );
        } catch(final Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static TLSParams getDummyTLSParams() {
        final X509TrustManager[] tms = TrustAnyTrustManager.getInstanceArray();
        return ( new TLSParams(tms, null) );
    }
}
