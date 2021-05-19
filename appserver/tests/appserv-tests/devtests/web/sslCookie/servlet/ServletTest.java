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
import java.net.*;
import java.util.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class ServletTest extends HttpServlet implements HttpSessionListener {


    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        System.out.println("[Servlet.init]");

    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet.doGet]");
        doPost(request, response);
    }

    private static boolean sessionCreated = false;


    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        System.out.println("[Servlet.doPost]");

        response.setContentType("text/html");
        PrintWriter out = response.getWriter();

        if (!sessionCreated){
            System.out.println("Create session: " + request.getSession(true).getId());
            sessionCreated = true;
        }


        out.println("getRequestSessionId::" + request.getRequestedSessionId());
        System.out.println("getRequestSessionId::" + request.getRequestedSessionId());
        if (request.getSession(false) != null){
            out.println("getSession(false).getId::" + request.getSession(false).getId());
            System.out.println("getSession(false).getId()::" + request.getSession(false).getId());
        }
        out.println("getRequestURI::" + request.getRequestURI());
        System.out.println("getRequestURI::" + request.getRequestURI());

    }

    public void sessionCreated(jakarta.servlet.http.HttpSessionEvent httpSessionEvent) {
        System.out.println("[Servlet.sessionCreated]");
    }

    public void sessionDestroyed(jakarta.servlet.http.HttpSessionEvent httpSessionEvent) {
        System.out.println("[Servlet.sessionDestroyed]");
    }
}



