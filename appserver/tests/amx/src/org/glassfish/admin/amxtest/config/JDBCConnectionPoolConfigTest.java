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
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/config/JDBCConnectionPoolConfigTest.java,v 1.6 2007/05/05 05:23:54 tcfujii Exp $
* $Revision: 1.6 $
* $Date: 2007/05/05 05:23:54 $
*/
package org.glassfish.admin.amxtest.config;

import com.sun.appserv.management.base.Container;
import com.sun.appserv.management.base.XTypes;
import com.sun.appserv.management.config.AMXConfig;
import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.JDBCConnectionPoolConfig;

import java.util.Map;

/**
 */
public final class JDBCConnectionPoolConfigTest
        extends ConfigMgrTestBase {
    private static final String JDBC_DATASOURCE_CLASSNAME = "com.pointbase.xa.xaDataSource";


    public JDBCConnectionPoolConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getDomainConfig());
        }
    }

    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("JDBCConnectionPoolConfig");
    }

    public static JDBCConnectionPoolConfig
    ensureDefaultInstance(final DomainConfig domainConfig) {
        JDBCConnectionPoolConfig result =
                domainConfig.getResourcesConfig().getJDBCConnectionPoolConfigMap().get(
                        getDefaultInstanceName());

        if (result == null) {
            result = createInstance(domainConfig,
                                    getDefaultInstanceName(), JDBC_DATASOURCE_CLASSNAME, null);
        }

        return result;
    }

    public static JDBCConnectionPoolConfig
    createInstance(
            final DomainConfig domainConfig,
            final String name,
            final String datasourceClassname,
            final Map<String, String> optional) {
        return domainConfig.getResourcesConfig().createJDBCConnectionPoolConfig(
                name, datasourceClassname, optional);
    }


    protected Container
    getProgenyContainer() {
        return getDomainConfig();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.JDBC_CONNECTION_POOL_CONFIG;
    }


    protected void
    removeProgeny(final String name) {
        getDomainConfig().getResourcesConfig().removeJDBCConnectionPoolConfig(name);
    }

    protected final AMXConfig
    createProgeny(
            String name,
            Map<String, String> options) {
        final JDBCConnectionPoolConfig config =
                getDomainConfig().getResourcesConfig().createJDBCConnectionPoolConfig(name,
                                                                 JDBC_DATASOURCE_CLASSNAME,
                                                                 options);
        assert (config != null);
        return (config);
    }
}


