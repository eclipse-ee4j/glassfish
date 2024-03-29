/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation
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

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.connectors.config.ConnectorResource;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.jvnet.hk2.annotations.Service;

import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getPMJndiName;
import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getResourceInfo;
import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getValidSuffix;

/**
 * @author Srikanth P
 */
@Service
@Singleton
@ResourceDeployerInfo(ConnectorResource.class)
public class ConnectorResourceDeployer extends AbstractConnectorResourceDeployer<ConnectorResource> {

    private static Logger _logger = LogDomains.getLogger(ConnectorResourceDeployer.class, LogDomains.RSR_LOGGER);

    @Inject
    private ConnectorRuntime runtime;

    @Override
    public synchronized void deployResource(ConnectorResource resource, String applicationName, String moduleName) throws Exception {
        // DeployResource is not synchronized as there is only one caller
        // ResourceProxy which is synchronized
        ResourceInfo resourceInfo = new ResourceInfo(new SimpleJndiName(resource.getJndiName()), applicationName, moduleName);
        PoolInfo poolInfo = new PoolInfo(new SimpleJndiName(resource.getPoolName()), applicationName, moduleName);
        createConnectorResource(resource, resourceInfo, poolInfo);
    }


    @Override
    public void deployResource(ConnectorResource resource) throws Exception {
        // DeployResource is not synchronized as there is only one caller
        // ResourceProxy which is synchronized
        SimpleJndiName poolName = new SimpleJndiName(resource.getPoolName());
        ResourceInfo resourceInfo = getResourceInfo(resource);
        PoolInfo poolInfo = new PoolInfo(poolName, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
        createConnectorResource(resource, resourceInfo, poolInfo);
    }

    private void createConnectorResource(ConnectorResource connectorResource, ResourceInfo resourceInfo, PoolInfo poolInfo) throws ConnectorRuntimeException {
        _logger.log(Level.FINE, "Calling backend to add connector resource: {0}", resourceInfo);

        runtime.createConnectorResource(resourceInfo, poolInfo, null);
        SimpleJndiName jndiName = resourceInfo.getName();
        // In-case the resource is explicitly created with a suffix (__nontx or __PM), no need to create one
        if (getValidSuffix(jndiName) == null) {
            ResourceInfo pmResourceInfo = new ResourceInfo(getPMJndiName(jndiName), resourceInfo.getApplicationName(),
                resourceInfo.getModuleName());
            runtime.createConnectorResource(pmResourceInfo, poolInfo, null);
        }
        _logger.log(Level.FINE, "Added connector resource in backend: {0}", resourceInfo);
    }


    @Override
    public void undeployResource(ConnectorResource resource, String applicationName, String moduleName) throws Exception {
        ResourceInfo resourceInfo = new ResourceInfo(new SimpleJndiName(resource.getJndiName()), applicationName, moduleName);
        deleteConnectorResource(resource, resourceInfo);
    }


    @Override
    public synchronized void undeployResource(ConnectorResource resource) throws Exception {
        deleteConnectorResource(resource, getResourceInfo(resource));
    }

    private void deleteConnectorResource(ConnectorResource connectorResource, ResourceInfo resourceInfo) throws Exception {

        runtime.deleteConnectorResource(resourceInfo);
        // In-case the resource is explicitly created with a suffix (__nontx or __PM), no need to delete one
        if (getValidSuffix(resourceInfo.getName()) == null) {
            SimpleJndiName pmJndiName = getPMJndiName(resourceInfo.getName());
            ResourceInfo pmResourceInfo = new ResourceInfo(pmJndiName, resourceInfo.getApplicationName(),
                    resourceInfo.getModuleName());

            runtime.deleteConnectorResource(pmResourceInfo);
        }

        // If no more resource-ref to the pool of this resource in this server instance, remove pool
        // from connector runtime
        checkAndDeletePool(connectorResource);
    }


    @Override
    public synchronized void disableResource(ConnectorResource resource) throws Exception {
        undeployResource(resource);
    }


    @Override
    public synchronized void enableResource(ConnectorResource resource) throws Exception {
        deployResource(resource);
    }


    @Override
    public boolean handles(Object resource) {
        return resource instanceof ConnectorResource;
    }


    /**
     * Checks if no more resource-refs to resources exists for the
     * connector connection pool and then deletes the pool
     *
     * @param connectorResource ConnectorResource
     * @throws Exception (ConfigException / undeploy exception)
     * @since 8.1 pe/se/ee
     */
    private void checkAndDeletePool(ConnectorResource connectorResource) throws Exception {
        SimpleJndiName poolName = new SimpleJndiName(connectorResource.getPoolName());
        ResourceInfo resourceInfo = getResourceInfo(connectorResource);
        PoolInfo poolInfo = new PoolInfo(poolName, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
        Resources resources = (Resources) connectorResource.getParent();

        // It's possible that the ConnectorResource here is a ConnectorResourceeDefinition. Ignore optimization.
        if (resources != null) {
            try {
                boolean poolReferred = ResourcesUtil.createInstance().isPoolReferredInServerInstance(poolInfo);
                if (!poolReferred) {
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.fine("Deleting connector connection pool [" + poolName + "] as there are no more " +
                                "resource-refs to the pool in this server instance");
                    }

                    ConnectorConnectionPool pool = resources.getResourceByName(ConnectorConnectionPool.class, poolName);
                    runtime.getResourceDeployer(pool).undeployResource(pool);
                }
            } catch (Exception ce) {
                _logger.warning(ce.getMessage());
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine("Exception while deleting pool [ " + poolName + " ] : " + ce);
                }

                throw ce;
            }
        }
    }
}
