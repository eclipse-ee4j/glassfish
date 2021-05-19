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

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;

import jakarta.xml.ws.*;

import com.example.hello1.*;
import com.example.hello2.*;

public class Client extends HttpServlet {

       @WebServiceRef(wsdlLocation="http://HTTP_HOST:HTTP_PORT/containerresolver-app1/Hello1Service?wsdl") Hello1Service service1;
       @WebServiceRef(wsdlLocation="http://HTTP_HOST:HTTP_PORT/containerresolver-app2/Hello2Service?wsdl") Hello2Service service2;

       public void doGet(HttpServletRequest req, HttpServletResponse resp)
                throws jakarta.servlet.ServletException {
           doPost(req, resp);
       }

       public void doPost(HttpServletRequest req, HttpServletResponse resp)
              throws jakarta.servlet.ServletException {
            try {
                Hello1 port1 = service1.getHello1Port();
                String ret = port1.sayHello1("Hi");
                PrintWriter out = resp.getWriter();
                resp.setContentType("text/html");
                out.println("<html>");
                out.println("<head>");
                out.println("<title>TestServlet</title>");
                out.println("</head>");
                out.println("<body>");
                out.println("<p>");
                out.println("So the RESULT OF SERVICE IS :");
                out.println("</p>");
                out.println("[" + ret + "]");
                out.println("</p>");
                Hello2 port2 = service2.getHello2Port();
                ret = port2.sayHello2("Hi");
                out.println("[" + ret + "]");
                out.println("</p>");
                out.println("</body>");
                out.println("</html>");
                out.flush();
                out.close();
            } catch(Exception e) {
                e.printStackTrace();
            }
       }
}

