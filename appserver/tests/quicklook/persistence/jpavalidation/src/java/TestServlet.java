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

import jakarta.ejb.EJB;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


public class TestServlet extends HttpServlet {

    @EJB
    private TestEJB testEJB;

    protected void processRequest(HttpServletRequest request,
                                  HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        boolean status = false;
        String testcase = null;
        try {
            out.println("TestServlet at " + request.getContextPath());
            testcase = request.getParameter("tc");
        System.out.println("testcase = " + testcase);

            if ("initialize".equals(testcase)) {

                status = testEJB.test1();

            } else if ("validatePersist".equals(testcase)) {

                status = testEJB.test2();

            } else if ("validateUpdate".equals(testcase)) {

                status = testEJB.test3();

            } else if ("validateRemove".equals(testcase)) {

                status = testEJB.test4();

            } else if ("verify".equals(testcase)) {

                status = testEJB.test5();

            } else {

                System.out.println("Invalid test case: " + testcase);
                out.println("Invalid test case: " + testcase);

            }
        } catch (Exception ex) {

            System.out.println("Failure in TestServlet");
        out.println("Failure in TestServlet");

        } finally {
            if (status)
              out.println(testcase+":pass");
            else
              out.println(testcase+":fail");
            out.close();
        }
    }

    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }


    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    public String getServletInfo() {
        return "TestServlet";
    }

}
