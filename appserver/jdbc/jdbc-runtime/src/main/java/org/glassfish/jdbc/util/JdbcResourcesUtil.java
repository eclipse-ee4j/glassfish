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

package org.glassfish.jdbc.util;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.BindableResource;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.util.ClassLoadingUtility;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.logging.LogDomains;
import org.glassfish.jdbc.config.JdbcConnectionPool;
import org.glassfish.jdbc.config.JdbcResource;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for JDBC related classes
 */
public class JdbcResourcesUtil {

    private volatile static JdbcResourcesUtil jdbcResourcesUtil;
    static Logger _logger = LogDomains.getLogger(JdbcResourcesUtil.class,
            LogDomains.RSR_LOGGER);
    private ConnectorRuntime runtime;

    private JdbcResourcesUtil(){
    }

    public static JdbcResourcesUtil createInstance() {
        //stateless, no synchronization needed
        if(jdbcResourcesUtil == null){
            synchronized(JdbcResourcesUtil.class) {
                if(jdbcResourcesUtil == null) {
                    jdbcResourcesUtil = new JdbcResourcesUtil();
                }
            }
        }
        return jdbcResourcesUtil;
    }

    private ConnectorRuntime getRuntime(){
        if(runtime == null){
            runtime = ConnectorRuntime.getRuntime();
        }
        return runtime;
    }


    public static <T> Resource getResourceByName(Resources resources, Class<T> type, String name) {
        return resources.getResourceByName(type, name);
    }

    public static Collection<BindableResource> getResourcesOfPool(Resources resources, String connectionPoolName) {
        Set<BindableResource> resourcesReferringPool = new HashSet<BindableResource>();
        ResourcePool pool = (ResourcePool) getResourceByName(resources, ResourcePool.class, connectionPoolName);
        if (pool != null) {
            Collection<BindableResource> bindableResources = resources.getResources(BindableResource.class);
            for (BindableResource resource : bindableResources) {
                if (JdbcResource.class.isAssignableFrom(resource.getClass())) {
                    if ((((JdbcResource) resource).getPoolName()).equals(connectionPoolName)) {
                        resourcesReferringPool.add(resource);
                    }
                }
            }
        }
        return resourcesReferringPool;
    }

    /**
     * This method takes in an admin JdbcConnectionPool and returns the RA
     * that it belongs to.
     *
     * @param pool - The pool to check
     * @return The name of the JDBC RA that provides this pool's data-source
     */

    public String getRANameofJdbcConnectionPool(JdbcConnectionPool pool) {
        String dsRAName = ConnectorConstants.JDBCDATASOURCE_RA_NAME;

        Class clz = null;

        if(pool.getDatasourceClassname() != null && !pool.getDatasourceClassname().isEmpty()) {
            try {
                clz = ClassLoadingUtility.loadClass(pool.getDatasourceClassname());
            } catch (ClassNotFoundException cnfe) {
                Object params[] = new Object[]{dsRAName, pool.getName()};
                _logger.log(Level.WARNING, "using.default.ds", params);
                return dsRAName;
            }
        } else if(pool.getDriverClassname() != null && !pool.getDriverClassname().isEmpty()) {
            try {
                clz = ClassLoadingUtility.loadClass(pool.getDriverClassname());
            } catch (ClassNotFoundException cnfe) {
                Object params[] = new Object[]{dsRAName, pool.getName()};
                _logger.log(Level.WARNING, "using.default.ds", params);
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
        _logger.log(Level.WARNING, "using.default.ds", params);
        //default to __ds
        return dsRAName;
    }

    public JdbcConnectionPool getJdbcConnectionPoolOfResource(ResourceInfo resourceInfo) {
        JdbcResource resource = null;
        JdbcConnectionPool pool = null;
        Resources resources = getResources(resourceInfo);
        if(resources != null){
            resource = (JdbcResource) ConnectorsUtil.getResourceByName(resources, JdbcResource.class, resourceInfo.getName());
            if(resource != null){
                pool = (JdbcConnectionPool)ConnectorsUtil.getResourceByName(resources, JdbcConnectionPool.class, resource.getPoolName());
            }
        }
        return pool;
    }

    private Resources getResources(ResourceInfo resourceInfo) {
        return getRuntime().getResources(resourceInfo);
    }

    /**
     * Determines if a JDBC connection pool is referred in a
     * server-instance via resource-refs
     * @param poolInfo pool-name
     * @return boolean true if pool is referred in this server instance as well enabled, false
     * otherwise
     */
    public boolean isJdbcPoolReferredInServerInstance(PoolInfo poolInfo) {

        Collection<JdbcResource> jdbcResources = getRuntime().getResources(poolInfo).getResources(JdbcResource.class);

        for (JdbcResource resource : jdbcResources) {
            ResourceInfo resourceInfo = ConnectorsUtil.getResourceInfo(resource);
            //Have to check isReferenced here!
            if ((resource.getPoolName().equalsIgnoreCase(poolInfo.getName())) &&
                    ResourcesUtil.createInstance().isReferenced(resourceInfo)
                    && ResourcesUtil.createInstance().isEnabled(resource)){
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine("pool " + poolInfo + "resource " + resourceInfo
                            + " referred " + ResourcesUtil.createInstance().isReferenced(resourceInfo));

                    _logger.fine("JDBC resource " + resource.getJndiName() + "refers " + poolInfo
                            + "in this server instance and is enabled");
                }
                return true;
            }
        }
        if(_logger.isLoggable(Level.FINE)) {
            _logger.fine("No JDBC resource refers [ " + poolInfo + " ] in this server instance");
        }
        return false;
    }
}
