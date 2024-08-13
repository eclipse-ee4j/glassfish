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

package com.sun.enterprise.v3.services.impl.monitor;

import com.sun.enterprise.v3.services.impl.monitor.probes.ConnectionQueueProbeProvider;
import com.sun.enterprise.v3.services.impl.monitor.probes.FileCacheProbeProvider;
import com.sun.enterprise.v3.services.impl.monitor.probes.KeepAliveProbeProvider;
import com.sun.enterprise.v3.services.impl.monitor.probes.ThreadPoolProbeProvider;
import com.sun.enterprise.v3.services.impl.monitor.stats.ConnectionQueueStatsProvider;
import com.sun.enterprise.v3.services.impl.monitor.stats.ConnectionQueueStatsProviderGlobal;
import com.sun.enterprise.v3.services.impl.monitor.stats.FileCacheStatsProvider;
import com.sun.enterprise.v3.services.impl.monitor.stats.FileCacheStatsProviderGlobal;
import com.sun.enterprise.v3.services.impl.monitor.stats.KeepAliveStatsProvider;
import com.sun.enterprise.v3.services.impl.monitor.stats.KeepAliveStatsProviderGlobal;
import com.sun.enterprise.v3.services.impl.monitor.stats.ThreadPoolStatsProvider;
import com.sun.enterprise.v3.services.impl.monitor.stats.ThreadPoolStatsProviderGlobal;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.glassfish.external.probe.provider.PluginPoint;
import org.glassfish.external.probe.provider.StatsProviderManager;

/**
 * Grizzly monitoring manager, which is responsible for registering, unregistering
 * Grizzly statistics probes.
 *
 * @author Alexey Stashok
 */
public class GrizzlyMonitoring {
    private static final String CONFIG_ELEMENT = "http-service";

    // network-listener->thread-pool-stats Map
    private final Map<String, ThreadPoolStatsProvider> threadPoolStatsProvidersMap =
            new ConcurrentHashMap<String, ThreadPoolStatsProvider>();
    // network-listener->file-cache-stats Map
    private final Map<String, FileCacheStatsProvider> fileCacheStatsProvidersMap =
            new ConcurrentHashMap<String, FileCacheStatsProvider>();
    // network-listener->keep-alive-stats Map
    private final Map<String, KeepAliveStatsProvider> keepAliveStatsProvidersMap =
            new ConcurrentHashMap<String, KeepAliveStatsProvider>();
    // network-listener->connection-queue-stats Map
    private final Map<String, ConnectionQueueStatsProvider> connectionQueueStatsProvidersMap =
            new ConcurrentHashMap<String, ConnectionQueueStatsProvider>();

    // thread-pool emitter probe
    private final ThreadPoolProbeProvider threadPoolProbeProvider;
    // file-cache emitter probe
    private final FileCacheProbeProvider fileCacheProbeProvider;
    // keep-alive emitter probe
    private final KeepAliveProbeProvider keepAliveProbeProvider;
    // connection queue emitter probe
    private final ConnectionQueueProbeProvider connectionQueueProbeProvider;

    public GrizzlyMonitoring() {
        threadPoolProbeProvider = new ThreadPoolProbeProvider();
        fileCacheProbeProvider = new FileCacheProbeProvider();
        keepAliveProbeProvider = new KeepAliveProbeProvider();
        connectionQueueProbeProvider = new ConnectionQueueProbeProvider();
    }

    /**
     * Get thread-pool probe provider
     *
     * @return thread-pool probe provider
     */
    public ThreadPoolProbeProvider getThreadPoolProbeProvider() {
        return threadPoolProbeProvider;
    }

    /**
     * Get file-cache probe provider
     *
     * @return file-cache probe provider
     */
    public FileCacheProbeProvider getFileCacheProbeProvider() {
        return fileCacheProbeProvider;
    }

    /**
     * Get keep-alive probe provider
     *
     * @return keep-alive probe provider
     */
    public KeepAliveProbeProvider getKeepAliveProbeProvider() {
        return keepAliveProbeProvider;
    }

    /**
     * Get connection queue probe provider
     *
     * @return connection queue probe provider
     */
    public ConnectionQueueProbeProvider getConnectionQueueProbeProvider() {
        return connectionQueueProbeProvider;
    }

    /**
     * Register thread-pool statistics provider for a network listener
     *
     * @param name network listener name
     */
    public void registerThreadPoolStatsProvider(String name) {
        ThreadPoolStatsProvider threadPoolStatsProvider = new ThreadPoolStatsProvider(name);
        ThreadPoolStatsProvider oldthreadPoolStatsProvider =
                threadPoolStatsProvidersMap.put(name, threadPoolStatsProvider);

        if (oldthreadPoolStatsProvider != null) {
            StatsProviderManager.unregister(oldthreadPoolStatsProvider);
        }

        StatsProviderManager.register(CONFIG_ELEMENT, PluginPoint.SERVER,
                subtreePrefix(name) + "/thread-pool", threadPoolStatsProvider);
    }

