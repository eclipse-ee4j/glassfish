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

/*
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/config/StandaloneServerConfigTest.java,v 1.9 2007/05/05 05:23:55 tcfujii Exp $
* $Revision: 1.9 $
* $Date: 2007/05/05 05:23:55 $
*/
package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.DomainRoot;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.config.ConfigConfig;
import com.sun.appserv.management.config.NodeAgentConfig;
import com.sun.appserv.management.config.StandaloneServerConfig;
import com.sun.appserv.management.util.misc.ExceptionUtil;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.Capabilities;

import java.util.Map;


/**
 */
public final class StandaloneServerConfigTest
        extends AMXTestBase {
    public StandaloneServerConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getDomainRoot());
        }
    }

    public static String
    getDefaultInstanceName() {
        return "server";
    }

    /**
     We want the default instance to be available on both PE and EE
     so we have no choice but to use the DAS instance.
     */
    public static StandaloneServerConfig
    ensureDefaultInstance(final DomainRoot domainRoot) {
        final Map<String, StandaloneServerConfig> servers =
                domainRoot.getDomainConfig().getServersConfig().getStandaloneServerConfigMap();

        StandaloneServerConfig server = servers.get(getDefaultInstanceName());
        assert (server != null);

        return server;
    }

    public static Capabilities
    getCapabilities() {
        return getOfflineCapableCapabilities(false);
    }


    private void
    _testCreateStandaloneServerConfig(
            final String serverNameSuffix,
            final int basePort) {
        final ConfigSetup setup = new ConfigSetup(getDomainRoot());

        final Map<String, NodeAgentConfig> nodeAgentConfigs = getDomainConfig().getNodeAgentsConfig().getNodeAgentConfigMap();

        if (nodeAgentConfigs.keySet().size() == 0) {
            warning("testCreateStandaloneServerConfig: No node agents available, skipping test.");
        } else {
            // create a server for each node agent
            for (final String nodeAgentName : nodeAgentConfigs.keySet()) {
                final String serverName = nodeAgentName + serverNameSuffix;
                final String configName = serverName + "-config";

                // in case a previous failed run left them around
                setup.removeServer(serverName);
                setup.removeConfig(configName);

                final ConfigConfig config = setup.createConfig(configName);
                assert (configName.equals(config.getName()));

                // sanity check
                final Map<String, Object> attrs = Util.getExtra(config).getAllAttributes();

                try {
                    final StandaloneServerConfig server =
                            setup.createServer(serverName, basePort, nodeAgentName, config.getName());
                    // it worked, get rid of it
                    setup.removeServer(server.getName());
                }
                catch (Throwable t) {
                    assert false : ExceptionUtil.toString(t);
                }
                finally {
                    try {
                        setup.removeConfig(config.getName());
                    }
                    catch (Exception ee) {
                        // we wanted to get rid of it...oh well.
                    }
                }
            }
        }
    }

    public void
    testCreateStandaloneServerConfigWithDefaults() {
        final int basePort = 0; // use the defaults

        _testCreateStandaloneServerConfig(".StandaloneServerConfigTestWithDefaults", basePort);
    }


    public void
    testCreateStandaloneServerConfig() {
        final int basePort = 52788;

        _testCreateStandaloneServerConfig(".StandaloneServerConfigTest", basePort);
    }

}

























