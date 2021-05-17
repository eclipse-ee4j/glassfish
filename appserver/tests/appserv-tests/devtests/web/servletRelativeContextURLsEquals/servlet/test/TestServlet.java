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
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class TestServlet extends HttpServlet {

    private ServletContext context;

    public void init(ServletConfig sconfig) throws ServletException {
        super.init(sconfig);
        this.context = sconfig.getServletContext();
    }

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        boolean passed = false;

    try {
            URL main = context.getResource("/test/res1.jsp");
            URL sub = new URL(main, "res2.jsp");
            URL sub1 = context.getResource("/test/res2.jsp");
            if (sub.equals(sub1) && sub.toString().equals(sub1.toString())) {
                passed = true;
            }
        } catch (Throwable t) {
            throw new ServletException(t);
        }

        res.getWriter().print(passed);
    }
}
