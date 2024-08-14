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

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;

public class ServletTest3 extends HttpServlet{

    private ServletContext context;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("[Servlet3.init]");
        context = config.getServletContext();
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet3.doGet]");
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet3.doPost]");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        out.println("FILTER-REQUEST:" + request.getSession().getAttribute("FILTER-REQUEST"));
        out.println("FILTER-FORWARD:" + request.getSession().getAttribute("FILTER-FORWARD"));
        out.println("FILTER-INCLUDE:" + request.getSession().getAttribute("FILTER"));
    }

}



