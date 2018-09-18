/*
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

package org.glassfish.soteria.test;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet that is invoked when it's determined that the caller needs to authenticate/login.
 *
 */
@WebServlet({"/login-servlet"})
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.getWriter().write(
            "<html><body> Login to continue \n" +
                "<form method=\"POST\" action=\"j_security_check\">" +
                    "<p><strong>Username </strong>" +
                    "<input type=\"text\" name=\"j_username\">" +
                    
                    "<p><strong>Password </strong>" +
                    "<input type=\"password\" name=\"j_password\">" +
                    "<p>" +
                    "<input type=\"submit\" value=\"Submit\">" +
                "</form>" +
            "</body></html>");
    }

}
