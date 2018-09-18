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

import com.sun.appserv.management.base.AMX;
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.config.DomainConfig;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 */
public final class DomainConfigTest
        extends AMXTestBase {
    public DomainConfigTest() {
    }

    public void
    testGetDeployedItemProxies() {
        final DomainConfig mgr = getDomainConfig();

        final Set proxies = mgr.getContaineeSet();
        assert (proxies.size() != 0);

        final Iterator iter = proxies.iterator();
        while (iter.hasNext()) {
            final AMX proxy = Util.asAMX(iter.next());
        }
    }

    public void
    testGetDeployedItemProxiesByName() {
        final DomainConfig mgr = getDomainConfig();

        final Map<String, Map<String, AMX>> typeMap = mgr.getMultiContaineeMap(null);

        for (final String j2eeType : typeMap.keySet()) {
            final Map<String, AMX> proxyMap = typeMap.get(j2eeType);
            for (final String name : proxyMap.keySet()) {
                final AMX amx = Util.asAMX(proxyMap.get(name));

                final AMX proxy = mgr.getContainee(j2eeType, name);

                assert (Util.getObjectName(proxy).equals(Util.getObjectName(amx)));
                assert (proxy.getName().equals(name));
            }
        }
    }


    public void
    testGetAttributes() {
        final DomainConfig mgr = getDomainConfig();

        mgr.getApplicationRoot();
        mgr.getLocale();
        mgr.getLogRoot();
    }

    private <T extends AMX> void
    checkMap(final Map<String, T> m) {
        assert (m != null);
        assert (!m.keySet().contains(AMX.NO_NAME));
        assert (!m.keySet().contains(AMX.NULL_NAME));
    }


    public void
    testGetMaps() {
        final DomainConfig m = getDomainConfig();

        //checkMap(m.getServersConfig().getServerConfigMap());
        checkMap(m.getServersConfig().getStandaloneServerConfigMap());
        checkMap(m.getServersConfig().getClusteredServerConfigMap());
        checkMap(m.getLBConfigsConfig().getLBConfigMap());
        checkMap(m.getLoadBalancersConfig().getLoadBalancerConfigMap());
        checkMap(m.getNodeAgentsConfig().getNodeAgentConfigMap());
        checkMap(m.getConfigsConfig().getConfigConfigMap());
        checkMap(m.getClustersConfig().getClusterConfigMap());

        checkMap(m.getResourcesConfig().getPersistenceManagerFactoryResourceConfigMap());
        checkMap(m.getResourcesConfig().getJDBCResourceConfigMap());
        checkMap(m.getResourcesConfig().getJDBCConnectionPoolConfigMap());
        checkMap(m.getResourcesConfig().getConnectorResourceConfigMap());
        checkMap(m.getResourcesConfig().getConnectorConnectionPoolConfigMap());
        checkMap(m.getResourcesConfig().getAdminObjectResourceConfigMap());
        checkMap(m.getResourcesConfig().getResourceAdapterConfigMap());
        checkMap(m.getResourcesConfig().getMailResourceConfigMap());

        //checkMap(m.getApplicationsConfig().getJ2EEApplicationConfigMap());
        checkMap(m.getApplicationsConfig().getEJBModuleConfigMap());
        checkMap(m.getApplicationsConfig().getWebModuleConfigMap());
        checkMap(m.getApplicationsConfig().getRARModuleConfigMap());
        checkMap(m.getApplicationsConfig().getAppClientModuleConfigMap());
        checkMap(m.getApplicationsConfig().getLifecycleModuleConfigMap());
    }

    /*
         KEEP, not quite ready to test this yet.
         public void
     testCreateStandaloneServerConfig()
     {
         final ConfigSetup setup  = new ConfigSetup( getDomainRoot() );

         setup.removeTestServer();

         final StandaloneServerConfig server = setup.createTestServer();
         setup.removeTestServer();
     }
     */


    public void
    testCreateClusterConfig() {
        // to be done
    }
}



























