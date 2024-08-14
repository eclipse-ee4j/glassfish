/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.config.util;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.net.NetUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sun.enterprise.config.util.PortConstants.PORTSLIST;

/**
 * Simple pkg-priv class for keeping the system-properties of a Server handy. We add the Port Props in the correct order
 * Order of precedence from LOW to HIGH == (0) domain (1) cluster (2) config (3) server Just add the props in that same
 * order and we're good to go! Note that I'm VERY paranoid about NPE's
 *
 * @author Byron Nevins
 */
class ServerPorts {

    // The new server is in the middle of its creation transaction which makes
    // things trickier -- its config, for instance, might not be in the domains
    //list of configs yet.
    // So we send everything in explicitly from the Transaction...
    ServerPorts(Cluster cluster, Config config, Domain domain, Server theServer) {
        initialize(cluster, config, domain, theServer);
    }

    // this constructor is for use for pre-existing servers.
    ServerPorts(Domain domain, Server theServer) {
        Cluster cluster = null;
        Config config = null;

        if (theServer.isInstance())
            cluster = domain.getClusterForInstance(theServer.getName());

        String configName = theServer.getConfigRef();

        if (StringUtils.ok(configName))
            config = domain.getConfigNamed(configName);

        initialize(cluster, config, domain, theServer);
    }

    Map<String, Integer> getMap() {
        return props;
    }

    //////////////////////  all private below   //////////////////////////

    private void initialize(Cluster cluster, Config config, Domain domain, Server theServer) {
        List<SystemProperty> propList;
        server = theServer;

        // 0. domain
        propList = domain.getSystemProperty();
        addAll(propList);

        // 1. cluster
        if (cluster != null) {
            propList = cluster.getSystemProperty();
            addAll(propList);
        }

        // 2. config
        if (config != null) {
            propList = config.getSystemProperty();
            addAll(propList);
        }

        // 3. server
        propList = server.getSystemProperty();
        addAll(propList);
    }

    private void addAll(List<SystemProperty> propList) {

        if (propList == null)
            return;

        for (SystemProperty sp : propList) {
            // we only care about
            // 1. the official Port Props that we support
            // 2. But only if they also have a value that is a legal port number

            String name = sp.getName();
            String value = sp.getValue();

            if (StringUtils.ok(name) && StringUtils.ok(value) && PORTSLIST.contains(name)) {
                try {
                    int port = Integer.parseInt(value);

                    if (NetUtils.isPortValid(port))
                        props.put(name, port);
                } catch (Exception e) {
                    // we're all done here!
                }
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("ServerPorts Dump:").append('\n');
        sb.append("server: ").append(server.getName()).append(", ");
        sb.append("Properties: ").append(props).append('\n');
        return sb.toString();
    }

    private Server server;
    private final Map<String, Integer> props = new HashMap<String, Integer>();
}
