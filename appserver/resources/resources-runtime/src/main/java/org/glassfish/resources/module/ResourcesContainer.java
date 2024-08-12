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

package org.glassfish.resources.module;

import com.sun.logging.LogDomains;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.resourcebase.resources.api.ResourceConstants;
import org.jvnet.hk2.annotations.Service;


@Service(name = "org.glassfish.resources.module.ResourcesContainer")
public class ResourcesContainer implements Container, PostConstruct, PreDestroy {

    private final static Logger _logger = LogDomains.getLogger(ResourcesContainer.class, LogDomains.RSR_LOGGER);

    public void postConstruct() {
        logFine("postConstruct of ConnectorContainer");
    }

    public void preDestroy() {
        logFine("preDestroy of ConnectorContainer");
    }

    /**
     * Returns the Deployer implementation capable of deploying applications to this
     * container.
     *
     * @return the Deployer implementation
     */
    public Class<? extends Deployer> getDeployer() {
        return ResourcesDeployer.class;
    }

    /**
     * Returns a human readable name for this container, this name is not used for
     * identifying the container but can be used to display messages belonging to
     * the container.
     *
     * @return a human readable name for this container.
     */
    public String getName() {
        return ResourceConstants.GF_RESOURCES_MODULE;
    }

    public void logFine(String message) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, message);
        }
    }

}
