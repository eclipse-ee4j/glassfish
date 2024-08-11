/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.webservices;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.container.common.spi.util.InjectionManager;
import com.sun.enterprise.module.ModulesRegistry;

import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;

import java.util.Collections;
import java.util.logging.Logger;

import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.container.Adapter;
import org.glassfish.api.invocation.InvocationManager;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.internal.api.Globals;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

/**
 * This is the implementation class which will provide the implementation
 * to access the injected fields like the NamingManager , ComponentEnvManager
 */
@Service
public class WebServiceContractImpl implements WebServicesContract{

    @Inject
    private ComponentEnvManager compEnvManager;

    @Inject
    private ServerEnvironmentImpl env;

    @Inject
    private ModulesRegistry modulesRegistry;

    @Inject
    private InvocationManager invManager;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME) @Optional
    private Provider<Config> configProvider;

    @Inject @Optional
    private Provider<ApplicationRegistry> applicationRegistryProvider;

    @Inject @Optional
    private IterableProvider<Adapter> adapters;

    @Inject @Optional
    private Provider<InjectionManager> injectionManagerProvider;

    private  static WebServiceContractImpl wscImpl;

    private static final Logger logger = LogUtils.getLogger();

    public ComponentEnvManager getComponentEnvManager() {
        return compEnvManager;
    }

    public Config getConfig() {
        return configProvider.get();
    }

    public InvocationManager getInvocationManager() {
            return invManager;
    }

    public ServerEnvironmentImpl getServerEnvironmentImpl (){
        return env;
    }

    public ModulesRegistry getModulesRegistry (){
            return modulesRegistry;
    }

    public static WebServiceContractImpl getInstance() {
        // Create the instance first to access the logger.
        wscImpl = Globals.getDefaultHabitat().getService(
                WebServiceContractImpl.class);
        return wscImpl;
    }

    public Logger getLogger() {
        return logger;
    }

    public ApplicationRegistry getApplicationRegistry() {
        return applicationRegistryProvider.get();
    }

    public ServerEnvironment getServerEnvironment() {
        return env;
    }

    public Iterable<Adapter> getAdapters() {
        return (adapters != null) ? adapters : Collections.EMPTY_LIST;
    }

    public InjectionManager getInjectionManager() {
        return injectionManagerProvider.get();
    }
}
