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

//import com.sun.enterprise.Switch;

import com.sun.enterprise.container.common.spi.util.ComponentEnvManager;
import com.sun.enterprise.deployment.*;
import com.sun.xml.ws.api.server.Adapter;
import com.sun.xml.ws.transport.http.servlet.ServletAdapter;
import org.glassfish.webservices.monitoring.Endpoint;
import org.glassfish.webservices.monitoring.WebServiceEngineImpl;
import org.glassfish.webservices.monitoring.WebServiceTesterServlet;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.xml.ws.WebServiceException;
import javax.xml.ws.http.HTTPBinding;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.external.probe.provider.annotations.Probe;
import org.glassfish.external.probe.provider.annotations.ProbeParam;
import org.glassfish.external.probe.provider.annotations.ProbeProvider;

/**
 * The JAX-WS dispatcher servlet.
 *
 * @author Bhakti Mehta
 * @author Rama Pulavarthi
 */
@ProbeProvider(moduleProviderName = "glassfish", moduleName = "webservices", probeProviderName = "servlet-109")
public class JAXWSServlet extends HttpServlet {

    private static final Logger logger = LogUtils.getLogger();
    private WebServiceEndpoint endpoint;
    private String contextRoot;
    private transient WebServiceEngineImpl wsEngine_;
    private boolean wsdlExposed = true;
    private String urlPattern;

    public void init(ServletConfig servletConfig) throws ServletException {
        String servletName = "unknown";

        try {
            super.init(servletConfig);
            wsEngine_ = WebServiceEngineImpl.getInstance();
            // Register endpoints here


            WebServiceContractImpl wscImpl = WebServiceContractImpl.getInstance();
            ComponentEnvManager compEnvManager = wscImpl.getComponentEnvManager();
            JndiNameEnvironment jndiNameEnv = compEnvManager.getCurrentJndiNameEnvironment();
            WebBundleDescriptor webBundle = null;
            if (jndiNameEnv != null && jndiNameEnv instanceof WebBundleDescriptor) {
                webBundle = ((WebBundleDescriptor) jndiNameEnv);
            } else {
                throw new WebServiceException("Cannot intialize the JAXWSServlet for " + jndiNameEnv);
            }


            servletName = servletConfig.getServletName();
            contextRoot = webBundle.getContextRoot();
            WebComponentDescriptor webComponent =
                    webBundle.getWebComponentByCanonicalName(servletName);

            if (webComponent != null) {
                WebServicesDescriptor webServices = webBundle.getWebServices();
                Collection<WebServiceEndpoint> endpoints =
                        webServices.getEndpointsImplementedBy(webComponent);
                // Only 1 endpoint per servlet is supported, even though
                // data structure implies otherwise.
                endpoint = endpoints.iterator().next();
            } else {
                throw new ServletException(servletName + " not found");
            }
            // need to invoke the endpoint lifecylcle
            if (!(HTTPBinding.HTTP_BINDING.equals(endpoint.getProtocolBinding()))) {
                // Doing this so that restful service are not monitored
                wsEngine_.createHandler(endpoint);
            }
            if (endpoint.getWsdlExposed() != null) {
                wsdlExposed = Boolean.parseBoolean(endpoint.getWsdlExposed());
            }
            // For web components, this will be relative to the web app
            // context root.  Make sure there is a leading slash.
            String uri = endpoint.getEndpointAddressUri();
            urlPattern = uri.startsWith("/") ? uri : "/" + uri;

        } catch (Throwable t) {
            String msg = MessageFormat.format(
                    logger.getResourceBundle().getString(LogUtils.SERVLET_ENDPOINT_FAILURE),
                    servletName);
            logger.log(Level.WARNING, msg, t);
            throw new ServletException(t);
        }
    }

    public void destroy() {
        synchronized (this) {
            wsEngine_.removeHandler(endpoint);
        }
    }

    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException {
        startedEvent(endpoint.getEndpointAddressPath());
        if (("Tester".equalsIgnoreCase(request.getQueryString())) &&
                (!(HTTPBinding.HTTP_BINDING.equals(endpoint.getProtocolBinding())))) {
            Endpoint endpt = wsEngine_.getEndpoint(request.getServletPath());
            if (endpt != null && Boolean.parseBoolean(endpt.getDescriptor().getDebugging())) {
                WebServiceTesterServlet.invoke(request, response,
                        endpt.getDescriptor());
                endedEvent(endpoint.getEndpointAddressPath());
                return;
            }
        }

        // lookup registered URLs and get the appropriate adapter;
        // pass control to the adapter
        try {
            ServletAdapter targetEndpoint = (ServletAdapter) getEndpointFor(request);
            if (targetEndpoint != null) {
                targetEndpoint.handle(getServletContext(), request, response);
            } else {
                throw new ServletException("Service not found");
            }
        } catch (Throwable t) {
            ServletException se = new ServletException();
            se.initCause(t);
            throw se;
        }
        endedEvent(endpoint.getEndpointAddressPath());
    }

    @Probe(name = "startedEvent")
    private void startedEvent(
            @ProbeParam("endpointAddress") String endpointAddress) {

    }

    @Probe(name = "endedEvent")
    private void endedEvent(
            @ProbeParam("endpointAddress") String endpointAddress) {

    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        if (("Tester".equalsIgnoreCase(request.getQueryString())) &&
                (!(HTTPBinding.HTTP_BINDING.equals(endpoint.getProtocolBinding())))) {

            Endpoint endpt = wsEngine_.getEndpoint(request.getServletPath());
            if ((endpt != null) && ((endpt.getDescriptor().isSecure()) ||
                    (endpt.getDescriptor().getMessageSecurityBinding() != null) ||
                    endpoint.hasSecurePipeline())) {
                String message = endpt.getDescriptor().getWebService().getName() +
                        "is a secured web service; Tester feature is not supported for secured services";
                (new WsUtil()).writeInvalidMethodType(response, message);
                return;
            }
            if (endpt != null && Boolean.parseBoolean(endpt.getDescriptor().getDebugging())) {

                WebServiceTesterServlet.invoke(request, response,
                        endpt.getDescriptor());

                return;
            }
        }

        // If it is not a "Tester request" and it is not a WSDL request,
        // this might be a restful service

        if (!("WSDL".equalsIgnoreCase(request.getQueryString())) &&
                (HTTPBinding.HTTP_BINDING.equals(endpoint.getProtocolBinding()))) {
            doPost(request, response);
            return;
        }

        // normal WSDL retrieval invocation
        try {
            ServletAdapter targetEndpoint = (ServletAdapter) getEndpointFor(request);
            if (targetEndpoint != null && wsdlExposed) {
                targetEndpoint.publishWSDL(getServletContext(), request, response);
            } else {
                String message =
                        "Invalid wsdl request " + request.getRequestURL();
                (new WsUtil()).writeInvalidMethodType(response, message);
            }
        } catch (Throwable t) {
            ServletException se = new ServletException();
            se.initCause(t);
            throw se;
        }
    }

    private Adapter getEndpointFor(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return JAXWSAdapterRegistry.getInstance().getAdapter(contextRoot, urlPattern, path);
    }
}
