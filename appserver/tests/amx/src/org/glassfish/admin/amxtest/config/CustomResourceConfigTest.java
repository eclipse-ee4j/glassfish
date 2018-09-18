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
import com.sun.appserv.management.config.CustomResourceConfig;
import com.sun.appserv.management.config.DomainConfig;

import java.util.HashMap;
import java.util.Map;

/**
 */
public final class CustomResourceConfigTest
        extends ResourceConfigTestBase {
    static final Map<String, String> OPTIONAL = new HashMap<String, String>();

    // doesn't exist, just give a syntactically valid name
    static private final String RES_TYPE = "CustomResourceConfigTest.Dummy";
    static private final String FACTORY_CLASS =
            "org.glassfish.admin.amxtest.config.CustomResourceConfigTestDummy";

    public CustomResourceConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getDomainConfig());
        }
    }

    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("CustomResourceConfig");
    }


    public static CustomResourceConfig
    ensureDefaultInstance(final DomainConfig domainConfig) {
        CustomResourceConfig result =
                domainConfig.getResourcesConfig().getCustomResourceConfigMap().get(getDefaultInstanceName());

        if (result == null) {
            result = createInstance(domainConfig,
                                    getDefaultInstanceName(), RES_TYPE, FACTORY_CLASS, null);
        }

        return result;
    }

    public static CustomResourceConfig
    createInstance(
            final DomainConfig domainConfig,
            final String name,
            final String resType,
            final String factoryClass,
            final Map<String, String> optional) {
        final CustomResourceConfig config =
                domainConfig.getResourcesConfig().createCustomResourceConfig(name, resType, factoryClass, optional);

        return config;
    }


    protected Container
    getProgenyContainer() {
        return getDomainConfig();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.CUSTOM_RESOURCE_CONFIG;
    }


    protected void
    removeProgeny(final String name) {
        getDomainConfig().getResourcesConfig().removeCustomResourceConfig(name);
    }

    protected String
    getProgenyTestName() {
        return ("CustomResourceConfigTest");
    }

    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> options) {
        final CustomResourceConfig config =
                createInstance(getDomainConfig(), name, RES_TYPE, FACTORY_CLASS, options);

        addReference(config);

        return config;
    }
}


