/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2022 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.appserv.server.util.Version;
import com.sun.enterprise.config.serverbeans.VirtualServer;
import com.sun.enterprise.v3.services.impl.monitor.ConnectionMonitor;
import com.sun.enterprise.v3.services.impl.monitor.FileCacheMonitor;
import com.sun.enterprise.v3.services.impl.monitor.GrizzlyMonitoring;
import com.sun.enterprise.v3.services.impl.monitor.KeepAliveMonitor;
import com.sun.enterprise.v3.services.impl.monitor.ThreadPoolMonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.config.GenericGrizzlyListener;
import org.glassfish.grizzly.config.dom.Http;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.config.dom.Protocol;
import org.glassfish.grizzly.config.dom.ThreadPool;
import org.glassfish.grizzly.config.dom.Transport;
import org.glassfish.grizzly.filterchain.FilterChainBuilder;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.KeepAlive;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.ServerFilterConfiguration;
import org.glassfish.grizzly.http.server.filecache.FileCache;
import org.glassfish.grizzly.http.server.util.Mapper;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.grizzly.utils.DelayedExecutor;
import org.glassfish.hk2.api.DynamicConfiguration;
import org.glassfish.hk2.api.DynamicConfigurationService;
import org.glassfish.hk2.api.IndexedFilter;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.AbstractActiveDescriptor;
import org.glassfish.hk2.utilities.BuilderHelper;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.internal.grizzly.V3Mapper;
import org.jvnet.hk2.config.types.Property;

public class GlassfishNetworkListener extends GenericGrizzlyListener {
    private final GrizzlyService grizzlyService;
    private final NetworkListener networkListener;
    private final Logger logger;

    private volatile HttpAdapter httpAdapter;

    public GlassfishNetworkListener(final GrizzlyService grizzlyService,
            final NetworkListener networkListener,
            final Logger logger) {
        this.grizzlyService = grizzlyService;
        this.networkListener = networkListener;
        this.logger = logger;
    }

    public NetworkListener getNetworkListener() {
        return networkListener;
    }

