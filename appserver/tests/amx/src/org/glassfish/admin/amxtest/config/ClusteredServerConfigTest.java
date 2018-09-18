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

import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.ClusterConfig;
import com.sun.appserv.management.config.ClusteredServerConfig;
import com.sun.appserv.management.config.DeployedItemRefConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.NodeAgentConfig;
import com.sun.appserv.management.config.RefConfig;
import com.sun.appserv.management.config.ResourceRefConfig;
import com.sun.appserv.management.util.misc.GSetUtil;
import org.glassfish.admin.amxtest.AMXTestBase;
import org.glassfish.admin.amxtest.ClusterSupportRequired;
import org.glassfish.admin.amxtest.PropertyKeys;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 */
public final class ClusteredServerConfigTest
        extends AMXTestBase
        implements ClusterSupportRequired {
    public ClusteredServerConfigTest() {
    }

    private void
    sanityCheck(final ClusteredServerConfig csc) {
        assert XTypes.CLUSTERED_SERVER_CONFIG.equals(csc.getJ2EEType());

        final String configName = csc.getConfigRef();
        final String nodeAgentName = csc.getConfigRef();

        final Map<String, DeployedItemRefConfig> deployedItems =
                csc.getDeployedItemRefConfigMap();


        final Map<String, ResourceRefConfig> resources =
                csc.getResourceRefConfigMap();

        final String lbWeight = csc.getLBWeight();
        csc.setLBWeight(lbWeight);
    }


    public ClusteredServerConfig
    createClusteredServer(
            final String serverName,
            final String nodeAgentName,
            final int basePort) {
        final DomainConfig domainConfig = getDomainConfig();

        if (domainConfig.getServersConfig().getClusteredServerConfigMap().get(serverName) != null) {
            domainConfig.getServersConfig().removeClusteredServerConfig(serverName);
        } else if (domainConfig.getServersConfig().getStandaloneServerConfigMap().get(serverName) != null) {
            domainConfig.getServersConfig().removeStandaloneServerConfig(serverName);
        }

        final ClusterConfig clusterConfig =
                ClusterConfigTest.ensureDefaultInstance(domainConfig);

        if (domainConfig.getServersConfig().getClusteredServerConfigMap().get(serverName) != null) {
            domainConfig.getServersConfig().removeClusteredServerConfig(serverName);
            assert domainConfig.getServersConfig().getClusteredServerConfigMap().get(serverName) == null;
        }

        final ConfigSetup setup = new ConfigSetup(getDomainRoot());
        final Map<String, String> options = new HashMap<String, String>();
        setup.setupServerPorts(options, basePort);

        final ClusteredServerConfig csc =
                domainConfig.getServersConfig().createClusteredServerConfig(serverName,
                                                         clusterConfig.getName(),
                                                         nodeAgentName,
                                                         options);
        sanityCheck(csc);

        return csc;
    }

    private void
    verifyRefContainers() {
        final Set<String> j2eeTypes =
                GSetUtil.newUnmodifiableStringSet(
                        XTypes.DEPLOYED_ITEM_REF_CONFIG, XTypes.RESOURCE_REF_CONFIG);

        final Set<RefConfig> refs = getQueryMgr().queryJ2EETypesSet(j2eeTypes);

        for (final RefConfig ref : refs) {
            assert ref.getContainer() != null :
                    "MBean " + Util.getObjectName(ref) + " return null from getContainer()";
        }
    }

    public void
    testCreateRemove() {
        final DomainConfig domainConfig = getDomainConfig();
        final NodeAgentConfig nodeAgentConfig = getDASNodeAgentConfig();

        if (nodeAgentConfig == null) {
            warning("SKIPPING ClusteredServerConfigTest.testCreateRemove: " +
                    "no DAS Node Agent has been specified; use " +
                    PropertyKeys.DAS_NODE_AGENT_NAME);
        } else {
            final int NUM = 5;
            final String baseName = "ClusteredServerConfigTest";

            verifyRefContainers();

            final ClusteredServerConfig[] servers = new ClusteredServerConfig[NUM];
            for (int i = 0; i < NUM; ++i) {
                final int basePort = 11000 + i * 10;
                servers[i] = createClusteredServer(baseName + "-" + i,
                                                   nodeAgentConfig.getName(),
                                                   basePort);
                printVerbose("Created ClusteredServerConfig: " + servers[i].getName());
                assert XTypes.CLUSTERED_SERVER_CONFIG.equals(servers[i].getJ2EEType());

                verifyRefContainers();
            }

            for (int i = 0; i < NUM; ++i) {
                final String name = servers[i].getName();
                domainConfig.getServersConfig().removeClusteredServerConfig(name);
                printVerbose("Removed ClusteredServerConfig: " + name);
            }

        }
    }

}


