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

package org.glassfish.webservices.transport.tcp;

import com.sun.enterprise.web.WebComponentInvocation;
import com.sun.enterprise.web.WebModule;
import com.sun.istack.NotNull;
import com.sun.xml.ws.api.server.Adapter;
import com.sun.xml.ws.transport.tcp.resources.MessagesMessages;
import com.sun.xml.ws.transport.tcp.server.TCPAdapter;
import com.sun.xml.ws.transport.tcp.server.WSTCPAdapterRegistry;
import com.sun.xml.ws.transport.tcp.util.WSTCPURI;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.invocation.ComponentInvocation;
import org.glassfish.webservices.AdapterInvocationInfo;
import org.glassfish.webservices.EjbRuntimeEndpointInfo;
import org.glassfish.webservices.JAXWSAdapterRegistry;

/**
 * @author Alexey Stashok
 */
public final class WSTCPAdapterRegistryImpl implements WSTCPAdapterRegistry {
    private static final Logger logger = Logger.getLogger(
            com.sun.xml.ws.transport.tcp.util.TCPConstants.LoggingDomain + ".server");

    /**
     * Registry holds correspondents between service name and adapter
     */
    final Map<String, RegistryRecord> registry = new ConcurrentHashMap<String, RegistryRecord>();
    private static final WSTCPAdapterRegistryImpl instance = new WSTCPAdapterRegistryImpl();

    private WSTCPAdapterRegistryImpl() {
    }

    public static @NotNull WSTCPAdapterRegistryImpl getInstance() {
        return instance;
    }

    @Override
    public TCPAdapter getTarget(@NotNull final WSTCPURI requestURI) {
        // path should have format like "/context-root/url-pattern", where context-root and url-pattern could be /xxxx/yyyy/zzzz

        RegistryRecord record;
        // check if URI path is not empty
        if (requestURI.path.length() > 0 && !requestURI.path.equals("/")) {
            record = registry.get(requestURI.path);
        } else {
            record = registry.get("/");
        }

        if (record != null) {
            if (record.adapter == null) {
                try {
                    record.adapter = createWSAdapter(requestURI.path,
                            record.wsEndpointDescriptor);
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, LogUtils.ADAPTER_REGISTERED, requestURI.path);
                    }
                } catch (Exception e) {
                    // This common exception is thrown from ejbEndPtInfo.prepareInvocation(true)
                    logger.log(Level.SEVERE, "WSTCPAdapterRegistryImpl. " +
                            MessagesMessages.WSTCP_0008_ERROR_TCP_ADAPTER_CREATE(
                            record.wsEndpointDescriptor.getWSServiceName()), e);
                }
            }
            return record.adapter;
        }

        return null;
    }


    public void registerEndpoint(@NotNull final String path,
            @NotNull final WSEndpointDescriptor wsEndpointDescriptor) {
        registry.put(path, new RegistryRecord(wsEndpointDescriptor));
    }

    public void deregisterEndpoint(@NotNull final String path) {
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, LogUtils.ADAPTER_DEREGISTERED, path);
        }
        registry.remove(path);
    }

    public WSEndpointDescriptor lookupEndpoint(@NotNull final String path) {
        RegistryRecord record = registry.get(path);
        return record != null ? record.wsEndpointDescriptor : null;
    }

    private TCPAdapter createWSAdapter(@NotNull final String wsPath,
            @NotNull final WSEndpointDescriptor wsEndpointDescriptor) throws Exception {
        if (wsEndpointDescriptor.isEJB()) {
            final EjbRuntimeEndpointInfo ejbEndPtInfo = (EjbRuntimeEndpointInfo) V3Module.getWSEjbEndpointRegistry().
                    getEjbWebServiceEndpoint(wsEndpointDescriptor.getURI(), "POST", null);
            final AdapterInvocationInfo adapterInfo =
                    (AdapterInvocationInfo) ejbEndPtInfo.prepareInvocation(true);

            return new Ejb109Adapter(wsEndpointDescriptor.getWSServiceName().toString(),
                    wsPath, adapterInfo.getAdapter().getEndpoint(),
                    new ServletFakeArtifactSet(wsEndpointDescriptor.getRequestURL(), wsEndpointDescriptor.getUrlPattern()),
                    ejbEndPtInfo, adapterInfo);
        } else {
            final String uri = wsEndpointDescriptor.getURI();
            final Adapter adapter =
                    JAXWSAdapterRegistry.getInstance().getAdapter(wsEndpointDescriptor.getContextRoot(), uri, uri);

            final WebModule webModule = AppServRegistry.getWebModule(wsEndpointDescriptor.getWSServiceEndpoint());
            final ComponentInvocation invocation = new WebComponentInvocation(webModule);

            return new Web109Adapter(wsEndpointDescriptor.getWSServiceName().toString(),
                wsPath,
                adapter.getEndpoint(),
                new ServletFakeArtifactSet(wsEndpointDescriptor.getRequestURL(), wsEndpointDescriptor.getUrlPattern()),
                invocation);
        }
    }

    protected static class RegistryRecord {
        public TCPAdapter adapter;
        public WSEndpointDescriptor wsEndpointDescriptor;

        public RegistryRecord(WSEndpointDescriptor wsEndpointDescriptor) {
            this.wsEndpointDescriptor = wsEndpointDescriptor;
        }
    }
}
