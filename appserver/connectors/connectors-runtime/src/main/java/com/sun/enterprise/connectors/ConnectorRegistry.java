/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.connectors;

import com.sun.enterprise.connectors.authentication.RuntimeSecurityMap;
import com.sun.enterprise.connectors.module.ConnectorApplication;
import com.sun.enterprise.connectors.util.SecurityMapUtils;
import com.sun.enterprise.deployment.ConnectorDescriptor;
import com.sun.logging.LogDomains;

import jakarta.resource.spi.ManagedConnectionFactory;
import jakarta.validation.Validator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.connectors.config.ResourceAdapterConfig;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.glassfish.resourcebase.resources.api.ResourceInfo;


/**
 * This is an registry class which holds various objects in hashMaps,
 * hash tables, and vectors. These objects are updated and queried
 * during various funtionalities of rar deployment/undeployment, resource
 * creation/destruction
 * Ex. of these objects are ResourcesAdapter instances, security maps for
 * pool managed connection factories.
 * It follows singleton pattern. i.e only one instance at any point of time.
 *
 * @author Binod P.G and Srikanth P
 */

public class ConnectorRegistry {

    static final Logger _logger = LogDomains.getLogger(ConnectorRegistry.class, LogDomains.RSR_LOGGER);

    protected static final ConnectorRegistry connectorRegistryInstance = new ConnectorRegistry();

    /**
     * <code>resourceAdapters</code> keeps track of all active resource
     * adapters in the connector runtime.
     * String:resourceadapterName Vs ActiveResourceAdapter
     */
    protected final Map<String, ActiveResourceAdapter> resourceAdapters;

    protected final Map<PoolInfo, PoolMetaData> factories;
    protected final Map<String, ResourceAdapterConfig> resourceAdapterConfig;
    protected final Map<String, ConnectorApplication> rarModules;
    protected final Map<String, Validator> beanValidators;
    protected final ConcurrentMap<ResourceInfo, AtomicLong> resourceInfoVersion;
    protected final Set<ResourceInfo> resourceInfos;
    protected final Set<PoolInfo> transparentDynamicReconfigPools;
    protected final Map<String, Object> locks;

    /**
     * Return the ConnectorRegistry instance
     *
     * @return ConnectorRegistry instance which is a singleton
     */
    public static ConnectorRegistry getInstance() {
        return connectorRegistryInstance;
    }

    /**
     * Protected constructor.
     * It is protected as it follows singleton pattern.
     */
    protected ConnectorRegistry() {
        resourceAdapters = Collections.synchronizedMap(new HashMap<String, ActiveResourceAdapter>());
        factories = Collections.synchronizedMap(new HashMap<PoolInfo, PoolMetaData>());
        resourceAdapterConfig = Collections.synchronizedMap(new HashMap<String, ResourceAdapterConfig>());
        rarModules = Collections.synchronizedMap(new HashMap<String, ConnectorApplication>());
        beanValidators = Collections.synchronizedMap(new HashMap<String, Validator>());
        resourceInfoVersion = new ConcurrentHashMap<>();
        resourceInfos = new HashSet<>();
        transparentDynamicReconfigPools = new HashSet<>();
        locks = new HashMap<>();
        _logger.log(Level.FINE, "Initialized the connector registry");
    }


    /**
     * Adds the object implementing ActiveResourceAdapter
     * interface to the registry.
     *
     * @param rarModuleName RarName which is the key
     * @param rar ActiveResourceAdapter instance which is the value.
     */
    public void addActiveResourceAdapter(String rarModuleName, ActiveResourceAdapter rar) {
        resourceAdapters.put(rarModuleName, rar);
        _logger.log(Level.FINE, "Added the active resource adapter {0} to connector registry", rarModuleName);
    }


    /**
     * get the version counter of  a resource info
     * @param resourceInfo resource-name
     * @return version counter. {@code -1L} if the resource is invalid
     */
    public long getResourceInfoVersion(ResourceInfo resourceInfo) {
        AtomicLong version = resourceInfoVersion.get(resourceInfo);
        if (version == null) {
           // resource is no longer valid
           return -1L;
        }
        return version.get();
    }

    /**
     * Update version information for a resource.
     * @param resourceInfo resource info to be updated.
     * @return new version couter
     */
    public long updateResourceInfoVersion(ResourceInfo resourceInfo) {
        AtomicLong version = resourceInfoVersion.get(resourceInfo);
        if (version == null) {
            AtomicLong newVersion = new AtomicLong();
            version = resourceInfoVersion.putIfAbsent(resourceInfo, newVersion);
               if (version == null) {
              version = newVersion;
            }
        }
        return version.incrementAndGet();
    }

