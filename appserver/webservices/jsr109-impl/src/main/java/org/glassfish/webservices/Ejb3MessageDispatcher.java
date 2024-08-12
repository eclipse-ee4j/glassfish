/*
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

import com.sun.xml.ws.transport.http.servlet.ServletAdapter;

import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Logger;

import static java.util.logging.Level.FINE;
import static java.util.logging.Level.SEVERE;
import static java.util.logging.Level.WARNING;
import static org.glassfish.webservices.LogUtils.EJB_ENDPOINT_EXCEPTION;
import static org.glassfish.webservices.LogUtils.ERROR_ON_EJB;
import static org.glassfish.webservices.LogUtils.UNABLE_FIND_ADAPTER;
import static org.glassfish.webservices.LogUtils.WEBSERVICE_DISPATCHER_INFO;

/**
 * Implementation of the Ejb Message Dispatcher for EJB3 endpoints.
 *
 * @author Jerome Dochez
 */
public class Ejb3MessageDispatcher implements EjbMessageDispatcher {

    private static final Logger logger = LogUtils.getLogger();

    private static WsUtil wsUtil = new WsUtil();

    @Override
    public void invoke(HttpServletRequest req, HttpServletResponse resp, ServletContext ctxt, EjbRuntimeEndpointInfo endpointInfo) {

        if (logger.isLoggable(FINE)) {
            logger.log(FINE, WEBSERVICE_DISPATCHER_INFO, new Object[] { req.getMethod(), req.getRequestURI(), req.getQueryString() });
        }

        String method = req.getMethod();
        try {
            switch (method) {
            case "POST":
                handlePost(req, resp, endpointInfo);
                break;
            case "GET":
                handleGet(req, resp, ctxt, endpointInfo);
                break;
            default:
                String errorMessage = MessageFormat.format(logger.getResourceBundle().getString(LogUtils.UNSUPPORTED_METHOD_REQUEST),
                        new Object[] { method, endpointInfo.getEndpoint().getEndpointName(), endpointInfo.getEndpointAddressUri() });
                logger.log(WARNING, errorMessage);
                wsUtil.writeInvalidMethodType(resp, errorMessage);
                break;
            }
        } catch (Exception e) {
            logger.log(WARNING, EJB_ENDPOINT_EXCEPTION, e);
        }
    }

    private void handlePost(HttpServletRequest req, HttpServletResponse resp, EjbRuntimeEndpointInfo endpointInfo) throws IOException {
        AdapterInvocationInfo adapterInfo = null;
        ServletAdapter adapter;
        try {
            try {
                adapterInfo = (AdapterInvocationInfo) endpointInfo.prepareInvocation(true);
                adapter = adapterInfo.getAdapter();
                if (adapter != null) {
                    adapter.handle(null, req, resp);
                } else {
                    logger.log(SEVERE, UNABLE_FIND_ADAPTER, endpointInfo.getEndpoint().getName());
                }
            } finally {
                // Always call release, even if an error happened
                // during getImplementor(), since some of the
                // preInvoke steps might have occurred. It's ok
                // if implementor is null.
                endpointInfo.releaseImplementor((adapterInfo == null) ? null : adapterInfo.getInv());
            }
        } catch (Throwable e) {
            String errorMessage = MessageFormat.format(logger.getResourceBundle().getString(ERROR_ON_EJB),
                    new Object[] { endpointInfo.getEndpoint().getEndpointName(), endpointInfo.getEndpointAddressUri(), e.getMessage() });
            logger.log(WARNING, errorMessage, e);
            String binding = endpointInfo.getEndpoint().getProtocolBinding();
            WsUtil.raiseException(resp, binding, errorMessage);
        }
    }

    private void handleGet(HttpServletRequest req, HttpServletResponse resp, ServletContext ctxt, EjbRuntimeEndpointInfo endpointInfo) throws IOException {
        AdapterInvocationInfo adapterInfo = null;
        ServletAdapter adapter;
        try {
            adapterInfo = (AdapterInvocationInfo) endpointInfo.prepareInvocation(true);
            adapter = adapterInfo.getAdapter();
            if (adapter != null) {
                adapter.publishWSDL(ctxt, req, resp);
            } else {
                String message = "Invalid wsdl request " + req.getRequestURL();
                (new WsUtil()).writeInvalidMethodType(resp, message);
            }
        } catch (Throwable e) {
            String errorMessage = MessageFormat.format(logger.getResourceBundle().getString(ERROR_ON_EJB),
                    new Object[] { endpointInfo.getEndpoint().getEndpointName(), endpointInfo.getEndpointAddressUri(), e.getMessage() });
            logger.log(WARNING, errorMessage, e);
            String binding = endpointInfo.getEndpoint().getProtocolBinding();
            WsUtil.raiseException(resp, binding, errorMessage);
        } finally {
            endpointInfo.releaseImplementor((adapterInfo == null) ? null : adapterInfo.getInv());
        }
    }
}
