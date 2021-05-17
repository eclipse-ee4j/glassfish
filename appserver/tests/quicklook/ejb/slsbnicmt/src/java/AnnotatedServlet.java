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

package slsbnicmt;

import java.io.*;
import java.net.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.ejb.EJB;
import javax.sql.DataSource;
import jakarta.transaction.UserTransaction;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class AnnotatedServlet extends HttpServlet {

    @EJB
    private AnnotatedEJB simpleEJB;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        boolean status = false;
        try {

            out.println("-------AnnotatedServlet--------");
            out.println("AnntatedServlet at " + request.getContextPath ());

            String testcase = request.getParameter("tc");
            out.println("testcase = " + testcase);
            if (testcase != null) {

          if ("EJBInject".equals(testcase)){

        out.println("Simple EJB:");
        out.println("@EJB Injection="+simpleEJB);
        String simpleEJBName = null;

        if (simpleEJB != null) {
          simpleEJBName = simpleEJB.getName();
          out.println("@EJB.getName()=" + simpleEJBName);
        }

        if (simpleEJB != null &&
            "foo".equals(simpleEJBName)){
          status = true;
        }

          } else if ("JpaPersist".equals(testcase)){

        if (simpleEJB != null) {
          out.println("Persist Entity");
          status  = simpleEJB.persistEntity();
        }

          } else if ("JpaRemove".equals(testcase)){

        if (simpleEJB != null) {
          out.println("Verify Persisted Entity and Remove Entity");
          status  = simpleEJB.removeEntity();
        }

          } else if ("JpaVerify".equals(testcase)){

        if (simpleEJB != null) {
          out.println("Verify Removed Enitity");
          status  = simpleEJB.verifyRemove();
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
        return "AnnontatedServlet";
    }

    private Object lookupField(String name) {
        try {
            return new InitialContext().lookup("java:comp/env/" + getClass().getName() + "/" + name);
        } catch (NamingException e) {
            return null;
        }
    }

}



