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
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class TestServlet extends HttpServlet {

    ServletContext sc;

    public void init(ServletConfig config) throws ServletException {
        sc = config.getServletContext();
    }

    public void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        String result = null;
        String timeout = req.getParameter("timeout");
        if (timeout != null) {
            req.getSession(true).setMaxInactiveInterval(Integer.parseInt(timeout));
        } else {
            Class clazz = (Class)sc.getAttribute(MyHttpSessionListener.KEY);
System.out.println("XXX KEY = " + clazz);
            if (clazz != null) {
                sc.setAttribute(MyHttpSessionListener.KEY, null);
                result = clazz.getName();
            }
        }

        writer.println(result);
    }
}