    @Override
    public void stop() throws IOException {
        ServiceLocator locator = grizzlyService.getServiceLocator();
        IndexedFilter removeFilter = BuilderHelper.createNameAndContractFilter(Mapper.class.getName(),
                (address.toString() + port));

        DynamicConfigurationService dcs = locator.getService(DynamicConfigurationService.class);
        DynamicConfiguration config = dcs.createDynamicConfiguration();

        config.addUnbindFilter(removeFilter);

        config.commit();

        unregisterMonitoringStatsProviders();
        super.stop();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getAdapter(Class<T> adapterClass) {
        if (HttpAdapter.class.equals(adapterClass)) {
            return (T) httpAdapter;
        }

        return super.getAdapter(adapterClass);
    }

    @Override
    protected void configureTransport(final NetworkListener networkListener,
                                      final Transport transportConfig,
                                      final FilterChainBuilder filterChainBuilder) {

        super.configureTransport(networkListener, transportConfig,
                filterChainBuilder);

        transport.getConnectionMonitoringConfig().addProbes(new ConnectionMonitor(
                grizzlyService.getMonitoring(), name, transport));
    }

    @Override
    protected void configureHttpProtocol(final ServiceLocator habitat,
            final NetworkListener networkListener,
            final Http http, final FilterChainBuilder filterChainBuilder,
            boolean securityEnabled) {

        if (httpAdapter != null) {
            super.configureHttpProtocol(habitat, networkListener, http, filterChainBuilder, securityEnabled);
            return;
        }

        registerMonitoringStatsProviders();

        final V3Mapper mapper = new V3Mapper(logger);

        mapper.setPort(port);
        mapper.setId(name);

        final ContainerMapper containerMapper = new ContainerMapper(grizzlyService, this);
        containerMapper.setMapper(mapper);
        containerMapper.setDefaultHost(http.getDefaultVirtualServer());
        containerMapper.setRequestURIEncoding(http.getUriEncoding());
        containerMapper.configureMapper();

        VirtualServer vs = null;
        String webAppRootPath = null;

        final Collection<VirtualServer> list = grizzlyService.getServiceLocator().getAllServices(VirtualServer.class);
        final String vsName = http.getDefaultVirtualServer();
        for (final VirtualServer virtualServer : list) {
            if (virtualServer.getId().equals(vsName)) {
                vs = virtualServer;
                webAppRootPath = vs.getDocroot();

                if (!grizzlyService.hasMapperUpdateListener() && vs.getProperty() != null
                        && !vs.getProperty().isEmpty()) {
                    for (final Property p : vs.getProperty()) {
                        final String propertyName = p.getName();
                        if (propertyName.startsWith("alternatedocroot")) {
                            String value = p.getValue();
                            String[] mapping = value.split(" ");

                            if (mapping.length != 2) {
                                logger.log(Level.WARNING, "Invalid alternate_docroot {0}", value);
                                continue;
                            }

                            String docBase = mapping[1].substring("dir=".length());
                            String urlPattern = mapping[0].substring("from=".length());
                            containerMapper.addAlternateDocBase(urlPattern, docBase);
                        }
                    }
                }
                break;
            }
        }

        httpAdapter = new HttpAdapterImpl(vs, containerMapper, webAppRootPath);
        containerMapper.addDocRoot(webAppRootPath);

        AbstractActiveDescriptor<V3Mapper> aad = BuilderHelper.createConstantDescriptor(mapper);
        aad.addContractType(Mapper.class);
        aad.setName(address.toString() + port);

        ServiceLocatorUtilities.addOneDescriptor(grizzlyService.getServiceLocator(), aad);
        super.configureHttpProtocol(habitat, networkListener, http, filterChainBuilder, securityEnabled);
        final Protocol protocol = http.getParent();
        for (NetworkListener listener : protocol.findNetworkListeners()) {
            grizzlyService.notifyMapperUpdateListeners(listener, mapper);
        }
    }

    @Override
    protected ServerFilterConfiguration getHttpServerFilterConfiguration(Http http) {
        // Set the default Glassfish error page generator
        final ServerFilterConfiguration config =
                super.getHttpServerFilterConfiguration(http);
        config.setDefaultErrorPageGenerator(new GlassfishErrorPageGenerator());
        return config;
    }

    @Override
    protected HttpHandler getHttpHandler() {
        return httpAdapter.getMapper();
    }

    @Override
    protected KeepAlive configureKeepAlive(Http http) {
        final KeepAlive keepAlive = super.configureKeepAlive(http);
        keepAlive.getMonitoringConfig().addProbes(new KeepAliveMonitor(
                grizzlyService.getMonitoring(), name, keepAlive));
        return keepAlive;
    }

    @Override
    protected FileCache configureHttpFileCache(org.glassfish.grizzly.config.dom.FileCache cache) {

        final FileCache fileCache = super.configureHttpFileCache(cache);
        fileCache.getMonitoringConfig().addProbes(new FileCacheMonitor(
                grizzlyService.getMonitoring(), name, fileCache));

        return fileCache;
    }

    @Override
    protected ThreadPoolConfig configureThreadPoolConfig(final NetworkListener networkListener,
                                                         final ThreadPool threadPool) {

        final ThreadPoolConfig config = super.configureThreadPoolConfig(
                networkListener, threadPool);
        config.getInitialMonitoringConfig().addProbes(new ThreadPoolMonitor(
                grizzlyService.getMonitoring(), name, config));
        return config;
    }

    @Override
    protected org.glassfish.grizzly.http.HttpServerFilter createHttpServerCodecFilter(
            final Http http,
            final boolean isChunkedEnabled, final int headerBufferLengthBytes,
            final String defaultResponseType, final KeepAlive keepAlive,
            final DelayedExecutor delayedExecutor,
            final int maxRequestHeaders, final int maxResponseHeaders) {

        final org.glassfish.grizzly.http.HttpServerFilter httpCodecFilter =
                new GlassfishHttpCodecFilter(
                http == null || Boolean.parseBoolean(http.getXpoweredBy()),
                isChunkedEnabled,
                headerBufferLengthBytes,
                defaultResponseType,
                keepAlive,
                delayedExecutor,
                maxRequestHeaders,
                maxResponseHeaders);

        if (http != null) { // could be null for HTTP redirect
            httpCodecFilter.setMaxPayloadRemainderToSkip(
                    Integer.parseInt(http.getMaxSwallowingInputBytes()));

            httpCodecFilter.setAllowPayloadForUndefinedHttpMethods(
                    Boolean.parseBoolean(http.getAllowPayloadForUndefinedHttpMethods()));
        }

        return httpCodecFilter;
    }


    protected void registerMonitoringStatsProviders() {
        final String nameLocal = name;
        final GrizzlyMonitoring monitoring = grizzlyService.getMonitoring();

        monitoring.registerThreadPoolStatsProvider(nameLocal);
        monitoring.registerKeepAliveStatsProvider(nameLocal);
        monitoring.registerFileCacheStatsProvider(nameLocal);
        monitoring.registerConnectionQueueStatsProvider(nameLocal);
    }

    protected void unregisterMonitoringStatsProviders() {
        final String localName = name;
        final GrizzlyMonitoring monitoring = grizzlyService.getMonitoring();

        monitoring.unregisterThreadPoolStatsProvider(localName);
        monitoring.unregisterKeepAliveStatsProvider(localName);
        monitoring.unregisterFileCacheStatsProvider(localName);
        monitoring.unregisterConnectionQueueStatsProvider(localName);
    }

    static List<String> toArray(String s, String token) {
        final ArrayList<String> list = new ArrayList<>();

        int from = 0;
        do {
            final int idx = s.indexOf(token, from);

            if (idx == -1) {
                final String str = s.substring(from, s.length()).trim();
                list.add(str);
                break;
            }

            final String str = s.substring(from, idx).trim();
            list.add(str);

            from = idx + 1;

        } while (true);

        return list;
    }

    protected static class HttpAdapterImpl implements HttpAdapter {
        private final VirtualServer virtualServer;
        private final ContainerMapper conainerMapper;
        private final String webAppRootPath;

        public HttpAdapterImpl(VirtualServer virtualServer, ContainerMapper conainerMapper, String webAppRootPath) {
            this.virtualServer = virtualServer;
            this.conainerMapper = conainerMapper;
            this.webAppRootPath = webAppRootPath;
        }


        @Override
        public ContainerMapper getMapper() {
            return conainerMapper;
        }

        @Override
        public VirtualServer getVirtualServer() {
            return virtualServer;
        }

        @Override
        public String getWebAppRootPath() {
            return webAppRootPath;
        }
    }

    /**
     * Glassfish specific HttpCodecFilter extension.
     */
    private static class GlassfishHttpCodecFilter extends org.glassfish.grizzly.http.HttpServerFilter {
        private final String serverVersion;
        private final String xPoweredBy;

        public GlassfishHttpCodecFilter(
                final boolean isXPoweredByEnabled,
                final boolean chunkingEnabled,
                final int maxHeadersSize,
                final String defaultResponseContentType,
                final KeepAlive keepAlive, final DelayedExecutor executor,
                final int maxRequestHeaders, final int maxResponseHeaders) {
            super(chunkingEnabled, maxHeadersSize, defaultResponseContentType,
                    keepAlive, executor, maxRequestHeaders, maxResponseHeaders);

            /*
            * Set the server info.
            * By default, the server info is taken from Version#getProductId.
            * However, customers may override it via the product.name system
            * property.
            * Some customers prefer not to disclose the server info
            * for security reasons, in which case they would set the value of the
            * product.name system property to the empty string. In this case,
            * the server name will not be publicly disclosed via the "Server"
            * HTTP response header (which will be suppressed) or any container
            * generated error pages. However, it will still appear in the
            * server logs (see IT 6900).
            *
            * Taken from com.sun.enterprise.web.WebContainer code
            */
            String serverInfo = System.getProperty("product.name");

            serverVersion = serverInfo == null ? Version.getProductId() : serverInfo;

            if (isXPoweredByEnabled) {
                String serverVersionForXPoweredBy = (serverInfo == null || serverInfo.isEmpty()) ? Version.getProductId() : serverInfo;
                xPoweredBy = "Servlet/6.0 JSP/3.1"
                        + "(" + serverVersionForXPoweredBy
                        + " Java/"
                        + System.getProperty("java.vm.vendor") + "/"
                        + System.getProperty("java.specification.version") + ")";
            } else {
                xPoweredBy = null;
            }
        }

        @Override
        protected boolean onHttpHeaderParsed(final HttpHeader httpHeader,
                final Buffer buffer,
                final FilterChainContext ctx) {

            final boolean result = super.onHttpHeaderParsed(httpHeader,
                    buffer, ctx);

            final HttpRequestPacket request = (HttpRequestPacket) httpHeader;
            final HttpResponsePacket response = request.getResponse();

            // Set response "Server" header
            if (serverVersion != null && !serverVersion.isEmpty()) {
                response.addHeader(Header.Server, serverVersion);
            }

            // Set response "X-Powered-By" header
            if (xPoweredBy != null) {
                response.addHeader(Header.XPoweredBy, xPoweredBy);
            }

            return result;
        }
    }
}
