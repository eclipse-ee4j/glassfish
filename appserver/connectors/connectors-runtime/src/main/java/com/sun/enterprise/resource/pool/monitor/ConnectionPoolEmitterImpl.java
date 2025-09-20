/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

import com.sun.appserv.connectors.internal.api.ConnectorsUtil;
import com.sun.enterprise.config.serverbeans.ResourcePool;
import com.sun.enterprise.connectors.ConnectorRuntime;
import com.sun.enterprise.resource.listener.PoolLifeCycleListener;

import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.glassfish.connectors.config.ConnectorConnectionPool;
import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;
import org.glassfish.resourcebase.resources.api.PoolInfo;

import static com.sun.appserv.connectors.internal.api.ConnectorsUtil.escapeResourceNameForMonitoring;
import static java.lang.System.Logger.Level.TRACE;
import static org.glassfish.api.naming.SimpleJndiName.JNDI_CTX_JAVA_APP;

/**
 * Implementation of PoolLifeCycleListener interface to listen to events related
 * to jdbc monitoring. The methods invoke the probe providers internally to
 * provide the monitoring related information.
 *
 * @author Shalini M
 */
public class ConnectionPoolEmitterImpl implements PoolLifeCycleListener {
    private static final Logger LOG = System.getLogger(ConnectionPoolEmitterImpl.class.getName());
    // keep a static reference to InitialContext so as to avoid performance issues.
    private static volatile InitialContext ic;

    private final PoolInfo poolInfo;
    private final ConnectionPoolProbeProvider poolProbeProvider;
    /** Map of app names and respective emitters for a pool. */
    private final Map<PoolInfo, Map<String, ConnectionPoolAppEmitterImpl>> appStatsMap;
    /** Map of app names for a resource handle id */
    private final Map<Long, String> resourceAppAssociationMap;
    private final List<ConnectorConnPoolAppStatsProvider> ccPoolAppStatsProviders;
    private final ConnectorRuntime runtime;


    /**
     * Constructor
     *
     * @param poolInfo connection pool on whose behalf this emitter emits pool related
     *            probe events
     * @param provider
     */
    public ConnectionPoolEmitterImpl(PoolInfo poolInfo, ConnectionPoolProbeProvider provider) {
        this.poolInfo = poolInfo;
        this.poolProbeProvider = provider;
        this.ccPoolAppStatsProviders = new ArrayList<>();
        this.appStatsMap = new HashMap<>();
        this.resourceAppAssociationMap = new ConcurrentHashMap<>();
        this.runtime = ConnectorRuntime.getRuntime();
        if (ic == null) {
            synchronized (ConnectionPoolEmitterImpl.class) {
                if (ic == null) {
                    try {
                        ic = new InitialContext();
                    } catch (NamingException e) {
                        // ignore
                    }
                }
            }
        }
    }

    /**
     * Fires probe event that a stack trace is to be printed on the server.log.
     * The stack trace is mainly related to connection leak tracing for the
     * given jdbc connection pool.
     * @param stackTrace
     */
    @Override
    public void toString(StringBuffer stackTrace) {
        stackTrace.append("\n Monitoring Statistics for \n" + poolInfo);
        poolProbeProvider.toString(poolInfo.getName().toString(), poolInfo.getApplicationName(), poolInfo.getModuleName(), stackTrace);
    }

