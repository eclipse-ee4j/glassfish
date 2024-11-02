/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.connectors.service;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorRuntimeException;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Resource;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.connectors.ActiveResourceAdapter;
import com.sun.enterprise.connectors.ConnectorRegistry;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.DeferredResourceConfig;
import com.sun.enterprise.connectors.util.ConnectorDDTransformUtils;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.enterprise.resource.pool.PoolManager;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.logging.LogDomains;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.naming.SimpleJndiName;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceConstants;

import static java.util.logging.Level.SEVERE;


/**
 * This is the base class for all the connector services. It defines the
 * enviroment of execution (client or server), and holds the reference to
 * connector runtime for inter service method invocations.
 *
 * @author Srikanth P
 */
public class ConnectorService implements ConnectorConstants {
    protected static final Logger _logger = LogDomains.getLogger(ConnectorService.class, LogDomains.RSR_LOGGER);

    protected static final ConnectorRegistry _registry = ConnectorRegistry.getInstance();

    protected ConnectorRuntime _runtime;
    private ResourcesUtil resourcesUtil;

    /**
     * Default Constructor
     */
    public ConnectorService() {
        _runtime = ConnectorRuntime.getRuntime();
    }

    public ResourcesUtil getResourcesUtil(){
        if (resourcesUtil == null) {
            resourcesUtil = ResourcesUtil.createInstance();
        }

        return resourcesUtil;
    }

    /**
     * Returns the generated default connection poolName for a
     * connection definition.
     *
     * @param moduleName        rar module name
     * @param connectionDefName connection definition name
     * @return generated connection poolname
     */
    // TODO V3 can the default pool name generation be fully done by connector-admin-service-utils ?
    public SimpleJndiName getDefaultPoolName(String moduleName, String connectionDefName) {
        return new SimpleJndiName(moduleName + POOLNAME_APPENDER + connectionDefName);
    }

    /**
     * Returns the generated default connector resource for a
     * connection definition.
     *
     * @param moduleName        resource-adapter name
     * @param connectionDefName connection definition name
     * @return generated default connector resource name
     */
    // TODO V3 can the default resource name generation be fully done by connector-admin-service-utils ?
    public SimpleJndiName getDefaultResourceName(String moduleName, String connectionDefName) {
        // Construct the default resource name as <JNDIName_of_RA>#<connectionDefnName>
        SimpleJndiName resourceJNDIName = ConnectorAdminServiceUtils.getReservePrefixedJNDINameForResource(moduleName);
        return new SimpleJndiName(resourceJNDIName + RESOURCENAME_APPENDER + connectionDefName);
    }


    public boolean loadResourcesAndItsRar(DeferredResourceConfig defResConfig) {
        if (defResConfig != null) {
            try {
                loadDeferredResources(defResConfig.getResourceAdapterConfig());
                final String rarName = defResConfig.getRarName();
                loadDeferredResourceAdapter(rarName);

                try {
                    loadDeferredResources(defResConfig.getResourcesToLoad());
                } catch (Exception ex) {
                    Object params[] = new Object[]{rarName, ex};
                    _logger.log(SEVERE, "failed.to.load.deferred.resources", params);
                }
            } catch (Exception ex) {
                Object params[] = new Object[]{defResConfig.getRarName(), ex};
                _logger.log(SEVERE, "failed.to.load.deferred.ra", params);
                return false;
            }
            return true;
        }

        return false;
    }


    public void loadDeferredResourceAdapter(String rarModuleName) throws ConnectorRuntimeException {
        // load the RA if its not already loaded
        if (_registry.getActiveResourceAdapter(rarModuleName) == null) {
            try {
                //Do this only for System RA
                if (ConnectorsUtil.belongsToSystemRA(rarModuleName)) {
                    String systemModuleLocation = ConnectorsUtil.getSystemModuleLocation(rarModuleName);
                    if(_runtime.isServer()){
                        _runtime.getMonitoringBootstrap().registerProbes(rarModuleName,
                                new File(systemModuleLocation), _runtime.getSystemRARClassLoader(rarModuleName));
                    }
                    _runtime.createActiveResourceAdapter(systemModuleLocation, rarModuleName, null);
                }
            } catch (Exception e) {
                throw new ConnectorRuntimeException(e.getMessage(), e);
            }
        }
    }

    public void createActiveResourceAdapterForEmbeddedRar(String rarModuleName) throws ConnectorRuntimeException {
        ConnectorDescriptor cdesc = loadConnectorDescriptorForEmbeddedRAR(rarModuleName);
        String appName = ConnectorAdminServiceUtils.toApplicationName(rarModuleName);
        String rarFileName = ConnectorAdminServiceUtils.toRarFileName(rarModuleName);
        String loc = getResourcesUtil().getApplicationDeployLocation(appName);
        loc = loc + File.separator + FileUtils.makeFriendlyFilename(rarFileName);

        String path = null;
        try {
            URI uri = new URI(loc);
            path = uri.getPath();
        } catch (URISyntaxException use) {
            ConnectorRuntimeException cre = new ConnectorRuntimeException("Invalid path [ "+use.getMessage()+" ]");
            cre.setStackTrace(use.getStackTrace());
            _logger.log(Level.WARNING, cre.getMessage(), cre);
            throw cre;
        }
        // start RA
        _runtime.createActiveResourceAdapter(cdesc, rarModuleName, path);
    }


    public void loadDeferredResources(Resource[] resourcesToLoad)
            throws Exception {
        if (resourcesToLoad == null || resourcesToLoad.length == 0) {
            return;
        }
        for (Resource resource : resourcesToLoad) {
            if (resource == null) {
                continue;
            } else if (getResourcesUtil().isEnabled(resource)) {
                try {
                    _runtime.getResourceDeployer(resource).deployResource(resource);
                } catch (Exception e) {
                    ConnectorRuntimeException cre = new ConnectorRuntimeException(e.getMessage());
                    cre.initCause(e);
                    throw cre;
                }
            }
        }
    }