    /**
     * remove and invalidate factories (proxy to actual factory) using the resource.
     * @param resourceInfo resource-name
     * @return boolean indicating whether the factories will get invalidated
     */
    public boolean removeResourceFactories(ResourceInfo resourceInfo){
        resourceInfoVersion.remove(resourceInfo);
        return false; // we actually don't know if there are any resource factories instantiated.
    }

    /**
     * Add resourceInfo that is deployed for book-keeping purposes.
     * @param resourceInfo Resource being deployed.
     */
    public void addResourceInfo(ResourceInfo resourceInfo){
        if (resourceInfo != null) {
            synchronized (resourceInfos){
                resourceInfos.add(resourceInfo);
            }
            updateResourceInfoVersion(resourceInfo);
        }
    }

    /**
     * Remove ResourceInfo from registry. Called when resource is disabled/undeployed.
     * @param resourceInfo ResourceInfo
     * @return boolean indicating whether resource exists and removed.
     */
    public boolean removeResourceInfo(ResourceInfo resourceInfo){
        boolean removed = false;
        if (resourceInfo != null) {
            synchronized (resourceInfos) {
                removed = resourceInfos.remove(resourceInfo);
            }
            resourceInfoVersion.remove(resourceInfo);
        }
        return removed;
    }

    /**
     * indicates whether the resource is deployed (enabled)
     * @param resourceInfo resource-info
     * @return boolean indicating whether the resource is deployed.
     */
    public boolean isResourceDeployed(ResourceInfo resourceInfo){
        if (resourceInfo != null) {
            return resourceInfos.contains(resourceInfo);
        }
        return false;
    }

    /**
     * Add PoolInfo that has transparent-dynamic-reconfiguration enabled .
     * @param poolInfo Pool being deployed.
     */
    public void addTransparentDynamicReconfigPool(PoolInfo poolInfo){
        if (poolInfo != null) {
            synchronized (transparentDynamicReconfigPools) {
                transparentDynamicReconfigPools.add(poolInfo);
            }
        }
    }

    /**
     * Remove ResourceInfo from registry. Called when resource is disabled/undeployed.
     * @param poolInfo poolInfo
     * @return boolean indicating whether the pool exists and removed.
     */
    public boolean removeTransparentDynamicReconfigPool(PoolInfo poolInfo){
        if (poolInfo != null) {
            synchronized (transparentDynamicReconfigPools) {
                return transparentDynamicReconfigPools.remove(poolInfo);
            }
        }
        return false;
    }

    /**
     * indicates whether the pool has transparent-dynamic-reconfiguration property enabled
     * @param poolInfo poolInfo
     * @return boolean false if pool is not deployed
     */
    public boolean isTransparentDynamicReconfigPool(PoolInfo poolInfo){
        if(poolInfo != null){
            return transparentDynamicReconfigPools.contains(poolInfo);
        }
        return false;
    }

    /**
     * Removes the object implementing ActiveResourceAdapter
     * interface from the registry.
     * This method is called whenever an active connector module
     * is removed from the Connector runtime. [eg. undeploy/recreate etc]
     *
     * @param rarModuleName RarName which is the key
     * @return true if successfully removed
     *         false if deletion fails.
     */
    public boolean removeActiveResourceAdapter(String rarModuleName) {
        Object o = resourceAdapters.remove(rarModuleName);
        if (o == null) {
            _logger.log(Level.FINE, "Failed to remove the resource adapter {0} from connector registry.", rarModuleName);
            return false;
        }
        _logger.log(Level.FINE, "Removed the active resource adapter {0} from connector registry", rarModuleName);
        return true;
    }


    /**
     * Retrieves the object implementing ActiveResourceAdapter interface
     * from the registry. Key is the rarName.
     *
     * @param rarModuleName Rar name. It is the key
     * @return object implementing ActiveResourceAdapter interface
     */
    public ActiveResourceAdapter getActiveResourceAdapter(String rarModuleName) {
        _logger.log(Level.FINEST, "getActiveResourceAdapter(rarModuleName={0})", rarModuleName);
        if (rarModuleName == null) {
            return null;
        }
        return resourceAdapters.get(rarModuleName);
    }

    /**
     * lock object that will be used by ResourceAdapterAdminService
     * to avoid multiple calls to create ActiveRA for same resource-adapter
     * @param rarName resource-adapter name
     * @return lock object for the resource-adapter
     */
    public Object getLockObject(String rarName) {
        if (rarName == null) {
            return null;
        }
        Object lock;
        synchronized (locks) {
            lock = locks.get(rarName);
            if (lock == null) {
                lock = new Object();
                locks.put(rarName, lock);
                if (_logger.isLoggable(Level.FINE)) {
                    _logger.fine("added lock-object [ " + lock + " ] for rar : " + rarName);
                }
            }
        }
        return lock;
    }

