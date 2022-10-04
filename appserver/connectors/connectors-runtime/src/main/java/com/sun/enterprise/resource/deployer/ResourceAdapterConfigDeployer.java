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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.connectors.config.ResourceAdapterConfig;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;

/**
 * @author Srikanth P
 */

@Service
@ResourceDeployerInfo(ResourceAdapterConfig.class)
@Singleton
public class ResourceAdapterConfigDeployer extends AbstractConnectorResourceDeployer {

    @Inject
    private Provider<ConnectorRuntime> connectorRuntimeProvider;

    private static Logger _logger = LogDomains.getLogger(ResourceAdapterConfigDeployer.class, LogDomains.RSR_LOGGER);

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void deployResource(Object resource, String applicationName, String moduleName)
            throws Exception {
        deployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void deployResource(Object resource) throws Exception {

        ResourceAdapterConfig domainConfig =
                (ResourceAdapterConfig) resource;
        String rarName = domainConfig.getResourceAdapterName();
        ConnectorRuntime crt = getConnectorRuntime();
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,
                    "Calling backend to add resource adapterConfig ", rarName);
        }
        crt.addResourceAdapterConfig(rarName, domainConfig);
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE,
                    "Added resource adapterConfig in backend", rarName);
        }
    }

    private ConnectorRuntime getConnectorRuntime() {
        return connectorRuntimeProvider.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void undeployResource(Object resource, String applicationName, String moduleName) throws Exception{
        undeployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void undeployResource(Object resource)
            throws Exception {
        ResourceAdapterConfig domainConfig =
                (ResourceAdapterConfig) resource;
        String rarName = domainConfig.getResourceAdapterName();
        ConnectorRuntime crt = getConnectorRuntime();
        crt.deleteResourceAdapterConfig(rarName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void redeployResource(Object resource)
            throws Exception {
        deployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean handles(Object resource) {
        boolean canHandle = false;
        if (resource instanceof ResourceAdapterConfig) {
            canHandle = true;
        }
        return canHandle;
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean supportsDynamicReconfiguration() {
        return false;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Class[] getProxyClassesForDynamicReconfiguration() {
        return new Class[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void disableResource(Object resource)
            throws Exception {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void enableResource(Object resource) throws Exception {
    }
}
