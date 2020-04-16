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

package com.sun.s1asdev.deployment.ejb30.ear.security;

import java.io.IOException;
import java.io.PrintWriter;

import javax.annotation.security.RunAs;
import javax.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@RunAs(value="sunuser")
public class Servlet extends HttpServlet {
    @EJB private Sless sless;

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        out.println("<HTML><HEAD><TITLE>Servlet Output</TTILE></HEAD><BODY>");
        if (req.isUserInRole("j2ee")) {
            out.println("in j2ee role:  " + req.isUserInRole("j2ee") + "<br>");
            out.println("in sunuser role:  " + req.isUserInRole("sunuser") + "<br>");
            out.println("Calling sless.hello()=" + sless.hello());
            if (req.getSession(true) != null) {
                out.println(req.getSession(true).getAttribute(
                    "deployment.ejb30.ear.security"));
            } else {
                out.println("<br>No session attr!");
            }
        }
        out.println("</BODY></HTML>");
    }
}
