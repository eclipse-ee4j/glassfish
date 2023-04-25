/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.net.NetUtils;

import java.util.List;
import java.util.Objects;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.GlassFishConfigBean;
import org.glassfish.config.support.PropertyResolver;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.jvnet.hk2.config.Dom;

/**
 * The {@code Server.java} file is getting pretty bloated. Offload some utilities here.
 *
 * @author Byron Nevins
 */
public class ServerHelper {

    private final Server server;
    private final Config config;

    public ServerHelper(Server theServer, Config theConfig) {
        server = theServer;
        config = theConfig;

        if (server == null || config == null)
            throw new IllegalArgumentException();
    }

    public final int getAdminPort() {
        try {
            String portString = getAdminPortString(server, config);
            if (portString == null) {
                return -1; // get out quick.  it is kosher to call with a null Server
            }
            return Integer.parseInt(portString);
        } catch (Exception e) {
            // drop through...
        }
        return -1;
    }

    public final String getAdminHost() {
        // Look at the address for the admin-listener first
        String addr = translateAddressAndPort(getAdminListener(config), server, config)[0];
        if (addr != null && !addr.equals("0.0.0.0")) {
            return addr;
        }

        Dom serverDom = Objects.requireNonNull(Dom.unwrap(server));
        Domain domain = serverDom.getHabitat().getService(Domain.class);
        Nodes nodes = serverDom.getHabitat().getService(Nodes.class);
        ServerEnvironment env = serverDom.getHabitat().getService(ServerEnvironment.class);

        if (server.isDas()) {
            if (env.isDas()) {
                // We are the DAS. Return our hostname
                return System.getProperty(SystemPropertyConstants.HOST_NAME_PROPERTY);
            } else {
                return null; // IT 12778 -- it is impossible to know
            }
        }

        String hostName = null;

        // Get it from the node associated with the server
        String nodeName = server.getNodeRef();
        if (StringUtils.ok(nodeName)) {
            Node node = nodes.getNode(nodeName);
            if (node != null) {
                hostName = node.getNodeHost();
            }
            // XXX Hack to get around the fact that the default localhost
            // node entry is malformed
            if (hostName == null && nodeName.equals("localhost-" + domain.getName())) {
                hostName = "localhost";
            }
        }

        if (StringUtils.ok(hostName)) {
            return hostName;
        }
        return null;
    }

    // very simple generic check
    public final boolean isRunning() {
        try {
            return NetUtils.isRunning(getAdminHost(), getAdminPort());
        } catch (Exception e) {
            // fall through
        }
        return false;
    }

    public static NetworkListener getAdminListener(Config config) {
        NetworkConfig networkConfig = config.getNetworkConfig();
        if (networkConfig == null) {
            throw new IllegalStateException("Can't operate without <http-service>");
        }

        List<NetworkListener> listeners = networkConfig.getNetworkListeners().getNetworkListener();
        if (listeners == null || listeners.isEmpty()) {
            throw new IllegalStateException("Can't operate without at least one <network-listener>");
        }

        for (NetworkListener listener : listeners) {
            if (ServerTags.ADMIN_LISTENER_ID.equals(listener.getName())) {
                return listener;
            }
        }
        // if we can't find the admin-listener, then use the first one
        return listeners.get(0);
    }

    ///////////////////////////////////////////
    ///////////////////  all private below
    ///////////////////////////////////////////

    private static String getAdminPortString(Server server, Config config) {
        if (server == null || config == null) {
            return null;
        }
        return translateAddressAndPort(getAdminListener(config), server, config)[1];
    }

    /**
     *
     * @param adminListener the admin listener
     * @param server the server
     * @param config the config
     * @return ret[0] == address, ret[1] == port
     */
    private static String[] translateAddressAndPort(NetworkListener adminListener, Server server, Config config) {
        NetworkListener adminListenerRaw;
        String[] ret = new String[2];
        String portString = null;
        String addressString = null;

        try {
            Dom serverDom = Objects.requireNonNull(Dom.unwrap(server));
            Domain domain = serverDom.getHabitat().getService(Domain.class);

            adminListenerRaw = GlassFishConfigBean.getRawView(adminListener);
            portString = adminListenerRaw.getPort();
            addressString = adminListenerRaw.getAddress();
            PropertyResolver resolver = new PropertyResolver(domain, server.getName());

            if (isToken(portString)) {
                ret[1] = resolver.getPropertyValue(portString);
            } else {
                ret[1] = portString;
            }

            if (isToken(addressString)) {
                ret[0] = resolver.getPropertyValue(addressString);
            } else {
                ret[0] = addressString;
            }
        } catch (ClassCastException e) {
            // jc: workaround for issue 12354
            // TODO severe error
            ret[0] = translatePortOld(addressString, server, config);
            ret[1] = translatePortOld(portString, server, config);
        }
        return ret;
    }

    private static String translatePortOld(String portString, Server server, Config config) {
        if (!isToken(portString)) {
            return portString;
        }

        // isToken returned true, so we are NOT assuming anything below!
        String key = portString.substring(2, portString.length() - 1);

        // check cluster and the cluster's config if applicable
        // bnevins Jul 18, 2010 -- don't bother this should never be called anymore
        SystemProperty prop = server.getSystemProperty(key);

        if (prop != null) {
            return prop.getValue();
        }

        prop = config.getSystemProperty(key);

        if (prop != null) {
            return prop.getValue();
        }

        return null;
    }

    private static boolean isToken(String s) {
        return s != null && s.startsWith("${") && s.endsWith("}") && s.length() > 3;
    }
}
