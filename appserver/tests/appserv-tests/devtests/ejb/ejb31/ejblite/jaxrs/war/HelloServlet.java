/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
 * Copyright (c) 2017, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.acme;

import jakarta.ejb.EJB;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import javax.naming.*;
import jakarta.annotation.Resource;

@WebServlet(urlPatterns = "/HelloServlet", loadOnStartup = 1)
public class HelloServlet extends HttpServlet {

    @EJB
    private SingletonBean simpleSingleton;
    
    @EJB
    private StatelessBean simpleStateless;
    
    @Resource
    private FooManagedBean fooManagedBean;

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        System.out.println("In HelloServlet::init");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        System.out.println("In HelloServlet::doGet");

        simpleSingleton.hello();
        simpleStateless.hello();

        simpleSingleton.assertInterceptorBinding();
        System.out.println("Singleton interceptor binding asserted");

        simpleStateless.assertInterceptorBinding();
        System.out.println("Stateless interceptor binding asserted");

        fooManagedBean.assertInterceptorBinding();
        System.out.println("FooManagedBean interceptor binding asserted");
        fooManagedBean.hello();

        out.println("<HTML> <HEAD> <TITLE> JMS Servlet Output </TITLE> </HEAD> <BODY BGCOLOR=white>");
        out.println("<CENTER> <FONT size=+1 COLOR=blue>DatabaseServelt :: All information I can give </FONT> </CENTER> <p> ");
        out.println("<FONT size=+1 color=red> Context Path :  </FONT> " + req.getContextPath() + "<br>");
        out.println("<FONT size=+1 color=red> Servlet Path :  </FONT> " + req.getServletPath() + "<br>");
        out.println("<FONT size=+1 color=red> Path Info :  </FONT> " + req.getPathInfo() + "<br>");
        out.println("</BODY> </HTML> ");
    }

}
