/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import static java.util.Collections.newSetFromMap;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static org.glassfish.api.admin.ServerEnvironment.DEFAULT_INSTANCE_NAME;
import static org.glassfish.api.event.EventTypes.SERVER_READY;
import static org.glassfish.common.util.Constants.IMPORTANT_RUN_LEVEL_SERVICE;

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import org.glassfish.api.FutureProvider;
import org.glassfish.api.StartupRunLevel;
import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.api.container.RequestDispatcher;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.event.EventListener;
import org.glassfish.api.event.EventTypes;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.RestrictTo;
import org.glassfish.grizzly.config.GenericGrizzlyListener;
import org.glassfish.grizzly.config.dom.NetworkConfig;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.NetworkListeners;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.http.HttpCodecFilter;
import org.glassfish.grizzly.http.HttpProbe;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.util.Mapper;
import org.glassfish.grizzly.impl.FutureImpl;
import org.glassfish.grizzly.utils.Futures;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.api.Rank;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.runlevel.RunLevel;
import org.glassfish.kernel.KernelLoggerInfo;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.ConfigBeanProxy;
import org.jvnet.hk2.config.ConfigSupport;
import org.jvnet.hk2.config.ObservableBean;
import org.jvnet.hk2.config.Transactions;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.util.Result;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.v3.services.impl.monitor.GrizzlyMonitoring;

import jakarta.inject.Inject;
import jakarta.inject.Named;

/**
 * The Network Service is responsible for starting grizzly and register the top level proxy. It is also providing a
 * runtime service where other services (like admin for instance) can register endpoints proxy to particular context
 * root.
 *
 * @author Jerome Dochez
 */
@Service
@RunLevel(StartupRunLevel.VAL)
@Rank(IMPORTANT_RUN_LEVEL_SERVICE)
public class GrizzlyService implements RequestDispatcher, PostConstruct, PreDestroy, EventListener, FutureProvider<Result<Thread>> {

    private static final Logger LOGGER = KernelLoggerInfo.getLogger();
    private static final String NETWORK_CONFIG_PREFIX = "";

    private final Set<MapperUpdateListener> mapperUpdateListeners = newSetFromMap(new ConcurrentHashMap<>());

    private final Collection<NetworkProxy> proxies = new LinkedBlockingQueue<NetworkProxy>();
    private final GrizzlyMonitoring monitoring;

    /**
     * HTTP Mapper update lock.
     */
    private final ReentrantReadWriteLock mapperLock = new ReentrantReadWriteLock();

    // Future to be set on server READY_EVENT
    private final FutureImpl<Boolean> serverReadyFuture = Futures.<Boolean>createSafeFuture();

    // Listeners to be notified once server is in READY state.
    private final Queue<Callable<Void>> serverReadyListeners = new ConcurrentLinkedQueue<>();

    volatile List<Future<Result<Thread>>> futures;
    Collection<String> hosts = new ArrayList<String>();
    private DynamicConfigListener configListener;

    @Inject
    @Named(DEFAULT_INSTANCE_NAME)
    Config config;

    @Inject
    private ServiceLocator serviceLocator;

    @Inject
    Transactions transactions;

    @Inject
    Events events;


    public GrizzlyService() {
        futures = new ArrayList<Future<Result<Thread>>>();
        monitoring = new GrizzlyMonitoring();
    }

    /**
     * Add the new proxy to our list of proxies.
     *
     * @param proxy new proxy to be added
     */
    public void addNetworkProxy(NetworkProxy proxy) {
        proxies.add(proxy);
    }

    /**
     * Remove the proxy from our list of proxies by listener.
     *
     * @param listener removes the proxy associated with the specified listener
     * @return <tt>true</tt>, if proxy removed, <tt>false</tt> if no proxy was associated with the specified listener.
     */
    public boolean removeNetworkProxy(NetworkListener listener) {
        return removeNetworkProxy(lookupNetworkProxy(listener));
    }

    /**
     * Remove the proxy from our list of proxies by id.
     *
     * @return <tt>true</tt>, if proxy on specified port was removed, <tt>false</tt> if no proxy was associated with the
     * port.
     */
    public boolean removeNetworkProxy(String id) {
        return removeNetworkProxy(getNetworkProxy(id));
    }

