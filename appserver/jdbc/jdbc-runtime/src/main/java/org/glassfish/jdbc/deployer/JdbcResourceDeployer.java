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

package org.glassfish.jdbc.deployer;

import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getPMJndiName;
import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getResourceByName;
import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getResourceInfo;
import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getValidSuffix;

import java.util.Collection;
import java.util.logging.Logger;

import org.glassfish.jdbc.config.JdbcConnectionPool;
import org.glassfish.jdbc.config.JdbcResource;
import org.glassfish.jdbc.util.JdbcResourcesUtil;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceConflictException;
import org.glassfish.resourcebase.resources.api.ResourceDeployer;
import org.glassfish.resourcebase.resources.api.ResourceDeployerInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.jvnet.hk2.annotations.Service;

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Handles Jdbc resource events in the server instance. When user adds a jdbc
 * resource, the admin instance emits resource event. The jdbc resource events
 * are propagated to this object.
 * <p/>
 * The methods can potentially be called concurrently, therefore implementation
 * need to be synchronized.
 *
 * @author Nazrul Islam
 * @since JDK1.4
 */
@Service
@ResourceDeployerInfo(JdbcResource.class)
@Singleton
public class JdbcResourceDeployer implements ResourceDeployer {

    private static Logger _logger = LogDomains.getLogger(JdbcResourceDeployer.class, LogDomains.RSR_LOGGER);

    @Inject
    private ConnectorRuntime runtime;

    @Override
    public synchronized void deployResource(Object resource, String applicationName, String moduleName) throws Exception {

        // deployResource is not synchronized as there is only one caller
        // ResourceProxy which is synchronized

        JdbcResource jdbcRes = (JdbcResource) resource;
        String jndiName = jdbcRes.getJndiName();
        String poolName = jdbcRes.getPoolName();
        PoolInfo poolInfo = new PoolInfo(poolName, applicationName, moduleName);
        ResourceInfo resourceInfo = new ResourceInfo(jndiName, applicationName, moduleName);

        runtime.createConnectorResource(resourceInfo, poolInfo, null);

        // In-case the resource is explicitly created with a suffix (__nontx or __PM),
        // no need to create one
        if (getValidSuffix(jndiName) == null) {
            ResourceInfo pmResourceInfo =
                new ResourceInfo(
                    getPMJndiName(jndiName),
                    resourceInfo.getApplicationName(),
                    resourceInfo.getModuleName());

            runtime.createConnectorResource(pmResourceInfo, poolInfo, null);
        }

        _logger.finest(() -> "deployed resource " + jndiName);
    }

    @Override
    public void deployResource(Object resource) throws Exception {
        JdbcResource jdbcRes = (JdbcResource) resource;
        ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(jdbcRes);

        deployResource(jdbcRes, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
    }

    @Override
    public boolean canDeploy(boolean postApplicationDeployment, Collection<Resource> allResources, Resource resource) {
        if (handles(resource)) {
            if (!postApplicationDeployment) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void undeployResource(Object resource, String applicationName, String moduleName) throws Exception {
        JdbcResource jdbcRes = (JdbcResource) resource;
        ResourceInfo resourceInfo = new ResourceInfo(jdbcRes.getJndiName(), applicationName, moduleName);

        deleteResource(jdbcRes, resourceInfo);
    }

    @Override
    public synchronized void undeployResource(Object resource) throws Exception {
        JdbcResource jdbcRes = (JdbcResource) resource;

        deleteResource(jdbcRes, getResourceInfo(jdbcRes));
    }

    private void deleteResource(JdbcResource jdbcResource, ResourceInfo resourceInfo) throws Exception {
        runtime.deleteConnectorResource(resourceInfo);
        ConnectorRegistry.getInstance().removeResourceFactories(resourceInfo);

        // In-case the resource is explicitly created with a suffix (__nontx or __PM),
        // no need to delete one
        if (getValidSuffix(resourceInfo.getName()) == null) {
            String pmJndiName = getPMJndiName(resourceInfo.getName());
            ResourceInfo pmResourceInfo = new ResourceInfo(pmJndiName, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
            runtime.deleteConnectorResource(pmResourceInfo);
            ConnectorRegistry.getInstance().removeResourceFactories(pmResourceInfo);
        }

        // Since 8.1 PE/SE/EE - if no more resource-ref to the pool
        // of this resource in this server instance, remove pool from connector
        // runtime
        checkAndDeletePool(jdbcResource);
    }

    @Override
    public synchronized void redeployResource(Object resource) throws Exception {
        undeployResource(resource);
        deployResource(resource);
    }

    @Override
    public boolean handles(Object resource) {
        return resource instanceof JdbcResource;
    }

    /**
     * @inheritDoc
     */
    @Override
    public Class[] getProxyClassesForDynamicReconfiguration() {
        return new Class[0];
    }

    /**
     * @inheritDoc
     */
    @Override
    public boolean supportsDynamicReconfiguration() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void enableResource(Object resource) throws Exception {
        deployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void disableResource(Object resource) throws Exception {
        undeployResource(resource);
    }

    /**
     * Checks if no more resource-refs to resources exists for the JDBC connection
     * pool and then deletes the pool
     *
     * @param jdbcResource Jdbc Resource Config bean
     * @throws Exception if unable to access configuration/undeploy resource.
     * @since 8.1 pe/se/ee
     */
    private void checkAndDeletePool(JdbcResource jdbcResource) throws Exception {
        String poolName = jdbcResource.getPoolName();
        ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(jdbcResource);
        PoolInfo poolInfo = new PoolInfo(poolName, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
        Resources resources = (Resources) jdbcResource.getParent();

        // Its possible that the JdbcResource here is a DataSourceDefinition. Ignore
        // optimization.
        if (resources != null) {
            try {
                boolean poolReferred = JdbcResourcesUtil.createInstance().isJdbcPoolReferredInServerInstance(poolInfo);
                if (!poolReferred) {
                    _logger.fine(() ->
                        "Deleting JDBC pool [" + poolName + " ] as there are no more " +
                        "resource-refs to the pool in this server instance");

                    JdbcConnectionPool jcp = (JdbcConnectionPool)
                        getResourceByName(resources, JdbcConnectionPool.class, poolName);

                    // Delete/Undeploy Pool
                    runtime.getResourceDeployer(jcp).undeployResource(jcp);
                }
            } catch (Exception ce) {
                _logger.warning(ce.getMessage());
                _logger.fine(() -> "Exception while deleting pool [ " + poolName + " ] : " + ce);
                throw ce;
            }
        }
    }

    @Override
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource, Resources allResources) throws ResourceConflictException {
        // do nothing.
    }
}
