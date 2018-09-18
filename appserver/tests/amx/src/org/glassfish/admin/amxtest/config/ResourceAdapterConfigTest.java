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
* $Header: /cvs/glassfish/admin/mbeanapi-impl/tests/org.glassfish.admin.amxtest/config/ResourceAdapterConfigTest.java,v 1.5 2007/05/05 05:23:55 tcfujii Exp $
* $Revision: 1.5 $
* $Date: 2007/05/05 05:23:55 $
*/
package org.glassfish.admin.amxtest.config;


import com.sun.appserv.management.config.DomainConfig;
import com.sun.appserv.management.config.ResourceAdapterConfig;
import org.glassfish.admin.amxtest.AMXTestBase;


/**
 */
public final class ResourceAdapterConfigTest
        extends AMXTestBase {
    public ResourceAdapterConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getDomainConfig());
        }
    }

    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("ResourceAdapterConfig");
    }

    public static ResourceAdapterConfig
    ensureDefaultInstance(final DomainConfig domainConfig) {
        ResourceAdapterConfig result =
                domainConfig.getResourcesConfig().getResourceAdapterConfigMap().get(getDefaultInstanceName());

        if (result == null) {
            result = createInstance(domainConfig, getDefaultInstanceName());
        }

        return result;
    }

    public static ResourceAdapterConfig
    createInstance(
            final DomainConfig domainConfig,
            final String name) {
        final ResourceAdapterConfig rac =
                domainConfig.getResourcesConfig().createResourceAdapterConfig(name, null);

        return rac;
    }

    private void
    testGetters(final ResourceAdapterConfig resAdapterConfig) {
        resAdapterConfig.getResourceAdapterName();
        resAdapterConfig.getThreadPoolIDs();
    }

    public void
    testCreateRemove() {
        if (checkNotOffline("testCreateSSL")) {
            final ResourceAdapterConfig resAdapterConfig =
                    createInstance(getDomainConfig(),
                                   "ResourceAdapterConfigTest.testCreateRemove");

            try {
                testGetters(resAdapterConfig);
            }
            finally {
                getDomainConfig().getResourcesConfig().removeResourceAdapterConfig(resAdapterConfig.getName());
            }
        }
    }

}



























