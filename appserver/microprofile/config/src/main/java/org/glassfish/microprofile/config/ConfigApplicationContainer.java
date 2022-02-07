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

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.deployment.ApplicationContext;
import org.glassfish.api.deployment.DeploymentContext;

public class ConfigApplicationContainer implements ApplicationContainer<Object> {

    private final DeploymentContext deploymentContext;

    public ConfigApplicationContainer(DeploymentContext deploymentContext) {
        this.deploymentContext = deploymentContext;
    }

    @Override
    public Object getDescriptor() {
        return null;
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
        return false;
    }

    @Override
    public boolean resume() throws Exception {
        return false;
    }

    @Override
    public ClassLoader getClassLoader() {
        return deploymentContext.getFinalClassLoader();
    }

}
