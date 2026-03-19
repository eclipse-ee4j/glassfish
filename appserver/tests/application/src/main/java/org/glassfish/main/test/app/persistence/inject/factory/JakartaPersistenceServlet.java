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

package org.glassfish.main.test.app.persistence.inject.factory;

import jakarta.annotation.Resource;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.UserTransaction;

import java.io.IOException;
import java.io.PrintWriter;

public class JakartaPersistenceServlet extends HttpServlet {

    @PersistenceUnit(unitName = "pu1")
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;

    @Resource
    private UserTransaction transaction;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        boolean status = false;

        out.println("@PersistenceUnit EntityManagerFactory=" + entityManagerFactory);
        out.println("@Resource UserTransaction=" + transaction);

        entityManager = entityManagerFactory.createEntityManager();
        out.println("createEM  EntityManager=" + entityManager);

        String testcase = request.getParameter("testcase");
        System.out.println("testcase=" + testcase);
        if (testcase != null) {
            JakartaPersistenceTest JakartaPersistenceTest =
                new JakartaPersistenceTest(entityManager, transaction, out);

            try {

                if ("llinit".equals(testcase)) {
                    status = JakartaPersistenceTest.lazyLoadingInit();
                } else if ("llfind".equals(testcase)) {
                    status = JakartaPersistenceTest.lazyLoadingByFind(1);
                } else if ("llquery".equals(testcase)) {
                    status = JakartaPersistenceTest.lazyLoadingByQuery("Carla");
                }
                if (status) {
                    out.println(testcase + ":pass");
                } else {
                    out.println(testcase + ":fail");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                System.out.println("servlet test failed");
                throw new ServletException(ex);
            }
        }
    }
}
