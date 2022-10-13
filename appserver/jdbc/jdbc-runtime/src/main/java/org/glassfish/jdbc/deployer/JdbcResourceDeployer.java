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

package org.glassfish.jdbc.deployer;

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

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

import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getPMJndiName;
import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getResourceByName;
import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getResourceInfo;
import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.getValidSuffix;

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
@Singleton
@ResourceDeployerInfo(JdbcResource.class)
public class JdbcResourceDeployer implements ResourceDeployer<JdbcResource> {

    private static final Logger LOG = LogDomains.getLogger(JdbcResourceDeployer.class, LogDomains.RSR_LOGGER);

    @Inject
    private ConnectorRuntime runtime;

    @Override
    public boolean handles(Object resource) {
        return resource instanceof JdbcResource;
    }


    @Override
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource,
        Resources allResources) throws ResourceConflictException {
        // do nothing.
    }


    @Override
    public void deployResource(JdbcResource resource) throws Exception {
        ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(resource);
        deployResource(resource, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
    }


    @Override
    public synchronized void deployResource(JdbcResource resource, String applicationName, String moduleName) throws Exception {

        // deployResource is not synchronized as there is only one caller
        // ResourceProxy which is synchronized

        String jndiName = resource.getJndiName();
        String poolName = resource.getPoolName();
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

        LOG.finest(() -> "deployed resource " + jndiName);
    }

    @Override
    public synchronized void undeployResource(JdbcResource resource) throws Exception {
        deleteResource(resource, getResourceInfo(resource));
    }

    @Override
    public void undeployResource(JdbcResource resource, String applicationName, String moduleName) throws Exception {
        ResourceInfo resourceInfo = new ResourceInfo(resource.getJndiName(), applicationName, moduleName);
        deleteResource(resource, resourceInfo);
    }


    @Override
    public synchronized void enableResource(JdbcResource resource) throws Exception {
        deployResource(resource);
    }


    @Override
    public synchronized void disableResource(JdbcResource resource) throws Exception {
        undeployResource(resource);
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
            boolean poolReferred = JdbcResourcesUtil.createInstance().isJdbcPoolReferredInServerInstance(poolInfo);
            if (!poolReferred) {
                LOG.fine(() -> "Deleting JDBC pool [" + poolName + " ] as there are no more "
                    + "resource-refs to the pool in this server instance");

                JdbcConnectionPool jcp = (JdbcConnectionPool) getResourceByName(resources, JdbcConnectionPool.class,
                    poolName);

                // Delete/Undeploy Pool
                runtime.getResourceDeployer(jcp).undeployResource(jcp);
            }
        }
    }
}
