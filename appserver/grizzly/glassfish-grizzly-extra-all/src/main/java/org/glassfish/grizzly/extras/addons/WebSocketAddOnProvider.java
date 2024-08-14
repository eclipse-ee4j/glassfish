/*
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

package org.glassfish.grizzly.extras.addons;

import java.io.IOException;

import org.glassfish.grizzly.config.ConfigAwareElement;
import org.glassfish.grizzly.config.dom.Http;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.server.AddOn;
import org.glassfish.grizzly.http.server.util.Mapper;
import org.glassfish.grizzly.websockets.WebSocketAddOn;
import org.glassfish.grizzly.websockets.WebSocketEngine;
import org.glassfish.grizzly.websockets.WebSocketFilter;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.grizzly.ContextMapper;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

/**
 * Websocket service.
 *
 * @author Alexey Stashok
 */
@Service(name="websocket")
@ContractsProvided({WebSocketAddOnProvider.class, AddOn.class})
public class WebSocketAddOnProvider extends WebSocketAddOn implements ConfigAwareElement<Http> {

    private Mapper mapper;

    @Override
    public void configure(ServiceLocator habitat,
            NetworkListener networkListener, Http configuration) {
        mapper = getMapper(habitat, networkListener);

        setTimeoutInSeconds(Long.parseLong(configuration.getWebsocketsTimeoutSeconds()));
    }

    @Override
    protected WebSocketFilter createWebSocketFilter() {
        return new GlassfishWebSocketFilter(mapper, getTimeoutInSeconds());
    }

    private static Mapper getMapper(final ServiceLocator habitat,
            final NetworkListener listener) {

        final int port;
        try {
            port = Integer.parseInt(listener.getPort());
        } catch (NumberFormatException e) {
            throw new IllegalStateException("Port number is not integer");
        }

        for (Mapper m : habitat.<Mapper>getAllServices(Mapper.class)) {
            if (m.getPort() == port &&
                    m instanceof ContextMapper) {
                ContextMapper cm = (ContextMapper) m;
                if (listener.getName().equals(cm.getId())) {
                    return m;
                }
            }
        }

        return null;
    }

    private static class GlassfishWebSocketFilter extends WebSocketFilter {
        private final Mapper mapper;

        public GlassfishWebSocketFilter(final Mapper mapper,
                long wsTimeoutInSeconds) {
            super(wsTimeoutInSeconds);
            this.mapper = mapper;
        }

        @Override
        protected boolean doServerUpgrade(final FilterChainContext ctx,
                final HttpContent requestContent) throws IOException {
            return !WebSocketEngine.getEngine().upgrade(
                    ctx, requestContent, mapper);
        }
    }
}
