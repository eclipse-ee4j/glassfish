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

package org.glassfish.tests.embedded.web;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

//@WebServlet(urlPatterns={"/new"})
public class NewServlet extends HttpServlet {

    private String initParamValue;
    private String myParamValue;


    public NewServlet() {
        System.out.println("Servlet NewServlet initialized");
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter pw = res.getWriter();
        try {
            pw.println("Hello World!");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /*public void setMyParameter(String value) {
        myParamValue = value;
    }

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        initParamValue = config.getInitParameter("servletInitParamName");
    }

        public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter pw = res.getWriter();
        try {
            pw.println("Hello World!");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void service(ServletRequest req, ServletResponse res)
            throws IOException, ServletException {
        if (!"myServletParamValue".equals(myParamValue)) {
            throw new ServletException("Wrong servlet instance");
        }

        if (!"servletInitParamValue".equals(initParamValue)) {
            throw new ServletException("Missing servlet init param");
        }

        if (!"myFilterParamValue".equals(
                req.getAttribute("myFilterParamName"))) {
            throw new ServletException("Wrong filter instance");
        }

        if (!"filterInitParamValue".equals(
                req.getAttribute("filterInitParamName"))) {
            throw new ServletException("Missing filter init param");
        }
    }*/
}
