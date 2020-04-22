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

package org.glassfish.webservices.monitoring;

import org.glassfish.webservices.SOAPMessageContext;


/**
 * This interface permits implementors to register a global message listener
 * which will be notified for all the web services requests and responses
 * on installed and enabled Web Services. Each invocation will be notified
 * through founr callbacks (preProcessRequest, processRequest, processResponse,
 * postProcessResponse).
 *
 * @author Jerome Dochez
 */
public interface GlobalMessageListener {

    /**
     * Callback when a web service request entered the web service container
     * and before any system processing is done.
     * @param endpoint is the endpoint the web service request is targeted to
     * @return a message ID to trace the request in the subsequent callbacks
     * or null if this invocation should not be traced further.
     */
    public String preProcessRequest(Endpoint endpoint);

    /**
     * Callback when a 2.X web service request is about the be delivered to the
     * Web Service Implementation Bean.
     * @param mid message ID returned by preProcessRequest call
     * @param ctx the jaxrpc message trace, transport dependent
     */
    public void processRequest(String mid, SOAPMessageContext ctx, TransportInfo info);

    /**
     * Callback when a 2.X web service response was returned by the Web Service
     * Implementation Bean
     * @param mid message ID returned by the preProcessRequest call
     * @param ctx jaxrpc message trace, transport dependent.
     */
    public void processResponse(String mid, SOAPMessageContext ctx);

    /**
     * Callback when a web service response has finished being processed
     * by the container and was sent back to the client
     * @param mid returned by the preProcessRequest call
     * @param info the response transport dependent information
     */
    public void postProcessResponse(String mid, TransportInfo info);

}

