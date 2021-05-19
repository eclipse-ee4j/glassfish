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

package webclient;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import ejbclient.*;
import webclient.*;
import jakarta.xml.ws.*;

public class Client extends HttpServlet {

        @jakarta.xml.ws.WebServiceRef(ejbclient.HelloEjbService.class)
        ejbclient.Hello hiport1;

    @jakarta.xml.ws.WebServiceRef(webclient.HelloService.class)
            webclient.Hello hiport2;

       public void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws jakarta.servlet.ServletException {
           doPost(req, resp);
       }

       public void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws jakarta.servlet.ServletException {
            try {
                String ret1 = hiport1.sayHello("All");
                String ret2 = hiport2.sayHello("All");

                PrintWriter out = resp.getWriter();
                resp.setContentType("text/html");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>TestServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("So the RESULT OF HELLO SERVICE IS :");
                out.println("</p>");
                out.println("[" + ret1 + "]");
                out.println("[" + ret2 + "]");
                out.println("</body>");
                out.println("</html>");
                out.flush();
                out.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
       }
}

