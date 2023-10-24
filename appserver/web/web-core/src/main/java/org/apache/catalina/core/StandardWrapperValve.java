/*
 * Copyright (c) 1997-2018 Oracle and/or its affiliates. All rights reserved.
 * Copyright 2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.catalina.core;

import static jakarta.servlet.RequestDispatcher.ERROR_EXCEPTION;
import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static jakarta.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static java.text.MessageFormat.format;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;
import static org.apache.catalina.Globals.DISPATCHER_REQUEST_PATH_ATTR;
import static org.apache.catalina.Globals.JSP_FILE_ATTR;
import static org.apache.catalina.LogFacade.APP_UNAVAILABLE;
import static org.apache.catalina.LogFacade.DEALLOCATE_EXCEPTION;
import static org.apache.catalina.LogFacade.RELEASE_FILTERS_EXCEPTION;
import static org.apache.catalina.LogFacade.SEND_ACKNOWLEDGEMENT_EXCEPTION;
import static org.apache.catalina.LogFacade.SERVLET_ALLOCATE_EXCEPTION;
import static org.apache.catalina.LogFacade.SERVLET_NOT_FOUND;
import static org.apache.catalina.LogFacade.SERVLET_SERVICE_EXCEPTION;
import static org.apache.catalina.LogFacade.SERVLET_UNAVAILABLE;
import static org.apache.catalina.LogFacade.STANDARD_WRAPPER_VALVE;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.catalina.Context;
import org.apache.catalina.HttpRequest;
import org.apache.catalina.LogFacade;
import org.apache.catalina.Request;
import org.apache.catalina.Response;
import org.apache.catalina.connector.ClientAbortException;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.valves.ValveBase;
import org.glassfish.grizzly.http.util.DataChunk;

import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Valve that implements the default basic behavior for the <code>StandardWrapper</code> container implementation.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.10 $ $Date: 2007/05/05 05:31:54 $
 */

final class StandardWrapperValve extends ValveBase {

    private static final Logger log = LogFacade.getLogger();
    private static final ResourceBundle rb = log.getResourceBundle();

    // --------------------------------------------------------- Public Methods

