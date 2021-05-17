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

package myapp;

import java.io.*;
import java.net.*;

import jakarta.ejb.EJB;
import javax.naming.*;

import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class TestServlet extends HttpServlet {

    @EJB
    private BeanRootInterface root;

    @EJB
    private BeanMessageInterface msg;

    private ServletContext sc;

    private String message;

    public void init(ServletConfig config) throws ServletException  {
        super.init(config);
        sc = config.getServletContext();
    message = msg.getMessage();
        System.out.println("servlet init: message="+message);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
    String EXPECTED_RESULT ="PostBeanRootPostBeanLeafHelloBeanLeaf";
        boolean status = false;

    try {

      String testcase = request.getParameter("tc");
      out.println("testcase = " + testcase);
      out.println("TestServlet");
      out.println("contextPath=" + request.getContextPath ());

      if (testcase != null) {

        if ("InjectLookup".equals(testcase)){
          // EJB injection check
          // out.println("injected root: " + root);
          String hello = root.sayHello();
          out.println("Hello from injected bean: " + hello);

          // EJB lookup check
          InitialContext ic = new InitialContext();
          // "java"glabal[/<app-name>]/<module-name>/<bean-name>"
              // app-name -- name of ear file (option)
              // module-name -- name of war or jar file
              // bean-name -- name of ejb
          BeanRootInterface root2 = (BeanRootInterface) ic.lookup("java:global/singleton/singletonEJB/BeanRoot");

          // out.println("global root: " + root2);
          String hello2 = root2.sayHello();
          out.println("Hello from lookup bean: " + hello2);

          if (hello.equals(hello2)){
        status = true;
          }
        } else if ("Startup".equals(testcase)){
              // deployment check for startup
          out.println("message by deployment: " + message);
          if (message != null && message.equals(EXPECTED_RESULT)){
        status = true;
          }
        }
      }

        } catch (Throwable th) {
            th.printStackTrace(out);
        } finally {
        if (status){
          out.println("Test:Pass");
        } else {
          out.println("Test:Fail");
        }
            out.close();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        processRequest(request, response);
    }


    public String getServletInfo() {
        return "Short description";
    }

}
