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

package com.sun.enterprise.admin.util;

import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.config.serverbeans.*;
import org.glassfish.hk2.api.ServiceLocator;

import java.util.*;

/**
 * @author Byron Nevins
 *
 * Implementation Note:
 *
 * Ideally this class would be extended by AdminCommand's that need these services. The problem is getting the values
 * out of the habitat. The ctor call would be TOO EARLY in the derived classes. The values are injected AFTER
 * construction. We can't easily inject here -- because we don't want this class to be a Service. We could do it by
 * having the derived class call a set method in here but that gets very messy as we have to make sure we are in a valid
 * state for every single method call.
 *
 */
public final class RemoteInstanceCommandHelper {

    public RemoteInstanceCommandHelper(ServiceLocator habitatIn) {

        try {
            habitat = habitatIn;
            servers = habitat.<Servers>getService(Servers.class).getServer();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public final String getHost(final String serverName) {
        String host = null;
        Server server = getServer(serverName);
        if (server != null) {
            host = server.getAdminHost();
        }
        return host;
    }

    public final Server getServer(final String serverName) {
        for (Server server : servers) {
            final String name = server.getName();

            // ??? TODO is this crazy?
            if (serverName == null) {
                if (name == null) // they match!!
                    return server;
            } else if (serverName.equals(name))
                return server;
        }
        return null;
    }

    public final String getNode(final Server server) {

        if (server == null)
            return null;

        String node = server.getNodeRef();

        if (StringUtils.ok(node))
            return node;
        else
            return "no node";
    }

    public final int getAdminPort(final String serverName) {
        return getAdminPort(getServer(serverName));
    }

    public final int getAdminPort(Server server) {
        return server.getAdminPort();
    }

    ///////////////////////////////////////////////////////////////////////////
    //  All private below.  If you need something below in a derived class then
    // upgrade to pkg-private and move it above this line.  Change the keyword
    // private to final on the method
    ///////////////////////////////////////////////////////////////////////////

    final private List<Server> servers;
    final private ServiceLocator habitat;
}
