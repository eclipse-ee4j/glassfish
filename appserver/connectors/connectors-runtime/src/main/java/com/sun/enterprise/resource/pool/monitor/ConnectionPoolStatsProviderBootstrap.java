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

package com.sun.enterprise.resource.pool.monitor;

import com.sun.appserv.connectors.internal.api.ConnectorConstants;
import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.config.serverbeans.Resources;
import com.sun.enterprise.connectors.ConnectionPoolMonitoringExtension;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.connectors.util.ResourcesUtil;
import com.sun.enterprise.resource.listener.PoolLifeCycle;
import com.sun.enterprise.resource.pool.PoolLifeCycleListenerRegistry;
import com.sun.enterprise.resource.pool.PoolLifeCycleRegistry;
import com.sun.enterprise.resource.pool.PoolManager;
import com.sun.logging.LogDomains;
import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.ServiceLocator;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.inject.Singleton;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bootstrap operations of stats provider objects are done by this class.
 * Registering of provider to the StatsProviderManager, adding pools to the
 * PoolLifeCycle listeners are done during the bootstrap.
 * Depending on the lifecycle of the pool - creation/destroy, the listeners
 * are added or removed and providers registered/unregistered.
 *
 * This is an implementation of PoolLifeCycle. All pool creation or destroy
 * events are got and based on the type, provider is registered for a pool if pool
 * is created or provider is unregistered if pool is destroyed. Monitoring
 * levels when changed from HIGH-> OFF or
 * OFF->HIGH are taken care and appropriate monitoring levels are set.
 *
 * @author Shalini M
 */
