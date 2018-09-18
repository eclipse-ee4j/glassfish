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

package org.glassfish.webservices.monitoring;

import java.util.Iterator;

/**
 * This interface holds all behaviour associated with the
 * web service engine. For instance, it gives a list of all
 * registered endpoints, provide hooks to register listener
 * interfaces for endpoints creation/deletion and so on...
 *
 * @author Jerome Dochez
 * @author Bhakti Mehta
 */
public interface WebServiceEngine {

    /**
     * @return an iterator of all the registered active endpoints in
     * the engine.
     */
    public Iterator<Endpoint> getEndpoints();

    /**
     * @return an Endpoint instance if the supplied selector is the endpoint's
     * invocation selector. In case of HTTP based web services, the selector is
     * the endpoint URL
     * @param endpointSelector the endpoint selector
     */
    public Endpoint getEndpoint(String endpointSelector);

    /**
     * Register a new listener interface to receive notification of
     * web service endpoint creation and deletion
     * @param listener instance to register
     */
    public void addLifecycleListener(EndpointLifecycleListener listener);

    /**
     * Unregister a listener interface
     * @param listener to unregister.
     */
    public void removeLifecycleListener(EndpointLifecycleListener listener);

    /**
     * Register a new listener interface to receive authentication
     * notification.
     * @param listener to add
     */
    public void addAuthListener(AuthenticationListener listener);

    /**
     * Unregister a listener interface
     * @param listener to remove
     */
    public void removeAuthListener(AuthenticationListener listener);

    /**
     * Set the unique global listener interface to trace all web service requests
     * or responses. Set to null if no tracing is needed
     * @param listener to register
     */
    public void setGlobalMessageListener(GlobalMessageListener listener);

    /**
     * get the global listener interface or null if none is set.
     * @return the global message listener
     */
    public GlobalMessageListener getGlobalMessageListener();

}
