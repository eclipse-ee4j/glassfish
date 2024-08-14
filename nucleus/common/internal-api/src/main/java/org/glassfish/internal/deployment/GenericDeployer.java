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

package org.glassfish.internal.deployment;

import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.jvnet.hk2.annotations.Service;

/**
 * Generic implementation of the deployer contract, enough to get started with adding a container to
 * GlassFish.
 *
 * @author Jerome Dochez
 */
@Service
public class GenericDeployer<T extends Container> implements Deployer<T, GenericApplicationContainer> {

    public MetaData getMetaData() {
        return null;
    }

    public <V> V loadMetaData(Class<V> type, DeploymentContext context) {
        return null;
    }

    public boolean prepare(DeploymentContext context) {
        return true;
    }

    public GenericApplicationContainer load(T container, DeploymentContext context) {
        return new GenericApplicationContainer(context.getFinalClassLoader());
    }

    public void unload(GenericApplicationContainer appContainer, DeploymentContext context) {
    }

    public void clean(DeploymentContext context) {
    }
}