    /**
     * removes the lock-object used for creating the ActiveRA for a particular RAR
     * @param rarName resource-adapter
     */
    public void removeLockObject(String rarName) {
        if (rarName == null) {
            return;
        }
        synchronized (locks) {
            Object lock = locks.remove(rarName);
            if (_logger.isLoggable(Level.FINE)) {
                _logger.fine("removed lock-object [ " + lock + " ] for rar : " + rarName);
            }
        }
    }


    /**
     * Adds the bean validator to the registry.
     *
     * @param rarModuleName RarName which is the key
     * @param validator to be added to registry
     */
    public void addBeanValidator(String rarModuleName, Validator validator){
        beanValidators.put(rarModuleName, validator);
        _logger.log(Level.FINE, "Added the bean validator for RAR [{0}] to connector registry", rarModuleName);
    }

    /**
     * Retrieves the bean validator of a resource-adapter
     * from the registry. Key is the rarName.
     *
     * @param rarModuleName Rar name. It is the key
     * @return bean validator
     */
    public Validator getBeanValidator(String rarModuleName){
        _logger.log(Level.FINE, "getBeanValidator(rarModuleName={0})", rarModuleName);
        if (rarModuleName == null) {
            return null;
        }
        return beanValidators.get(rarModuleName);
    }

    /**
     * Removes the bean validator of a resource-adapter
     * from the registry.
     * This method is called whenever an active connector module
     * is removed from the Connector runtime. [eg. undeploy/recreate etc]
     *
     * @param rarModuleName RarName which is the key
     * @return true if successfully removed
     *         false if deletion fails.
     */
    public boolean removeBeanValidator(String rarModuleName) {
        _logger.log(Level.FINE, "removeBeanValidator(rarModuleName={0})", rarModuleName);
        Object o = beanValidators.remove(rarModuleName);
        if (o == null) {
            _logger.log(Level.FINE, "Failed to remove the bean validator for RAR [{0}] from connector registry",
                rarModuleName);
            return false;
        }
        _logger.log(Level.FINE, "Removed the active bean validator for RAR [{0}] from connector registry",
            rarModuleName);
        return true;
    }

    /**
     * Checks if the MCF pertaining to the pool is instantiated and present
     * in the registry. Each pool has its own MCF instance.
     *
     * @param poolInfo Name of the pool
     * @return true if the MCF is found.
     *         false if MCF is not found
     */
    public boolean isMCFCreated(PoolInfo poolInfo) {
        return factories.containsKey(poolInfo);
    }


    /**
     * Remove MCF instance pertaining to the poolName from the registry.
     *
     * @param poolInfo Name of the pool
     * @return true if successfully removed.
     *         false if removal fails.
     */
    public boolean removeManagedConnectionFactory(PoolInfo poolInfo) {
        _logger.log(Level.FINE, "removeManagedConnectionFactory(poolInfo={0})", poolInfo);
        if (factories.remove(poolInfo) == null) {
            _logger.log(Level.FINE, "Failed to remove the MCF from connector registry for {0}.", poolInfo);
            return false;
        }
        _logger.log(Level.FINE, "Removed MCF from connector registry for {0}.", poolInfo);
        return true;
    }

    /**
     * Add MCF instance pertaining to the poolName to the registry.
     *
     * @param poolInfo Name of the pool
     * @param pmd      MCF instance to be added.
     */
    public void addManagedConnectionFactory(PoolInfo poolInfo, PoolMetaData pmd) {
        factories.put(poolInfo, pmd);
        _logger.log(Level.FINE, "Added MCF to connector registry for {0}", poolInfo);
    }


    /**
     * Retrieve MCF instance pertaining to the poolName from the registry.
     *
     * @param poolInfo Name of the pool
     * @return factory MCF instance retrieved.
     */
    public ManagedConnectionFactory getManagedConnectionFactory(PoolInfo poolInfo) {
        _logger.log(Level.FINE, "getManagedConnectionFactory(poolInfo={0})", poolInfo);
        if (poolInfo != null) {
            PoolMetaData pmd = factories.get(poolInfo);
            if (pmd != null) {
                return pmd.getMCF();
            }
        }
        return null;
    }

    /**
     * Checks whether the rar is already deployed i.e registered with
     * connector registry
     *
     * @param rarModuleName rar Name.
     * @return true if rar is registered
     *         false if rar is not registered.
     */
    public boolean isRegistered(String rarModuleName) {
        _logger.log(Level.FINE, "isRegistered(rarModuleName={0})", rarModuleName);
        return resourceAdapters.containsKey(rarModuleName);
    }

    /**
     * Gets the connector descriptor pertaining the rar
     *
     * @param rarModuleName rarName
     * @return ConnectorDescriptor which represents the ra.xml
     */

