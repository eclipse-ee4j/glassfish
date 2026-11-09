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

import com.sun.enterprise.deployment.WebServiceEndpoint;


/**
 * This interface defines all information and behaviour
 * accessible from a particular endpoint.
 *
 * @author Jerome Dochez
 */
public interface Endpoint {

    /**
     * @return the endpoint selector as a string. For Http transport endpoing,
     * this is the URL web service clients use to invoke the endpoint.
     */
    String getEndpointSelector();

    /**
     * @return the endpoint type
     */
    EndpointType getEndpointType();

    /**
     * Returns the transport
     */
    TransportType getTransport();

    /**
     * Returns the Deployment Descriptors associated with this endpoint
     */
    WebServiceEndpoint getDescriptor();

    /**
     * registers a new SOAPMessageListener for this endpoint
     * @param newListener the listener instance to register.
     */
    void addListener(MessageListener newListener);

    /**
     * unregiters a SOAPMessageListener for this endpoint
     * @param listener the listener instance to unregister.
     */
    void removeListener(MessageListener listener);
}

