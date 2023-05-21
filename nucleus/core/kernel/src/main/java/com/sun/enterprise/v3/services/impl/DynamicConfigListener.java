/*
 * Copyright (c) 2007, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.services.impl;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.config.GrizzlyListener;
import org.glassfish.grizzly.config.dom.FileCache;
import org.glassfish.grizzly.config.dom.Http;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.Ssl;
import org.glassfish.grizzly.config.dom.ThreadPool;
import org.glassfish.grizzly.config.dom.Transport;
import org.glassfish.grizzly.impl.FutureImpl;
import org.glassfish.grizzly.impl.SafeFutureImpl;
import org.jvnet.hk2.config.Changed;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigListener;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.NotProcessed;
import org.jvnet.hk2.config.UnprocessedChangeEvents;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.util.Result;

/**
 * Grizzly dynamic configuration handler
 *
 * @author Alexey Stashok
 */
public class DynamicConfigListener implements ConfigListener {
    private static final String ADMIN_LISTENER = "admin-listener";
    private GrizzlyService grizzlyService;
    private Config config;
    private final Logger logger;
    private static final int RECONFIG_LOCK_TIMEOUT_SEC = 30;
    private static final ReentrantLock reconfigLock = new ReentrantLock();
    private static final Map<Integer, FutureImpl> reconfigByPortLock =
            new HashMap<>();

    public DynamicConfigListener(final Config parent, final Logger logger) {
        config = parent;
        this.logger = logger;
    }

