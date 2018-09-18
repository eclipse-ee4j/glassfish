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

import com.sun.appserv.management.config.JMXConnectorConfig;
import com.sun.appserv.management.config.NodeAgentConfig;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.util.Map;


/**
 */
public final class NodeAgentConfigTest
        extends AMXTestBase {
    public NodeAgentConfigTest() {
    }

    private void
    sanityCheck(final NodeAgentConfig na) {
        final JMXConnectorConfig jmx = na.getJMXConnectorConfig();

        final String startServers = na.getStartServersInStartup();
        na.setStartServersInStartup(startServers);

        final String name = na.getSystemJMXConnectorName();
        na.setSystemJMXConnectorName(name);
    }

    public void
    testGetters() {
        final Map<String, NodeAgentConfig> m = getDomainConfig().getNodeAgentsConfig().getNodeAgentConfigMap();

        if (m.size() == 0) {
            warning("NodeAgentConfigTest: no NodeAgentConfigs to test");
        } else {
            for (final NodeAgentConfig na : m.values()) {
                sanityCheck(na);
            }
        }
    }

}


