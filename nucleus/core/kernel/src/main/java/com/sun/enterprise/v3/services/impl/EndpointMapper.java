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

import java.util.Collection;

import org.glassfish.api.container.EndpointRegistrationException;
import org.glassfish.api.deployment.ApplicationContainer;

/**
 * registration interface to use with the Mapper classes.
 *
 * @author Jeanfrancois Arcand
 */
public interface EndpointMapper<E> {


    /**
     * Registers a new endpoint (proxy implementation) for a particular
     * context-root. All request coming with the context root will be dispatched
     * to the proxy instance passed in.
     * @param contextRoot for the proxy
     * @param endpointAdapter servicing requests.
     */
    public void registerEndpoint(String contextRoot, Collection<String> vsServers, E adapter,
                                 ApplicationContainer container) throws EndpointRegistrationException;


    /**
     * Removes the context-root from our list of endpoints.
     */
    public void unregisterEndpoint(String contextRoot, ApplicationContainer app) throws EndpointRegistrationException;

    /**
     * Registers a new endpoint (proxy implementation) defined by the passed
     * {@link Endpoint} object.
     * @param endpoint {@link Endpoint}
     */
    public void registerEndpoint(Endpoint endpoint) throws EndpointRegistrationException;

    /**
     * Removes the {@link Endpoint} from our list of endpoints.
     * @param endpoint {@link Endpoint}
     */
    public void unregisterEndpoint(Endpoint endpoint) throws EndpointRegistrationException;
}
