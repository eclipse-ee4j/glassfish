/*
 * Copyright (c) 2023, 2026 Contributors to the Eclipse Foundation
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

package org.glassfish.appclient.server.core;

import com.sun.enterprise.deployment.ApplicationClientDescriptor;

import java.net.URL;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jdke.cl.GlassfishUrlClassLoader;
import org.jvnet.hk2.annotations.Service;

/**
 * Represents an app client module, either stand-alone or nested inside an EAR, loaded on the server.
 * <p>
 * App clients do not run in the server, so this container has nothing to start or stop.
 * It exists so the deployment framework tracks the module and invokes unload on the deployer.
 *
 * @author tjquinn
 */
@Service
@PerLookup
public class AppClientServerApplication implements ApplicationContainer<ApplicationClientDescriptor> {

    private ApplicationClientDescriptor applicationClientDescriptor;

    private String deployedAppName;

    public void init(final DeploymentContext dc, final AppClientDeployerHelper helper) {
        applicationClientDescriptor = helper.appClientDesc();
        deployedAppName = dc.getCommandParameters(DeployCommandParameters.class).name();
    }

    @Override
    public ApplicationClientDescriptor getDescriptor() {
        return applicationClientDescriptor;
    }

    @Override
    public boolean start(ApplicationContext startupContext) throws Exception {
        return true;
    }

    @Override
    public boolean stop(ApplicationContext stopContext) {
        return true;
    }

    @Override
    public boolean suspend() {
        return true;
    }

    @Override
    public boolean resume() throws Exception {
        return true;
    }

    @Override
    public ClassLoader getClassLoader() {
        // This cannot be null or it prevents the framework from invoking unload
        // on the deployer for this app.
        return new GlassfishUrlClassLoader("AppClientServer(" + deployedAppName + ")", new URL[0]);
    }
}
