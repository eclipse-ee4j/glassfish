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

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
import test.beans.artifacts.InjectViaAtEJB;
import test.beans.artifacts.InjectViaAtInject;
import test.util.JpaTest;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class NoInterfaceEJBTestServlet extends HttpServlet {

    @Inject
    @InjectViaAtInject
    TestBeanInterface testBeanInject;

    @Inject
    @InjectViaAtEJB
    TestBeanInterface testBeanEJB;

    @Inject
    @InjectViaAtInject
    private EntityManager em;

    private @Resource
    UserTransaction utx;


    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException,
            IOException {
        PrintWriter writer = response.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";
        //test EJB injection via @EJB
        if (!testBeanEJB.m2())
            msg += "Invocation on no-interface EJB -- obtained through @EJB -- (method defined in EJB) failed";
        if (!testBeanEJB.m1())
            msg += "Invocation on no-interface EJB -- obtained through @EJB -- (method defined in super class) failed";

        //test EJB injection via @Inject
        if (!testBeanInject.m2())
            msg += "Invocation on no-interface EJB -- obtained through @Inject -- (method defined in EJB)  failed";

        //TODO: This fails currently
        if (!testBeanInject.m1())
            msg += "Invocation on no-interface EJB -- obtained through @Inject -- (method defined in super class) failed";

        JpaTest jt = new JpaTest(em, utx);
        boolean status = jt.lazyLoadingInit();
        if (!status) msg += "Injection and use of EntityMaanger failed";
        status = jt.lazyLoadingByFind(1);
        if (!status) msg += "Injection and use of EntityMaanger lazy loading test failed";

        writer.write(msg + "\n");

    }
}
