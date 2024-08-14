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

/*
 * EndpointImpl.java
 *
 * Created on March 14, 2005, 10:35 AM
 */

package org.glassfish.webservices.monitoring;

import com.sun.enterprise.deployment.WebServiceEndpoint;

import java.util.ArrayList;
import java.util.List;


/**
 * Implementation of the endpoint interface
 *
 * @author Jerome Dochez
 */
public class EndpointImpl implements Endpoint {

    public final static String NAME = "MONITORING_ENDPOINT";
    public final static String MESSAGE_ID = "MONITORING_MESSAGE_ID";
    public final static String REQUEST_TRACE = "MONITORING_REQUEST_MESSAGE_TRACE";

    final String endpointSelector;
    final EndpointType type;
    WebServiceEndpoint endpointDesc;
    List<MessageListener> listeners = new ArrayList<MessageListener>();

    /** Creates a new instance of EndpointImpl */
    EndpointImpl(String endpointSelector, EndpointType type) {
        this.endpointSelector = endpointSelector;
        this.type = type;
    }

    /**
     * @return the endpoint URL as a string. This is the URL
     * web service clients use to invoke the endpoint.
     */
    public String getEndpointSelector() {
        return endpointSelector;
    }

    /**
     * @return the endpoint type
     */
    public EndpointType getEndpointType() {
        return type;
    }

    /**
     * Returns the Transport type
     */
    public TransportType getTransport() {
        return TransportType.HTTP;
    }

    /**
     * registers a new SOAPMessageListener for this endpoint
     * @param  newListener instance to register.
     */
    public void addListener(MessageListener newListener) {
        listeners.add(newListener);
    }

    /**
     * unregiters a SOAPMessageListener for this endpoint
     * @param  listener instance to unregister.
     */
    public void removeListener(MessageListener listener) {
        listeners.remove(listener);
    }

    /**
     * Returns true if this endpoint has listeners registered
     * @return true if at least one listener is registered
     */
    public boolean hasListeners() {
        return !listeners.isEmpty();
    }

    /**
     * Return the deployment descriptors associated with this
     * endpoint.
     */
    public WebServiceEndpoint getDescriptor() {
        return endpointDesc;
    }

    /**
     * Set the WebServiceEndpoint DOL descriptor
     */
    public void setDescriptor(WebServiceEndpoint endpointDesc) {

        if (endpointDesc!=null) {
            endpointDesc.addExtraAttribute(EndpointImpl.NAME, this);
        }
        this.endpointDesc = endpointDesc;
    }
}