    @Override
    public synchronized UnprocessedChangeEvents changed(final PropertyChangeEvent[] events) {
        return ConfigSupport.sortAndDispatch(
            events, new Changed() {
                @Override
                public <T extends ConfigBeanProxy> NotProcessed changed(TYPE type,
                    Class<T> tClass, T t) {
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "NetworkConfig changed {0} {1} {2}",
                                new Object[]{type, tClass, t});
                    }
                    if (tClass == NetworkListener.class && t instanceof NetworkListener) {
                        return processNetworkListener(type, (NetworkListener) t, events);
                    } else if (tClass == Http.class && t instanceof Http) {
                        return processProtocol(type, (Protocol) t.getParent(), events);
                    } else if (tClass == FileCache.class && t instanceof FileCache) {
                        return processProtocol(type, (Protocol) t.getParent().getParent(), null);
                    } else if (tClass == Ssl.class && t instanceof Ssl) {
                        /*
                         * Make sure the SSL parent is in fact a protocol.  It could
                         * be a jmx-connector.
                         */
                        final ConfigBeanProxy parent = t.getParent();
                        if (parent instanceof Protocol) {
                            return processProtocol(type, (Protocol) parent, null);
                        }
                    } else if (tClass == Protocol.class && t instanceof Protocol) {
                        return processProtocol(type, (Protocol) t, null);
                    } else if (tClass == ThreadPool.class && t instanceof ThreadPool) {
                        NotProcessed notProcessed = null;
                        for (NetworkListener listener : ((ThreadPool) t).findNetworkListeners()) {
                            notProcessed = processNetworkListener(type, listener, null);
                        }
                        return notProcessed;
                    } else if (tClass == Transport.class && t instanceof Transport) {
                        NotProcessed notProcessed = null;
                        for (NetworkListener listener : ((Transport) t).findNetworkListeners()) {
                            notProcessed = processNetworkListener(type, listener, null);
                        }
                        return notProcessed;
                    } else if (tClass == VirtualServer.class
                                    && t instanceof VirtualServer
                                    && !grizzlyService.hasMapperUpdateListener()) {
                        return processVirtualServer(type, (VirtualServer) t);
                    } else if (tClass == SystemProperty.class && t instanceof SystemProperty) {
                        NetworkConfig networkConfig = config.getNetworkConfig();
                        if ((networkConfig != null) && ((SystemProperty)t).getName().endsWith("LISTENER_PORT")) {
                            for (NetworkListener listener : networkConfig.getNetworkListeners().getNetworkListener()) {
                                if (listener.getPort().equals(((SystemProperty)t).getValue())) {
                                    return processNetworkListener(Changed.TYPE.CHANGE, listener, events);
                                }
                            }
                        }
                        return null;
                    }
                    return null;
                }
            }, logger);
    }

    private <T extends ConfigBeanProxy> NotProcessed processNetworkListener(Changed.TYPE type,
        NetworkListener listener, PropertyChangeEvent[] changedProperties) {

        if (findConfigName(listener).equals(findConfigName(config))) {

            boolean isAdminListener = ADMIN_LISTENER.equals(listener.getName());
            Lock portLock = null;
            try {
                portLock = acquirePortLock(listener);
                if (type == Changed.TYPE.ADD) {
                    final int[] ports = portLock.getPorts();
                    if (isAdminListener && ports[ports.length - 1] == -1) {
                        return null;
                    }

                    final Future future = grizzlyService.createNetworkProxy(listener);
                    if (future != null) {
                        future.get(RECONFIG_LOCK_TIMEOUT_SEC, TimeUnit.SECONDS);
                        grizzlyService.registerContainerAdapters();
                    } else {
                        logger.log(Level.FINE, "Skipping proxy registration for the listener {0}",
                                listener.getName());
                    }
                } else if (type == Changed.TYPE.REMOVE) {
                    if (!isAdminListener) {
                        grizzlyService.removeNetworkProxy(listener);
                    }
                } else if (type == Changed.TYPE.CHANGE) {
                    if (isAdminListener) {
                        final boolean dynamic = isAdminDynamic(changedProperties);
                        if (dynamic) {
                            GrizzlyProxy proxy = (GrizzlyProxy) grizzlyService.lookupNetworkProxy(listener);
                            if (proxy != null) {
                                GrizzlyListener netListener = proxy.getUnderlyingListener();
                                netListener.processDynamicConfigurationChange(
                                        grizzlyService.getServiceLocator(), changedProperties);
                                return null;
                            }
                        }
                        return null;
                    }

                    // Restart the network listener
                    grizzlyService.restartNetworkListener(listener, RECONFIG_LOCK_TIMEOUT_SEC, TimeUnit.SECONDS);
                }
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Network listener configuration error. Type: " + type, e);
            } finally {
                if (portLock != null) {
                    releaseListenerLock(portLock);
                }
            }
        }
        return null;
    }

    private String findConfigName(final ConfigBeanProxy child) {
        ConfigBeanProxy bean = child;
        while(bean != null && ! (bean instanceof Config)) {
            bean = bean.getParent();
        }
        return bean != null ? ((Config) bean).getName() : "";
    }

    private boolean isAdminDynamic(PropertyChangeEvent[] events) {
        if (events == null || events.length == 0) {
            return false;
        }
        // for now, anything other than comet support will require a restart
        for (PropertyChangeEvent e : events) {
            if ("comet-support-enabled".equals(e.getPropertyName())) {
                return true;
            }
        }
        return false;
    }

    private NotProcessed processProtocol(Changed.TYPE type, Protocol protocol, PropertyChangeEvent[] events) {
        NotProcessed notProcessed = null;
        for (NetworkListener listener : protocol.findNetworkListeners()) {
            notProcessed = processNetworkListener(type, listener, events);
        }
        return notProcessed;
    }

    private NotProcessed processVirtualServer(Changed.TYPE type, VirtualServer vs) {
        NotProcessed notProcessed = null;
        for (NetworkListener n : vs.findNetworkListeners()) {
            notProcessed = processNetworkListener(type, n, null);
        }
        return notProcessed;
    }

    public void setGrizzlyService(GrizzlyService grizzlyService) {
        this.grizzlyService = grizzlyService;
    }

    public Logger getLogger() {
        return logger;
    }

    /**
     * Lock TCP ports, which will take part in the reconfiguration to avoid collisions
     */
    private Lock acquirePortLock(NetworkListener listener) throws InterruptedException, TimeoutException {
        final boolean isLoggingFinest = logger.isLoggable(Level.FINEST);
        final int port = getPort(listener);
        try {
            while (true) {
                logger.finest("Aquire reconfig lock");
                if (reconfigLock.tryLock(RECONFIG_LOCK_TIMEOUT_SEC, TimeUnit.SECONDS)) {
                    Future lock = reconfigByPortLock.get(port);
                    if (isLoggingFinest) {
                        logger.log(Level.FINEST, "Reconfig lock for port: {0} is {1}",
                                new Object[]{port, lock});
                    }
                    int proxyPort = -1;
                    if (lock == null) {
                        final NetworkProxy runningProxy = grizzlyService.lookupNetworkProxy(listener);
                        if (runningProxy != null) {
                            proxyPort = runningProxy.getPort();
                            if (port != proxyPort) {
                                lock = reconfigByPortLock.get(proxyPort);
                                if (isLoggingFinest) {
                                    logger.log(Level.FINEST, "Reconfig lock for proxyport: {0} is {1}",
                                            new Object[]{proxyPort, lock});
                                }
                            } else {
                                proxyPort = -1;
                            }
                        }
                    }
                    if (lock != null) {
                        reconfigLock.unlock();
                        try {
                            logger.finest("Waiting on reconfig lock");
                            lock.get(RECONFIG_LOCK_TIMEOUT_SEC, TimeUnit.SECONDS);
                        } catch (ExecutionException e) {
                            throw new IllegalStateException(e);
                        }
                    } else {
                        final FutureImpl future = SafeFutureImpl.create();
                        if (isLoggingFinest) {
                            logger.log(Level.FINEST, "Set reconfig lock for ports: {0} and {1}: {2}",
                                    new Object[]{port, proxyPort, future});
                        }
                        reconfigByPortLock.put(port, future);
                        if (proxyPort != -1) {
                            reconfigByPortLock.put(proxyPort, future);
                        }
                        return new Lock(port, proxyPort);
                    }
                } else {
                    throw new TimeoutException("Lock timeout");
                }
            }
        } finally {
            if (reconfigLock.isHeldByCurrentThread()) {
                reconfigLock.unlock();
            }
        }
    }

    private void releaseListenerLock(Lock lock) {
        final boolean isLoggingFinest = logger.isLoggable(Level.FINEST);
        reconfigLock.lock();
        try {
            final int[] ports = lock.getPorts();
            if (isLoggingFinest) {
                logger.log(Level.FINEST, "Release reconfig lock for ports: {0}",
                        Arrays.toString(ports));
            }
            FutureImpl future = null;
            for (int port : ports) {
                if (port != -1) {
                    future = reconfigByPortLock.remove(port);
                }
            }
            if (future != null) {
                if (isLoggingFinest) {
                    logger.log(Level.FINEST, "Release reconfig lock, set result: {0}",
                            future);
                }
                future.result(new Result<>(Thread.currentThread()));
            }
        } finally {
            reconfigLock.unlock();
        }
    }

    private int getPort(NetworkListener listener) {
        int listenerPort = -1;
        try {
            listenerPort = Integer.parseInt(listener.getPort());
        } catch (NumberFormatException e) {
            if (logger.isLoggable(Level.WARNING)) {
                logger.log(Level.WARNING, "Can not parse network-listener port number: {0}",
                        listener.getPort());
            }
        }
        return listenerPort;
    }

    private static final class Lock {
        private final int[] ports;

        public Lock(int... ports) {
            this.ports = ports;
        }

        public int[] getPorts() {
            return ports;
        }
    }
}
