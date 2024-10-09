/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.main.boot.embedded;

import com.sun.enterprise.glassfish.bootstrap.log.LogFacade;
import com.sun.enterprise.module.ModuleDefinition;
import com.sun.enterprise.module.ModulesRegistry;
import com.sun.enterprise.module.common_impl.AbstractFactory;
import com.sun.enterprise.module.common_impl.ModuleId;
import com.sun.enterprise.module.single.SingleModulesRegistry;

/**
 * Factory which provides SingleModulesRegistry
 * This should actually be in HK2 workspace.
 *
 * @author bhavanishankar@dev.java.net
 */
class SingleHK2Factory extends AbstractFactory {

    private final ClassLoader classloader;
    private ModulesRegistry modulesRegistry;

    public static synchronized void initialize(ClassLoader classloader) {
        if (Instance != null) {
            LogFacade.BOOTSTRAP_LOGGER.finer(() -> "Singleton already initialized as " + getInstance());
        }
        Instance = new SingleHK2Factory(classloader);
        LogFacade.BOOTSTRAP_LOGGER.finer(() -> "Reinitialized singleton as " + getInstance());
    }

    SingleHK2Factory(ClassLoader classloader) {
        this.classloader = classloader;
        this.modulesRegistry = new SingleModulesRegistry(classloader);
    }

    @Override
    public ModulesRegistry createModulesRegistry() {
        return modulesRegistry == null ? modulesRegistry = new SingleModulesRegistry(classloader) : modulesRegistry;
    }

    @Override
    public ModuleId createModuleId(String name, String version) {
        return new ModuleId(name);
    }

    @Override
    public ModuleId createModuleId(ModuleDefinition md) {
        return new ModuleId(md.getName());
    }
}
