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

package client;

import jakarta.servlet.http.*;
import java.io.PrintWriter;
import java.security.Principal;

import jakarta.xml.ws.*;

import endpoint.ejb.*;

public class Client extends HttpServlet {

    @WebServiceRef(name="sun-web.serviceref/HelloEJBService")
    HelloEJBService service;

    public void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws jakarta.servlet.ServletException {
        doPost(req, resp);
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws jakarta.servlet.ServletException {
        try {
            Principal p = req.getUserPrincipal();
            String principal = (p==null)? "NULL": p.toString();
            System.out.println("****Servlet: principal = " + principal);

            Hello port = service.getHelloEJBPort();
            String ret = port.sayHello("PrincipalSent="+principal);
            System.out.println("Return value from webservice:"+ret);

            PrintWriter out = resp.getWriter();
            resp.setContentType("text/html");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>TestServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<p>");
            out.println("So the RESULT OF EJB webservice IS :");
            out.println("</p>");
            out.println("[" + ret + "]");
            out.println("</body>");
            out.println("</html>");
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
