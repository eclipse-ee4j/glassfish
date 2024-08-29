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

package com.sun.enterprise.glassfish.bootstrap;

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

public class SingleHK2Factory extends AbstractFactory {

    ClassLoader cl;
    ModulesRegistry modulesRegistry;

    public static synchronized void initialize(ClassLoader cl) {
        if (Instance != null) {
            LogFacade.BOOTSTRAP_LOGGER.finer(() -> "Singleton already initialized as " + getInstance());
        }
        Instance = new SingleHK2Factory(cl);
        LogFacade.BOOTSTRAP_LOGGER.finer(() -> "Reinitialized singleton as " + getInstance());
    }

    public SingleHK2Factory(ClassLoader cl) {
        this.cl = cl;
        this.modulesRegistry = new SingleModulesRegistry(cl);
    }

    @Override
    public ModulesRegistry createModulesRegistry() {
        return modulesRegistry == null ? modulesRegistry = new SingleModulesRegistry(cl) : modulesRegistry;
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
