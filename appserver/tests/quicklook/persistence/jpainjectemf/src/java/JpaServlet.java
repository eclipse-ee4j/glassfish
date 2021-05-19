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

import jakarta.persistence.*;
import jakarta.transaction.*;
import jakarta.annotation.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.*;


public class JpaServlet extends HttpServlet {

    @PersistenceUnit(unitName="pu1")
    private EntityManagerFactory emf;
    private EntityManager em;
    private @Resource UserTransaction utx;

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        boolean status = false;

        out.println("@PersistenceUnit EntityManagerFactory=" + emf);
        out.println("@Resource UserTransaction=" + utx);

        em = emf.createEntityManager();
        out.println("createEM  EntityManager=" + em);

        String testcase = request.getParameter("testcase");
        System.out.println("testcase="+testcase);
        if (testcase != null) {
          JpaTest jt = new JpaTest(emf, em, utx, out);

      try {

        if ("llinit".equals(testcase)) {
                 status = jt.lazyLoadingInit();
        } else if ("llfind".equals(testcase)) {
                 status = jt.lazyLoadingByFind(1);
        } else if ("llquery".equals(testcase)) {
                 status = jt.lazyLoadingByQuery("Carla");
        }
            if (status) {
          out.println(testcase+":pass");
        } else {
          out.println(testcase+":fail");
        }
      } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println("servlet test failed");
            throw new ServletException(ex);
      }
    }
    }
}