    /**
     * Unregister thread-pool statistics provider for a network listener
     *
     * @param name network listener name
     */
    public void unregisterThreadPoolStatsProvider(String name) {
        final ThreadPoolStatsProvider threadPoolStatsProvider =
                threadPoolStatsProvidersMap.remove(name);
        if (threadPoolStatsProvider != null) {
            StatsProviderManager.unregister(threadPoolStatsProvider);
        }
    }

    /**
     * Register keep-alive statistics provider for a network listener
     *
     * @param name network listener name
     */
    public void registerKeepAliveStatsProvider(String name) {
        KeepAliveStatsProvider keepAliveStatsProvider = new KeepAliveStatsProvider(name);
        KeepAliveStatsProvider oldKeepAliveStatsProvider =
                keepAliveStatsProvidersMap.put(name, keepAliveStatsProvider);

        if (oldKeepAliveStatsProvider != null) {
            StatsProviderManager.unregister(oldKeepAliveStatsProvider);
        }

        StatsProviderManager.register(CONFIG_ELEMENT, PluginPoint.SERVER,
                subtreePrefix(name) + "/keep-alive", keepAliveStatsProvider);
    }

    /**
     * Unregister keep-alive statistics provider for a network listener
     *
     * @param name network listener name
     */
    public void unregisterKeepAliveStatsProvider(String name) {
        final KeepAliveStatsProvider keepAliveStatsProvider =
                keepAliveStatsProvidersMap.remove(name);
        if (keepAliveStatsProvider != null) {
            StatsProviderManager.unregister(keepAliveStatsProvider);
        }
    }

    /**
     * Register file-cache statistics provider for a network listener
     *
     * @param name network listener name
     */
    public void registerFileCacheStatsProvider(String name) {
        FileCacheStatsProvider fileCacheStatsProvider = new FileCacheStatsProvider(name);
        FileCacheStatsProvider oldFileCacheStatsProvider =
                fileCacheStatsProvidersMap.put(name, fileCacheStatsProvider);

        if (oldFileCacheStatsProvider != null) {
            StatsProviderManager.unregister(oldFileCacheStatsProvider);
        }

        StatsProviderManager.register(CONFIG_ELEMENT, PluginPoint.SERVER,
                subtreePrefix(name) + "/file-cache", fileCacheStatsProvider);
    }

    /**
     * Unregister file-cache statistics provider for a network listener
     *
     * @param name network listener name
     */
    public void unregisterFileCacheStatsProvider(String name) {
        final FileCacheStatsProvider fileCacheStatsProvider =
                fileCacheStatsProvidersMap.remove(name);
        if (fileCacheStatsProvider != null) {
            StatsProviderManager.unregister(fileCacheStatsProvider);
        }
    }

    /**
     * Register connection queue statistics provider for a network listener
     *
     * @param name network listener name
     */
    public void registerConnectionQueueStatsProvider(String name) {
        ConnectionQueueStatsProvider connectionQueueStatsProvider = new ConnectionQueueStatsProvider(name);
        ConnectionQueueStatsProvider oldConnectionQueueStatsProvider =
                connectionQueueStatsProvidersMap.put(name, connectionQueueStatsProvider);

        if (oldConnectionQueueStatsProvider != null) {
            StatsProviderManager.unregister(oldConnectionQueueStatsProvider);
        }

        StatsProviderManager.register(CONFIG_ELEMENT, PluginPoint.SERVER,
                subtreePrefix(name) + "/connection-queue", connectionQueueStatsProvider);
    }

    /**
     * Unregister connection queue statistics provider for a network listener
     *
     * @param name network listener name
     */
    public void unregisterConnectionQueueStatsProvider(String name) {
        final ConnectionQueueStatsProvider connectionQueueStatsProvider =
                connectionQueueStatsProvidersMap.remove(name);
        if (connectionQueueStatsProvider != null) {
            StatsProviderManager.unregister(connectionQueueStatsProvider);
        }
    }

    /**
     * Register server wide thread-pool statistics provider
     */
    public void registerThreadPoolStatsProviderGlobal(String name) {
        ThreadPoolStatsProvider threadPoolStatsProvider = new ThreadPoolStatsProviderGlobal(name);
        ThreadPoolStatsProvider oldthreadPoolStatsProvider =
                threadPoolStatsProvidersMap.put(name, threadPoolStatsProvider);

        if (oldthreadPoolStatsProvider != null) {
            StatsProviderManager.unregister(oldthreadPoolStatsProvider);
        }

        StatsProviderManager.register(CONFIG_ELEMENT, PluginPoint.SERVER,
                subtreePrefix(name) + "/thread-pool", threadPoolStatsProvider);
    }

