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

package test;

import java.io.*;
import java.util.*;
import jakarta.servlet.*;

public class MyListener implements ServletContextListener {

    /**
     * Receives notification that the web application initialization
     * process is starting.
     *
     * @param sce The servlet context event
     */
    public void contextInitialized(ServletContextEvent sce) {
        try {
            doContextInitialized(sce);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Receives notification that the servlet context is about to be shut down.
     *
     * @param sce The servlet context event
     */
    public void contextDestroyed(ServletContextEvent sce) {
        // Do nothing
    }

    private void doContextInitialized(ServletContextEvent sce)
            throws ClassNotFoundException {

        ServletContext sc = sce.getServletContext();

        /*
         * Register servlet
         */
        NewServlet servlet = new NewServlet();
        servlet.setMyParameter("myServletParamValue");
        ServletRegistration sr = sc.addServlet("NewServlet", servlet);
        sr.setInitParameter("servletInitParamName", "servletInitParamValue");
        sr.addMapping("/newServlet");

        /*
         * Make sure that if we register a different servlet instance
         * under the same name, null is returned
         */
        if (sc.addServlet("NewServlet", new NewServlet()) != null) {
            throw new RuntimeException(
                "Duplicate servlet name not detected by " +
                "ServletContext#addServlet");
        }

        /*
         * Make sure that if we register the same servlet instance again
         * (under a different name), null is returned
         */
        if (sc.addServlet("AgainServlet", servlet) != null) {
            throw new RuntimeException(
                "Duplicate servlet instance not detected by " +
                "ServletContext#addServlet");
        }

        /*
         * Register filter
         */
        NewFilter filter = new NewFilter();
        filter.setMyParameter("myFilterParamValue");
        FilterRegistration fr = sc.addFilter("NewFilter", filter);
        fr.setInitParameter("filterInitParamName", "filterInitParamValue");
        fr.addMappingForServletNames(EnumSet.of(DispatcherType.REQUEST),
                                     true, "NewServlet"); 

        /*
         * Make sure that if we register a different filter instance
         * under the same name, null is returned
         */
        if (sc.addFilter("NewFilter", new NewFilter()) != null) {
            throw new RuntimeException(
                "Duplicate filter name not detected by " +
                "ServletContext#addFilter");
        }

        /*
         * Make sure that if we register the same filter instance again
         * (under a different name), null is returned
         */
        if (sc.addFilter("AgainFilter", filter) != null) {
            throw new RuntimeException(
                "Duplicate filter instance not detected by " +
                "ServletContext#addFilter");
        }
    }
}
