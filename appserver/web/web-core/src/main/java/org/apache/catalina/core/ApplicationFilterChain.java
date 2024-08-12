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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

import java.io.IOException;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import org.apache.catalina.LogFacade;
import org.apache.catalina.util.InstanceSupport;

import static org.apache.catalina.InstanceEvent.EventType.AFTER_FILTER_EVENT;
import static org.apache.catalina.InstanceEvent.EventType.BEFORE_FILTER_EVENT;

/**
 * Implementation of <code>jakarta.servlet.FilterChain</code> used to manage the execution of a set of filters for a
 * particular request. When the set of defined filters has all been executed, the next call to <code>doFilter()</code>
 * will execute the servlet's <code>service()</code> method itself.
 *
 * @author Craig R. McClanahan
 * @version $Revision: 1.6 $ $Date: 2006/11/21 17:39:39 $
 */

final class ApplicationFilterChain implements FilterChain {

    private static final Logger log = LogFacade.getLogger();
    private static final ResourceBundle rb = log.getResourceBundle();

    // -------------------------------------------------------------- Constants

    public static final int INCREMENT = 10;

    // ----------------------------------------------------------- Constructors

    /**
     * Construct a new chain instance with no defined filters.
     */
    public ApplicationFilterChain() {
        super();
    }

    // ----------------------------------------------------- Instance Variables

    /**
     * Filters.
     */
    private ApplicationFilterConfig[] filters = new ApplicationFilterConfig[0];

    /**
     * The int which is used to maintain the current position in the filter chain.
     */
    private int pos = 0;

    /**
     * The int which gives the current number of filters in the chain.
     */
    private int n = 0;

    /**
     * The servlet instance to be executed by this chain.
     */
    private Servlet servlet;

    /**
     * The wrapper around the servlet instance to be executed by this chain.
     */
    private StandardWrapper wrapper;

    /**
     * Static class array used when the SecurityManager is turned on and <code>doFilter</code is invoked.
     */
    private static Class<?>[] classType = new Class[] { ServletRequest.class, ServletResponse.class, FilterChain.class };


    // ---------------------------------------------------- FilterChain Methods

    /**
     * Invoke the next filter in this chain, passing the specified request and response. If there are no more filters in
     * this chain, invoke the <code>service()</code> method of the servlet itself.
     *
     * @param request The servlet request we are processing
     * @param response The servlet response we are creating
     *
     * @exception IOException if an input/output error occurs
     * @exception ServletException if a servlet exception occurs
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        internalDoFilter(request, response);
    }

    private void internalDoFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        if (wrapper == null) {
            throw new IllegalStateException("Missing wrapper");
        }

        InstanceSupport support = wrapper.getInstanceSupport();

        // Call the next filter if there is one
        if (pos < n) {
            ApplicationFilterConfig filterConfig = filters[pos++];
            if (!filterConfig.isAsyncSupported()) {
                RequestFacadeHelper reqFacHelper = RequestFacadeHelper.getInstance(request);
                if (reqFacHelper != null) {
                    reqFacHelper.disableAsyncSupport();
                }
            }
            Filter filter = null;
            try {
                filter = filterConfig.getFilter();
                support.fireInstanceEvent(BEFORE_FILTER_EVENT, filter, request, response);

                filter.doFilter(request, response, this);

                support.fireInstanceEvent(AFTER_FILTER_EVENT, filter, request, response);
            } catch (IOException | ServletException | RuntimeException  e) {
                if (filter != null)
                    support.fireInstanceEvent(AFTER_FILTER_EVENT, filter, request, response, e);
                throw e;
            } catch (Throwable e) {
                if (filter != null)
                    support.fireInstanceEvent(AFTER_FILTER_EVENT, filter, request, response, e);

                throw new ServletException(rb.getString(LogFacade.FILTER_EXECUTION_EXCEPTION), e);
            }
            return;
        }

        // We fell off the end of the chain -- call the servlet instance

        wrapper.service(request, response, servlet);
    }

    // -------------------------------------------------------- Package Methods

    /**
     * Add a filter to the set of filters that will be executed in this chain.
     *
     * @param filterConfig The FilterConfig for the servlet to be executed
     */
    void addFilter(ApplicationFilterConfig filterConfig) {
        if (n == filters.length) {
            ApplicationFilterConfig[] newFilters = new ApplicationFilterConfig[n + INCREMENT];
            System.arraycopy(filters, 0, newFilters, 0, n);
            filters = newFilters;
        }
        filters[n++] = filterConfig;
    }

    /**
     * Release references to the filters and wrapper executed by this chain.
     */
    void release() {
        n = 0;
        pos = 0;
        servlet = null;
        wrapper = null;
    }

    /**
     * Sets the Servlet instance that will be executed at the end of this Filter chain.
     *
     * @param servlet the Servlet instance that will be executed at the end of this Filter chain.
     */
    void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }

    /**
     * Sets the wrapper of the Servlet that will be executed at the end of this Filter chain.
     *
     * @param wrapper the wrapper of the Servlet that will be executed at the end of this Filter chain.
     */
    void setWrapper(StandardWrapper wrapper) {
        this.wrapper = wrapper;
    }

}
