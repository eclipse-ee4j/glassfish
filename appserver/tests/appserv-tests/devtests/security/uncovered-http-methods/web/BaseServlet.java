/*
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.jacc.test.uncoveredmethods;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.Principal;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;

public class BaseServlet extends HttpServlet {

    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            out.println("<HTML> <HEAD> <TITLE> Servlet Output </TITLE> </HEAD> <BODY>");
            out.println("Uncovered HTTP Methods Servlet<br>");
            out.println("<table border=\"2\"><caption>HTTP Request Values</caption>");
            out.println("<thead><tr><th>HTTP</th><th>Value</th></tr></thead><tbody>");
            out.println("<tr><td>URL</td><td>" + request.getRequestURL() + "</td>/<tr>");
            out.println("<tr><td>Method</td><td>" + request.getMethod() + "</td>/<tr>");
            out.println("<tr><td>Servlet</td><td>" + request.getServletPath() + "</td>/<tr>");
            out.println("<tr><td>Context</td><td>" + request.getContextPath() + "</td>/<tr>");
            out.println("<tr><td>Secure</td><td>" + (request.isSecure() ? "true" : "false") + "</td>/<tr>");
            out.println("<tr><td>UserPrincipal</td><td>"
                    + (request.getUserPrincipal() == null ? "null" : request.getUserPrincipal().getName()) + "</td>/<tr>");
            out.println("<tr><td>AuthType</td><td>" + request.getAuthType() + "</td>/<tr>");
            out.println("</tbody></table>");
            out.println("</BODY> </HTML>");
        } catch (Throwable t) {
            out.println("Something went wrong: " + t);
        } finally {
            out.close();
        }
    }

    public String getServletInfo() {
        return "Base Servlet implementation class of Test Servlet";
    }
}
