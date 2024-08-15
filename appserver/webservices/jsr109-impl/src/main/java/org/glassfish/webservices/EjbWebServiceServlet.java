/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation.
 * Copyright (c) 1997, 2020 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.WebServiceEndpoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Logger;

import org.glassfish.api.logging.LogHelper;
import org.glassfish.ejb.api.EjbEndpointFacade;
import org.glassfish.ejb.spi.WSEjbEndpointRegistry;
import org.glassfish.internal.api.Globals;
import org.glassfish.webservices.monitoring.Endpoint;
import org.glassfish.webservices.monitoring.WebServiceEngineImpl;
import org.glassfish.webservices.monitoring.WebServiceTesterServlet;

import static com.sun.enterprise.deployment.WebServiceEndpoint.PUBLISHING_SUBCONTEXT;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static jakarta.servlet.http.HttpServletResponse.SC_UNAUTHORIZED;
import static jakarta.xml.ws.http.HTTPBinding.HTTP_BINDING;
import static java.util.logging.Level.WARNING;
import static org.glassfish.webservices.LogUtils.AUTH_FAILED;
import static org.glassfish.webservices.LogUtils.EXCEPTION_THROWN;
import static org.glassfish.webservices.LogUtils.INVALID_REQUEST_SCHEME;

/**
 * Servlet responsible for invoking EJB webservice endpoint.
 *
 * Most of this code used to be in com.sun.enterprise.webservice.EjbWebServiceValve.
 *
 * @author Qingqing Ouyang
 * @author Kenneth Saks
 * @author Jan Luehe
 */
public class EjbWebServiceServlet extends HttpServlet {

    private static final Logger logger = LogUtils.getLogger();

    private SecurityService securityService;

    public EjbWebServiceServlet() {
        super();
        if (Globals.getDefaultHabitat() != null) {
            securityService = Globals.get(SecurityService.class);
        }
    }

    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String requestUriRaw = request.getRequestURI();
        String requestUri = (requestUriRaw.charAt(0) == '/') ? requestUriRaw.substring(1) : requestUriRaw;
        String query = request.getQueryString();

        WebServiceEjbEndpointRegistry wsejbEndpointRegistry = (WebServiceEjbEndpointRegistry)
            Globals.getDefaultHabitat().getService(WSEjbEndpointRegistry.class);
        EjbRuntimeEndpointInfo ejbEndpoint = wsejbEndpointRegistry.getEjbWebServiceEndpoint(requestUri, request.getMethod(), query);

        if (requestUri.contains(PUBLISHING_SUBCONTEXT) && ejbEndpoint == null) {
            requestUri = requestUri.substring(0, requestUri.indexOf(PUBLISHING_SUBCONTEXT) - 1);
            ejbEndpoint = wsejbEndpointRegistry.getEjbWebServiceEndpoint(requestUri, request.getMethod(), query);
        }

        if (ejbEndpoint != null) {
            /*
             * We can actually assert that ejbEndpoint is != null, because this EjbWebServiceServlet would not have been invoked
             * otherwise
             */
            String scheme = request.getScheme();
            WebServiceEndpoint endPoint = ejbEndpoint.getEndpoint();
            if ("http".equals(scheme) && endPoint.isSecure()) {
                // redirect to correct protocol scheme if needed
                logger.log(WARNING, INVALID_REQUEST_SCHEME, new Object[] { endPoint.getEndpointName(), "https", scheme });

                URL url = endPoint.composeEndpointAddress(new WsUtil().getWebServerInfoForDAS().getWebServerRootURL(true));
                StringBuilder sb = new StringBuilder(url.toExternalForm());
                if (query != null && query.trim().length() > 0) {
                    sb.append("?");
                    sb.append(query);
                }

                response.sendRedirect(URLEncoder.encode(sb.toString(), "UTF-8"));
            } else {
                boolean dispatch = true;

                // Check if it is a tester servlet invocation
                if ("Tester".equalsIgnoreCase(query) && (!(HTTP_BINDING.equals(endPoint.getProtocolBinding())))) {
                    Endpoint endpoint = WebServiceEngineImpl.getInstance().getEndpoint(request.getRequestURI());
                    if ((endpoint.getDescriptor().isSecure()) || (endpoint.getDescriptor().getMessageSecurityBinding() != null)) {

                        new WsUtil().writeInvalidMethodType(response,
                            endpoint.getDescriptor().getWebService().getName() +
                                "is a secured web service; Tester feature is not supported for secured services");

                        return;
                    }

                    if (Boolean.parseBoolean(endpoint.getDescriptor().getDebugging())) {
                        dispatch = false;
                        WebServiceTesterServlet.invoke(request, response, endpoint.getDescriptor());
                    }
                }

                if ("wsdl".equalsIgnoreCase(query) && (!(HTTP_BINDING.equals(endPoint.getProtocolBinding())))) {
                    if (endPoint.getWsdlExposed() != null && !Boolean.parseBoolean(endPoint.getWsdlExposed())) {
                        response.sendError(SC_NOT_FOUND);
                    }
                }

                if (dispatch) {
                    dispatchToEjbEndpoint(request, response, ejbEndpoint);
                }
            }
        } else {
            response.sendError(SC_NOT_FOUND);
        }
    }

    private void dispatchToEjbEndpoint(HttpServletRequest hreq, HttpServletResponse hresp, EjbRuntimeEndpointInfo ejbEndpoint) {
        EjbEndpointFacade container = ejbEndpoint.getContainer();
        ClassLoader savedClassLoader = null;

        boolean authenticated = false;
        try {
            // Set context class loader to application class loader
            savedClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(container.getEndpointClassLoader());

            // compute realmName
            String realmName = null;
            Application app = ejbEndpoint.getEndpoint().getBundleDescriptor().getApplication();
            if (app != null) {
                realmName = app.getRealm();
            }
            if (realmName == null) {
                realmName = ejbEndpoint.getEndpoint().getRealm();
            }
            if (realmName == null) {
                // use the same logic as BasicAuthenticator
                realmName = hreq.getServerName() + ":" + hreq.getServerPort();
            }

            try {
                if (securityService != null) {
                    WebServiceContextImpl context = (WebServiceContextImpl) ejbEndpoint.getWebServiceContext();
                    authenticated = securityService.doSecurity(hreq, ejbEndpoint, realmName, context);
                }

            } catch (Exception e) {
                // sendAuthenticationEvents(false, hreq.getRequestURI(), null);
                LogHelper.log(logger, WARNING, AUTH_FAILED, e, ejbEndpoint.getEndpoint().getEndpointName());
            }

            if (!authenticated) {
                hresp.setHeader("WWW-Authenticate", "Basic realm=\"" + realmName + "\"");
                hresp.sendError(SC_UNAUTHORIZED);
                return;
            }

            // depending on the jaxrpc or jax-ws version, this will return the
            // right dispatcher.
            EjbMessageDispatcher msgDispatcher = ejbEndpoint.getMessageDispatcher();
            msgDispatcher.invoke(hreq, hresp, getServletContext(), ejbEndpoint);

        } catch (Throwable t) {
            logger.log(WARNING, EXCEPTION_THROWN, t);
        } finally {
            // remove any security context from the thread local before returning
            if (securityService != null) {
                securityService.resetSecurityContext();
                securityService.resetPolicyContext();
            }

            // Restore context class loader
            Thread.currentThread().setContextClassLoader(savedClassLoader);
        }
    }
}
