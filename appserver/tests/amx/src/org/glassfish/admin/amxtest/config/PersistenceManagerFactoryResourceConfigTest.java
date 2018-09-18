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
import com.sun.appserv.management.config.PersistenceManagerFactoryResourceConfig;

import java.util.Map;

/**
 */
public final class PersistenceManagerFactoryResourceConfigTest
        extends ResourceConfigTestBase {
    public PersistenceManagerFactoryResourceConfigTest() {
        if (checkNotOffline("ensureDefaultInstance")) {
            ensureDefaultInstance(getDomainConfig());
        }
    }

    public static String
    getDefaultInstanceName() {
        return getDefaultInstanceName("PersistenceManagerFactoryResourceConfig");
    }

    public static PersistenceManagerFactoryResourceConfig
    ensureDefaultInstance(final DomainConfig dc) {
        PersistenceManagerFactoryResourceConfig result =
                dc.getResourcesConfig().getPersistenceManagerFactoryResourceConfigMap().get(getDefaultInstanceName());

        if (result == null) {
            result = createInstance(dc,
                                    getDefaultInstanceName(),
                                    null);
        }

        return result;
    }

    public static PersistenceManagerFactoryResourceConfig
    createInstance(
            final DomainConfig dc,
            final String name,
            final Map<String, String> optional) {
        return dc.getResourcesConfig().createPersistenceManagerFactoryResourceConfig(
                name, optional);
    }


    protected Container
    getProgenyContainer() {
        return getDomainConfig();
    }

    protected String
    getProgenyJ2EEType() {
        return XTypes.PERSISTENCE_MANAGER_FACTORY_RESOURCE_CONFIG;
    }


    protected void
    removeProgeny(final String name) {
        getDomainConfig().getResourcesConfig().removePersistenceManagerFactoryResourceConfig(name);
    }

    protected final AMXConfig
    createProgeny(
            final String name,
            final Map<String, String> options) {
        final PersistenceManagerFactoryResourceConfig config =
                getDomainConfig().getResourcesConfig().createPersistenceManagerFactoryResourceConfig(name, options);

        addReference(config);

        return (config);
    }
}