    private NetworkProxy getNetworkProxy(String id) {
        NetworkProxy proxyToReturn = null;

        for (NetworkProxy networkProxy : proxies) {
            if (networkProxy instanceof GrizzlyProxy) {
                GrizzlyProxy grizzlyProxy = (GrizzlyProxy) networkProxy;
                if (grizzlyProxy.networkListener != null &&
                    grizzlyProxy.networkListener.getName() != null &&
                    grizzlyProxy.networkListener.getName().equals(id)) {

                    proxyToReturn = networkProxy;
                    break;
                }
            }
        }

        return proxyToReturn;
    }

    /**
     * Remove the proxy from our list of proxies.
     *
     * @return <tt>true</tt>, if proxy on specified port was removed, <tt>false</tt> otherwise.
     */
    public boolean removeNetworkProxy(NetworkProxy proxy) {
        if (proxy != null) {
            try {
                proxy.stop();
            } catch (IOException e) {
                LOGGER.log(WARNING, KernelLoggerInfo.grizzlyStopProxy, e);
            }

            proxy.destroy();
            proxies.remove(proxy);
            return true;
        }

        return false;
    }

    /**
     * Lookup {@link GrizzlyProxy}, which corresponds to the {@link NetworkListener}.
     *
     * @param listener {@link NetworkListener}.
     * @return {@link GrizzlyProxy}, or <tt>null</tt>, if correspondent {@link GrizzlyProxy} wasn't found.
     */
    public NetworkProxy lookupNetworkProxy(NetworkListener listener) {
        int listenerPort = -1;
        InetAddress address = null;
        try {
            listenerPort = Integer.parseInt(listener.getPort());
        } catch (NumberFormatException e) {
            LOGGER.log(FINE, e.toString());
        }

        try {
            address = InetAddress.getByName(listener.getAddress());
        } catch (UnknownHostException uhe) {
            LOGGER.log(FINE, uhe.toString());
        }

        if (listenerPort != -1) {
            for (NetworkProxy p : proxies) {
                if (p.getPort() == listenerPort && p.getAddress().equals(address)) {
                    return p;
                }
            }
        }

        final String listenerId = listener.getName();

        for (NetworkProxy p : proxies) {
            if (p instanceof GrizzlyProxy) {
                GrizzlyProxy grizzlyProxy = (GrizzlyProxy) p;
                if (grizzlyProxy.networkListener != null && grizzlyProxy.networkListener.getName() != null
                        && grizzlyProxy.networkListener.getName().equals(listenerId)) {
                    return p;
                }
            }
        }

        return null;

    }

    /**
     * Restart {@link NetworkListener}.
     *
     * @param networkListenerName
     * @param timeout restart timeout, if timeout value is negative - then no timeout will be applied.
     * @param timeUnit restart timeout unit
     *
     * @throws TimeoutException thrown if timeout had expired before server succeeded to restart
     * @throws IOException
     */
    public void restartNetworkListener(String networkListenerName, long timeout, TimeUnit timeUnit) throws IOException, TimeoutException {
        restartNetworkListener(config.getNetworkConfig().getNetworkListener(networkListenerName), timeout, timeUnit);
    }

