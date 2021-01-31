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

package org.glassfish.api.container;

import java.net.InetAddress;
import java.util.Collection;

import org.glassfish.api.deployment.ApplicationContainer;
import org.glassfish.grizzly.http.server.HttpHandler;
import org.jvnet.hk2.annotations.Contract;

/**
 * RequestDispatcher is responsible for dispatching incoming requests.
 *
 * @author Jerome Dochez
 */
@Contract
public interface RequestDispatcher {
    /**
     * Registers a new endpoint (proxy implementation) for a particular context-root. All request coming with the context
     * root will be dispatched to the proxy instance passed in.
     *
     * @param contextRoot for the proxy
     * @param endpointAdapter servicing requests.
     */
    void registerEndpoint(String contextRoot, HttpHandler endpointAdapter, ApplicationContainer container) throws EndpointRegistrationException;

    /**
     * Registers a new endpoint (proxy implementation) for a particular context-root. All request coming with the context
     * root will be dispatched to the proxy instance passed in.
     *
     * @param contextRoot for the proxy
     * @param endpointAdapter servicing requests.
     * @param container
     * @param virtualServers comma separated list of the virtual servers
     */
    void registerEndpoint(String contextRoot, HttpHandler endpointAdapter, ApplicationContainer container, String virtualServers)
            throws EndpointRegistrationException;

    /**
     * Registers a new endpoint (proxy implementation) for a particular context-root. All request coming with the context
     * root will be dispatched to the proxy instance passed in.
     *
     * @param contextRoot for the proxy
     * @param endpointAdapter servicing requests.
     */
    void registerEndpoint(String contextRoot, Collection<String> vsServers, HttpHandler endpointAdapter, ApplicationContainer container)
            throws EndpointRegistrationException;

    /**
     * Registers a new endpoint for the given context root at the given port number.
     */
    void registerEndpoint(String contextRoot, InetAddress address, int port, Collection<String> vsServers, HttpHandler endpointAdapter,
            ApplicationContainer container) throws EndpointRegistrationException;

    /**
     * Removes the context root from our list of endpoints.
     */
    void unregisterEndpoint(String contextRoot) throws EndpointRegistrationException;

    /**
     * Removes the context root from our list of endpoints.
     */
    void unregisterEndpoint(String contextRoot, ApplicationContainer app) throws EndpointRegistrationException;
}
