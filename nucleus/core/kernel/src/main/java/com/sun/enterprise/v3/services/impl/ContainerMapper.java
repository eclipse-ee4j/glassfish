/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 2007, 2020 Oracle and/or its affiliates. All rights reserved.
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

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.WARNING;
import static org.glassfish.kernel.KernelLoggerInfo.exceptionMapper;
import static org.glassfish.kernel.KernelLoggerInfo.exceptionMapper2;

import java.io.CharConversionException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

import org.glassfish.api.container.Adapter;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.api.logging.LogHelper;
import org.glassfish.grizzly.config.ContextRootInfo;
import org.glassfish.grizzly.config.GrizzlyListener;
import org.glassfish.grizzly.http.Note;
import org.glassfish.grizzly.http.server.AfterServiceListener;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.glassfish.grizzly.http.server.HttpHandlerChain;
import org.glassfish.grizzly.http.server.Request;
import org.glassfish.grizzly.http.server.Response;
import org.glassfish.grizzly.http.server.util.Mapper;
import org.glassfish.grizzly.http.server.util.MappingData;
import org.glassfish.grizzly.http.util.CharChunk;
import org.glassfish.grizzly.http.util.DataChunk;
import org.glassfish.grizzly.http.util.MimeType;
import org.glassfish.internal.grizzly.ContextMapper;
import org.glassfish.kernel.KernelLoggerInfo;

/**
 * Container's mapper which maps {@link ByteBuffer} bytes representation to an {@link HttpHandler},
 * {@link ApplicationContainer} and ProtocolFilter chain. The mapping result is stored inside {@link MappingData} which
 * is eventually shared with the CoyoteAdapter, which is the entry point with the Catalina Servlet Container.
 *
 * @author Jeanfrancois Arcand
 * @author Alexey Stashok
 */
public class ContainerMapper extends ADBAwareHttpHandler {

    private static final Logger LOGGER = KernelLoggerInfo.getLogger();
    private static final String ROOT = "";

    private static final AfterServiceListener afterServiceListener = new AfterServiceListenerImpl();
    private static final Note<MappingData> MAPPING_DATA = Request.<MappingData>createNote("MappingData");

    // Make sure this value is always aligned with {@link org.apache.catalina.connector.CoyoteAdapter}
    // (@see org.apache.catalina.connector.CoyoteAdapter)
    private static final Note<DataChunk> DATA_CHUNK = Request.<DataChunk>createNote("DataChunk");

    private final GrizzlyListener listener;
    private final GrizzlyService grizzlyService;
    private final ReentrantReadWriteLock mapperLock;

    private ContextMapper mapper;
    private String defaultHostName = "server";

    /**
     * Are we running multiple {@ Adapter} or {@link HttpHandlerChain}
     */
    private boolean mapMultipleAdapter;

    public ContainerMapper(final GrizzlyService service, final GrizzlyListener grizzlyListener) {
        listener = grizzlyListener;
        grizzlyService = service;
        mapperLock = service.obtainMapperLock();
    }

    /**
     * Set the default host that will be used when we map.
     *
     * @param defaultHost
     */
    protected void setDefaultHost(String defaultHost) {
        mapperLock.writeLock().lock();
        try {
            defaultHostName = defaultHost;
        } finally {
            mapperLock.writeLock().unlock();
        }
    }