    /**
     * Restart {@link NetworkListener}.
     *
     * @param networkListener {@link NetworkListener}
     * @param timeout restart timeout, if timeout value is negative - then no timeout will be applied.
     * @param timeUnit restart timeout unit
     *
     * @throws TimeoutException thrown if timeout had expired before server succeeded to restart
     * @throws IOException
     */
    public void restartNetworkListener(NetworkListener networkListener, long timeout, TimeUnit timeUnit) throws IOException, TimeoutException {

        // Restart GrizzlyProxy on the address/port
        // Address/port/id could have been changed - so try to find
        // corresponding proxy both ways
        NetworkProxy proxy = lookupNetworkProxy(networkListener);
        Map<Class<? extends HttpCodecFilter>, List<HttpProbe>> filterToProbeMapping = new HashMap<>();
        if (proxy == null) {
            proxy = getNetworkProxy(networkListener.getName());
        }

        if (proxy != null) {
            if (proxy instanceof GrizzlyProxy) {
                GrizzlyProxy grizzlyProxy = (GrizzlyProxy) proxy;
                GenericGrizzlyListener grizzlyListener = (GenericGrizzlyListener) grizzlyProxy.getUnderlyingListener();
                List<HttpCodecFilter> codecFilters = grizzlyListener.getFilters(HttpCodecFilter.class);
                if (codecFilters != null && !codecFilters.isEmpty()) {
                    for (HttpCodecFilter codecFilter : codecFilters) {
                        HttpProbe[] probes = codecFilter.getMonitoringConfig().getProbes();
                        if (probes != null) {
                            List<HttpProbe> probesForType = filterToProbeMapping.get(codecFilter.getClass());
                            if (probesForType == null) {
                                probesForType = new ArrayList<>(4);
                                filterToProbeMapping.put(codecFilter.getClass(), probesForType);
                            }
                            Collections.addAll(probesForType, probes);
                        }
                    }
                }
            }

            removeNetworkProxy(proxy);
        }
        final Future<Result<Thread>> future = createNetworkProxy(networkListener);
        if (future == null) {
            LOGGER.log(FINE, "Skipping proxy registration for the listener {0}", networkListener.getName());
            return;
        }

        try {
            if (timeout <= 0) {
                future.get();
            } else {
                future.get(timeout, timeUnit);
            }

            NetworkProxy newNetworkProxy = getNetworkProxy(networkListener.getName());
            if (newNetworkProxy instanceof GrizzlyProxy) {
                GrizzlyProxy grizzlyProxy = (GrizzlyProxy) newNetworkProxy;
                GenericGrizzlyListener grizzlyListener = (GenericGrizzlyListener) grizzlyProxy.getUnderlyingListener();
                if (!filterToProbeMapping.isEmpty()) {
                    for (Class<? extends HttpCodecFilter> aClass : filterToProbeMapping.keySet()) {
                        List<? extends HttpCodecFilter> filters = grizzlyListener.getFilters(aClass);
                        if (filters != null && !filters.isEmpty()) {
                            if (filters.size() != 1) {
                                throw new IllegalStateException();
                            }
                            final List<HttpProbe> probes = filterToProbeMapping.get(aClass);
                            filters.get(0).getMonitoringConfig().addProbes(probes.toArray(new HttpProbe[probes.size()]));
                        }
                    }
                }
            }
        } catch (ExecutionException e) {
            throw new IOException(e.getCause());
        } catch (InterruptedException e) {
            throw new IOException(e);
        }

        registerContainerAdapters();
    }

    /**
     * Is there any {@link MapperUpdateListener} registered?
     */
    public boolean hasMapperUpdateListener() {
        return !mapperUpdateListeners.isEmpty();
    }

    /**
     * Adds {@link MapperUpdateListener} to listeners queue.
     *
     * @param listener the listener to be added.
     * @return <tt>true</tt>, if listener was successfully added, or <tt>false</tt> otherwise.
     */
    public boolean addMapperUpdateListener(MapperUpdateListener listener) {
        return mapperUpdateListeners.add(listener);
    }

    /**
     * Removes {@link MapperUpdateListener} to listeners queue.
     *
     * @param listener the listener to be removed.
     * @return <tt>true</tt>, if listener was successfully removed, or <tt>false</tt> otherwise.
     */
    public boolean removeMapperUpdateListener(MapperUpdateListener listener) {
        return mapperUpdateListeners.remove(listener);
    }

    /**
     * Notify all {@link MapperUpdateListener}s about update happened.
     *
     * @param networkListener {@link NetworkListener}, which {@link Mapper} got changed
     * @param mapper new {@link Mapper} value
     */
    public void notifyMapperUpdateListeners(NetworkListener networkListener, Mapper mapper) {
        final HttpService httpService = config.getHttpService();
        for (MapperUpdateListener listener : mapperUpdateListeners) {
            listener.update(httpService, networkListener, mapper);
        }
    }

    /**
     * Returns HTTP {@link Mapper} lock to prevent concurrent access to a {@link Mapper} object.
     */
    public ReentrantReadWriteLock obtainMapperLock() {
        return mapperLock;
    }

    /**
     * Gets the logger.
     *
     * @return the logger
     */
    public Logger getLogger() {
        return LOGGER;
    }

    /**
     * Gets the serviceLocator.
     *
     * @return the serviceLocator
     */
    public ServiceLocator getServiceLocator() {
        return serviceLocator;
    }

    public GrizzlyMonitoring getMonitoring() {
        return monitoring;
    }

    /**
     * Return the {@link Future}, that might be used to monitor server startup state.
     */
    final Future<Boolean> getServerReadyFuture() {
        return serverReadyFuture;
    }

    /**
     * Add the {@link Callable} listener, which will be notified once server state switches to "SERVER_READY".
     *
     * @param listener {@link Callable} listener.
     */
    final void addServerReadyListener(final Callable<Void> listener) {
        if (serverReadyFuture.isDone()) {
            try {
                listener.call();
            } catch (Exception ignored) {
            }

            return;
        }

        serverReadyListeners.add(listener);

        if (serverReadyFuture.isDone() && serverReadyListeners.remove(listener)) {
            try {
                listener.call();
            } catch (Exception ignored) {
            }
        }
    }

