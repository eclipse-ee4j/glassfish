/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.annotation.Resource;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.UserTransaction;

import test.beans.TestBeanInterface;
import test.beans.artifacts.Preferred;
import test.beans.artifacts.TestDatabase;
import test.util.JpaTest;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class JPAResourceInjectionServlet extends HttpServlet {

    @PersistenceUnit(unitName = "pu1")
    private EntityManagerFactory emf_at_pu;

    @Inject
    @TestDatabase
    private EntityManager emf_at_inj;

    private @Resource
    UserTransaction utx;

    @Inject
    @Preferred
    TestBeanInterface tbi;

    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException,
            IOException {
        PrintWriter writer = response.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";
        System.out.println("JPAResourceInjectionServlet::@PersistenceUnit " +
                        "CDI EntityManagerFactory=" + emf_at_inj);

        EntityManager em = emf_at_inj;
        System.out.println("JPAResourceInjectionServlet::createEM" +
                        "EntityManager=" + em);
        String testcase = request.getParameter("testcase");
        System.out.println("testcase=" + testcase);

        if (testcase != null) {
            JpaTest jt = new JpaTest(em, utx);
            boolean status = false;
            if ("llinit".equals(testcase)) {
                status = jt.lazyLoadingInit();
            } else if ("llfind".equals(testcase)) {
                status = jt.lazyLoadingByFind(1);
            } else if ("llquery".equals(testcase)) {
                status = jt.lazyLoadingByQuery("Carla");
            } else if ("llinj".equals(testcase)){
                status = ((tbi != null) &&
                        (tbi.testDatasourceInjection().trim().length()==0));
            }
            if (status) {
                msg += "";// pass
            } else {
                msg += (testcase + ":fail");
            }
        }

        writer.write(msg + "\n");

    }
}
