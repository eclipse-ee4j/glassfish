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

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.util.i18n.StringManager;
import com.sun.logging.LogDomains;
import org.glassfish.jdbc.config.JdbcConnectionPool;
import org.glassfish.jdbc.config.JdbcResource;
import org.glassfish.jdbc.util.JdbcResourcesUtil;
import org.glassfish.resourcebase.resources.api.*;
import org.jvnet.hk2.annotations.Service;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Handles Jdbc resource events in the server instance. When user adds a
 * jdbc resource, the admin instance emits resource event. The jdbc
 * resource events are propagated to this object.
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

    @Inject
    private ConnectorRuntime runtime;

    private static final StringManager localStrings =
            StringManager.getManager(JdbcResourceDeployer.class);
    // logger for this deployer
    private static Logger _logger = LogDomains.getLogger(JdbcResourceDeployer.class, LogDomains.RSR_LOGGER);

    /**
     * {@inheritDoc}
     */
    public synchronized void deployResource(Object resource, String applicationName, String moduleName)
            throws Exception {

        //deployResource is not synchronized as there is only one caller
        //ResourceProxy which is synchronized

        JdbcResource jdbcRes = (JdbcResource) resource;
        String jndiName = jdbcRes.getJndiName();
        String poolName = jdbcRes.getPoolName();
        PoolInfo poolInfo = new PoolInfo(poolName, applicationName, moduleName);
        ResourceInfo resourceInfo = new ResourceInfo(jndiName, applicationName, moduleName);

        runtime.createConnectorResource(resourceInfo, poolInfo, null);
        //In-case the resource is explicitly created with a suffix (__nontx or __PM), no need to create one
        if (ConnectorsUtil.getValidSuffix(jndiName) == null) {
            ResourceInfo pmResourceInfo = new ResourceInfo(ConnectorsUtil.getPMJndiName(jndiName),
                    resourceInfo.getApplicationName(), resourceInfo.getModuleName());
            runtime.createConnectorResource(pmResourceInfo, poolInfo, null);
        }
        if (_logger.isLoggable(Level.FINEST)) {
            _logger.finest("deployed resource " + jndiName);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deployResource(Object resource) throws Exception {
        JdbcResource jdbcRes = (JdbcResource) resource;
        ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(jdbcRes);
        deployResource(jdbcRes, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
    }

    /**
     * {@inheritDoc}
     */
    public boolean canDeploy(boolean postApplicationDeployment, Collection<Resource> allResources, Resource resource) {
        if (handles(resource)) {
            if (!postApplicationDeployment) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    public void undeployResource(Object resource, String applicationName, String moduleName) throws Exception {
        JdbcResource jdbcRes = (JdbcResource) resource;
        ResourceInfo resourceInfo = new ResourceInfo(jdbcRes.getJndiName(), applicationName, moduleName);
        deleteResource(jdbcRes, resourceInfo);
    }

    /**
     * {@inheritDoc}
     */
    public synchronized void undeployResource(Object resource)
            throws Exception {
        JdbcResource jdbcRes = (JdbcResource) resource;
        ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(jdbcRes);
        deleteResource(jdbcRes, resourceInfo);
    }

    private void deleteResource(JdbcResource jdbcResource, ResourceInfo resourceInfo) throws Exception {

        runtime.deleteConnectorResource(resourceInfo);
        ConnectorRegistry.getInstance().removeResourceFactories(resourceInfo);
        //In-case the resource is explicitly created with a suffix (__nontx or __PM), no need to delete one
        if (ConnectorsUtil.getValidSuffix(resourceInfo.getName()) == null) {
            String pmJndiName = ConnectorsUtil.getPMJndiName(resourceInfo.getName());
            ResourceInfo pmResourceInfo = new ResourceInfo(pmJndiName, resourceInfo.getApplicationName(),
                    resourceInfo.getModuleName());
            runtime.deleteConnectorResource(pmResourceInfo);
            ConnectorRegistry.getInstance().removeResourceFactories(pmResourceInfo);
        }

        //Since 8.1 PE/SE/EE - if no more resource-ref to the pool
        //of this resource in this server instance, remove pool from connector
        //runtime
        checkAndDeletePool(jdbcResource);

    }

    /**
     * {@inheritDoc}
     */
    public synchronized void redeployResource(Object resource)
            throws Exception {

        undeployResource(resource);
        deployResource(resource);
    }

    /**
     * {@inheritDoc}
     */
    public boolean handles(Object resource) {
        return resource instanceof JdbcResource;
    }

    /**
     * @inheritDoc
     */
    public Class[] getProxyClassesForDynamicReconfiguration() {
        return new Class[0];
    }

    /**
     * @inheritDoc
     */
    public boolean supportsDynamicReconfiguration() {
        return false;
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
    public synchronized void disableResource(Object resource) throws Exception {
        undeployResource(resource);
    }

    /**
     * Checks if no more resource-refs to resources exists for the
     * JDBC connection pool and then deletes the pool
     *
     * @param cr Jdbc Resource Config bean
     * @throws Exception if unable to access configuration/undeploy resource.
     * @since 8.1 pe/se/ee
     */
    private void checkAndDeletePool(JdbcResource cr) throws Exception {
        String poolName = cr.getPoolName();
        ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(cr);
        PoolInfo poolInfo = new PoolInfo(poolName, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
        Resources resources = (Resources) cr.getParent();
        //Its possible that the JdbcResource here is a DataSourceDefinition. Ignore optimization.
        if (resources != null) {
            try {
                boolean poolReferred =
                    JdbcResourcesUtil.createInstance().isJdbcPoolReferredInServerInstance(poolInfo);
                if (!poolReferred) {
                    if (_logger.isLoggable(Level.FINE)) {
                        _logger.fine("Deleting JDBC pool [" + poolName + " ] as there are no more " +
                                "resource-refs to the pool in this server instance");
                    }

                    JdbcConnectionPool jcp = (JdbcConnectionPool)
                            ConnectorsUtil.getResourceByName(resources, JdbcConnectionPool.class, poolName);
                    //Delete/Undeploy Pool
                    runtime.getResourceDeployer(jcp).undeployResource(jcp);
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

    /**
     * {@inheritDoc}
     */
    public void validatePreservedResource(Application oldApp, Application newApp, Resource resource,
                                          Resources allResources)
            throws ResourceConflictException {
        //do nothing.
    }
}