    /**
     * Removes the {@link Callable} listener, which will be notified once server state switches to "SERVER_READY".
     *
     * @param listener {@link Callable} listener.
     */
    final boolean removeServerListener(final Callable<Void> listener) {
        return serverReadyListeners.remove(listener);
    }

    /**
     * Method is invoked each time Glassfish state changes.
     */
    @Override
    public void event(@RestrictTo(EventTypes.SERVER_READY_NAME) Event<?> event) {
        if (event.is(SERVER_READY)) {
            serverReadyFuture.result(Boolean.TRUE);

            Callable<Void> listener;
            while ((listener = serverReadyListeners.poll()) != null) {
                try {
                    listener.call();
                } catch (Exception ignored) {
                }
            }
        }
    }

    /**
     * The component has been injected with any dependency and will be placed into commission by the subsystem.
     */
    @Override
    public void postConstruct() {
        events.register(this);

        final NetworkConfig networkConfig = config.getNetworkConfig();
        configListener = new DynamicConfigListener(config, LOGGER);
        ObservableBean bean = (ObservableBean) ConfigSupport.getImpl(networkConfig.getNetworkListeners());
        bean.addListener(configListener);
        bean = (ObservableBean) ConfigSupport.getImpl(config.getHttpService());
        bean.addListener(configListener);
        transactions.addListenerForType(SystemProperty.class, configListener);
        configListener.setGrizzlyService(this);

        try {
            boolean isAtLeastOneProxyStarted = false;

            futures = new ArrayList<Future<Result<Thread>>>();
            for (NetworkListener listener : networkConfig.getNetworkListeners().getNetworkListener()) {
                isAtLeastOneProxyStarted |= (createNetworkProxy(listener) != null);
            }

            if (isAtLeastOneProxyStarted) {
                registerContainerAdapters();
            }
        } catch (RuntimeException e) { // So far postConstruct can not throw any other exception type
            LOGGER.log(SEVERE, KernelLoggerInfo.grizzlyCantStart, e);
            for (NetworkProxy proxy : proxies) {
                try {
                    proxy.stop();
                } catch (Exception proxyStopException) {
                    LOGGER.log(SEVERE, KernelLoggerInfo.grizzlyCloseException, new Object[] { proxy.getPort(), proxyStopException });
                }
            }

            throw e;
        }

        registerMonitoringStatsProviders();
    }

    @Override
    public List<Future<Result<Thread>>> getFutures() {
        return futures;
    }

    /*
     * Creates a new NetworkProxy for a particular HttpListner
     *
     * @param listener NetworkListener
     *
     * @param networkConfig HttpService
     */
    public synchronized Future<Result<Thread>> createNetworkProxy(NetworkListener listener) {
        if (!Boolean.valueOf(listener.getEnabled())) {
            addChangeListener(listener); // in case the listener will be enabled

            LOGGER.log(INFO, KernelLoggerInfo.grizzlyPortDisabled, new Object[] { listener.getName(), listener.getPort() });
            return null;
        }

        // Create the proxy for the port.
        GrizzlyProxy proxy = new GrizzlyProxy(this, listener);

        Future<Result<Thread>> future = null;

        try {
            proxy.initialize();

            if (!isLightWeightListener(listener)) {
                final NetworkConfig networkConfig = listener.getParent(NetworkListeners.class).getParent(NetworkConfig.class);

                // Attach all virtual servers to this port
                for (VirtualServer vs : networkConfig.getParent(Config.class).getHttpService().getVirtualServer()) {
                    List<String> vsListeners = StringUtils.parseStringList(vs.getNetworkListeners(), " ,");
                    if (vsListeners == null || vsListeners.isEmpty() || vsListeners.contains(listener.getName())) {
                        if (!hosts.contains(vs.getId())) {
                            hosts.add(vs.getId());
                        }
                    }
                }
                addChangeListener(listener);
                addChangeListener(listener.findThreadPool());
                addChangeListener(listener.findTransport());

                final Protocol protocol = listener.findHttpProtocol();
                if (protocol != null) {
                    addChangeListener(protocol);
                    addChangeListener(protocol.getHttp());
                    addChangeListener(protocol.getHttp().getFileCache());
                    addChangeListener(protocol.getSsl());
                }
            }

            future = proxy.start();

            // add the new proxy to our list of proxies.
            proxies.add(proxy);
        } catch (Throwable e) {
            final Future<Result<Thread>> errorFuture = Futures.createReadyFuture(new Result<Thread>(e));
            future = errorFuture;
        } finally {
            if (future == null) {
                final FutureImpl<Result<Thread>> errorFuture = Futures.<Result<Thread>>createUnsafeFuture();
                errorFuture.result(new Result<Thread>(new IllegalStateException("Unexpected error")));
                future = errorFuture;
            }

            futures.add(future);
        }

        return future;
    }

