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
import com.sun.appserv.management.config.AdminObjectResourceConfig;
import com.sun.appserv.management.config.DomainConfig;

import java.util.Map;

/**
 */
public final class AdminObjectResourceConfigTest
        extends ResourceConfigTestBase {
    private static final String ADM_OBJ_RES_TYPE = "user";
    private static final String ADM_OBJ_RES_ADAPTER = "cciblackbox-tx";


    public AdminObjectResourceConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getDomainConfig());
        }
    }

    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("AdminObjectResourceConfig");
    }

    public static AdminObjectResourceConfig
    ensureDefaultInstance(final DomainConfig domainConfig) {
        AdminObjectResourceConfig result =
                domainConfig.getResourcesConfig().getAdminObjectResourceConfigMap().get(getDefaultInstanceName());

        if (result == null) {
            result = createInstance(
                    domainConfig,
                    getDefaultInstanceName(),
                    ADM_OBJ_RES_TYPE,
                    ADM_OBJ_RES_ADAPTER,
                    null);
        }

        return result;
    }

    public static AdminObjectResourceConfig
    createInstance(
            final DomainConfig domainConfig,
            final String name,
            final String resType,
            final String resAdapter,
            final Map<String, String> optional) {
        return domainConfig.getResourcesConfig().createAdminObjectResourceConfig(name,
                                                            resType, resAdapter, optional);
    }


    protected Container
    getProgenyContainer() {
        return getDomainConfig();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.ADMIN_OBJECT_RESOURCE_CONFIG;
    }


    protected void
    removeProgeny(final String name) {
        getDomainConfig().getResourcesConfig().removeAdminObjectResourceConfig(name);
    }

    protected String
    getProgenyTestName() {
        return ("jndi/AdminObjectResourceConfigMgrTest");
    }

    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> options) {
        final AdminObjectResourceConfig config = getDomainConfig().getResourcesConfig().createAdminObjectResourceConfig(
                name,
                ADM_OBJ_RES_TYPE,
                ADM_OBJ_RES_ADAPTER,
                options);

        addReference(config);

        return (config);
    }
}


