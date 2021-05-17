/*
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

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.ConnectorRuntimeExtension;
import com.sun.enterprise.connectors.DeferredResourceConfig;
import com.sun.enterprise.connectors.util.ClassLoadingUtility;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.deployment.Application;
import com.sun.logging.LogDomains;
import org.glassfish.jdbc.config.JdbcConnectionPool;
import org.glassfish.jdbc.config.JdbcResource;
import org.glassfish.jdbc.deployer.DataSourceDefinitionDeployer;
import org.glassfish.jdbcruntime.service.JdbcDataSource;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Shalini M
 */
@Service
public class JdbcRuntimeExtension implements ConnectorRuntimeExtension {

    @Inject
    private Provider<Domain> domainProvider;

    @Inject
    private Provider<DataSourceDefinitionDeployer> dataSourceDefinitionDeployerProvider;

    protected final static Logger logger =
        LogDomains.getLogger(JdbcRuntimeExtension.class, LogDomains.RSR_LOGGER);

    protected ConnectorRuntime runtime;

    public JdbcRuntimeExtension() {
        runtime = ConnectorRuntime.getRuntime();
    }

    public Collection<Resource> getAllSystemRAResourcesAndPools() {
        List<Resource> resources = new ArrayList<Resource>();

        Domain domain = domainProvider.get();
        if(domain !=null) {
            Resources allResources = domain.getResources();
            for(Resource resource : allResources.getResources()){
                if(resource instanceof JdbcConnectionPool){
                    resources.add(resource);
                } else if(resource instanceof JdbcResource){
                    resources.add(resource);
                }
            }
        }

        System.out.println("JdbcRuntimeExtension,  getAllSystemRAResourcesAndPools = " + resources);
        return resources;
    }

    public void registerDataSourceDefinitions(Application application) {
        dataSourceDefinitionDeployerProvider.get().registerDataSourceDefinitions(application);
    }

    public void unRegisterDataSourceDefinitions(Application application) {
        dataSourceDefinitionDeployerProvider.get().unRegisterDataSourceDefinitions(application);
    }

    /**
     * Get a wrapper datasource specified by the jdbcjndi name
     * This API is intended to be used in the DAS. The motivation for having this
     * API is to provide the CMP backend/ JPA-Java2DB a means of acquiring a connection during
     * the codegen phase. If a user is trying to deploy an JPA-Java2DB app on a remote server,
     * without this API, a resource reference has to be present both in the DAS
     * and the server instance. This makes the deployment more complex for the
     * user since a resource needs to be forcibly created in the DAS Too.
     * This API will mitigate this need.
     *
     * @param resourceInfo the jndi name of the resource
     * @return DataSource representing the resource.
     */
    public Object lookupDataSourceInDAS(ResourceInfo resourceInfo) throws ConnectorRuntimeException {
        JdbcDataSource myDS = new JdbcDataSource();
        myDS.setResourceInfo(resourceInfo);
        return myDS;
    }

