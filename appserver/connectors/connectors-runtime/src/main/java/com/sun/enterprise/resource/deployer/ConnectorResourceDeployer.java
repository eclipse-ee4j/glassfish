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

package com.sun.enterprise.resource.deployer;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.logging.LogDomains;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.connectors.config.ConnectorResource;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Srikanth P
 */
@Service
@ResourceDeployerInfo(ConnectorResource.class)
@Singleton
public class ConnectorResourceDeployer extends AbstractConnectorResourceDeployer {

    @Inject
    private ConnectorRuntime runtime;
    private static Logger _logger = LogDomains.getLogger(ConnectorResourceDeployer.class, LogDomains.RSR_LOGGER);

    /**
     * {@inheritDoc}
     */
    public synchronized void deployResource(Object resource, String applicationName, String moduleName) throws Exception {
        //deployResource is not synchronized as there is only one caller
        //ResourceProxy which is synchronized
        ConnectorResource domainResource = (ConnectorResource) resource;
        ResourceInfo resourceInfo = new ResourceInfo(domainResource.getJndiName(), applicationName, moduleName);
        PoolInfo poolInfo = new PoolInfo(domainResource.getPoolName(), applicationName, moduleName);
        createConnectorResource(domainResource, resourceInfo, poolInfo);
    }

    /**
     * {@inheritDoc}
     */
    public void deployResource(Object resource) throws Exception {
        //deployResource is not synchronized as there is only one caller
        //ResourceProxy which is synchronized
        ConnectorResource domainResource = (ConnectorResource) resource;
        String poolName = domainResource.getPoolName();
        ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(domainResource);
        PoolInfo poolInfo = new PoolInfo(poolName, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
        createConnectorResource(domainResource, resourceInfo, poolInfo);
    }

    private void createConnectorResource(ConnectorResource connectorResource, ResourceInfo resourceInfo,
                                         PoolInfo poolInfo) throws ConnectorRuntimeException {
        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Calling backend to add connector resource",
                    resourceInfo);
        }
        runtime.createConnectorResource(resourceInfo, poolInfo, null);
        String jndiName = resourceInfo.getName();
        //In-case the resource is explicitly created with a suffix (__nontx or __PM), no need to create one
        if (ConnectorsUtil.getValidSuffix(jndiName) == null) {
            ResourceInfo pmResourceInfo = new ResourceInfo(ConnectorsUtil.getPMJndiName(jndiName),
                    resourceInfo.getApplicationName(), resourceInfo.getModuleName());
            runtime.createConnectorResource(pmResourceInfo, poolInfo, null);
        }

        if (_logger.isLoggable(Level.FINE)) {
            _logger.log(Level.FINE, "Added connector resource in backend",
                    resourceInfo);
        }

    }

    /**
     * {@inheritDoc}
     */
    public void undeployResource(Object resource, String applicationName, String moduleName) throws Exception {
        ConnectorResource domainResource = (ConnectorResource) resource;
        ResourceInfo resourceInfo = new ResourceInfo(domainResource.getJndiName(), applicationName, moduleName);
        deleteConnectorResource(domainResource, resourceInfo);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void undeployResource(Object resource)
            throws Exception {
        ConnectorResource domainResource = (ConnectorResource) resource;
        ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(domainResource);
        deleteConnectorResource(domainResource, resourceInfo);
    }

    private void deleteConnectorResource(ConnectorResource connectorResource, ResourceInfo resourceInfo)
            throws Exception {

        runtime.deleteConnectorResource(resourceInfo);
        //In-case the resource is explicitly created with a suffix (__nontx or __PM), no need to delete one
        if (ConnectorsUtil.getValidSuffix(resourceInfo.getName()) == null) {
            String pmJndiName = ConnectorsUtil.getPMJndiName(resourceInfo.getName());
            ResourceInfo pmResourceInfo = new ResourceInfo(pmJndiName, resourceInfo.getApplicationName(),
                    resourceInfo.getModuleName());
            runtime.deleteConnectorResource(pmResourceInfo);
        }

        //Since 8.1 PE/SE/EE - if no more resource-ref to the pool
        //of this resource in this server instance, remove pool from connector
        //runtime
        checkAndDeletePool(connectorResource);
    }

    /**
     * {@inheritDoc}
     */
    public void redeployResource(Object resource) throws Exception {
        undeployResource(resource);
        deployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void disableResource(Object resource)
            throws Exception {
        undeployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void enableResource(Object resource) throws Exception {
        deployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    public boolean handles(Object resource) {
        return resource instanceof ConnectorResource;
    }

    /**
     * @inheritDoc
     */
    public boolean supportsDynamicReconfiguration() {
        return false;
    }

    /**
     * @inheritDoc
     */
    public Class[] getProxyClassesForDynamicReconfiguration() {
        return new Class[0];
    }

    /**
     * Checks if no more resource-refs to resources exists for the
     * connector connection pool and then deletes the pool
     *
     * @param cr ConnectorResource
     * @throws Exception (ConfigException / undeploy exception)
     * @since 8.1 pe/se/ee
     */
    private void checkAndDeletePool(ConnectorResource cr) throws Exception {
        String poolName = cr.getPoolName();
        ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(cr);
        PoolInfo poolInfo = new PoolInfo(poolName, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
        Resources resources = (Resources) cr.getParent();
        //Its possible that the ConnectorResource here is a ConnectorResourceeDefinition. Ignore optimization.
        if (resources != null) {
            try {
                boolean poolReferred =
                        ResourcesUtil.createInstance().isPoolReferredInServerInstance(poolInfo);
                if (!poolReferred) {
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.fine("Deleting connector connection pool [" + poolName + "] as there are no more " +
                                "resource-refs to the pool in this server instance");
                    }

                    ConnectorConnectionPool ccp = (ConnectorConnectionPool)
                            ConnectorsUtil.getResourceByName(resources, ConnectorConnectionPool.class, poolName);
                    //Delete/Undeploy Pool
                    runtime.getResourceDeployer(ccp).undeployResource(ccp);
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