    /**
     * Fires probe event that a connection has been acquired by the application
     * for the given jdbc connection pool.
     */
    @Override
    public void connectionAcquired(long resourceHandleId) {
        ConnectionPoolAppEmitterImpl appEmitter =
                detectAppBasedProviders(getAppName(resourceHandleId));
        poolProbeProvider.connectionAcquiredEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(), poolInfo.getModuleName());
        if(appEmitter != null) {
            appEmitter.connectionAcquired();
        }
    }

    /**
     * Fires probe event related to the fact that a connection request is served
     * in the time <code>timeTakenInMillis</code> for the given jdbc connection
     * pool.
     *
     * @param timeTakenInMillis time taken to serve a connection
     */
    @Override
    public void connectionRequestServed(long timeTakenInMillis) {
        poolProbeProvider.connectionRequestServedEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(), poolInfo.getModuleName(), timeTakenInMillis);
    }

    /**
     * Fires probe event related to the fact that the given jdbc connection pool
     * has got a connection timed-out event.
     */
    @Override
    public void connectionTimedOut() {
        poolProbeProvider.connectionTimedOutEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(), poolInfo.getModuleName());
    }

    /**
     * Fires probe event that a connection under test does not match the
     * current request for the given jdbc connection pool.
     */
    @Override
    public void connectionNotMatched() {
        poolProbeProvider.connectionNotMatchedEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(), poolInfo.getModuleName());
    }

    /**
     * Fires probe event that a connection under test matches the current
     * request for the given jdbc connection pool.
     */
    @Override
    public void connectionMatched() {
        poolProbeProvider.connectionMatchedEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(), poolInfo.getModuleName());
    }

    /**
     * Fires probe event that a connection is destroyed for the
     * given jdbc connection pool.
     */
    @Override
    public void connectionDestroyed(long resourceHandleId) {
        poolProbeProvider.connectionDestroyedEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(), poolInfo.getModuleName());
        // Clearing the resource handle id appName mappings stored
        // This is useful in cases where connection-leak-reclaim is ON where we destroy
        // the connection. In this case, connection-release would not have happened.
        resourceAppAssociationMap.remove(resourceHandleId);
    }

    /**
     * Fires probe event that a connection is released for the given jdbc
     * connection pool.
     */
    @Override
    public void connectionReleased(long resourceHandleId) {
        ConnectionPoolAppEmitterImpl appEmitter = detectAppBasedProviders(getAppName(resourceHandleId));
        poolProbeProvider.connectionReleasedEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(),
            poolInfo.getModuleName());
        if (appEmitter != null) {
            appEmitter.connectionReleased();
        }
        // Clearing the resource handle id appName mappings stored
        resourceAppAssociationMap.remove(resourceHandleId);
    }

    /**
     * Fires probe event that a connection is created for the given jdbc
     * connection pool.
     */
    @Override
    public void connectionCreated() {
        poolProbeProvider.connectionCreatedEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(), poolInfo.getModuleName());
    }

    /**
     * Fires probe event related to the fact that the given jdbc connection pool
     * has got a connection leak event.
     *
     */
    @Override
    public void foundPotentialConnectionLeak() {
        poolProbeProvider.potentialConnLeakEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(), poolInfo.getModuleName());
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a connection validation failed event.
     *
     * @param count number of times the validation failed
     */
    @Override
    public void connectionValidationFailed(int count) {
        poolProbeProvider.connectionValidationFailedEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(), poolInfo.getModuleName(), count);
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a connection used event.
     */
    @Override
    public void connectionUsed(long resourceHandleId) {
        ConnectionPoolAppEmitterImpl appEmitter =
                detectAppBasedProviders(getAppName(resourceHandleId));
        poolProbeProvider.connectionUsedEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(), poolInfo.getModuleName());
        if (appEmitter != null) {
            appEmitter.connectionUsed();
        }
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a connection freed event.
     *
     * @param count number of connections freed to pool
     */
    @Override
    public void connectionsFreed(int count) {
        poolProbeProvider.connectionsFreedEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(), poolInfo.getModuleName(), count);
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a decrement connection used event.
     *
     */
    @Override
    public void decrementConnectionUsed(long resourceHandleId) {
        ConnectionPoolAppEmitterImpl appEmitter = detectAppBasedProviders(getAppName(resourceHandleId));
        poolProbeProvider.decrementConnectionUsedEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(),
            poolInfo.getModuleName());
        if (appEmitter != null) {
            appEmitter.decrementConnectionUsed();
        }
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a decrement free connections size event.
     *
     */
    @Override
    public void decrementNumConnFree() {
        poolProbeProvider.decrementNumConnFreeEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(), poolInfo.getModuleName());
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool has
     * got a decrement free connections size event.
     *
     * @param beingDestroyed if the connection is destroyed due to error
     * @param steadyPoolSize
     */
    @Override
    public void incrementNumConnFree(boolean beingDestroyed, int steadyPoolSize) {
        poolProbeProvider.incrementNumConnFreeEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(), poolInfo.getModuleName(), beingDestroyed, steadyPoolSize);
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool's
     * wait queue length has been incremented
     *
     */
    @Override
    public void connectionRequestQueued() {
        poolProbeProvider.connectionRequestQueuedEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(), poolInfo.getModuleName());
    }

    /**
     * Fires probe event related to the fact the given jdbc connection pool's
     * wait queue length has been decremented.
     *
     */
    @Override
    public void connectionRequestDequeued() {
        poolProbeProvider.connectionRequestDequeuedEvent(poolInfo.getName().toString(), poolInfo.getApplicationName(), poolInfo.getModuleName());
    }

    private String getAppName(long resourceHandleId) {

        // if monitoring is disabled, avoid sending events
        // as we need to do "java:app/AppName" to get applicationName for each
        // acquire/return connection call which is a performance bottleneck.
        if (!runtime.isJdbcPoolMonitoringEnabled() && !runtime.isConnectorPoolMonitoringEnabled()) {
            return null;
        }

        String appName = resourceAppAssociationMap.get(resourceHandleId);
        if (appName != null) {
            return appName;
        }
        try {
            if (ic == null) {
                synchronized (ConnectionPoolEmitterImpl.class) {
                    if (ic == null) {
                        ic = new InitialContext();
                    }
                }
            }
            appName = (String) ic.lookup(JNDI_CTX_JAVA_APP + "AppName");
            resourceAppAssociationMap.put(resourceHandleId, appName);
            return appName;
        } catch (NamingException e) {
            // Don't print stacktrace, it creates too much noise.
            LOG.log(TRACE, "Unable to get application name using java:app/AppName method: " + e);
            return null;
        }
    }

    /**
     * Detect if a Stats Provider has already been registered to the
     * monitoring framework for this appName and if so, return the specific
     * emitter. If not already registered, create and register the
     * Stats Provider object to the monitoring framework and add to the list
     * of emitters.
     *
     * @param appName
     * @return
     */
    private ConnectionPoolAppEmitterImpl detectAppBasedProviders(String appName) {
        if (appName == null) {
            //Case when appname cannot be detected. Emitter cannot exist for
            //a null appName for any pool.
            return null;
        }
        if (appStatsMap.containsKey(poolInfo)) {
            // Some apps have been registered for this pool.
            // Find if this appName is already registered.
            // All appEmitters for this pool
            Map<String, ConnectionPoolAppEmitterImpl> appEmitters = appStatsMap.get(poolInfo);
            //Check if the appEmitters list has an emitter for the appName.
            ConnectionPoolAppEmitterImpl emitter = appEmitters.get(appName);
            if (emitter != null) {
                // This appName has already been registered to StatsProviderManager
                return emitter;
            }
            if (ConnectorsUtil.isApplicationScopedResource(poolInfo)) {
                return null;
            }
            // register to the StatsProviderManager and add to the list.
            ConnectionPoolAppProbeProvider probeAppProvider = registerConnectionPool(appName);
            return addToList(appName, probeAppProvider, appEmitters);
        } else if (ConnectorsUtil.isApplicationScopedResource(poolInfo)) {
            return null;
        } else {
            // Does not contain any app providers associated with this poolname
            // Create a map of app emitters for the appName and add them to the
            // appStatsMap
            ConnectionPoolAppProbeProvider probeAppProvider = registerConnectionPool(appName);
            return addToList(appName, probeAppProvider, new HashMap<>());
        }
    }

    /**
     * Register the jdbc/connector connection pool Stats Provider object to the
     * monitoring framework under the specific application name monitoring
     * sub tree.
     *
     * @param appName
     * @return
     */
    private ConnectionPoolAppProbeProvider registerConnectionPool(String appName) {
        ResourcePool pool = runtime.getConnectionPoolConfig(poolInfo);
        ConnectionPoolAppProbeProvider probeAppProvider =
                runtime.getProbeProviderUtil().getConnPoolBootstrap().registerPool(poolInfo, appName);
        if (pool instanceof ConnectorConnectionPool) {
            probeAppProvider = new ConnectorConnPoolAppProbeProvider();
            ConnectorConnPoolAppStatsProvider ccPoolAppStatsProvider =
                    new ConnectorConnPoolAppStatsProvider(poolInfo, appName);
            StatsProviderManager.register(
                    "connector-connection-pool",
                    PluginPoint.SERVER,
                    "resources/" + escapeResourceNameForMonitoring(poolInfo.getName()) + "/" + appName,
                    ccPoolAppStatsProvider);
            ccPoolAppStatsProviders.add(ccPoolAppStatsProvider);
        }
        return probeAppProvider;
    }

    private ConnectionPoolAppEmitterImpl addToList(String appName, ConnectionPoolAppProbeProvider probeAppProvider,
        Map<String, ConnectionPoolAppEmitterImpl> appEmitters) {
        ConnectionPoolAppEmitterImpl emitter = addEmitter(appName, probeAppProvider, appEmitters);
        runtime.getProbeProviderUtil().getConnPoolBootstrap().addToPoolEmitters(poolInfo, this);
        return emitter;
    }

    /**
     * Add to the pool emitters list. the connection pool application emitter
     * for the specific poolInfo and appName.
     *
     * @param appName this appName here is different from "appName" instance variable.
     * @param probeAppProvider
     * @param appEmitters
     * @return
     */
    private ConnectionPoolAppEmitterImpl addEmitter(String appName, ConnectionPoolAppProbeProvider probeAppProvider,
        Map<String, ConnectionPoolAppEmitterImpl> appEmitters) {
        if (probeAppProvider == null) {
            return null;
        }
         ConnectionPoolAppEmitterImpl appEmitter = new ConnectionPoolAppEmitterImpl(poolInfo.getName(), appName,
            probeAppProvider);
        appEmitters.put(appName, appEmitter);
        appStatsMap.put(poolInfo, appEmitters);
        return appEmitter;
    }

    /**
     * Unregister the AppStatsProviders registered for this connection pool.
     */
    public void unregisterAppStatsProviders() {
        runtime.getProbeProviderUtil().getConnPoolBootstrap().unRegisterPool();
        for (ConnectorConnPoolAppStatsProvider ccPoolAppStatsProvider : ccPoolAppStatsProviders) {
            StatsProviderManager.unregister(ccPoolAppStatsProvider);
        }
        ccPoolAppStatsProviders.clear();
    }
}
