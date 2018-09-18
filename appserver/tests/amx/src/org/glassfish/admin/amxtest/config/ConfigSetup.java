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

package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.ServerConfigKeys;
import com.sun.appserv.management.config.StandaloneServerConfig;

import java.util.HashMap;
import java.util.Map;


/**
 */
public final class ConfigSetup {
    final DomainRoot mDomainRoot;

    public static final String TEST_SERVER_NAME = "testServer";
    public static final String TEST_CONFIG_NAME = TEST_SERVER_NAME + "-config";

    public ConfigSetup(final DomainRoot domainRoot) {
        mDomainRoot = domainRoot;
    }

    public DomainConfig
    getDomainConfig() {
        return mDomainRoot.getDomainConfig();
    }

    public ConfigConfig
    createConfig(final String name) {
        final Map<String, String> options = new HashMap<String, String>();

        final ConfigConfig config =
                getDomainConfig().getConfigsConfig().createConfigConfig(name, options);

        return config;
    }

    public boolean
    removeConfig(final String name) {
        boolean exists = getDomainConfig().getConfigsConfig().getConfigConfigMap().get(name) != null;

        if (exists) {
            getDomainConfig().getConfigsConfig().removeConfigConfig(name);
        }

        return exists;
    }

    public void
    setupServerPorts(
            final Map<String, String> options,
            final int basePort) {
        if (basePort > 0) {
            options.put(ServerConfigKeys.HTTP_LISTENER_1_PORT_KEY, "" + (basePort + 0));
            options.put(ServerConfigKeys.HTTP_LISTENER_2_PORT_KEY, "" + (basePort + 1));
            options.put(ServerConfigKeys.ORB_LISTENER_1_PORT_KEY, "" + (basePort + 2));
            options.put(ServerConfigKeys.SSL_PORT_KEY, "" + (basePort + 3));
            options.put(ServerConfigKeys.SSL_MUTUALAUTH_PORT_KEY, "" + (basePort + 4));
            options.put(ServerConfigKeys.JMX_SYSTEM_CONNECTOR_PORT_KEY, "" + (basePort + 5));
            options.put(ServerConfigKeys.JMS_PROVIDER_PORT_KEY, "" + (basePort + 6));
            options.put(ServerConfigKeys.ADMIN_LISTENER_PORT_KEY, "" + (basePort + 7));
        }
    }

    public StandaloneServerConfig
    createServer(
            final String name,
            int basePort,
            final String nodeAgentName,
            final String configName) {
        final Map<String, String> options = new HashMap<String, String>();

        setupServerPorts(options, basePort);

        final StandaloneServerConfig server =
                getDomainConfig().getServersConfig().createStandaloneServerConfig(
                        name, nodeAgentName, configName, options);

        return server;
    }


    public boolean
    removeServer(final String name) {
        boolean exists = getDomainConfig().getServersConfig().getStandaloneServerConfigMap().get(name) != null;

        if (exists) {
            getDomainConfig().getServersConfig().removeStandaloneServerConfig(name);
        }

        return exists;
    }
}






