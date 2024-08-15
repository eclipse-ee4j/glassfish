/*
 * Copyright (c) 2006, 2020 Oracle and/or its affiliates. All rights reserved.
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

import jakarta.servlet.http.HttpServletRequest;
import jakarta.xml.ws.handler.MessageContext;

import org.glassfish.webservices.LogUtils;
import org.glassfish.webservices.SOAPMessageContext;

import static java.util.logging.Level.WARNING;
import static org.glassfish.webservices.LogUtils.EXCEPTION_TRACING_RESPONSE;


public class JAXWSEndpointImpl extends EndpointImpl {

    JAXWSEndpointImpl(String endpointSelector, EndpointType type) {
        super(endpointSelector, type);
    }

    public boolean processRequest(SOAPMessageContext messageContext) throws Exception {

        boolean status = true;

        // let's get our thread local context
        WebServiceEngineImpl wsEngine = WebServiceEngineImpl.getInstance();
        try {
            if (!listeners.isEmpty() || wsEngine.hasGlobalMessageListener()) {

                String messageID = (String) messageContext.get(MESSAGE_ID);

                // someone is listening ?
                if (messageID != null) {
                    HttpServletRequest httpReq = (HttpServletRequest) messageContext.get(MessageContext.SERVLET_REQUEST);
                    HttpRequestInfoImpl info = new HttpRequestInfoImpl(httpReq);
                    wsEngine.processRequest(messageID, messageContext, info);
                }

                // any local listeners ?
                if (!listeners.isEmpty()) {
                    // create the message trace and save it to our message context
                    MessageTraceImpl request = new MessageTraceImpl();
                    request.setEndpoint(this);
                    request.setMessageContext(messageContext);
                    HttpServletRequest httpReq = (HttpServletRequest) messageContext.get(MessageContext.SERVLET_REQUEST);
                    request.setTransportInfo(new HttpRequestInfoImpl(httpReq));
                    messageContext.put(EndpointImpl.REQUEST_TRACE, request);
                }
            }
        } catch (Exception e) {
            WebServiceEngineImpl.sLogger.log(WARNING, LogUtils.EXCEPTION_TRACING_REQUEST, e.getMessage());
            throw e;
        }
        return status;
    }

    public void processResponse(SOAPMessageContext messageContext) throws Exception {

        // let's get our thread local context
        WebServiceEngineImpl wsEngine = WebServiceEngineImpl.getInstance();
        try {

            if (wsEngine.hasGlobalMessageListener() || !listeners.isEmpty()) {

                String messageID = (String) messageContext.get(MESSAGE_ID);
                // do we have a global listener ?
                if (messageID != null) {
                    wsEngine.processResponse(messageID, messageContext);
                }

                // local listeners
                if (!listeners.isEmpty()) {
                    MessageTraceImpl response = new MessageTraceImpl();
                    response.setEndpoint(this);
                    response.setMessageContext(messageContext);
                    // TODO BM check regarding this method
                    for (org.glassfish.webservices.monitoring.MessageListener listener : listeners) {
                        listener.invocationProcessed((MessageTrace) messageContext.get(REQUEST_TRACE), response);
                    }
                }
            }
        } catch (Exception e) {
            WebServiceEngineImpl.sLogger.log(WARNING, EXCEPTION_TRACING_RESPONSE, e.getMessage());
            throw e;
        }
    }

}
