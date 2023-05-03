/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2015, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.jvnet.hk2.config;

import jakarta.inject.Singleton;

import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.HK2Loader;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.api.InstanceLifecycleListener;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.DescriptorImpl;

/**
 * Utilities for working with HK2 config
 *
 * @author jwells
 *
 */
public class HK2DomConfigUtilities {
    /**
     * This method enables HK2 Dom based XML configuration parsing for
     * systems that do not use HK2 metadata files or use a non-default
     * name for HK2 metadata files.  This method is idempotent, so that
     * if the services already are available in the locator they will
     * not get added again
     *
     * @param locator The non-null locator to add the hk2 dom based
     * configuration services to
     */
    public static void enableHK2DomConfiguration(ServiceLocator locator, HK2Loader loader) {
        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        boolean dirty = false;
        boolean operationDirty;

        operationDirty = addIfNotThere(locator, config, getConfigSupport(), loader);
        dirty = dirty || operationDirty;

        operationDirty = addIfNotThere(locator, config, getConfigurationPopulator(), loader);
        dirty = dirty || operationDirty;

        operationDirty = addIfNotThere(locator, config, getTransactions(), loader);
        dirty = dirty || operationDirty;

        operationDirty = addIfNotThere(locator, config, getConfigInstanceListener(), loader);
        dirty = dirty || operationDirty;

        if (dirty) {
            config.commit();
        }

    }


    /**
     * This method enables HK2 Dom based XML configuration parsing for
     * systems that do not use HK2 metadata files or use a non-default
     * name for HK2 metadata files.  This method is idempotent, so that
     * if the services already are available in the locator they will
     * not get added again
     *
     * @param locator The non-null locator to add the hk2 dom based
     * configuration services to
     */
    public static void enableHK2DomConfiguration(ServiceLocator locator) {
        enableHK2DomConfiguration(locator, null);
    }

    private final static String CONFIG_SUPPORT_IMPL = "org.jvnet.hk2.config.ConfigSupport";
    private final static String CONFIGURATION_UTILITIES = "org.jvnet.hk2.config.api.ConfigurationUtilities";
    private static DescriptorImpl getConfigSupport() {
        return BuilderHelper.link(CONFIG_SUPPORT_IMPL).
            to(CONFIGURATION_UTILITIES).
            in(Singleton.class.getName()).build();
    }

    private final static String CONFIGURATION_POPULATOR_IMPL = "org.jvnet.hk2.config.ConfigurationPopulator";
    private final static String CONFIG_POPULATOR = "org.glassfish.hk2.bootstrap.ConfigPopulator";
    private static DescriptorImpl getConfigurationPopulator() {
        return BuilderHelper.link(CONFIGURATION_POPULATOR_IMPL).
            to(CONFIG_POPULATOR).
            in(Singleton.class.getName()).build();
    }

    private final static String TRANSACTIONS_IMPL = "org.jvnet.hk2.config.Transactions";
    private static DescriptorImpl getTransactions() {
        return BuilderHelper.link(TRANSACTIONS_IMPL).
            in(Singleton.class.getName()).build();
    }

    private final static String CONFIG_INSTANCE_LISTENER_IMPL = "org.jvnet.hk2.config.provider.internal.ConfigInstanceListener";
    private static DescriptorImpl getConfigInstanceListener() {
        return BuilderHelper.link(CONFIG_INSTANCE_LISTENER_IMPL).
            to(InstanceLifecycleListener.class.getName()).
            in(Singleton.class.getName()).build();
    }

    private static boolean addIfNotThere(ServiceLocator locator, DynamicConfiguration config, DescriptorImpl desc, HK2Loader loader) {
        IndexedFilter filter = BuilderHelper.createContractFilter(desc.getImplementation());
        if (locator.getBestDescriptor(filter) != null) {
            return false;
        }

        if (loader != null) {
            desc.setLoader(loader);
        }
        config.bind(desc);
        return true;
    }

}