    /**
     * Set the {@link ContextMapper} instance used for mapping the container and its associated {@link Adapter}.
     *
     * @param mapper
     */
    protected void setMapper(ContextMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Configure the {@link ContextMapper}.
     */
    protected void configureMapper() {
        mapperLock.writeLock().lock();

        try {
            mapper.setDefaultHostName(defaultHostName);
            mapper.addHost(defaultHostName, new String[] {}, null);
            mapper.addContext(defaultHostName, ROOT, new ContextRootInfo(this, null), new String[] { "index.html", "index.htm" }, null);
            // Container deployed have the right to override the default setting.
            Mapper.setAllowReplacement(true);
        } finally {
            mapperLock.writeLock().unlock();
        }
    }

    /**
     * Map the request to its associated {@link Adapter}.
     *
     * @param request
     * @param response
     *
     * @throws IOException
     */
    @Override
    public void service(final Request request, final Response response) throws Exception {
        try {
            request.addAfterServiceListener(afterServiceListener);
            lookupHandler(request, response).call();
        } catch (Exception ex) {
            logAndSendError(request, response, ex);
        }
    }

    private void logAndSendError(final Request request, final Response response, Exception ex) {
        if (LOGGER.isLoggable(WARNING)) {
            LogHelper.log(LOGGER, WARNING, exceptionMapper, ex, toUrlForLogging(request));
        }

        if (response.getResponse() == null) {
            LOGGER.log(WARNING, "Response is not set in {0}, there's nothing we can do now.", response);
            return;
        }

        try {
            response.sendError(500);
        } catch (Exception ex2) {
            LOGGER.log(WARNING, exceptionMapper2, ex2);
        }
    }

    private Object toUrlForLogging(final Request request) {
        try {
            return request.getRequest() == null ? null : request.getRequest().getRequestURIRef().getDecodedRequestURIBC();
        } catch (CharConversionException e) {
            return null;
        }
    }

    private Callable lookupHandler(final Request request, final Response response) throws CharConversionException, Exception {

        MappingData mappingData;
        mapperLock.readLock().lock();

        try {
            // If we have only one Adapter deployed, invoke that Adapter directly.
            if (!mapMultipleAdapter) {
                // Remove the MappingData as we might delegate the request
                // to be serviced directly by the WebContainer
                final HttpHandler httpHandler = mapper.getHttpHandler();
                if (httpHandler != null) {
                    request.setNote(MAPPING_DATA, null);
                    return new HttpHandlerCallable(httpHandler, request, response);
                }
            }

            final DataChunk decodedURI =
                request.getRequest()
                       .getRequestURIRef()
                       .getDecodedRequestURIBC(isAllowEncodedSlash());

            mappingData = request.getNote(MAPPING_DATA);
            if (mappingData == null) {
                mappingData = new MappingData();
                request.setNote(MAPPING_DATA, mappingData);
            } else {
                mappingData.recycle();
            }

            HttpHandler httpHandler;

            final CharChunk decodedURICC = decodedURI.getCharChunk();
            final int semicolon = decodedURICC.indexOf(';', 0);

            // Map the request without any trailling.
            httpHandler = mapUriWithSemicolon(request, decodedURI, semicolon, mappingData);
            if (httpHandler == null || httpHandler instanceof ContainerMapper) {
                String ext = decodedURI.toString();
                String type = "";
                if (ext.lastIndexOf(".") > 0) {
                    ext = "*" + ext.substring(ext.lastIndexOf("."));
                    type = ext.substring(ext.lastIndexOf(".") + 1);
                }

                if (!MimeType.contains(type) && !"/".equals(ext)) {
                    initializeFileURLPattern(ext);
                    mappingData.recycle();
                    httpHandler = mapUriWithSemicolon(request, decodedURI, semicolon, mappingData);
                } else {
                    return new SuperCallable(request, response);
                }
            }

            if (LOGGER.isLoggable(FINE)) {
                LOGGER.log(FINE, "Request: {0} was mapped to Adapter: {1}", new Object[] { decodedURI, httpHandler });
            }

            // The Adapter used for servicing static pages doesn't decode the
            // request by default, hence do not pass the undecoded request.
            if (httpHandler == null || httpHandler instanceof ContainerMapper) {
                return new SuperCallable(request, response);
            }

            return new HttpHandlerCallable(httpHandler, request, response);

        } finally {
            mapperLock.readLock().unlock();
        }
    }

    private void initializeFileURLPattern(String ext) {
        for (Sniffer sniffer : grizzlyService.getServiceLocator().getAllServices(Sniffer.class)) {
            boolean match = false;
            if (sniffer.getURLPatterns() != null) {

                for (String pattern : sniffer.getURLPatterns()) {
                    if (pattern.equalsIgnoreCase(ext)) {
                        match = true;
                        break;
                    }
                }

                HttpHandler httpHandler;
                if (match) {
                    httpHandler = grizzlyService.getServiceLocator().getService(SnifferAdapter.class);
                    ((SnifferAdapter) httpHandler).initialize(sniffer, this);
                    ContextRootInfo contextRootInfo = new ContextRootInfo(httpHandler, null);

                    mapperLock.readLock().unlock();
                    mapperLock.writeLock().lock();
                    try {
                        for (String pattern : sniffer.getURLPatterns()) {
                            for (String host : grizzlyService.hosts) {
                                mapper.addWrapper(host, ROOT, pattern, contextRootInfo, "*.jsp".equals(pattern) || "*.jspx".equals(pattern));
                            }
                        }
                    } finally {
                        mapperLock.readLock().lock();
                        mapperLock.writeLock().unlock();
                    }

                    return;
                }
            }
        }
    }

    /**
     * Maps the decodedURI to the corresponding Adapter, considering that URI may have a semicolon with extra data followed,
     * which shouldn't be a part of mapping process.
     *
     * @param req HTTP request
     * @param decodedURI URI
     * @param semicolonPos semicolon position. Might be <tt>0</tt> if position wasn't resolved yet (so it will be resolved
     * in the method), or <tt>-1</tt> if there is no semicolon in the URI.
     * @param mappingData
     * @return
     * @throws Exception
     */
    final HttpHandler mapUriWithSemicolon(final Request req, final DataChunk decodedURI, int semicolonPos, final MappingData mappingData) throws Exception {
        mapperLock.readLock().lock();

        try {
            final CharChunk charChunk = decodedURI.getCharChunk();
            final int oldStart = charChunk.getStart();
            final int oldEnd = charChunk.getEnd();

            if (semicolonPos == 0) {
                semicolonPos = decodedURI.indexOf(';', 0);
            }

            DataChunk localDecodedURI = decodedURI;
            if (semicolonPos >= 0) {
                charChunk.setEnd(semicolonPos);
                // duplicate the URI path, because Mapper may corrupt the attributes,
                // which follow the path
                localDecodedURI = req.getNote(DATA_CHUNK);
                if (localDecodedURI == null) {
                    localDecodedURI = DataChunk.newInstance();
                    req.setNote(DATA_CHUNK, localDecodedURI);
                }
                localDecodedURI.duplicate(decodedURI);
            }

            try {
                return map(req, localDecodedURI, mappingData);
            } finally {
                charChunk.setStart(oldStart);
                charChunk.setEnd(oldEnd);
            }
        } finally {
            mapperLock.readLock().unlock();
        }
    }

    private HttpHandler map(final Request req, final DataChunk decodedURI, MappingData mappingData) throws Exception {
        if (mappingData == null) {
            mappingData = getNote(req);
        }

        // Map the request to its Adapter/Container and also it's Servlet if
        // the request is targetted to the CoyoteAdapter.
        mapper.map(req.getRequest().serverName(), decodedURI, mappingData);

        updatePaths(req, mappingData);

        ContextRootInfo contextRootInfo;
        if (mappingData.context != null && (mappingData.context instanceof ContextRootInfo || mappingData.wrapper instanceof ContextRootInfo)) {
            if (mappingData.wrapper != null) {
                contextRootInfo = (ContextRootInfo) mappingData.wrapper;
            } else {
                contextRootInfo = (ContextRootInfo) mappingData.context;
            }

            return contextRootInfo.getHttpHandler();
        } else if (mappingData.context != null && "com.sun.enterprise.web.WebModule".equals(mappingData.context.getClass().getName())) {
            return mapper.getHttpHandler();
        }

        return null;
    }

    public void register(String contextRoot, Collection<String> hostNames, HttpHandler httpService, ApplicationContainer container) {
        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, "MAPPER({0}) REGISTER contextRoot: {1} adapter: {2} container: {3} port: {4}",
                    new Object[] { this, contextRoot, httpService, container, String.valueOf(listener.getPort()) });
        }

