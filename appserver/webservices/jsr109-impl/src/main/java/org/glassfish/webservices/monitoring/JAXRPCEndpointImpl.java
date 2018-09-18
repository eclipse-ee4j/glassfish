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
 * JAXRPCEndpointImpl.java
 */

package org.glassfish.webservices.monitoring;

import com.sun.xml.rpc.spi.runtime.SOAPMessageContext;
import com.sun.xml.rpc.spi.runtime.SystemHandlerDelegate;
import java.util.logging.Level;
import org.glassfish.webservices.LogUtils;

/**
 * Implementation of the JAXRPC endpoint interface and JAXRPC System Handler Delegate
 *
 * @author Jerome Dochez
 */
public class JAXRPCEndpointImpl extends EndpointImpl implements SystemHandlerDelegate {
    
    SystemHandlerDelegate parent = null;
    
    /** Creates a new instance of EndpointImpl */
    JAXRPCEndpointImpl(String endpointSelector, EndpointType type) {
        super(endpointSelector, type);
    }
    
    public boolean processRequest(SOAPMessageContext messageContext) {

	boolean status = true;

        if (parent!=null) {
            status = parent.processRequest(messageContext);
        }

        // let's get our thread local context
        WebServiceEngineImpl wsEngine = WebServiceEngineImpl.getInstance();
        try {
            if (!listeners.isEmpty() || wsEngine.hasGlobalMessageListener()) {
                
                // someone is listening
                ThreadLocalInfo config = 
                        (ThreadLocalInfo) wsEngine.getThreadLocal().get();

                // do we have a global listener ?
                if (config!=null && config.getMessageId()!=null) {
                    HttpRequestInfoImpl info = new HttpRequestInfoImpl(config.getRequest());
                    wsEngine.processRequest(config.getMessageId(), messageContext, info);
                } 
                
                // any local listeners ?
                if (!listeners.isEmpty()) {
                    if (config==null) {
                        config = new ThreadLocalInfo(null, null);
                    }
                    // create the message trace and save it to our thread local
                    MessageTraceImpl request = new MessageTraceImpl();
                    request.setEndpoint(this);
                    request.setMessageContext(messageContext);
                    if (config.getRequest()!=null) {
                        request.setTransportInfo(new HttpRequestInfoImpl(config.getRequest()));
                    }
                    
                    config.setRequestMessageTrace(request);
                }
                
            }
	    } catch(Throwable t) {
                WebServiceEngineImpl.sLogger.log(Level.WARNING, LogUtils.EXCEPTION_TRACING_REQUEST, t.getMessage());
	        RuntimeException re;
            if (t instanceof RuntimeException) {
		        re = (RuntimeException) t;
	        } else {
		        re = new RuntimeException(t);
	        }
	        throw re;
        }        
        return status;
    }

    public void processResponse(SOAPMessageContext messageContext) {   

        // let's get our thread local context
        WebServiceEngineImpl wsEngine = WebServiceEngineImpl.getInstance();
        try {
            
            if (wsEngine.hasGlobalMessageListener() || !listeners.isEmpty()) {
                
                // someone is listening
                ThreadLocalInfo config = 
                        (ThreadLocalInfo) wsEngine.getThreadLocal().get();

                if (config!=null) {                    
                    // do we have a global listener ?
                    if (config.getMessageId()!=null) {
                        wsEngine.processResponse(config.getMessageId(),  messageContext);
                    }

                    // local listeners
                    if (!listeners.isEmpty()) {
                        MessageTraceImpl response = new MessageTraceImpl();
                        response.setEndpoint(this);
                        response.setMessageContext(messageContext);
                        for (MessageListener listener : listeners) {                    
                            listener.invocationProcessed(config.getRequestMessageTrace(), response);
                        }   
                    }
                }
            }
            // cleanup
            wsEngine.getThreadLocal().remove();
            
	        // do security after tracing
	        if (parent!=null) {
		    parent.processResponse(messageContext);
	            }
        
        } catch(Throwable t) {
            WebServiceEngineImpl.sLogger.log(Level.WARNING, LogUtils.EXCEPTION_TRACING_RESPONSE, t.getMessage());
	        RuntimeException re;
	        if (t instanceof RuntimeException) {
		        re = (RuntimeException) t;
	        } else {
		        re = new RuntimeException(t);
	        }
	        throw re;
        }                 
    }
    
    public void setParent(SystemHandlerDelegate parent) {
        this.parent = parent;
    }        
}
