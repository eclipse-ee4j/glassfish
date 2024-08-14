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

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSessionListener;

import java.io.IOException;
import java.io.PrintWriter;

public class ServletTest2 extends HttpServlet implements HttpSessionListener {

    private ServletContext context;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("[Servlet2.init]");
        context = config.getServletContext();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet2.doGet]");
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet2.doPost]");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        request.getSession().setAttribute("FILTER-FORWARD",request.getSession().getAttribute("FILTER"));
        request.getSession().setAttribute("FILTER", "FAIL");

        RequestDispatcher rd = request.getRequestDispatcher("/ServletTest3");
        rd.include(request, response);
    }

    public void sessionCreated(jakarta.servlet.http.HttpSessionEvent httpSessionEvent) {
        System.out.println("[Servlet.sessionCreated]");
    }

    public void sessionDestroyed(jakarta.servlet.http.HttpSessionEvent httpSessionEvent) {
        System.out.println("[Servlet.sessionDestroyed]");
        System.out.println("Attributes: " + httpSessionEvent.getSession().getAttribute("test"));
    }

}


