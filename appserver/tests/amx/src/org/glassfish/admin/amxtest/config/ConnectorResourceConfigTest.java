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
import com.sun.appserv.management.base.Util;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.ConnectorConnectionPoolConfig;
import com.sun.appserv.management.config.ConnectorResourceConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.ResourceRefConfig;
import com.sun.appserv.management.util.misc.CollectionUtil;

import javax.management.ObjectName;
import java.util.Map;
import java.util.Set;

/**
 */
public final class ConnectorResourceConfigTest
        extends ResourceConfigTestBase {
    public ConnectorResourceConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getDomainConfig());
        }
    }

    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("ConnectorResourceConfig");
    }

    public static ConnectorResourceConfig
    ensureDefaultInstance(final DomainConfig domainConfig) {
        ConnectorResourceConfig result =
                domainConfig.getResourcesConfig().getConnectorResourceConfigMap().get(getDefaultInstanceName());

        final ConnectorConnectionPoolConfig connectorConnectionPool =
                ConnectorConnectionPoolConfigTest.ensureDefaultInstance(domainConfig);

        if (result == null) {
            result = createInstance(domainConfig,
                                    getDefaultInstanceName(),
                                    connectorConnectionPool.getName(), null);
        }

        return result;
    }

    public static ConnectorResourceConfig
    createInstance(
            final DomainConfig domainConfig,
            final String name,
            final String poolName,
            final Map<String, String> optional) {
        return domainConfig.getResourcesConfig().createConnectorResourceConfig(
                name, poolName, optional);
    }

    protected String
    getProgenyTestName() {
        return ("jndi/ConnectorResourceConfigTest");
    }

    protected Container
    getProgenyContainer() {
        return getDomainConfig();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.CONNECTOR_RESOURCE_CONFIG;
    }


    protected void
    removeProgeny(final String name) {
        final Set<ResourceRefConfig> resourceRefs =
                getQueryMgr().queryJ2EETypeNameSet(XTypes.RESOURCE_REF_CONFIG, name);

        getDomainConfig().getResourcesConfig().removeConnectorResourceConfig(name);
    }


    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> options) {
        final String poolName =
                ConnectorConnectionPoolConfigTest.ensureDefaultInstance(getDomainConfig()).getName();

        assert (getDomainConfig().getResourcesConfig().getConnectorResourceConfigMap().get(name) == null) :
                "A resource already exists with name: " + name;

        final Set<ResourceRefConfig> resourceRefs =
                getQueryMgr().queryJ2EETypeNameSet(XTypes.RESOURCE_REF_CONFIG, name);

        ConnectorResourceConfig config = null;

        final Set<ObjectName> resourceRefObjectNames = Util.toObjectNames(resourceRefs);
        if (resourceRefs.size() != 0) {
            assert (false);
            warning("A DANGLING resource ref already exists with name: " + name +
                    ", {" +
                    CollectionUtil.toString(resourceRefObjectNames) + "} (SKIPPING TEST)");
        } else {
            config = getDomainConfig().getResourcesConfig().createConnectorResourceConfig(name,
                                                                     poolName, options);

            final Set<ResourceRefConfig> refs =
                    getQueryMgr().queryJ2EETypeNameSet(XTypes.RESOURCE_REF_CONFIG, name);
            if (resourceRefs.size() != 0) {
                final ResourceRefConfig ref = refs.iterator().next();

                warning("A resource ref within " +
                        Util.getObjectName(ref.getContainer()) +
                        " was automatically created when creating the ConnectorResourceConfig ");
            }
        }

        addReference(config);

        return (config);
    }
}


