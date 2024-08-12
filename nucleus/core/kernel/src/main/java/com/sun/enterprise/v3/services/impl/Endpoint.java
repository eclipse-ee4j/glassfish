/*
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

package com.sun.enterprise.v3.services.impl;

import java.net.InetAddress;
import java.util.Collection;

import org.glassfish.api.container.Adapter;
import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.grizzly.http.server.HttpHandler;

/**
 * Abstraction represents an endpoint, which could be registered on {@link NetworkProxy}.
 *
 * @author Alexey Stashok
 */
public abstract class Endpoint {

    /**
     * Creates <tt>Endpoint</tt> based on the passed {@link Adapter} descriptor.
     * @param {@link Adapter}
     * @return {@link Endpoint}, which can be registered on {@link NetworkProxy}.
     */
    public static Endpoint createEndpoint(final Adapter adapter) {
        return new AdapterEndpoint(adapter);
    }

    /**
     * @return the {@link InetAddress} on which this endpoint is listening
     */
    public abstract InetAddress getAddress();

    /**
     * Returns the listener port for this endpoint
     * @return listener port
     */
    public abstract int getPort();

    /**
     * Returns the context root for this endpoint
     * @return context root
     */
    public abstract String getContextRoot();

    /**
     * Get the underlying Grizzly {@link HttpHandler}.
     *
     * @return the underlying Grizzly {@link HttpHandler}.
     */
    public abstract HttpHandler getEndpointHandler();

    /**
     * Returns the virtual servers supported by this endpoint
     * @return List&lt;String&gt; the virtual server list supported by the endpoint
     */
    public abstract Collection<String> getVirtualServers();

    /**
     * Return the {@link ApplicationContainer} endpoint belongs to.
     * @return the {@link ApplicationContainer} endpoint belongs to.
     */
    public abstract ApplicationContainer getContainer();


    /**
     * {@link Adapter} based <tt>Endpoint</tt> implementation.
     */
    private static class AdapterEndpoint extends Endpoint {
        private final Adapter adapter;

        public AdapterEndpoint(final Adapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public InetAddress getAddress() {
            return adapter.getListenAddress();
        }

        @Override
        public int getPort() {
            return adapter.getListenPort();
        }

        @Override
        public String getContextRoot() {
            return adapter.getContextRoot();
        }

        @Override
        public HttpHandler getEndpointHandler() {
            return adapter.getHttpService();
        }

        @Override
        public Collection<String> getVirtualServers() {
            return adapter.getVirtualServers();
        }

        @Override
        public ApplicationContainer getContainer() {
            return null;
        }
    }
}