    private void addChangeListener(ConfigBeanProxy bean) {
        if (bean != null) {
            ((ObservableBean) ConfigSupport.getImpl(bean)).addListener(configListener);
        }
    }

    /*
     * Registers all proxies
     */
    void registerContainerAdapters() {
        for (org.glassfish.api.container.Adapter subAdapter : serviceLocator.<org.glassfish.api.container.Adapter>getAllServices(
                org.glassfish.api.container.Adapter.class)) {
            // @TODO change EndportRegistrationException processing if required
            try {
                if (!subAdapter.isRegistered()) {
                    registerAdapter(subAdapter);
                    subAdapter.setRegistered(true);
                }
            } catch (EndpointRegistrationException e) {
                LOGGER.log(WARNING, KernelLoggerInfo.grizzlyEndpointRegistration, e);
            }
        }
    }

    /**
     * The component is about to be removed from commission
     */
    @Override
    public void preDestroy() {
        for (NetworkProxy proxy : proxies) {
            try {
                proxy.stop();
            } catch (IOException e) {
                LOGGER.log(WARNING, KernelLoggerInfo.grizzlyStopProxy, e);
            }
        }
        unregisterMonitoringStatsProviders();
    }

    /*
     * Registers a new endpoint (proxy implementation) for a particular context-root. All request coming with the context
     * root will be dispatched to the proxy instance passed in.
     *
     * @param contextRoot for the proxy
     *
     * @param endpointAdapter servicing requests.
     */
    @Override
    public void registerEndpoint(String contextRoot, HttpHandler endpointAdapter, ApplicationContainer container)
            throws EndpointRegistrationException {

        registerEndpoint(contextRoot, endpointAdapter, container, null);
    }

    /*
     * Registers a new endpoint (proxy implementation) for a particular context-root. All request coming with the context
     * root will be dispatched to the proxy instance passed in.
     *
     * @param contextRoot for the proxy
     *
     * @param endpointAdapter servicing requests.
     *
     * @param application container
     *
     * @param virtualServers comma separated list of the virtual servers
     */
    @Override
    public void registerEndpoint(String contextRoot, HttpHandler endpointAdapter, ApplicationContainer container, String virtualServers)
            throws EndpointRegistrationException {
        List<String> virtualServerList;
        if (virtualServers == null) {
            virtualServerList = config.getHttpService().getNonAdminVirtualServerList();
        } else {
            virtualServerList = StringUtils.parseStringList(virtualServers, ",");
        }
        registerEndpoint(contextRoot, virtualServerList, endpointAdapter, container);
    }

    /**
     * Registers a new endpoint (proxy implementation) for a particular context-root. All request coming with the context
     * root will be dispatched to the proxy instance passed in.
     *
     * @param contextRoot for the proxy
     *
     * @param endpointAdapter servicing requests.
     */
    @Override
    public void registerEndpoint(String contextRoot, Collection<String> vsServers, HttpHandler endpointAdapter, ApplicationContainer container) throws EndpointRegistrationException {
        Collection<AddressInfo> addressInfos = getAddressInfoFromVirtualServers(vsServers);
        for (AddressInfo info : addressInfos) {
            registerEndpoint(contextRoot, info.address, info.port, vsServers, endpointAdapter, container);
        }
    }

    /**
     * Registers a new endpoint for the given context root at the given port number.
     */
    @Override
    public void registerEndpoint(String contextRoot, InetAddress address, int port, Collection<String> vsServers, HttpHandler endpointAdapter, ApplicationContainer container) throws EndpointRegistrationException {
        for (NetworkProxy proxy : proxies) {
            if (proxy.getPort() == port && proxy.getAddress().equals(address)) {
                proxy.registerEndpoint(contextRoot, vsServers, endpointAdapter, container);
            }
        }
    }

