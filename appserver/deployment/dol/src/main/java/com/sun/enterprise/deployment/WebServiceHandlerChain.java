/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.glassfish.deployment.common.Descriptor;

/**
 * Represents a single handler-chains in a webservice in webservices.xml
 */
public class WebServiceHandlerChain extends Descriptor {

    private static final long serialVersionUID = 1L;

    // List of handlers associated with this endpoint.
    // Handler order is important and must be preserved.
    private LinkedList<WebServiceHandler> handlers;

    private String protocolBinding;
    private String serviceNamePattern;
    private String portNamePattern;

    // copy constructor
    public WebServiceHandlerChain(WebServiceHandlerChain other) {
        super(other);
        this.protocolBinding = other.protocolBinding;
        this.serviceNamePattern = other.serviceNamePattern;
        this.portNamePattern = other.portNamePattern;
        if (other.handlers == null) {
            handlers = null;
        } else {
            handlers = new LinkedList<>();
            for (WebServiceHandler handler : other.handlers) {
                handlers.addLast(new WebServiceHandler(handler));
            }
        }
    }


    public WebServiceHandlerChain() {
        handlers = new LinkedList<>();
    }


    public void setProtocolBindings(String bindingId) {
        protocolBinding = bindingId;

    }


    public String getProtocolBindings() {
        return protocolBinding;
    }


    public void setServiceNamePattern(String pattern) {
        serviceNamePattern = pattern;
    }


    public String getServiceNamePattern() {
        return serviceNamePattern;
    }


    public void setPortNamePattern(String pattern) {
        portNamePattern = pattern;
    }


    public String getPortNamePattern() {
        return portNamePattern;
    }


    /**
     * @return true if this endpoint has at least one handler in its
     *         handler chain.
     */
    public boolean hasHandlers() {
        return !handlers.isEmpty();
    }


    /**
     * Append handler to end of handler chain for this endpoint.
     */
    public void addHandler(WebServiceHandler handler) {
        handlers.addLast(handler);
    }


    public void removeHandler(WebServiceHandler handler) {
        handlers.remove(handler);
    }


    public void removeHandlerByName(String handlerName) {
        for (Iterator<WebServiceHandler> iter = handlers.iterator(); iter.hasNext();) {
            WebServiceHandler next = iter.next();
            if (next.getHandlerName().equals(handlerName)) {
                iter.remove();
                break;
            }
        }
    }


    /**
     * @return ordered list of WebServiceHandler handlers for this endpoint.
     */
    public List<WebServiceHandler> getHandlers() {
        return handlers;
    }
}
