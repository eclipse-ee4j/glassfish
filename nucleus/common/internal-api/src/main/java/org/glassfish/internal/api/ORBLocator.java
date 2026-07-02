/*
 * Copyright (c) 2022, 2026 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.api;

import com.sun.enterprise.util.net.NetUtils;

import java.util.Set;

import org.jvnet.hk2.annotations.Contract;
import org.omg.CORBA.ORB;

/**
 * Contract for ORB provider.
 *
 * @author Jerome Dochez
 */
@Contract
public interface ORBLocator {

    String JNDI_CORBA_ORB_PROPERTY = "java.naming.corba.orb";
    String JNDI_PROVIDER_URL_PROPERTY = "java.naming.provider.url";
    String OMG_ORB_INIT_HOST_PROPERTY = "org.omg.CORBA.ORBInitialHost";
    String OMG_ORB_INIT_PORT_PROPERTY = "org.omg.CORBA.ORBInitialPort";

    // Same as ORBConstants.FOLB_CLIENT_GROUP_INFO_SERVICE,
    // but we can't reference ORBConstants from the naming bundle!
    String FOLB_CLIENT_GROUP_INFO_SERVICE = "FolbClientGroupInfoService";

    /**
     * Zero ip addresses, ANY host, wildcard addresses,
     * and the IPv6 equivalent of the wildcard address.
     */
    Set<String> ANY_ADDRS = Set.of("0.0.0.0", "::", "::ffff:0.0.0.0");

    String DEFAULT_ORB_INIT_HOST = NetUtils.getCanonicalHostName();
    String DEFAULT_ORB_INIT_PORT = "3700";

    // This property is true if SSL is required to be used by
    // non-EJB CORBA objects in the server.
    String ORB_SSL_SERVER_REQUIRED = "com.sun.CSIV2.ssl.server.required";
    //
    // This property is true if client authentication is required by
    // non-EJB CORBA objects in the server.
    String ORB_CLIENT_AUTH_REQUIRED = "com.sun.CSIV2.client.auth.required";

    // This property is true (in appclient Main)
    // if SSL is required to be used by clients.
    String ORB_SSL_CLIENT_REQUIRED = "com.sun.CSIV2.ssl.client.required";

    /**
     * Get or create the default orb. This can be called for any process type. However,
     * protocol manager and CosNaming initialization only take place for the Server.
     *
     * @return an initialized ORB instance
     */
    ORB getORB();

    /**
     * If null or from {@link #ANY_ADDRS} set, returns {@link #DEFAULT_ORB_INIT_HOST}.
     * Returns the parameter otherwise.
     *
     * @param hostOrAny
     * @return host name to use for ORB initialization
     */
    public static String toConcreteHost(String hostOrAny) {
        if (hostOrAny == null || ANY_ADDRS.contains(hostOrAny)) {
            return DEFAULT_ORB_INIT_HOST;
        }
        return hostOrAny;
    }
}
