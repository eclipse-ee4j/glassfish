/*
 * Copyright (c) 2002, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.s1asdev.deployment.ejb30.war.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import jakarta.annotation.security.RunAs;
import javax.naming.InitialContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

@Resource(name="myDataSource", type=DataSource.class, mappedName="jdbc/__default")
public class Servlet extends HttpServlet {
    ThreadLocal pconstruct = new ThreadLocal();

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.println("<HTML><HEAD><TITLE>Servlet Output</TTILE></HEAD><BODY>");
        try {
            InitialContext ic = new InitialContext();
            DataSource ds = (DataSource)ic.lookup("java:comp/env/myDataSource");
            int loginTimeout = ds.getLoginTimeout();
            out.println("ds login timeout = " + loginTimeout);
        } catch(Exception ex) {
            out.println("myDataSource Exception: " + ex);
        }
        if (req.isUserInRole("j2ee") && !req.isUserInRole("guest")) {
            out.println("Hello World");
            if (req.getSession(false) != null &&
                    Boolean.TRUE.equals(pconstruct.get())) {
                out.println(req.getSession(false).getAttribute(
                    "deployment.ejb30.war.servlet"));
            }
        }
        out.println("in role j2ee = " + req.isUserInRole("j2ee"));
        out.println("in role guest = " + req.isUserInRole("guest"));
        out.println("</BODY></HTML>");
    }

    @PostConstruct
    private void afterAP() {
        pconstruct.set(Boolean.TRUE);
    }
}
