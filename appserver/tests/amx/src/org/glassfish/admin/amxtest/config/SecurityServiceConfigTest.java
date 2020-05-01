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
import com.sun.appserv.management.config.BackendPrincipalConfig;
import com.sun.appserv.management.config.ConnectorConnectionPoolConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.ResourceAdapterConfig;
import com.sun.appserv.management.config.SecurityMapConfig;
import com.sun.appserv.management.util.jmx.JMXUtil;
import com.sun.appserv.management.util.misc.GSetUtil;
import org.glassfish.admin.amxtest.AMXTestBase;

import java.util.Set;


/**
 */
public final class SecurityServiceConfigTest
        extends AMXTestBase {
    public SecurityServiceConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getDomainConfig());
        }
    }


    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("SecurityMapConfig");
    }

    private static final String DEFAULT_BACKEND_PRINCIPAL = "SecurityMapConfigTest.default";
    private static final String DEFAULT_BACKEND_PASSWORD = "changeme";
    private static final String[] DEFAULT_PRINCIPALS =
            new String[]{"SecurityMapConfigTest.principal1"};
    private static final String[] DEFAULT_USERGROUPS = new String[0];

    public static SecurityMapConfig
    ensureDefaultInstance(final DomainConfig domainConfig) {
        final ConnectorConnectionPoolConfig ccpc =
                ConnectorConnectionPoolConfigTest.ensureDefaultInstance(domainConfig);

        SecurityMapConfig result =
                ccpc.getSecurityMapConfigMap().get(getDefaultInstanceName());
        if (result == null) {
            result = createInstance(ccpc,
                                    getDefaultInstanceName(),
                                    DEFAULT_BACKEND_PRINCIPAL,
                                    DEFAULT_BACKEND_PASSWORD,
                                    DEFAULT_PRINCIPALS,
                                    DEFAULT_USERGROUPS);
        }

        return result;
    }


    private void
    testGetters(final SecurityMapConfig smc) {
        final String[] principalNames = smc.getPrincipalNames();
        final String[] userGroupNames = smc.getUserGroupNames();

        assert (principalNames != null || userGroupNames != null) : "both principals and usergroups are null";

        final BackendPrincipalConfig bpc = smc.getBackendPrincipalConfig();
        assert (bpc != null) : "null BackendPrincipalConfig for " + JMXUtil.toString(Util.getExtra(smc).getObjectName());
        final String s = bpc.getUserName();
        bpc.setUserName(s);
        final String password = bpc.getPassword();
        bpc.setPassword(password);
    }


    public static SecurityMapConfig
    createInstance(
            final ConnectorConnectionPoolConfig ccpc,
            final String name,
            final String backendPrincipalUsername,
            final String backendPrincipalPassword,
            final String[] principals,
            final String[] userGroups) {
        final SecurityMapConfig smc =
                ccpc.createSecurityMapConfig(name,
                                             backendPrincipalUsername, backendPrincipalPassword,
                                             principals, userGroups);

        return smc;
    }

    private static final String CONNECTOR_DEF_NAME = "jakarta.resource.cci.ConnectionFactory";

    public void
    testCreateRemove() {
        if (!checkNotOffline("testDeleteLBConfig")) {
            return;
        }

        final String TEST_NAME = "SecurityMapConfigTest.testCreateRemove";
        final ResourceAdapterConfig rac = ResourceAdapterConfigTest.createInstance(
                getDomainConfig(), TEST_NAME);

        try {
            final ConnectorConnectionPoolConfig ccpc =
                    ConnectorConnectionPoolConfigTest.createInstance(getDomainConfig(),
                                                                     TEST_NAME,
                                                                     CONNECTOR_DEF_NAME,
                                                                     rac.getName(), null);

            try {
                final String smcName = TEST_NAME;
                final String[] principals = new String[]{TEST_NAME};
                final String[] userGroups = new String[0];
                final SecurityMapConfig smc = createInstance(
                        ccpc,
                        smcName,
                        DEFAULT_BACKEND_PRINCIPAL,
                        DEFAULT_BACKEND_PASSWORD,
                        principals,
                        null);
                try {
                    assert (smcName.equals(smc.getName()));
                    assert (smc == ccpc.getSecurityMapConfigMap().get(smc.getName()));
                    testGetters(smc);

                    final Set<String> principalsBefore = GSetUtil.newSet(smc.getPrincipalNames());
                    final String PRINCIPAL1 = "testCreateRemove.test1";
                    smc.createPrincipal(PRINCIPAL1);

                    final Set<String> principalsAfter = GSetUtil.newSet(smc.getPrincipalNames());
                    assert (principalsAfter.contains(PRINCIPAL1));

                    smc.removePrincipal(PRINCIPAL1);
                    assert (principalsBefore.equals(GSetUtil.newSet(smc.getPrincipalNames())));

                }
                finally {
                    ccpc.removeSecurityMapConfig(smc.getName());
                }
            }
            finally {
                getDomainConfig().getResourcesConfig().removeConnectorConnectionPoolConfig(ccpc.getName());
            }
        }
        finally {
            getDomainConfig().getResourcesConfig().removeResourceAdapterConfig(rac.getName());
        }
    }

}



























