/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jdbcruntime;

import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.ConnectorRuntimeExtension;
import com.sun.enterprise.connectors.DeferredResourceConfig;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.deployment.Application;
import com.sun.logging.LogDomains;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.jdbc.config.JdbcConnectionPool;
import org.glassfish.jdbc.config.JdbcResource;
import org.glassfish.jdbc.deployer.DataSourceDefinitionDeployer;
import org.glassfish.jdbc.util.JdbcResourcesUtil;
import org.glassfish.jdbcruntime.service.JdbcDataSource;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceConstants;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;

/**
 * @author Shalini M
 */
@Service
public class JdbcRuntimeExtension implements ConnectorRuntimeExtension {

    private static final Logger LOG = LogDomains.getLogger(JdbcRuntimeExtension.class, LogDomains.RSR_LOGGER);

    @Inject
    private Provider<Domain> domainProvider;

    @Inject
    private Provider<DataSourceDefinitionDeployer> dataSourceDefinitionDeployerProvider;


    private final ConnectorRuntime runtime;

    public JdbcRuntimeExtension() {
        runtime = ConnectorRuntime.getRuntime();
    }

    @Override
    public Collection<Resource> getAllSystemRAResourcesAndPools() {
        List<Resource> resources = new ArrayList<>();

        Domain domain = domainProvider.get();
        if (domain != null) {
            Resources allResources = domain.getResources();
            for (Resource resource : allResources.getResources()) {
                if (resource instanceof JdbcConnectionPool) {
                    resources.add(resource);
                } else if (resource instanceof JdbcResource) {
                    resources.add(resource);
                }
            }
        }

        System.out.println("JdbcRuntimeExtension,  getAllSystemRAResourcesAndPools = " + resources);
        return resources;
    }

    @Override
    public void registerDataSourceDefinitions(Application application) {
        dataSourceDefinitionDeployerProvider.get().registerDataSourceDefinitions(application);
    }

    @Override
    public void unRegisterDataSourceDefinitions(Application application) {
        dataSourceDefinitionDeployerProvider.get().unRegisterDataSourceDefinitions(application);
    }

    /**
     * Get a wrapper datasource specified by the jdbcjndi name This API is intended
     * to be used in the DAS. The motivation for having this API is to provide the
     * CMP backend/ JPA-Java2DB a means of acquiring a connection during the codegen
     * phase. If a user is trying to deploy an JPA-Java2DB app on a remote server,
     * without this API, a resource reference has to be present both in the DAS and
     * the server instance. This makes the deployment more complex for the user
     * since a resource needs to be forcibly created in the DAS Too. This API will
     * mitigate this need.
     *
     * @param resourceInfo the jndi name of the resource
     * @return JdbcDataSource representing the resource.
     */
    @Override
    public JdbcDataSource lookupDataSourceInDAS(ResourceInfo resourceInfo) throws ConnectorRuntimeException {
        return new JdbcDataSource(resourceInfo);
    }

    /**
     * Gets the Pool name that this JDBC resource points to. In case of a PMF
     * resource gets the pool name of the pool pointed to by jdbc resource being
     * pointed to by the PMF resource
     *
     * @param resourceInfo the jndi name of the resource being used to get
     * Connection from This resource can either be a pmf resource or a jdbc resource
     * @return poolName of the pool that this resource directly/indirectly points to
     */
    @Override
    public PoolInfo getPoolNameFromResourceJndiName(ResourceInfo resourceInfo) {
        SimpleJndiName jndiName = resourceInfo.getName();
        ResourceInfo actualResourceInfo = new ResourceInfo(jndiName, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
        JdbcResource jdbcResource = runtime.getResources(actualResourceInfo).getResourceByName(JdbcResource.class,
            actualResourceInfo.getName());
        if (jdbcResource == null) {
            String suffix = ConnectorsUtil.getValidSuffix(jndiName);
            if (suffix != null) {
                jndiName = jndiName.removeSuffix(suffix);
                actualResourceInfo = new ResourceInfo(jndiName, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
            }
        }
        jdbcResource = runtime.getResources(actualResourceInfo).getResourceByName(JdbcResource.class,
            actualResourceInfo.getName());

        if (jdbcResource != null) {
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("jdbcRes is ---: " + jdbcResource.getJndiName());
                LOG.fine("poolName is ---: " + jdbcResource.getPoolName());
            }
        }
        if (jdbcResource != null) {
            SimpleJndiName poolName = new SimpleJndiName(jdbcResource.getPoolName());
            return new PoolInfo(poolName, actualResourceInfo.getApplicationName(), actualResourceInfo.getModuleName());
        }
        return null;
    }

    /**
     * Determines if a JDBC connection pool is referred in a server-instance via
     * resource-refs
     *
     * @param poolInfo pool-name
     * @return boolean true if pool is referred in this server instance as well
     * enabled, false otherwise
     */
    @Override
    public boolean isConnectionPoolReferredInServerInstance(PoolInfo poolInfo) {

        Collection<JdbcResource> jdbcResources = ConnectorRuntime.getRuntime().getResources(poolInfo).getResources(JdbcResource.class);

        for (JdbcResource resource : jdbcResources) {
            ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(resource);
            // Have to check isReferenced here!
            ResourcesUtil resourcesUtil = ResourcesUtil.createInstance();
            if (resource.getPoolName().equals(poolInfo.getName().toString())
                && resourcesUtil.isReferenced(resourceInfo) && resourcesUtil.isEnabled(resource)) {
                LOG.log(Level.CONFIG, "JDBC pool {0} is referred by resource {1} and is enabled on this server",
                    new Object[] {poolInfo, resourceInfo});
                return true;
            }
        }
        LOG.log(Level.FINE, "No JDBC resource refers [{0}] in this server instance", poolInfo);
        return false;
    }

    @Override
    public String getResourceType(ConfigBeanProxy cb) {
        if (cb instanceof JdbcConnectionPool) {
            return ResourceConstants.RES_TYPE_JCP;
        } else if (cb instanceof JdbcResource) {
            return ResourceConstants.RES_TYPE_JDBC;
        }
        return null;
    }

    @Override
    public DeferredResourceConfig getDeferredResourceConfig(Object resource, Object pool, String resType, String raName)
            throws ConnectorRuntimeException {
        String resourceAdapterName;
        DeferredResourceConfig resConfig = null;
        // TODO V3 there should not be res-type related check, refactor
        // deferred-ra-config
        // TODO V3 (not to hold specific resource types)
        if (resource instanceof JdbcResource || pool instanceof JdbcConnectionPool) {

            JdbcConnectionPool jdbcPool = (JdbcConnectionPool) pool;
            JdbcResource jdbcResource = (JdbcResource) resource;

            resourceAdapterName = getRANameofJdbcConnectionPool((JdbcConnectionPool) pool);

            resConfig = new DeferredResourceConfig(resourceAdapterName, null, jdbcPool, jdbcResource, null);

            Resource[] resourcesToload = new Resource[] { jdbcPool, jdbcResource };
            resConfig.setResourcesToLoad(resourcesToload);

        } else {
            throw new ConnectorRuntimeException("unsupported resource type : " + resource);
        }
        return resConfig;
    }


    /**
     * This method takes in an admin JdbcConnectionPool and returns the RA that it belongs to.
     *
     * @param pool - The pool to check
     * @return The name of the JDBC RA that provides this pool's data-source
     */
    private String getRANameofJdbcConnectionPool(JdbcConnectionPool pool) {
        final JdbcResourcesUtil resourcesUtil = JdbcResourcesUtil.createInstance();
        return resourcesUtil.getRANameofJdbcConnectionPool(pool);
    }
}