@Service
@Singleton
public class ConnectionPoolStatsProviderBootstrap implements PostConstruct,
        PoolLifeCycle {

    protected final static Logger logger =
    LogDomains.getLogger(ConnectionPoolStatsProviderBootstrap.class,LogDomains.RSR_LOGGER);

    @Inject
    private PoolManager poolManager;

    @Inject
    private Provider<Domain> domainProvider;

    @Inject
    private Provider<ConnectionPoolProbeProviderUtil> connectionPoolProbeProviderUtilProvider;

    @Inject
    private ServiceLocator habitat;

    //List of all connector conn pool stats providers that are created and stored
    private List<ConnectorConnPoolStatsProvider> ccStatsProviders = null;

    //Map of all ConnectionPoolEmitterImpl(s) for different pools
    private Map<PoolInfo, ConnectionPoolEmitterImpl> poolEmitters = null;
    private Map<PoolInfo, PoolLifeCycleListenerRegistry> poolRegistries = null;
    private ConnectorRuntime runtime;

    public ConnectionPoolStatsProviderBootstrap() {
        ccStatsProviders = new ArrayList<ConnectorConnPoolStatsProvider>();
        poolEmitters = new HashMap<PoolInfo, ConnectionPoolEmitterImpl>();
        poolRegistries = new HashMap<PoolInfo, PoolLifeCycleListenerRegistry>();
        runtime = ConnectorRuntime.getRuntime();

    }

    public void addToPoolEmitters(PoolInfo poolInfo, ConnectionPoolEmitterImpl emitter) {
        poolEmitters.put(poolInfo, emitter);
    }

    /**
     * All Jdbc Connection pools are added to the pool life cycle listener so as
     * to listen to creation/destroy events. If the JdbcPoolTree is not built,
     * by registering to the StatsProviderManager, its is done here.
     */
    public void registerProvider() {
        registerPoolLifeCycleListener();
    }

    public void postConstruct() {
        if(logger.isLoggable(Level.FINEST)) {
            logger.finest("[Monitor]In the ConnectionPoolStatsProviderBootstrap");
        }

       //createMonitoringConfig();
    }

    /**
     * Registers the pool lifecycle listener for this pool by creating a
     * new ConnectionPoolEmitterImpl object for this pool.
     * @param poolInfo
     * @return registry of pool lifecycle listeners
     */
    public PoolLifeCycleListenerRegistry registerPool(PoolInfo poolInfo,
            ConnectionPoolProbeProvider poolProvider) {
        PoolLifeCycleListenerRegistry poolRegistry = null;
        if(poolRegistries.get(poolInfo)==null){
            poolRegistry =
                new PoolLifeCycleListenerRegistry(poolInfo);
            poolRegistries.put(poolInfo, poolRegistry);
        }else{
            poolRegistry = poolRegistries.get(poolInfo);
        }
        ConnectionPoolEmitterImpl emitter =
                new com.sun.enterprise.resource.pool.monitor.ConnectionPoolEmitterImpl(
                poolInfo, poolProvider);
        poolRegistry.registerPoolLifeCycleListener(emitter);
        addToPoolEmitters(poolInfo, emitter);
        return poolRegistry;
    }

    public ConnectionPoolAppProbeProvider registerPool(PoolInfo poolInfo, String appName) {
        ConnectionPoolAppProbeProvider probeAppProvider = null;
        Collection<ConnectionPoolMonitoringExtension> extensions =
                habitat.getAllServices(ConnectionPoolMonitoringExtension.class);
        for(ConnectionPoolMonitoringExtension extension : extensions) {
            probeAppProvider = extension.registerConnectionPool(poolInfo, appName);
        }
        return probeAppProvider;
    }

    public Resources getResources(){
        return domainProvider.get().getResources();
    }

    public ConnectionPoolProbeProviderUtil getProbeProviderUtil(){
        return connectionPoolProbeProviderUtilProvider.get();
    }

    /**
     * Register connector connection pool to the StatsProviderManager.
     * Add the pool lifecycle listeners for the pool to receive events on
     * change of any of the monitoring attribute values.
     * Finally, add this provider to the list of connector connection pool
     * providers maintained.
     * @param poolInfo
     */
    private void registerCcPool(PoolInfo poolInfo) {
        if(poolManager.getPool(poolInfo) != null) {
            getProbeProviderUtil().createJcaProbeProvider();
            //Found in the pool table (pool has been initialized/created)
            ConnectorConnPoolStatsProvider ccPoolStatsProvider =
                    new ConnectorConnPoolStatsProvider(poolInfo, logger);

            StatsProviderManager.register(
                    "connector-connection-pool",
                    PluginPoint.SERVER,
                    ConnectorsUtil.getPoolMonitoringSubTreeRoot(poolInfo, true), ccPoolStatsProvider);

            PoolLifeCycleListenerRegistry registry = registerPool(
                    poolInfo, getProbeProviderUtil().getJcaProbeProvider());
            ccPoolStatsProvider.setPoolRegistry(registry);

            ccStatsProviders.add(ccPoolStatsProvider);

            if(!ConnectorsUtil.isApplicationScopedResource(poolInfo)){
                ResourcesUtil resourcesUtil = ResourcesUtil.createInstance();
                ResourcePool pool = resourcesUtil.getPoolConfig(poolInfo);
                Resources resources = resourcesUtil.getResources(poolInfo);
                String raName = resourcesUtil.getRarNameOfResource(pool, resources);

                ConnectorConnPoolStatsProvider connectorServicePoolStatsProvider =
                        new ConnectorConnPoolStatsProvider(poolInfo, logger);

                String dottedNamesHierarchy = null;
                String monitoringModuleName = null;

                if(ConnectorsUtil.isJMSRA(raName)){
                    monitoringModuleName =  ConnectorConstants.MONITORING_JMS_SERVICE_MODULE_NAME;
                    dottedNamesHierarchy = ConnectorConstants.MONITORING_JMS_SERVICE +
                        ConnectorConstants.MONITORING_SEPARATOR + ConnectorConstants.MONITORING_CONNECTION_FACTORIES
                            + ConnectorConstants.MONITORING_SEPARATOR +
                            ConnectorsUtil.escapeResourceNameForMonitoring(poolInfo.getName());

                }else{
                    monitoringModuleName =  ConnectorConstants.MONITORING_CONNECTOR_SERVICE_MODULE_NAME;
                    dottedNamesHierarchy = ConnectorConstants.MONITORING_CONNECTOR_SERVICE_MODULE_NAME +
                    ConnectorConstants.MONITORING_SEPARATOR + raName + ConnectorConstants.MONITORING_SEPARATOR +
                    ConnectorsUtil.escapeResourceNameForMonitoring(poolInfo.getName());
                }

                StatsProviderManager.register(monitoringModuleName, PluginPoint.SERVER,
                    dottedNamesHierarchy, connectorServicePoolStatsProvider);

                if(logger.isLoggable(Level.FINE)){
                    logger.log(Level.FINE, "Registered pool-monitoring stats [ "+dottedNamesHierarchy+" ]  " +
                        "for [ " + raName + " ] with monitoring-stats-registry.");
                }

                /* no need to create multiple probe provider instances, one per pool will
                   work for multiple stats providers
                PoolLifeCycleListenerRegistry poolLifeCycleListenerRegistry = registerPool(
                        poolInfo, getProbeProviderUtil().getJcaProbeProvider());
                */

                connectorServicePoolStatsProvider.setPoolRegistry(registry);
                ccStatsProviders.add(connectorServicePoolStatsProvider);
            }
        }
    }

    /**
     * Register <code> this </code> to PoolLifeCycleRegistry so as to listen to
     * PoolLifeCycle events - pool creation or destroy.
     */
    private void registerPoolLifeCycleListener() {
        //Register provider only for server and not for clients
        if(runtime.isServer()) {
            PoolLifeCycleRegistry poolLifeCycleRegistry = PoolLifeCycleRegistry.getRegistry();
            poolLifeCycleRegistry.registerPoolLifeCycle(this);
        }
    }

    /**
     * Unregister Connector Connection pool from the StatsProviderManager.
     * Remove the pool lifecycle listeners associated with this pool.
     * @param poolInfo
     */
    private void unregisterPool(PoolInfo poolInfo) {
        if(ccStatsProviders != null) {
            Iterator i = ccStatsProviders.iterator();
            while (i.hasNext()) {
                ConnectorConnPoolStatsProvider ccPoolStatsProvider =
                        (ConnectorConnPoolStatsProvider) i.next();
                if (poolInfo.equals(ccPoolStatsProvider.getPoolInfo())) {
                    //Get registry and unregister this pool from the registry
                    PoolLifeCycleListenerRegistry poolRegistry = ccPoolStatsProvider.getPoolRegistry();
                    poolRegistry.unRegisterPoolLifeCycleListener(poolInfo);
                    StatsProviderManager.unregister(ccPoolStatsProvider);

                    i.remove();
                }
            }
        }
        postUnregisterPool(poolInfo);
    }

    public void unRegisterPool() {
        Collection<ConnectionPoolMonitoringExtension> extensions =
                habitat.getAllServices(ConnectionPoolMonitoringExtension.class);
        for(ConnectionPoolMonitoringExtension extension : extensions) {
            extension.unRegisterConnectionPool();
        }
    }

    public void postUnregisterPool(PoolInfo poolInfo) {
        unregisterPoolAppProviders(poolInfo);
        poolRegistries.remove(poolInfo);
    }

    public void unregisterPoolAppProviders(PoolInfo poolInfo) {
        ConnectionPoolEmitterImpl emitter = poolEmitters.get(poolInfo);
        //If an emitter was created for the poolInfo
        if (emitter != null) {
            emitter.unregisterAppStatsProviders();
        }
    }

    /**
     * Find if the monitoring is enabled based on the monitoring level :
     * <code> strEnabled </code>
     * @param strEnabled
     * @return
     */
    public boolean getEnabledValue(String strEnabled) {
        if ("OFF".equals(strEnabled)) {
            return false;
        }
        return true;
    }

    /**
     * When a pool is created (or initialized) the pool should be registered
     * to the  StatsProviderManager. Also, the pool lifecycle
     * listener needs to be registered for this pool to track events on change
     * of any monitoring attributes.
     * @param poolInfo
     */
    public void poolCreated(PoolInfo poolInfo) {
        if(logger.isLoggable(Level.FINEST)) {
            logger.finest("Pool created : " + poolInfo);
        }
        if(runtime.isServer()) {
            ResourcePool pool = runtime.getConnectionPoolConfig(poolInfo);
            Collection<ConnectionPoolMonitoringExtension> extensions =
                    habitat.getAllServices(ConnectionPoolMonitoringExtension.class);
            for(ConnectionPoolMonitoringExtension extension : extensions) {
                extension.registerPool(poolInfo);
            }
            if (pool instanceof ConnectorConnectionPool) {
                registerCcPool(poolInfo);
            } /*else if (poolInfo.getName().contains(ConnectorConstants.DATASOURCE_DEFINITION_JNDINAME_PREFIX)){
                registerJdbcPool(poolInfo);
            }*/
        }
    }

    /**
     * When a pool is destroyed, the pool should be unregistered from the
     * StatsProviderManager. Also, the pool's lifecycle listener
     * should be unregistered.
     * @param poolInfo
     */
    public void poolDestroyed(PoolInfo poolInfo) {
        if(logger.isLoggable(Level.FINEST)) {
            logger.finest("Pool Destroyed : " + poolInfo);
        }
        if (runtime.isServer()) {
            Collection<ConnectionPoolMonitoringExtension> extensions =
                    habitat.getAllServices(ConnectionPoolMonitoringExtension.class);
            for(ConnectionPoolMonitoringExtension extension : extensions) {
                extension.unregisterPool(poolInfo);
            }
            unregisterPool(poolInfo);
        }
    }

    /**
     * Creates jdbc-connection-pool, connector-connection-pool, connector-service
     * config elements for monitoring.
     */
    //private void createMonitoringConfig() {
    //   createMonitoringConfig(JDBC_CONNECTION_POOL, JdbcConnectionPoolMI.class);
    //   createMonitoringConfig(CONNECTOR_CONNECTION_POOL, ConnectorConnectionPoolMI.class);
    //}

    /**
     * Creates config elements for monitoring.
     *
     * Check if the monitoring config has been created.
     * If it has not, then add it.
     */
    /*private void createMonitoringConfig(final String name, final Class monitoringItemClass) {
        if (monitoringService == null) {
            logger.log(Level.SEVERE, "monitoringService is null. " +
                    "jdbc-connection-pool and connector-connection-pool monitoring config not created");
            return;
        }
        List<MonitoringItem> itemList = monitoringService.getMonitoringItems();
        boolean hasMonitorConfig = false;
        for (MonitoringItem mi : itemList) {
            if (mi.getName().equals(name)) {
                hasMonitorConfig = true;
            }
        }

        try {
            if (!hasMonitorConfig) {
                ConfigSupport.apply(new SingleConfigCode<MonitoringService>() {

                    public Object run(MonitoringService param) throws PropertyVetoException, TransactionFailure {

                        MonitoringItem newItem = (MonitoringItem) param.createChild(monitoringItemClass);
                        newItem.setName(name);
                        newItem.setLevel(MonitoringItem.LEVEL_OFF);
                        param.getMonitoringItems().add(newItem);
                        return newItem;
                    }
                }, monitoringService);
            }
        } catch (TransactionFailure tfe) {
            logger.log(Level.SEVERE, "Exception adding " + name + " MonitoringItem", tfe);
        }
    }*/
}