        mapMultipleAdapter = true;
        ContextRootInfo contextRootInfo = new ContextRootInfo(httpService, container);
        for (String hostName : hostNames) {
            mapper.addContext(hostName, contextRoot, contextRootInfo, new String[0], null);
        }
    }

    public void unregister(String contextRoot) {
        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, "MAPPER({0}) UNREGISTER contextRoot: {1}", new Object[] { this, contextRoot });
        }

        for (String host : grizzlyService.hosts) {
            mapper.removeContext(host, contextRoot);
        }
    }

    public void register(final Endpoint endpoint) {
        if (LOGGER.isLoggable(FINE)) {
            LOGGER.log(FINE, "MAPPER({0}) REGISTER endpoint: {1}", new Object[] { this, endpoint });
        }

        mapMultipleAdapter = true;
        final String contextRoot = endpoint.getContextRoot();
        final Collection<String> virtualServerNames = endpoint.getVirtualServers();

        ContextRootInfo contextRootInfo = new ContextRootInfo(new ContextRootInfo.Holder() {
            @Override
            public HttpHandler getHttpHandler() {
                return endpoint.getEndpointHandler();
            }

            @Override
            public Object getContainer() {
                return endpoint.getContainer();
            }
        });

        for (String hostName : virtualServerNames) {
            mapper.addContext(hostName, contextRoot, contextRootInfo, new String[0], null);
        }
    }

    public void unregister(final Endpoint endpoint) {
        unregister(endpoint.getContextRoot());
    }


    protected static MappingData getNote(Request request) {
        return request.getNote(MAPPING_DATA);
    }

    private final static class HttpHandlerCallable implements Callable<Object> {

        private final HttpHandler httpHandler;
        private final Request request;
        private final Response response;

        private HttpHandlerCallable(final HttpHandler httpHandler, final Request request, final Response response) {
            this.httpHandler = httpHandler;
            this.request = request;
            this.response = response;
        }

        @Override
        public Object call() throws Exception {
            httpHandler.service(request, response);
            return null;
        }
    }

    private final class SuperCallable implements Callable<Object> {

        private final Request req;
        private final Response res;

        private SuperCallable(Request req, Response res) {
            this.req = req;
            this.res = res;
        }

        @Override
        public Object call() throws Exception {
            ContainerMapper.super.service(req, res);
            return null;
        }
    }

    private static final class AfterServiceListenerImpl implements AfterServiceListener {

        @Override
        public void onAfterService(final Request request) {
            final MappingData mappingData = getNote(request);
            if (mappingData != null) {
                mappingData.recycle();
            }
        }
    }
}
