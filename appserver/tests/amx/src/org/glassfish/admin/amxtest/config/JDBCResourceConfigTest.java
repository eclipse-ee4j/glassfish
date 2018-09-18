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
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.JDBCConnectionPoolConfig;
import com.sun.appserv.management.config.JDBCResourceConfig;

import java.util.Map;

/**
 */
public final class JDBCResourceConfigTest
        extends ResourceConfigTestBase {
    private static final String JDBC_RESOURCE_POOL_NAME_BASE = "JDBCResourceConfigMgrTest.test-pool";
    private static final String JDBC_DATASOURCE_CLASSNAME = "com.pointbase.xa.xaDataSource";
    private static final Map<String, String> OPTIONAL = null;

    private JDBCConnectionPoolConfig mPool;

    public JDBCResourceConfigTest() {
        mPool = null;
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getDomainConfig());
        }
    }

    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("JDBCResourceConfig");
    }

    public static JDBCResourceConfig
    ensureDefaultInstance(final DomainConfig domainConfig) {
        JDBCResourceConfig result =
                domainConfig.getResourcesConfig().getJDBCResourceConfigMap().get(getDefaultInstanceName());

        if (result == null) {
            final JDBCConnectionPoolConfig pool =
                    JDBCConnectionPoolConfigTest.ensureDefaultInstance(domainConfig);

            result = createInstance(domainConfig,
                                    getDefaultInstanceName(), pool.getName(), OPTIONAL);
        }

        return result;
    }

    public static JDBCResourceConfig
    createInstance(
            final DomainConfig domainConfig,
            final String name,
            final String datasourceClassname,
            final Map<String, String> optional) {
        return domainConfig.getResourcesConfig().createJDBCResourceConfig(
                name, datasourceClassname, optional);
    }


    protected Container
    getProgenyContainer() {
        return getDomainConfig();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.JDBC_RESOURCE_CONFIG;
    }


    protected void
    removeProgeny(final String name) {
        getDomainConfig().getResourcesConfig().removeJDBCConnectionPoolConfig(name);
    }

    protected String
    getProgenyTestName() {
        return ("jdbc/JDBCResourceConfigMgrTest");
    }

    private JDBCConnectionPoolConfig
    createPool(final String name) {
        try {
            getDomainConfig().getResourcesConfig().removeJDBCConnectionPoolConfig(name);
        }
        catch (Exception e) {
        }

        final JDBCConnectionPoolConfig config =
                getDomainConfig().getResourcesConfig().createJDBCConnectionPoolConfig(name, JDBC_DATASOURCE_CLASSNAME, null);

        return (config);
    }

    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> options) {
        mPool = createPool(name + "-temppool");

        final JDBCResourceConfig config =
                getDomainConfig().getResourcesConfig().createJDBCResourceConfig(name, mPool.getName(), options);
        assert (config != null);

        addReference(config);

        return (config);
    }

    protected final void
    remove(String name) {
        getDomainConfig().getResourcesConfig().removeJDBCResourceConfig(name);

        if (mPool != null) {
            getDomainConfig().getResourcesConfig().removeJDBCConnectionPoolConfig(mPool.getName());
            mPool = null;
        }
    }

}