    /**
     * Gets the Pool name that this JDBC resource points to. In case of a PMF resource
     * gets the pool name of the pool pointed to by jdbc resource being pointed to by
     * the PMF resource
     *
     * @param resourceInfo the jndi name of the resource being used to get Connection from
     *                 This resource can either be a pmf resource or a jdbc resource
     * @return poolName of the pool that this resource directly/indirectly points to
     */
    public PoolInfo getPoolNameFromResourceJndiName(ResourceInfo resourceInfo) {
        PoolInfo poolInfo= null;
        JdbcResource jdbcResource = null;
        String jndiName = resourceInfo.getName();

        ResourceInfo actualResourceInfo =
                new ResourceInfo(jndiName, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
        ConnectorRuntime runtime = ConnectorRuntime.getRuntime();
        jdbcResource = (JdbcResource) ConnectorsUtil.getResourceByName(runtime.getResources(actualResourceInfo),
                JdbcResource.class, actualResourceInfo.getName());
        if(jdbcResource == null){
            String suffix = ConnectorsUtil.getValidSuffix(jndiName);
            if(suffix != null){
                jndiName = jndiName.substring(0, jndiName.lastIndexOf(suffix));
                actualResourceInfo =
                        new ResourceInfo(jndiName, resourceInfo.getApplicationName(), resourceInfo.getModuleName());
            }
        }
        jdbcResource = (JdbcResource) ConnectorsUtil.getResourceByName(runtime.getResources(actualResourceInfo),
                JdbcResource.class, actualResourceInfo.getName());

        if (jdbcResource != null) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("jdbcRes is ---: " + jdbcResource.getJndiName());
                logger.fine("poolName is ---: " + jdbcResource.getPoolName());
            }
        }
        if(jdbcResource != null){
            poolInfo = new PoolInfo(jdbcResource.getPoolName(), actualResourceInfo.getApplicationName(),
                    actualResourceInfo.getModuleName());
        }
        return poolInfo;
    }

    /**
     * Determines if a JDBC connection pool is referred in a
     * server-instance via resource-refs
     * @param poolInfo pool-name
     * @return boolean true if pool is referred in this server instance as well enabled, false
     * otherwise
     */
    public boolean isConnectionPoolReferredInServerInstance(PoolInfo poolInfo) {

        Collection<JdbcResource> jdbcResources = ConnectorRuntime.getRuntime().
                getResources(poolInfo).getResources(JdbcResource.class);

        for (JdbcResource resource : jdbcResources) {
            ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(resource);
            //Have to check isReferenced here!
            if ((resource.getPoolName().equalsIgnoreCase(poolInfo.getName())) &&
                    ResourcesUtil.createInstance().isReferenced(resourceInfo) &&
                    ResourcesUtil.createInstance().isEnabled(resource)) {
                if (logger.isLoggable(Level.FINE)) {
                    logger.fine("pool " + poolInfo + "resource " + resourceInfo
                            + " referred is referenced by this server");

                    logger.fine("JDBC resource " + resource.getJndiName() + "refers " + poolInfo
                            + "in this server instance and is enabled");
                }
                return true;
            }
        }
        if(logger.isLoggable(Level.FINE)) {
            logger.fine("No JDBC resource refers [ " + poolInfo + " ] in this server instance");
        }
        return false;
    }

    public String getResourceType(ConfigBeanProxy cb) {
        if (cb instanceof JdbcConnectionPool) {
            return ConnectorConstants.RES_TYPE_JCP;
        } else if (cb instanceof JdbcResource) {
            return ConnectorConstants.RES_TYPE_JDBC;
        }
        return null;
    }

    public DeferredResourceConfig getDeferredResourceConfig(Object resource,
                                                            Object pool, String resType, String raName)
            throws ConnectorRuntimeException {
        String resourceAdapterName;
        DeferredResourceConfig resConfig = null;
        //TODO V3 there should not be res-type related check, refactor deferred-ra-config
        //TODO V3 (not to hold specific resource types)
        if (resource instanceof JdbcResource || pool instanceof JdbcConnectionPool) {

            JdbcConnectionPool jdbcPool = (JdbcConnectionPool) pool;
            JdbcResource jdbcResource = (JdbcResource) resource;

            resourceAdapterName = getRANameofJdbcConnectionPool((JdbcConnectionPool) pool);

            resConfig = new DeferredResourceConfig(resourceAdapterName, null, jdbcPool, jdbcResource, null);

            Resource[] resourcesToload = new Resource[]{jdbcPool, jdbcResource};
            resConfig.setResourcesToLoad(resourcesToload);

        } else {
            throw new ConnectorRuntimeException("unsupported resource type : " + resource);
        }
        return resConfig;
    }

    /**
     * This method takes in an admin JdbcConnectionPool and returns the RA
     * that it belongs to.
     *
     * @param pool - The pool to check
     * @return The name of the JDBC RA that provides this pool's data-source
     */

    private String getRANameofJdbcConnectionPool(JdbcConnectionPool pool) {
        String dsRAName = ConnectorConstants.JDBCDATASOURCE_RA_NAME;

        Class clz = null;

        if(pool.getDatasourceClassname() != null && !pool.getDatasourceClassname().isEmpty()) {
            try {
                clz = ClassLoadingUtility.loadClass(pool.getDatasourceClassname());
            } catch (ClassNotFoundException cnfe) {
                Object params[] = new Object[]{dsRAName, pool.getName()};
                logger.log(Level.WARNING, "using.default.ds", params);
                return dsRAName;
            }
        } else if(pool.getDriverClassname() != null && !pool.getDriverClassname().isEmpty()) {
            try {
                clz = ClassLoadingUtility.loadClass(pool.getDriverClassname());
            } catch (ClassNotFoundException cnfe) {
                Object params[] = new Object[]{dsRAName, pool.getName()};
                logger.log(Level.WARNING, "using.default.ds", params);
                return dsRAName;
            }
        }

        if(clz != null){
            //check if its XA
            if (ConnectorConstants.JAVAX_SQL_XA_DATASOURCE.equals(pool.getResType())) {
                if (javax.sql.XADataSource.class.isAssignableFrom(clz)) {
                    return ConnectorConstants.JDBCXA_RA_NAME;
                }
            }

            //check if its CP
            if (ConnectorConstants.JAVAX_SQL_CONNECTION_POOL_DATASOURCE.equals(pool.getResType())) {
                if (javax.sql.ConnectionPoolDataSource.class.isAssignableFrom(
                        clz)) {
                    return ConnectorConstants.JDBCCONNECTIONPOOLDATASOURCE_RA_NAME;
                }
            }

            //check if its DM
            if(ConnectorConstants.JAVA_SQL_DRIVER.equals(pool.getResType())) {
                if(java.sql.Driver.class.isAssignableFrom(clz)) {
                    return ConnectorConstants.JDBCDRIVER_RA_NAME;
                }
            }

            //check if its DS
            if ("javax.sql.DataSource".equals(pool.getResType())) {
                if (javax.sql.DataSource.class.isAssignableFrom(clz)) {
                    return dsRAName;
                }
            }
        }
        Object params[] = new Object[]{dsRAName, pool.getName()};
        logger.log(Level.WARNING, "using.default.ds", params);
        //default to __ds
        return dsRAName;
    }

}

