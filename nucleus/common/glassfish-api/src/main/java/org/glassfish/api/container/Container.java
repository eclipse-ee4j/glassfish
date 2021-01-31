/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.api.container;

import org.jvnet.hk2.annotations.Contract;
import org.glassfish.api.deployment.Deployer;

/**
 * Contract identifying a container implementation.
 *
 * Usually the names of the container should be specific enough to ensure uniqueness. In most cases, it is recommended
 * to use the full class name as the @Service name attribute to ensure that two containers do no collide.
 *
 * @author Jerome Dochez
 */
@Contract
public interface Container {

    /**
     * Returns the Deployer implementation capable of deploying applications to this container.
     *
     * @return the Deployer implementation
     */
    public Class<? extends Deployer> getDeployer();

    /**
     * Returns a human redeable name for this container, this name is not used for identifying the container but can be used
     * to display messages belonging to the container.
     * 
     * @return a human readable name for this container.
     */
    public String getName();
}
