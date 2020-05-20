/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package sahoo.hybridapp.example1.impl;

import sahoo.hybridapp.example1.UserAuthService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ejb.EJB;
import java.io.PrintWriter;

@WebServlet(urlPatterns = "/registration1")
public class RegistrationServlet extends HttpServlet
{
    @EJB
    UserAuthService userAuthService;

    public void service(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, java.io.IOException
    {
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();
        out.println("<HTML> <HEAD> <TITLE> Registration " +
                "</TITLE> </HEAD> <BODY BGCOLOR=white>");

        String name = req.getParameter("name");
        String password = req.getParameter("password");
        try
        {

            if (userAuthService.register(name, password)) {
                out.println("Registered " + name);
            } else {
                out.println("Failed to register " + name);
            }
        }
        catch (Exception e)
        {
            out.println("Failed to register " + name);
        }
        out.println("</BODY> </HTML> ");

    }
}