    /**
     * Registers a new endpoint for the given context root at the given port number.
     */
    public void registerEndpoint(final Endpoint endpoint) throws EndpointRegistrationException {
        final InetAddress address = endpoint.getAddress();
        final int port = endpoint.getPort();

        for (NetworkProxy proxy : proxies) {
            if (proxy.getPort() == port && proxy.getAddress().equals(address)) {
                proxy.registerEndpoint(endpoint);
            }
        }
    }

    /**
     * Removes the context-root from our list of endpoints.
     */
    @Override
    public void unregisterEndpoint(String contextRoot) throws EndpointRegistrationException {
        unregisterEndpoint(contextRoot, null);
    }

    /**
     * Removes the context-root from our list of endpoints.
     */
    @Override
    public void unregisterEndpoint(String contextRoot, ApplicationContainer app) throws EndpointRegistrationException {
        for (NetworkProxy proxy : proxies) {
            proxy.unregisterEndpoint(contextRoot, app);
        }
    }

    /**
     * Probe provider that implements each probe provider method as a no-op.
     */
    @SuppressWarnings({ "UnusedDeclaration" })
    public static class NoopInvocationHandler implements InvocationHandler {
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) {
            // Deliberate no-op
            return null;
        }
    }

    protected void registerMonitoringStatsProviders() {
        monitoring.registerThreadPoolStatsProviderGlobal(NETWORK_CONFIG_PREFIX);
        monitoring.registerKeepAliveStatsProviderGlobal(NETWORK_CONFIG_PREFIX);
        monitoring.registerFileCacheStatsProviderGlobal(NETWORK_CONFIG_PREFIX);
        monitoring.registerConnectionQueueStatsProviderGlobal(NETWORK_CONFIG_PREFIX);
    }

    protected void unregisterMonitoringStatsProviders() {
        monitoring.unregisterThreadPoolStatsProviderGlobal(NETWORK_CONFIG_PREFIX);
        monitoring.unregisterKeepAliveStatsProviderGlobal(NETWORK_CONFIG_PREFIX);
        monitoring.unregisterFileCacheStatsProviderGlobal(NETWORK_CONFIG_PREFIX);
        monitoring.unregisterConnectionQueueStatsProviderGlobal(NETWORK_CONFIG_PREFIX);
    }

    private void registerAdapter(org.glassfish.api.container.Adapter a) throws EndpointRegistrationException {
        Endpoint endpoint = Endpoint.createEndpoint(a);
        registerEndpoint(endpoint);
    }

    // get the ports from the http listeners that are associated with
    // the virtual servers
    private List<AddressInfo> getAddressInfoFromVirtualServers(Collection<String> virtualServerNames) {
        List<AddressInfo> addressInfos = new ArrayList<AddressInfo>();
        List<NetworkListener> networkListenerList = config.getNetworkConfig().getNetworkListeners().getNetworkListener();

        for (String virtualServerName : virtualServerNames) {
            VirtualServer virtualServer = config.getHttpService().getVirtualServerByName(virtualServerName);
            if (virtualServer == null) {
                // non-existent virtual server
                LOGGER.log(WARNING, KernelLoggerInfo.grizzlyNonExistentVS, virtualServerName);
                continue;
            }

            String vsNetworkListeners = virtualServer.getNetworkListeners();
            List<String> vsNetworkListenerList = StringUtils.parseStringList(vsNetworkListeners, ",");
            if (vsNetworkListenerList != null && !vsNetworkListenerList.isEmpty()) {
                for (String vsNetworkListener : vsNetworkListenerList) {
                    for (NetworkListener networkListener : networkListenerList) {
                        if (networkListener.getName().equals(vsNetworkListener) && Boolean.valueOf(networkListener.getEnabled())) {
                            addressInfos.add(new AddressInfo(networkListener.getAddress(), networkListener.getPort()));
                            break;
                        }
                    }
                }
            }
        }
        return addressInfos;
    }

    static boolean isLightWeightListener(final NetworkListener listener) {
        return "proxy".equalsIgnoreCase(listener.getType()) || "light-weight-listener".equalsIgnoreCase(listener.getProtocol());
    }

    // ---------------------------------------------------------- Nested Classes

    private static final class AddressInfo {

        private InetAddress address;
        private final int port;

        private AddressInfo(String address, String port) {
            this.port = Integer.parseInt(port);
            try {
                this.address = InetAddress.getByName(address);
            } catch (UnknownHostException ignore) {
            }
        }

    }
}