    public ConnectorDescriptor getDescriptor(String rarModuleName) {
        _logger.log(Level.FINE, "getDescriptor(rarModuleName={0})", rarModuleName);
        ActiveResourceAdapter ar = null;
        if (rarModuleName != null) {
            ar = resourceAdapters.get(rarModuleName);
        }
        if (ar == null) {
            _logger.log(Level.FINE, "Could not find Connector descriptor in connector registry for {0}", rarModuleName);
            return null;
        }
        return ar.getDescriptor();
    }


    /**
     * Gets the runtime equivalent of policies enforced by the Security Maps
     * pertaining to a pool from the Pool's Meta Data.
     *
     * @param poolInfo pool information
     * @return runtimeSecurityMap in the form of HashMap of HashMaps (user and groups).
     * @see SecurityMapUtils#processSecurityMaps
     */
    public RuntimeSecurityMap getRuntimeSecurityMap(PoolInfo poolInfo) {
        _logger.log(Level.FINE, "getRuntimeSecurityMap(poolInfo={0})", poolInfo);
        if (poolInfo == null) {
            return null;
        }
        PoolMetaData pmd = factories.get(poolInfo);
        return pmd.getRuntimeSecurityMap();
    }

    /**
     * Get the resource adapter config properties object registered with
     * registry against the rarName.
     *
     * @param rarName Name of the rar
     * @return ResourceAdapter configuration object
     */
    public ResourceAdapterConfig getResourceAdapterConfig(String rarName) {
        _logger.log(Level.FINE, "getResourceAdapterConfig(rarName={0})", rarName);
        if (rarName == null) {
            return null;
        }
        return resourceAdapterConfig.get(rarName);
    }

    /**
     * Add the resource adapter config properties object to registry
     * against the rarName.
     *
     * @param rarName  Name of the rar
     * @param raConfig ResourceAdapter configuration object
     */

    public void addResourceAdapterConfig(String rarName, ResourceAdapterConfig raConfig) {
        if (rarName != null) {
            _logger.log(Level.FINE, "Adding the resourceAdapter Config to connector registry for {0}", rarName);
            resourceAdapterConfig.put(rarName, raConfig);
        }
    }


    /**
     * Remove the resource adapter config properties object from registry
     *
     * @param rarName Name of the rar
     * @return true if successfully deleted
     *         false if deletion fails
     */
    public boolean removeResourceAdapterConfig(String rarName) {
        _logger.log(Level.FINE, "removeResourceAdapterConfig(rarName={0})", rarName);
        if (resourceAdapterConfig.remove(rarName) == null) {
            _logger.log(Level.FINE, "Failed to remove the resourceAdapter config from registry for {0}.", rarName);
            return false;
        }
        _logger.log(Level.FINE, "Removed the resourceAdapter config map from registry for {0}.", rarName);
        return true;
    }

    /**
     * Returns all Active Resource Adapters in the connector runtime.
     *
     * @return All active resource adapters in the connector runtime
     */
    public ActiveResourceAdapter[] getAllActiveResourceAdapters() {
        ActiveResourceAdapter[] araArray = new ActiveResourceAdapter[this.resourceAdapters.size()];
        return this.resourceAdapters.values().toArray(araArray);
    }

    public PoolMetaData getPoolMetaData(PoolInfo poolInfo) {
        return factories.get(poolInfo);
    }

    /**
     * register a connector application (rarModule) with the registry
     * @param rarModule resource-adapter module
     */
    public void addConnectorApplication(ConnectorApplication rarModule){
        rarModules.put(rarModule.getModuleName(), rarModule);
    }

    /**
     * retrieve a connector application (rarModule) from the registry
     * @param rarName resource-adapter name
     * @return ConnectorApplication app
     */
    public ConnectorApplication getConnectorApplication(String rarName){
        return rarModules.get(rarName);
    }

    /**
     * remove a connector application (rarModule) from the registry
     * @param rarName resource-adapter module
     */
    public void removeConnectorApplication(String rarName){
        rarModules.remove(rarName);
    }

    /**
     * get the list of resource-adapters that support this message-listener-type
     * @param messageListener message-listener class-name
     * @return List of resource-adapters
     */
    public List<String> getConnectorsSupportingMessageListener(String messageListener) {
        List<String> rars = new ArrayList<>();
        for (ActiveResourceAdapter ara : resourceAdapters.values()) {
            ConnectorDescriptor desc = ara.getDescriptor();
            if (desc.getInBoundDefined()) {
                if (desc.getInboundResourceAdapter().getMessageListener(messageListener) != null) {
                    rars.add(ara.getModuleName());
                }
            }
        }
        return rars;
    }
}
