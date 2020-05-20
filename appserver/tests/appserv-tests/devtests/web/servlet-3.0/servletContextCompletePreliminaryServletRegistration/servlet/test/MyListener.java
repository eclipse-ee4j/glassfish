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

        // Complete preliminary servlet registration
        ServletContext sc = sce.getServletContext();
        ServletRegistration srPrelim = sc.getServletRegistration("MyServlet");
        if (srPrelim == null) {
            throw new RuntimeException("Missing ServletRegistration for " + 
                "MyServlet");
        }

        ServletRegistration srComplete = sc.addServlet(
            "MyServlet", "test.MyServlet");
        if (srPrelim != srComplete) {
            throw new RuntimeException("Preliminary and complete " +
                "ServletRegistrations for MyServlet are different: " +
                "Preliminary=" + srPrelim + ", Complete=" + srComplete);
        }

        srComplete.setInitParameter("servletInitName", "servletInitValue");
    }

    /**
     * Receives notification that the servlet context is about to be shut down.
     *
     * @param sce The servlet context event
     */
    public void contextDestroyed(ServletContextEvent sce) {
        // Do nothing
    }

}
