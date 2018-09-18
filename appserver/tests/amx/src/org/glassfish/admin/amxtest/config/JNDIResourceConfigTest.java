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
import com.sun.appserv.management.config.JNDIResourceConfig;

import java.util.Map;

/**
 */
public final class JNDIResourceConfigTest
        extends ResourceConfigTestBase {
    private static final String JNDI_RESOURCE_JNDI_LOOKUP_NAME = "jndi/jndiTest";
    private static final String JNDI_RESOURCE_RES_TYPE = "javax.sql.DataSource";
    private static final String JNDI_RESOURCE_FACTORY_CLASS = "com.sun.jdo.spi.persistence.support.sqlstore.impl.PersistenceManagerFactoryImpl";
    private static final Map<String, String> OPTIONAL = null;

    public JNDIResourceConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getDomainConfig());
        }
    }

    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("JNDIResourceConfig");
    }

    public static JNDIResourceConfig
    ensureDefaultInstance(final DomainConfig dc) {
        JNDIResourceConfig result =
                dc.getResourcesConfig().getJNDIResourceConfigMap().get(getDefaultInstanceName());

        if (result == null) {
            result = createInstance(dc,
                                    getDefaultInstanceName(),
                                    JNDI_RESOURCE_JNDI_LOOKUP_NAME,
                                    JNDI_RESOURCE_RES_TYPE,
                                    JNDI_RESOURCE_FACTORY_CLASS,
                                    OPTIONAL);
        }

        return result;
    }

    public static JNDIResourceConfig
    createInstance(
            final DomainConfig dc,
            final String name,
            final String jndiLookupName,
            final String resType,
            final String factoryClass,
            final Map<String, String> optional) {
        return dc.getResourcesConfig().createJNDIResourceConfig(
                name, jndiLookupName, resType, factoryClass, optional);
    }


    protected Container
    getProgenyContainer() {
        return getDomainConfig();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.JNDI_RESOURCE_CONFIG;
    }


    protected void
    removeProgeny(final String name) {
        final JNDIResourceConfig item =
                getDomainConfig().getResourcesConfig().getJNDIResourceConfigMap().get(name);

        getDomainConfig().getResourcesConfig().removeJNDIResourceConfig(name);
    }

    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> options) {
        final JNDIResourceConfig config = getDomainConfig().getResourcesConfig().createJNDIResourceConfig(name,
                                                                                     JNDI_RESOURCE_JNDI_LOOKUP_NAME,
                                                                                     JNDI_RESOURCE_RES_TYPE,
                                                                                     JNDI_RESOURCE_FACTORY_CLASS,
                                                                                     options);

        addReference(config);

        return (config);
    }
}


