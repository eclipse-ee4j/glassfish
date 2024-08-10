/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors.module;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.logging.LogDomains;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.container.Container;
import org.glassfish.api.deployment.Deployer;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.jvnet.hk2.annotations.Service;


/**
 * Represents the container for connector runtime module
 *
 * @author Jagadish Ramu
 */
@Service(name = "com.sun.enterprise.connectors.module.ConnectorContainer")
public class ConnectorContainer implements Container, PostConstruct, PreDestroy {

    private static Logger _logger = LogDomains.getLogger(ConnectorContainer.class, LogDomains.RSR_LOGGER);

    @Override
    public void postConstruct() {
        logFine("postConstruct of ConnectorContainer");
    }

    @Override
    public void preDestroy() {
        logFine("preDestroy of ConnectorContainer");
    }

    /**
     * Returns the Deployer implementation capable of deploying applications to this
     * container.
     *
     * @return the Deployer implementation
     */
    @Override
    public Class<? extends Deployer> getDeployer() {
        return ConnectorDeployer.class;
    }

    /**
     * Returns a human redeable name for this container, this name is not used for
     * identifying the container but can be used to display messages belonging to
     * the container.
     *
     * @return a human readable name for this container.
     */
    @Override
    public String getName() {
        return ConnectorConstants.CONNECTOR_MODULE;
    }

    public void logFine(String message) {
        if(_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, message);
        }
    }
}
