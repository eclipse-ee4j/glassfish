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

package org.glassfish.webservices;

import org.glassfish.webservices.monitoring.*;

import javax.xml.namespace.QName;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPBinding;

import com.sun.xml.ws.api.pipe.Pipe;
import com.sun.xml.ws.api.message.Message;
import com.sun.xml.ws.api.pipe.PipeCloner;
import com.sun.xml.ws.api.message.Packet;
import com.sun.xml.ws.api.model.SEIModel;
import com.sun.xml.ws.api.model.JavaMethod;
import com.sun.xml.ws.api.model.wsdl.WSDLPort;
import com.sun.xml.ws.api.server.WSEndpoint;
import com.sun.xml.ws.api.pipe.ServerPipeAssemblerContext;

import com.sun.enterprise.deployment.WebServiceEndpoint;

import com.sun.xml.ws.api.pipe.helper.AbstractFilterPipeImpl;

/**
 * This pipe is used to do app server monitoring
 */
public class MonitoringPipe extends AbstractFilterPipeImpl {

    private final SEIModel seiModel;
    private final WSDLPort wsdlModel;
    private final WSEndpoint owner;
    private final WebServiceEndpoint endpoint;
    private final WebServiceEngineImpl wsEngine;

    public MonitoringPipe(ServerPipeAssemblerContext ctxt, Pipe tail,
                          WebServiceEndpoint ep) {
        super(tail);
        this.endpoint = ep;
        this.seiModel = ctxt.getSEIModel();
        this.wsdlModel = ctxt.getWsdlModel();
        this.owner = ctxt.getEndpoint();
        wsEngine = WebServiceEngineImpl.getInstance();
    }

    public MonitoringPipe(MonitoringPipe that, PipeCloner cloner) {
        super(that, cloner);
        this.endpoint = that.endpoint;
        this.seiModel = that.seiModel;
        this.wsdlModel = that.wsdlModel;
        this.owner = that.owner;
        wsEngine = WebServiceEngineImpl.getInstance();
    }

    public final Pipe copy(PipeCloner cloner) {
        return new MonitoringPipe(this, cloner);
    }

    public Packet process(Packet request) {
        // if it is a JBI request then skip the monitoring logic. This is done 
        // as HTTPServletRequest/Response is not available when the invocation 
        // is from JavaEE service engine.

        String delegateClassName = request.webServiceContextDelegate.getClass().getName();
        if (delegateClassName.equals("com.sun.enterprise.jbi.serviceengine." +
                "bridge.transport.NMRServerConnection")) {
            return next.process(request);
        }
  
        // No monitoring available for restful services
        if("http://www.w3.org/2004/08/wsdl/http".equals(endpoint.getProtocolBinding())) {
            return next.process(request);
        }
        SOAPMessageContext ctxt = new SOAPMessageContextImpl(request);
        HttpServletRequest httpRequest =
                (HttpServletRequest) request.get(javax.xml.ws.handler.MessageContext.SERVLET_REQUEST);
        HttpServletResponse httpResponse =
                (HttpServletResponse) request.get(javax.xml.ws.handler.MessageContext.SERVLET_RESPONSE);

        String messageId=null;

        JAXWSEndpointImpl endpt1;
        if(endpoint.implementedByWebComponent()) {
            endpt1 = (JAXWSEndpointImpl)wsEngine.getEndpoint(httpRequest.getServletPath());
        } else {
            endpt1 = (JAXWSEndpointImpl)wsEngine.getEndpoint(httpRequest.getRequestURI());
        }
        messageId = wsEngine.preProcessRequest(endpt1);
        if (messageId!=null) {
            ctxt.put(EndpointImpl.MESSAGE_ID, messageId);
            ThreadLocalInfo config = new ThreadLocalInfo(messageId, httpRequest);
            wsEngine.getThreadLocal().set(config);
        }

        try {

            endpt1.processRequest(ctxt);

        } catch (Exception e) {
            // temporary - need to send back SOAP fault message
        }

        Packet pipeResponse = next.process(request);

        //Make the response packet available in the MessageContext
        ((SOAPMessageContextImpl)ctxt).setPacket(pipeResponse);


        try {
            if (endpt1 != null) {
                endpt1.processResponse(ctxt);
            }

        } catch (Exception e) {
            // temporary - need to send back SOAP fault message
        }

        if (messageId!=null) {
            HttpResponseInfoImpl info = new HttpResponseInfoImpl(httpResponse);
            wsEngine.postProcessResponse(messageId, info);
        }
        return pipeResponse;
    }
}
