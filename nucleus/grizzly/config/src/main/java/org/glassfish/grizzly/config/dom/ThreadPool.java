/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.grizzly.config.dom;

import java.beans.PropertyVetoException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jvnet.hk2.config.Attribute;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.Configured;
import org.jvnet.hk2.config.Dom;
import org.jvnet.hk2.config.types.PropertyBag;

@Configured
public interface ThreadPool extends ConfigBeanProxy, PropertyBag {

    String DEFAULT_THREAD_POOL_CLASS_NAME = "org.glassfish.grizzly.threadpool.GrizzlyExecutorService";

    int IDLE_THREAD_TIMEOUT = 900;

    int MAX_QUEUE_SIZE = 4096;

    // min and max are set to the same value to force the use
    // of the fixed thread pool in default configuration cases.
    // This thread pool offers better performance characteristics
    // over the sync thread pool.
    int MAX_THREADPOOL_SIZE = 5;

    int MIN_THREADPOOL_SIZE = 5;

    /**
     * The classname of a thread pool implementation.
     */
    @Attribute(defaultValue = DEFAULT_THREAD_POOL_CLASS_NAME)
    String getClassname();

    void setClassname(String classname);

    /**
     * Idle threads are removed from pool, after this time (in seconds).
     */
    @Attribute(defaultValue = "" + IDLE_THREAD_TIMEOUT, dataType = Integer.class)
    String getIdleThreadTimeoutSeconds();

    void setIdleThreadTimeoutSeconds(String idleThreadTimeout);

    /**
     * The maxim number of tasks, which could be queued on the thread pool.
     *
     * <p>{@code -1} disables any maximum checks.
     */
    @Attribute(defaultValue = "" + MAX_QUEUE_SIZE, dataType = Integer.class)
    String getMaxQueueSize();

    void setMaxQueueSize(String maxQueueSize);

    /**
     * Maximum number of threads in the thread pool servicing
     * requests in this queue. This is the upper bound on the number
     * of threads that exist in the thread pool.
     */
    @Attribute(defaultValue = "" + MAX_THREADPOOL_SIZE, dataType = Integer.class)
    String getMaxThreadPoolSize();

    void setMaxThreadPoolSize(String maxThreadPoolSize) throws PropertyVetoException;

    /**
     * Minimum number of threads in the thread pool servicing
     * requests in this queue. These are created up front when this
     * thread pool is instantiated
     */
    @Attribute(defaultValue = "" + MIN_THREADPOOL_SIZE, dataType = Integer.class)
    String getMinThreadPoolSize();

    void setMinThreadPoolSize(String minThreadPoolSize);

    /**
     * This is an id for the work-queue e.g. {@code thread-pool-1}, {@code thread-pool-2} etc.
     */
    @Attribute(required = true, key = true)
    String getName();

    void setName(String name);

    /**
     * This is an id for the work-queue e.g. {@code thread-pool-1}, {@code thread-pool-2} etc.
     */
    @Attribute
    @Deprecated
    String getThreadPoolId();

    void setThreadPoolId(String threadPoolId);

    default List<NetworkListener> findNetworkListeners() {
        NetworkConfig config = getParent().getParent(NetworkConfig.class);
        Dom configProxy = Objects.requireNonNull(Dom.unwrap(config));
        if (!configProxy.getProxyType().equals(NetworkConfig.class)) {
            config = configProxy.element("network-config").createProxy();
        }

        List<NetworkListener> networkListeners = new ArrayList<>();
        for (NetworkListener listener : config.getNetworkListeners().getNetworkListener()) {
            if (listener.getThreadPool().equals(getName())) {
                networkListeners.add(listener);
            }
        }
        return networkListeners;
    }
}
