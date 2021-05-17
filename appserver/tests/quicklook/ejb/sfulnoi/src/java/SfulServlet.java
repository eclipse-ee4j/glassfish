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

package sfulnoi;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.ejb.EJB;

public class SfulServlet extends HttpServlet {

    @EJB
    private SfulBean simpleEJB;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        boolean status = false;
        try {

            out.println("-------SfulServlet--------");
            out.println("SfulServlet at " + request.getContextPath ());

            String testcase = request.getParameter("tc");
            out.println("testcase = " + testcase);
            if (testcase != null) {

        if ("SetName".equals(testcase)){
        out.println("Simple EJB:");
        out.println("@EJB Injection="+simpleEJB);

        if (simpleEJB != null) {
          out.println("SetName in a stateful session bean.");
            try {
                simpleEJB.setName("Duke");
                status = true;
            } catch (Exception e) {
                e.printStackTrace();
                status = false;
            }
        }

          } else if ("GetName".equals(testcase)){

        String simpleEJBName = null;

        if (simpleEJB != null) {
          simpleEJBName = simpleEJB.getName();
          out.println("@EJB.getName()=" + simpleEJBName);
        }

        if (simpleEJB != null &&
            "Duke".equals(simpleEJBName)){
          status = true;
        }

          } else {
        out.println("No such testcase");
          }
      }
        } catch (Exception ex ) {
            ex.printStackTrace();
            System.out.println("servlet test failed");
            throw new ServletException(ex);
        } finally {
            if (status)
          out.println("Test:Pass");
            else
          out.println("Test:Fail");
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
        return "SfulServlet";
    }

}



