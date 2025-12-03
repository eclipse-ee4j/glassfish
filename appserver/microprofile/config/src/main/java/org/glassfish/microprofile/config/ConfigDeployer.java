/*
 * Copyright (c) 2022 Contributors to Eclipse Foundation.
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
package org.glassfish.microprofile.config;

import io.helidon.microprofile.config.ConfigCdiExtension;

import java.util.ServiceLoader;

import org.eclipse.microprofile.config.spi.ConfigProviderResolver;
import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.weld.DeploymentImpl;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.weld.WeldDeployer.WELD_DEPLOYMENT;

@Service
public class ConfigDeployer implements Deployer {

    @Override
    public MetaData getMetaData() {
        return null;
    }

    @Override
    public boolean prepare(DeploymentContext deploymentContext) {
        return true;
    }

    @Override
    public ApplicationContainer load(Container container, DeploymentContext deploymentContext) {

        // Initialise Config providers
        final var resolver = ServiceLoader.load(ConfigProviderResolver.class).iterator().next();
        ConfigProviderResolver.setInstance(resolver);

        enableCdiExtensions(deploymentContext);

        return new ConfigApplicationContainer(deploymentContext);
    }

    @Override
    public void unload(ApplicationContainer applicationContainer, DeploymentContext deploymentContext) {}

    @Override
    public void clean(DeploymentContext deploymentContext) {}

    @Override
    public Object loadMetaData(Class aClass, DeploymentContext deploymentContext) {
        return null;
    }

    private void enableCdiExtensions(DeploymentContext context) {
        DeploymentImpl deploymentImpl = context.getTransientAppMetaData(WELD_DEPLOYMENT, DeploymentImpl.class);
        deploymentImpl.addExtensions(new ConfigCdiExtension());
    }
}
