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

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.ConnectorConnectionPoolConfig;
import com.sun.appserv.management.config.ConnectorConnectionPoolConfigKeys;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.util.misc.MapUtil;

import java.util.Map;

/**
 */
public final class ConnectorConnectionPoolConfigTest
        extends ConfigMgrTestBase {
    private static final String CONNECTOR_DEF_NAME = "jakarta.resource.cci.ConnectionFactory";
    private static final String RESOURCE_ADAPTOR_NAME = "cciblackbox-tx";
    private static final Map<String, String> OPTIONS = MapUtil.newMap(
            ConnectorConnectionPoolConfigKeys.IGNORE_MISSING_REFERENCES_KEY, "true");

    public ConnectorConnectionPoolConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getDomainConfig());
        }
    }

    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("ConnectorConnectionPoolConfig");
    }

    public static ConnectorConnectionPoolConfig
    ensureDefaultInstance(final DomainConfig dc) {
        ConnectorConnectionPoolConfig result =
                dc.getResourcesConfig().getConnectorConnectionPoolConfigMap().get(getDefaultInstanceName());

        if (result == null) {
            result = createInstance(dc, getDefaultInstanceName(),
                                    ResourceAdapterConfigTest.ensureDefaultInstance(dc).getName(),
                                    CONNECTOR_DEF_NAME, OPTIONS);
        }

        return result;
    }

    public static ConnectorConnectionPoolConfig
    createInstance(
            final DomainConfig dc,
            final String name,
            final String resourceAdapterName,
            final String connectorDefinitionName,
            Map<String, String> optional) {
        return dc.getResourcesConfig().createConnectorConnectionPoolConfig(name,
                                                      connectorDefinitionName, resourceAdapterName, optional);
    }

    protected Container
    getProgenyContainer() {
        return getDomainConfig();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.CONNECTOR_CONNECTION_POOL_CONFIG;
    }

    protected void
    removeProgeny(final String name) {
        getDomainConfig().getResourcesConfig().removeConnectorConnectionPoolConfig(name);
    }

    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> options) {
        final Map<String, String> allOptions = MapUtil.newMap(OPTIONS, options);

        final ConnectorConnectionPoolConfig config =
                getDomainConfig().getResourcesConfig().createConnectorConnectionPoolConfig(
                        name,
                        RESOURCE_ADAPTOR_NAME,
                        CONNECTOR_DEF_NAME, allOptions);
        return (config);
    }
}