    /**
     * Unregister server wide thread-pool statistics provider
     */
    public void unregisterThreadPoolStatsProviderGlobal(String name) {
        final ThreadPoolStatsProvider threadPoolStatsProvider =
                threadPoolStatsProvidersMap.remove(name);
        if (threadPoolStatsProvider != null) {
            StatsProviderManager.unregister(threadPoolStatsProvider);
        }
    }

    /**
     * Register server wide keep-alive statistics provider for a network listener
     */
    public void registerKeepAliveStatsProviderGlobal(String name) {
        KeepAliveStatsProvider keepAliveStatsProvider = new KeepAliveStatsProviderGlobal(name);
        KeepAliveStatsProvider oldKeepAliveStatsProvider =
                keepAliveStatsProvidersMap.put(name, keepAliveStatsProvider);

        if (oldKeepAliveStatsProvider != null) {
            StatsProviderManager.unregister(oldKeepAliveStatsProvider);
        }

        StatsProviderManager.register(CONFIG_ELEMENT, PluginPoint.SERVER,
                subtreePrefix(name) + "/keep-alive", keepAliveStatsProvider);
    }

    /**
     * Unregister server wide keep-alive statistics provider
     */
    public void unregisterKeepAliveStatsProviderGlobal(String name) {
        final KeepAliveStatsProvider keepAliveStatsProvider =
                keepAliveStatsProvidersMap.remove(name);
        if (keepAliveStatsProvider != null) {
            StatsProviderManager.unregister(keepAliveStatsProvider);
        }
    }

    /**
     * Register server wide file-cache statistics provider for a network listener
     */
    public void registerFileCacheStatsProviderGlobal(String name) {
        FileCacheStatsProvider fileCacheStatsProvider = new FileCacheStatsProviderGlobal(name);
        FileCacheStatsProvider oldFileCacheStatsProvider =
                fileCacheStatsProvidersMap.put(name, fileCacheStatsProvider);

        if (oldFileCacheStatsProvider != null) {
            StatsProviderManager.unregister(oldFileCacheStatsProvider);
        }

        StatsProviderManager.register(CONFIG_ELEMENT, PluginPoint.SERVER,
                subtreePrefix(name) + "/file-cache", fileCacheStatsProvider);
    }

    /**
     * Unregister serve wide file-cache statistics provider for a network listener
     */
    public void unregisterFileCacheStatsProviderGlobal(String name) {
        final FileCacheStatsProvider fileCacheStatsProvider =
                fileCacheStatsProvidersMap.remove(name);
        if (fileCacheStatsProvider != null) {
            StatsProviderManager.unregister(fileCacheStatsProvider);
        }
    }

    /**
     * Register server wide connection queue statistics provider for a network listener
     */
    public void registerConnectionQueueStatsProviderGlobal(String name) {
        ConnectionQueueStatsProvider connectionQueueStatsProvider = new ConnectionQueueStatsProviderGlobal(name);
        ConnectionQueueStatsProvider oldConnectionQueueStatsProvider =
                connectionQueueStatsProvidersMap.put(name, connectionQueueStatsProvider);

        if (oldConnectionQueueStatsProvider != null) {
            StatsProviderManager.unregister(oldConnectionQueueStatsProvider);
        }

        StatsProviderManager.register(CONFIG_ELEMENT, PluginPoint.SERVER,
                subtreePrefix(name) + "/connection-queue", connectionQueueStatsProvider);
    }

    /**
     * Unregister server wide connection queue statistics provider for a network listener
     */
    public void unregisterConnectionQueueStatsProviderGlobal(String name) {
        final ConnectionQueueStatsProvider connectionQueueStatsProvider =
                connectionQueueStatsProvidersMap.remove(name);
        if (connectionQueueStatsProvider != null) {
            StatsProviderManager.unregister(connectionQueueStatsProvider);
        }
    }

    public ConnectionQueueStatsProvider getConnectionQueueStatsProvider(String name) {
        return connectionQueueStatsProvidersMap.get(name);
    }

    public FileCacheStatsProvider getFileCacheStatsProvider(String name) {
        return fileCacheStatsProvidersMap.get(name);
    }

    public KeepAliveStatsProvider getKeepAliveStatsProvider(String name) {
        return keepAliveStatsProvidersMap.get(name);
    }

    public ThreadPoolStatsProvider getThreadPoolStatsProvider(String name) {
        return threadPoolStatsProvidersMap.get(name);
    }

    private String subtreePrefix(String name) {
        return "network/" + name;
    }
}