    /**
     * Invoke the servlet we are managing, respecting the rules regarding servlet lifecycle and SingleThreadModel support.
     *
     * @param request Request to be processed
     * @param response Response to be produced
     *
     * @exception IOException if an input/output error occurred
     * @exception ServletException if a servlet error occurred
     */
    @Override
    public int invoke(Request request, Response response) throws IOException, ServletException {
        boolean unavailable = false;
        Throwable throwable = null;
        Servlet servlet = null;

        StandardWrapper wrapper = (StandardWrapper) getContainer();
        Context context = (Context) wrapper.getParent();

        HttpRequest hrequest = (HttpRequest) request;

        /*
         * Create a request facade such that if the request was received at the root context, and the root context is mapped to
         * a default-web-module, the default-web-module mapping is masked from the application code to which the request facade
         * is being passed. For example, the request.facade's getContextPath() method will return "/", rather than the context
         * root of the default-web-module, in this case.
         */
        RequestFacade requestFacade = (RequestFacade) request.getRequest(true);
        HttpServletResponse httpServletResponse = (HttpServletResponse) response.getResponse();

        // Check for the application being marked unavailable
        if (!context.getAvailable()) {
            httpServletResponse.sendError(SC_SERVICE_UNAVAILABLE);
            response.setDetailMessage(rb.getString(APP_UNAVAILABLE));
            unavailable = true;
        }

        // Check for the servlet being marked unavailable
        if (!unavailable && wrapper.isUnavailable()) {
            String msg = format(rb.getString(SERVLET_UNAVAILABLE), wrapper.getName());
            log(format(rb.getString(SERVLET_UNAVAILABLE), wrapper.getName()));

            if (httpServletResponse == null) {
                ; // NOTE - Not much we can do generically
            } else {
                long available = wrapper.getAvailable();
                if ((available > 0L) && (available < Long.MAX_VALUE)) {
                    httpServletResponse.setDateHeader("Retry-After", available);
                    httpServletResponse.sendError(SC_SERVICE_UNAVAILABLE);

                    response.setDetailMessage(msg);
                } else if (available == Long.MAX_VALUE) {
                    httpServletResponse.sendError(SC_NOT_FOUND);
                    response.setDetailMessage(format(rb.getString(SERVLET_NOT_FOUND), wrapper.getName()));
                }
            }

            unavailable = true;
        }

        // Allocate a servlet instance to process this request
        try {
            if (!unavailable) {
                servlet = wrapper.allocate();
            }
        } catch (UnavailableException e) {
            if (e.isPermanent()) {
                httpServletResponse.sendError(SC_NOT_FOUND);
                response.setDetailMessage(format(rb.getString(SERVLET_NOT_FOUND), wrapper.getName()));
            } else {
                httpServletResponse.setDateHeader("Retry-After", e.getUnavailableSeconds());
                httpServletResponse.sendError(SC_SERVICE_UNAVAILABLE);
                response.setDetailMessage(format(rb.getString(SERVLET_UNAVAILABLE), wrapper.getName()));
            }
        } catch (ServletException e) {
            log(format(rb.getString(SERVLET_ALLOCATE_EXCEPTION), wrapper.getName()));

            throwable = e;
            exception(request, response, e);
            servlet = null;
        } catch (Throwable e) {
            log(format(rb.getString(SERVLET_ALLOCATE_EXCEPTION), wrapper.getName()), e);

            throwable = e;
            exception(request, response, e);
            servlet = null;
        }

        // Acknowledge the request
        try {
            response.sendAcknowledgement();
        } catch (IOException e) {
            log(format(rb.getString(SEND_ACKNOWLEDGEMENT_EXCEPTION), wrapper.getName()));
            throwable = e;
            exception(request, response, e);
        } catch (Throwable e) {
            log(format(rb.getString(SEND_ACKNOWLEDGEMENT_EXCEPTION), wrapper.getName()));
            throwable = e;
            exception(request, response, e);
            servlet = null;
        }

        DataChunk requestPathMB = hrequest.getRequestPathMB();
        requestFacade.setAttribute(DISPATCHER_REQUEST_PATH_ATTR, requestPathMB);

        // Create the filter chain for this request
        ApplicationFilterChain filterChain =
            ApplicationFilterFactory.getInstance()
                                    .createFilterChain((ServletRequest) request, wrapper, servlet);

        // Call the filter chain for this request
        // NOTE: This also calls the servlet's service() method
        try {
            String jspFile = wrapper.getJspFile();
            if (jspFile != null) {
                requestFacade.setAttribute(JSP_FILE_ATTR, jspFile);
            }

            if (servlet != null) {
                if (filterChain != null) {
                    filterChain.setWrapper(wrapper);
                    filterChain.doFilter(requestFacade, httpServletResponse);
                } else {
                    wrapper.service(requestFacade, httpServletResponse, servlet);
                }
            }
        } catch (ClientAbortException e) {
            throwable = e;
            exception(request, response, e);
        } catch (IOException e) {
            log(format(rb.getString(SERVLET_SERVICE_EXCEPTION), wrapper.getName()));
            throwable = e;
            exception(request, response, e);
        } catch (UnavailableException e) {
            log(format(rb.getString(SERVLET_SERVICE_EXCEPTION), wrapper.getName()));
            wrapper.unavailable(e);
            long available = wrapper.getAvailable();
            if ((available > 0L) && (available < Long.MAX_VALUE)) {
                httpServletResponse.setDateHeader("Retry-After", available);
                httpServletResponse.sendError(SC_SERVICE_UNAVAILABLE);
                String msgServletUnavailable = MessageFormat.format(rb.getString(SERVLET_UNAVAILABLE), wrapper.getName());
                response.setDetailMessage(msgServletUnavailable);
            } else if (available == Long.MAX_VALUE) {
                httpServletResponse.sendError(SC_NOT_FOUND);
                String msgServletNotFound = MessageFormat.format(rb.getString(SERVLET_NOT_FOUND), wrapper.getName());
                response.setDetailMessage(msgServletNotFound);
            }
            // Do not save exception in 'throwable', because we
            // do not want to do exception(request, response, e) processing
        } catch (ServletException e) {
            Throwable rootCause = StandardWrapper.getRootCause(e);
            if (!(rootCause instanceof ClientAbortException)) {
                log(format(rb.getString(SERVLET_SERVICE_EXCEPTION), wrapper.getName()), rootCause);
            }
            throwable = e;
            exception(request, response, e);
        } catch (Throwable e) {
            log(format(rb.getString(SERVLET_SERVICE_EXCEPTION), wrapper.getName()));
            throwable = e;
            exception(request, response, e);
        }

        // Release the filter chain (if any) for this request
        try {
            if (filterChain != null)
                filterChain.release();
        } catch (Throwable e) {
            log(format(rb.getString(RELEASE_FILTERS_EXCEPTION), wrapper.getName()), e);
            if (throwable == null) {
                throwable = e;
                exception(request, response, e);
            }
        }

        // Deallocate the allocated servlet instance
        try {
            if (servlet != null) {
                wrapper.deallocate(servlet);
            }
        } catch (Throwable e) {
            log(format(rb.getString(DEALLOCATE_EXCEPTION), wrapper.getName()), e);
            if (throwable == null) {
                throwable = e;
                exception(request, response, e);
            }
        }

        // If this servlet has been marked permanently unavailable,
        // unload it and release this instance
        try {
            if ((servlet != null) && (wrapper.getAvailable() == Long.MAX_VALUE)) {
                wrapper.unload();
            }
        } catch (Throwable e) {
            log(format(rb.getString(LogFacade.SERVLET_UNLOAD_EXCEPTION), wrapper.getName()), e);
            if (throwable == null) {
                exception(request, response, e);
            }
        }

        return END_PIPELINE;
    }

