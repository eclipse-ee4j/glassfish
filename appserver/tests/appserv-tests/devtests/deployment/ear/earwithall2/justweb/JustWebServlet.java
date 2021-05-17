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

package justweb;

import justbean.JustBean;
import justbean.JustBeanHome;
import java.io.IOException;
import java.io.PrintWriter;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class JustWebServlet extends HttpServlet
{
  public void
  init ()
    throws ServletException
  {
    super.init();
    System.out.println("JustWebServlet : init()");
  }

  public void
  service (HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException
  {
    System.out.println("JustWebServlet : service()");

    JustBean bean = null;
    try {
        Object o = (new InitialContext()).lookup("java:comp/env/ejb/JustBean");
        JustBeanHome home = (JustBeanHome)
            PortableRemoteObject.narrow(o, JustBeanHome.class);
        bean = home.create();
    }
    catch (Exception ex) {
        ex.printStackTrace();
    }

    System.out.println("JustWebServlet.service()... JustBean created.");
    System.out.println("USERNAME = " + getInitParameter("USERNAME"));
    System.out.println("PASSWORD = " + getInitParameter("PASSWORD"));

    String[] marbles = bean.findAllMarbles();
    for (int i = 0; i < marbles.length; i++) {
        System.out.println(marbles[i]);
    }

    sendResponse(request, response);
  }

  private void
  sendResponse (HttpServletRequest request, HttpServletResponse response)
    throws IOException
  {
    PrintWriter out = response.getWriter();
    response.setContentType("text/html");

    out.println("<html>");
    out.println("<head>");
    out.println("<title>Just Web Test</title>");
    out.println("</head>");
    out.println("<body>");
    out.println("<p>");
    out.println("Check log information on the server side.");
    out.println("<br>");
    out.println("Isn't this a wonderful life?");
    out.println("</p>");
    out.println("</body>");
    out.println("</html>");
  }
}