    /**
     * Obtains the connector Descriptor pertaining to rar.
     * If ConnectorDescriptor is present in registry, it is obtained from
     * registry and returned. Else it is explicitly read from directory
     * where rar is exploded.
     *
     * @param rarName Name of the rar
     * @return ConnectorDescriptor pertaining to rar.
     * @throws ConnectorRuntimeException when unable to get descriptor
     */
    public ConnectorDescriptor getConnectorDescriptor(String rarName)
            throws ConnectorRuntimeException {

        if (rarName == null) {
            return null;
        }
        ConnectorDescriptor desc = null;
        desc = _registry.getDescriptor(rarName);
        if (desc != null) {
            return desc;
        }
        String moduleDir;

        //If the RAR is embedded try loading the descriptor directly
        //using the applicationarchivist
        if (rarName.indexOf(ResourceConstants.EMBEDDEDRAR_NAME_DELIMITER) != -1){
            try {
                desc = loadConnectorDescriptorForEmbeddedRAR(rarName);
                if (desc != null) {
                    return desc;
                }
            } catch (ConnectorRuntimeException e) {
                throw e;
            }
        }

        if (ConnectorsUtil.belongsToSystemRA(rarName)) {
            moduleDir = ConnectorsUtil.getSystemModuleLocation(rarName);
        } else {
            moduleDir = ConnectorsUtil.getLocation(rarName);
        }
        if (moduleDir != null) {
            desc = ConnectorDDTransformUtils.getConnectorDescriptor(moduleDir, rarName);
        } else {
            _logger.log(SEVERE,
                    "rardeployment.no_module_deployed", rarName);
        }
        return desc;
    }


    /**
     * Matching will be switched off in the pool, by default. This will be
     * switched on if the connections with different resource principals reach the pool.
     *
     * @param poolInfo Name of the pool to switchOn matching.
     * @param rarName  Name of the resource adater.
     */
    public void switchOnMatching(String rarName, PoolInfo poolInfo) {
        // At present it is applicable to only JDBC resource adapters
        // Later other resource adapters also become applicable.
        if (rarName.equals(ConnectorConstants.JDBCDATASOURCE_RA_NAME)
            || rarName.equals(ConnectorConstants.JDBCCONNECTIONPOOLDATASOURCE_RA_NAME)
            || rarName.equals(ConnectorConstants.JDBCXA_RA_NAME)) {

            PoolManager poolMgr = _runtime.getPoolManager();
            boolean result = poolMgr.switchOnMatching(poolInfo);
            if (!result) {
                try {
                    _runtime.switchOnMatchingInJndi(poolInfo);
                } catch (ConnectorRuntimeException cre) {
                    // This will never happen.
                }
            }
        }
    }

    public boolean checkAndLoadPool(PoolInfo poolInfo) {
        try {
            ResourcePool pool = _runtime.getConnectionPoolConfig(poolInfo);
            if (pool == null) {
                return false;
            }
            DeferredResourceConfig defResConfig = getResourcesUtil().getDeferredResourceConfig(null, pool, null, null);
            return loadResourcesAndItsRar(defResConfig);
        } catch (ConnectorRuntimeException e) {
            _logger.log(Level.WARNING, "unable.to.load.connection.pool", new Object[]{poolInfo, e});
            return false;
        }
    }


    public void ifSystemRarLoad(String rarName) throws ConnectorRuntimeException {
        if (ConnectorsUtil.belongsToSystemRA(rarName)) {
            loadDeferredResourceAdapter(rarName);
        }
    }

    // TODO V3 with annotations, is it right a approach to load the descriptor using Archivist ?
    private ConnectorDescriptor loadConnectorDescriptorForEmbeddedRAR(String rarName) throws ConnectorRuntimeException {
        //If the RAR is embedded try loading the descriptor directly
        //using the applicationarchivist
        ResourcesUtil resutil = ResourcesUtil.createInstance();
        String rarFileName = ConnectorAdminServiceUtils.toRarFileName(rarName);
        return resutil.getConnectorDescriptorFromUri(rarName, rarFileName);
    }

    /**
     * Check whether ClassLoader is permitted to access this resource adapter.
     * If the RAR is deployed and is not a standalone RAR, then only the ClassLoader
     * that loaded the archive (any of its child) should be able to access it. Otherwise everybody can
     * access the RAR.
     *
     * @param rarName Resource adapter module name.
     * @param loader  <code>ClassLoader</code> to verify.
     */
    public boolean checkAccessibility(String rarName, ClassLoader loader) {
        ActiveResourceAdapter activeResourceAdapter = _registry.getActiveResourceAdapter(rarName);
        if (activeResourceAdapter != null && loader != null) { // If RA is deployed

            ClassLoader rarLoader = activeResourceAdapter.getClassLoader();

            // If the RAR is not standalone.
            if (rarLoader != null && ConnectorAdminServiceUtils.isEmbeddedConnectorModule(rarName)) {
                ClassLoader rarLoaderParent = rarLoader.getParent();
                ClassLoader parent = loader;
                while (true) {
                    if (parent.equals(rarLoaderParent)) {
                        return true;
                    }

                    final ClassLoader temp = parent;
                    Object obj = temp.getParent();

                    if (obj == null) {
                        break;
                    } else {
                        parent = (ClassLoader) obj;
                    }
                }

                // If no parent matches return false;
                return false;
            }
        }

        return true;
    }
}