    /**
     * Tomcat style invocation.
     */
    @Override
    public void invoke(org.apache.catalina.connector.Request request, org.apache.catalina.connector.Response response) throws IOException, ServletException {
        invoke((Request) request, (Response) response);
    }

    // -------------------------------------------------------- Private Methods

    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     */
    private void log(String message) {
        org.apache.catalina.Logger logger = null;
        String containerName = null;
        if (container != null) {
            logger = container.getLogger();
            containerName = container.getName();
        }

        if (logger != null) {
            logger.log("StandardWrapperValve[" + containerName + "]: " + message);
        } else {
            if (log.isLoggable(INFO)) {
                log.log(INFO, LogFacade.STANDARD_WRAPPER_VALVE, new Object[] { containerName, message });
            }
        }
    }

    /**
     * Log a message on the Logger associated with our Container (if any)
     *
     * @param message Message to be logged
     * @param t Associated exception
     */
    private void log(String message, Throwable t) {
        org.apache.catalina.Logger logger = null;
        String containerName = null;
        if (container != null) {
            logger = container.getLogger();
            containerName = container.getName();
        }

        if (logger != null) {
            logger.log("StandardWrapperValve[" + containerName + "]: " + message, t, org.apache.catalina.Logger.WARNING);
        } else {
            log.log(WARNING, format(rb.getString(STANDARD_WRAPPER_VALVE), new Object[] { containerName, message }), t);
        }
    }

    /**
     * Handle the specified ServletException encountered while processing the specified Request to produce the specified
     * Response. Any exceptions that occur during generation of the exception report are logged and swallowed.
     *
     * @param request The request being processed
     * @param response The response being generated
     * @param exception The exception that occurred (which possibly wraps a root cause exception
     */
    private void exception(Request request, Response response, Throwable exception) {
        ServletRequest servletRequest = request.getRequest();
        servletRequest.setAttribute(ERROR_EXCEPTION, exception);

        ServletResponse servletResponse = response.getResponse();
        ((HttpServletResponse) servletResponse).setStatus(SC_INTERNAL_SERVER_ERROR);
    }

    // Don't register in JMX

    @Override
    public ObjectName createObjectName(String domain, ObjectName parent) throws MalformedObjectNameException {
        return null;
    }
}
