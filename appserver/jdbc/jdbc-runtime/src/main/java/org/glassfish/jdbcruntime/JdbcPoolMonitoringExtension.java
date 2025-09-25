/*
 * Copyright (c) 2023, 2025 Contributors to the Eclipse Foundation
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

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.connectors.ConnectionPoolMonitoringExtension;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.pool.PoolLifeCycleListenerRegistry;
import com.sun.enterprise.resource.pool.PoolManager;
import com.sun.enterprise.resource.pool.monitor.ConnectionPoolAppProbeProvider;
import com.sun.enterprise.resource.pool.monitor.ConnectionPoolProbeProviderUtil;
import com.sun.enterprise.resource.pool.monitor.ConnectionPoolStatsProviderBootstrap;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.jdbc.config.JdbcConnectionPool;
import org.glassfish.jdbc.pool.monitor.JdbcConnPoolAppProbeProvider;
import org.glassfish.jdbc.pool.monitor.JdbcConnPoolAppStatsProvider;
import org.glassfish.jdbc.pool.monitor.JdbcConnPoolProbeProvider;
import org.glassfish.jdbc.pool.monitor.JdbcConnPoolStatsProvider;
import org.glassfish.resourcebase.resources.api.PoolInfo;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Shalini M
 */
@Service
public class JdbcPoolMonitoringExtension implements ConnectionPoolMonitoringExtension {

    @Inject
    private Provider<ConnectionPoolProbeProviderUtil> connectionPoolProbeProviderUtilProvider;

    @Inject
    private Provider<ConnectionPoolStatsProviderBootstrap> connectionPoolStatsProviderBootstrapProvider;

    @Inject
    private PoolManager poolManager;

    private final ConnectorRuntime runtime;

    // List of all jdbc pool stats providers that are created and stored.
    private final List<JdbcConnPoolStatsProvider> jdbcStatsProviders;
    private final List<JdbcConnPoolAppStatsProvider> jdbcPoolAppStatsProviders;

    public JdbcPoolMonitoringExtension() {
        jdbcStatsProviders = new ArrayList<>();
        jdbcPoolAppStatsProviders = new ArrayList<>();
        runtime = ConnectorRuntime.getRuntime();
    }

    public ConnectionPoolProbeProviderUtil getProbeProviderUtil() {
        return connectionPoolProbeProviderUtilProvider.get();
    }

    /**
     * Register jdbc connection pool to the StatsProviderManager. Add the pool
     * lifecycle listeners for the pool to receive events on change of any of the
     * monitoring attribute values. Finally, add this provider to the list of jdbc
     * providers maintained.
     *
     * @param poolInfo
     */
    @Override
    public void registerPool(PoolInfo poolInfo) {
        ResourcePool pool = runtime.getConnectionPoolConfig(poolInfo);
        if (pool instanceof JdbcConnectionPool && poolManager.getPool(poolInfo) != null) {
            getProbeProviderUtil().createJdbcProbeProvider();
            // Found in the pool table (pool has been initialized/created)
            JdbcConnPoolStatsProvider jdbcPoolStatsProvider = new JdbcConnPoolStatsProvider(poolInfo);
            StatsProviderManager.register("jdbc-connection-pool", PluginPoint.SERVER,
                    ConnectorsUtil.getPoolMonitoringSubTreeRoot(poolInfo, true), jdbcPoolStatsProvider);
            // String jdbcPoolName = jdbcPoolStatsProvider.getJdbcPoolName();
            PoolLifeCycleListenerRegistry registry = connectionPoolStatsProviderBootstrapProvider.get().registerPool(poolInfo,
                    getProbeProviderUtil().getJdbcProbeProvider());
            jdbcPoolStatsProvider.setPoolRegistry(registry);
            jdbcStatsProviders.add(jdbcPoolStatsProvider);
        }
    }

    /**
     * Unregister Jdbc Connection pool from the StatsProviderManager. Remove the
     * pool lifecycle listeners associated with this pool.
     *
     * @param poolInfo
     */
    @Override
    public void unregisterPool(PoolInfo poolInfo) {
        if (jdbcStatsProviders != null) {
            Iterator<JdbcConnPoolStatsProvider> i = jdbcStatsProviders.iterator();
            while (i.hasNext()) {
                JdbcConnPoolStatsProvider jdbcPoolStatsProvider = i.next();
                if (poolInfo.equals(jdbcPoolStatsProvider.getPoolInfo())) {
                    // Get registry and unregister this pool from the registry
                    PoolLifeCycleListenerRegistry poolRegistry = jdbcPoolStatsProvider.getPoolRegistry();
                    poolRegistry.unRegisterPoolLifeCycleListener(poolInfo);
                    StatsProviderManager.unregister(jdbcPoolStatsProvider);

                    i.remove();
                }
            }
        }
        connectionPoolStatsProviderBootstrapProvider.get().postUnregisterPool(poolInfo);
    }

    /**
     * Register the jdbc connection pool Stats Provider object to the monitoring
     * framework under the specific application name monitoring sub tree.
     *
     * @param appName
     * @return
     */
    @Override
    public ConnectionPoolAppProbeProvider registerConnectionPool(PoolInfo poolInfo, String appName) {
        ConnectionPoolAppProbeProvider probeAppProvider = null;
        ResourcePool pool = runtime.getConnectionPoolConfig(poolInfo);
        if (pool instanceof JdbcConnectionPool) {
            probeAppProvider = new JdbcConnPoolAppProbeProvider();
            JdbcConnPoolAppStatsProvider jdbcPoolAppStatsProvider = new JdbcConnPoolAppStatsProvider(poolInfo, appName);
            StatsProviderManager.register("jdbc-connection-pool", PluginPoint.SERVER,
                    "resources/" + ConnectorsUtil.escapeResourceNameForMonitoring(poolInfo.getName()) + "/" + appName,
                    jdbcPoolAppStatsProvider);
            jdbcPoolAppStatsProviders.add(jdbcPoolAppStatsProvider);
        }
        return probeAppProvider;
    }

    /**
     * Unregister the AppStatsProviders registered for this connection pool.
     */
    @Override
    public void unRegisterConnectionPool() {
        for (JdbcConnPoolAppStatsProvider jdbcPoolAppStatsProvider : jdbcPoolAppStatsProviders) {
            StatsProviderManager.unregister(jdbcPoolAppStatsProvider);
        }
        jdbcPoolAppStatsProviders.clear();
    }

    @Override
    public JdbcConnPoolProbeProvider createProbeProvider() {
        return new JdbcConnPoolProbeProvider();
    }

}
