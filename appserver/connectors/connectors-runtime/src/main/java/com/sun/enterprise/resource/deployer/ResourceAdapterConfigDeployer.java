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

package com.sun.enterprise.resource.deployer;

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.connectors.config.ResourceAdapterConfig;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Srikanth P
 */

@Service
@Singleton
@ResourceDeployerInfo(ResourceAdapterConfig.class)
public class ResourceAdapterConfigDeployer extends AbstractConnectorResourceDeployer<ResourceAdapterConfig> {

    private static final Logger LOG = LogDomains.getLogger(ResourceAdapterConfigDeployer.class, LogDomains.RSR_LOGGER);

    @Inject
    private Provider<ConnectorRuntime> connectorRuntimeProvider;



    @Override
    public synchronized void deployResource(ResourceAdapterConfig resource, String applicationName, String moduleName)
            throws Exception {
        deployResource(resource);
    }


    @Override
    public synchronized void deployResource(ResourceAdapterConfig resource) throws Exception {
        String rarName = resource.getResourceAdapterName();
        ConnectorRuntime crt = getConnectorRuntime();
        LOG.log(Level.FINE, "Calling backend to add resource adapterConfig ", rarName);
        crt.addResourceAdapterConfig(rarName, resource);
        LOG.log(Level.FINE, "Added resource adapterConfig in backend", rarName);
    }


    private ConnectorRuntime getConnectorRuntime() {
        return connectorRuntimeProvider.get();
    }


    @Override
    public void undeployResource(ResourceAdapterConfig resource, String applicationName, String moduleName)
        throws Exception {
        undeployResource(resource);
    }


    @Override
    public synchronized void undeployResource(ResourceAdapterConfig resource) throws Exception {
        ResourceAdapterConfig domainConfig = resource;
        String rarName = domainConfig.getResourceAdapterName();
        ConnectorRuntime crt = getConnectorRuntime();
        crt.deleteResourceAdapterConfig(rarName);
    }


    @Override
    public boolean handles(Object resource) {
        return resource instanceof ResourceAdapterConfig;
    }


    @Override
    public synchronized void disableResource(ResourceAdapterConfig resource) throws Exception {
    }


    @Override
    public synchronized void enableResource(ResourceAdapterConfig resource) throws Exception {
    }
}
