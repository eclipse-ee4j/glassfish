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

import jakarta.servlet.*;
import jakarta.servlet.http.*;

import com.sun.xml.rpc.spi.JaxRpcObjectFactory;
import com.sun.xml.rpc.spi.runtime.ServletDelegate;
import com.sun.xml.rpc.spi.runtime.ServletSecondDelegate;
import org.glassfish.webservices.monitoring.*;
import org.apache.catalina.Loader;


/**
 * The JAX-RPC dispatcher servlet.
 *
 */
public class JAXRPCServlet extends HttpServlet {

    private volatile ServletDelegate delegate_;
    private volatile ServletWebServiceDelegate myDelegate_=null;

    public void init(ServletConfig servletConfig) throws ServletException {
        try {
            super.init(servletConfig);
            JaxRpcObjectFactory rpcFactory = JaxRpcObjectFactory.newInstance();
            delegate_ =
                    (ServletDelegate) rpcFactory.createServletDelegate();
            myDelegate_ = new ServletWebServiceDelegate(delegate_);
            delegate_.setSecondDelegate(myDelegate_);
            delegate_.init(servletConfig);
        } catch (ServletException e) {
            throw e;
        } catch (Throwable e) {
            throw new ServletException(e);
        }
    }

    public void destroy() {
        if (delegate_ != null) {
            delegate_.destroy();
        }
        if (myDelegate_ != null) {
            myDelegate_.destroy();
        }
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
        throws ServletException {
        
        WebServiceEngineImpl wsEngine_ = WebServiceEngineImpl.getInstance();

        if ("Tester".equalsIgnoreCase(request.getQueryString())) {
            Endpoint endpt = wsEngine_.getEndpoint(request.getServletPath());
            if (endpt!=null && Boolean.parseBoolean(endpt.getDescriptor().getDebugging())) {
                WebServiceTesterServlet.invoke(request, response,
                        endpt.getDescriptor());
                return;
            }
        }
                
        if (delegate_ != null) {
            // check if we need to trace this...
            String messageId=null;
            if (wsEngine_.getGlobalMessageListener()!=null) {
                Endpoint endpt = wsEngine_.getEndpoint(request.getServletPath());
                messageId = wsEngine_.preProcessRequest(endpt);
                if (messageId!=null) {
                    ThreadLocalInfo config = new ThreadLocalInfo(messageId, request);
                    wsEngine_.getThreadLocal().set(config);
                }
            }

            delegate_.doPost(request, response);

            if (messageId!=null) {
                HttpResponseInfoImpl info = new HttpResponseInfoImpl(response);
                wsEngine_.postProcessResponse(messageId, info);
            }
        }
    }

    protected void doGet(HttpServletRequest request, 
                         HttpServletResponse response)
        throws ServletException {
        
        // test for tester servlet invocation.
        if ("Tester".equalsIgnoreCase(request.getQueryString())) {

            Endpoint endpt = WebServiceEngineImpl.getInstance().getEndpoint(request.getServletPath());
            if (endpt!=null && Boolean.parseBoolean(endpt.getDescriptor().getDebugging())) {
                Loader loader = (Loader) endpt.getDescriptor().getBundleDescriptor().getExtraAttribute("WEBLOADER");
                if (loader != null) {
                    endpt.getDescriptor().getBundleDescriptor().setClassLoader(loader.getClassLoader());
                    endpt.getDescriptor().getBundleDescriptor().removeExtraAttribute("WEBLOADER");
                }
                WebServiceTesterServlet.invoke(request, response,
                        endpt.getDescriptor());
                return;
            }
        }
        if (delegate_ != null) {
            delegate_.doGet(request, response);
        }
    }
}
