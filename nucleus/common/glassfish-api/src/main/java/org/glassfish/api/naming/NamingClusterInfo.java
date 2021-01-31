/*
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.naming;

import java.util.Hashtable;
import java.util.List;

import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Contract;
import org.omg.CORBA.ORB;

/**
 * A contract that manages naming load-balancing.
 */

@Contract
public interface NamingClusterInfo {
    String LOAD_BALANCING_PROPERTY = "com.sun.appserv.iiop.loadbalancingpolicy";

    String IIOP_ENDPOINTS_PROPERTY = "com.sun.appserv.iiop.endpoints";

    String IIOP_URL_PROPERTY = "com.sun.appserv.ee.iiop.endpointslist";

    String IC_BASED_WEIGHTED = "ic-based-weighted";

    String IC_BASED = "ic-based";

    String IIOP_URL = "iiop:1.2@";

    String CORBALOC = "corbaloc:";

    void initGroupInfoService(Hashtable<?, ?> myEnv, String defaultHost, String defaultPort, ORB orb, ServiceLocator services);

    void setClusterInstanceInfo(Hashtable<?, ?> myEnv, String defaultHost, String defaultPort, boolean membershipChangeForced);

    List<String> getNextRotation();

}
