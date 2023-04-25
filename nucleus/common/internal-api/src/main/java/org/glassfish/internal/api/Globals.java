/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.internal.api;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.config.serverbeans.ConfigBeansUtilities;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.single.StaticModulesRegistry;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Very sensitive class, anything stored here cannot be garbage collected
 *
 * @author Jerome Dochez
 */
@Service(name = "globals")
@Singleton
public class Globals {

    private static volatile ServiceLocator defaultServiceLocator;

    private static Object staticLock = new Object();

    // dochez : remove this once we can get rid of ConfigBeanUtilities class
    @Inject
    private ConfigBeansUtilities utilities;

    @Inject
    private Globals(ServiceLocator serviceLocator) {
        defaultServiceLocator = serviceLocator;
    }

    public static ServiceLocator getDefaultBaseServiceLocator() {
        return getDefaultHabitat();
    }

    public static ServiceLocator getDefaultHabitat() {
        return defaultServiceLocator;
    }

    public static <T> T get(Class<T> type) {
        return defaultServiceLocator.getService(type);
    }

    public static void setDefaultHabitat(final ServiceLocator habitat) {
        defaultServiceLocator = habitat;
    }

    public static ServiceLocator getStaticBaseServiceLocator() {
        return getStaticHabitat();
    }

    public static ServiceLocator getStaticHabitat() {
        if (defaultServiceLocator == null) {
            synchronized (staticLock) {
                if (defaultServiceLocator == null) {
                    ModulesRegistry modulesRegistry = new StaticModulesRegistry(Globals.class.getClassLoader());
                    defaultServiceLocator = modulesRegistry.createServiceLocator("default");
                }
            }
        }

        return defaultServiceLocator;
    }

    /**
     * The point of this service is to ensure that the Globals service is properly initialized by the RunLevelService at the
     * InitRunLevel. However, Globals itself must be of scope Singleton because it us used in contexts where the
     * RunLevelService is not there
     *
     * @author jwells
     *
     */
    @Service
    @RunLevel(value = (InitRunLevel.VAL - 1), mode = RunLevel.RUNLEVEL_MODE_NON_VALIDATING)
    public static class GlobalsInitializer {
        @Inject
        private Globals globals;
    }
}
