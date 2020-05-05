/*
 * Copyright (c) 2004, 2018 Oracle and/or its affiliates. All rights reserved.
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

package virtualserver;

import java.io.IOException;
import java.io.PrintWriter;
import javax.naming.Context;
import javax.naming.InitialContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class TestServlet extends HttpServlet
{
    public void
    init () throws ServletException
    {
        super.init();
        log("init()...");
    }

    public void
    service (HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException
    {
        log("service()...");
        try {
            Context ic = new InitialContext();

            //test: looking up the env-entry
            String name = (String) ic.lookup("java:comp/env/name");
            Integer value = (Integer) ic.lookup("java:comp/env/value");
            log("[" + name + "] = [" + value + "]");

            PrintWriter out = response.getWriter();
            response.setContentType("text/html");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>TestServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<p>");
            out.println("So what is your lucky number?");
            out.println("</p>");
            out.println("Mine is [" + value + "]");
            out.println("</body>");
            out.println("</html>");
            out.flush();
            out.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            ServletException se = new ServletException();
            se.initCause(ex);
            throw se;
        }
    }

    public void log (String message) {
       System.out.println("[war.virtualserver.TestServlet]:: " + message);
    }
}
