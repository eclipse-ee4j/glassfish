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

package com.acme.servlet;


import java.io.IOException;
import java.io.PrintWriter;

import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.PersistenceUnit;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.acme.ejb.api.Hello;
import com.acme.util.TestDependentBeanInLib;
import com.acme.util.ResourcesProducer;
import com.acme.util.TestDatabase;
import com.acme.util.TestManagedBean;

@WebServlet(urlPatterns = "/HelloServlet", loadOnStartup = 1)

@SuppressWarnings("serial")
public class HelloServlet extends HttpServlet {
    String msg = "";

    @EJB(name = "java:module/m1", beanName = "HelloSingleton", beanInterface = Hello.class)
    Hello h;

    @PersistenceUnit(unitName = "pu1")
    @TestDatabase
    private EntityManagerFactory emf;

    @Inject
    private ResourcesProducer rp;

    @Inject
    private TestDependentBeanInLib fb;

    @Inject
    private TestManagedBean tmb;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("In HelloServlet::doGet");
        resp.setContentType("text/html");
        PrintWriter out = resp.getWriter();

        checkForNull(emf, "Injection of EMF failed in Servlet");
        //ensure EMF works!
        emf.createEntityManager();

        //call Singleton EJB
        String response = h.hello();
        if(!response.equals(Hello.HELLO_TEST_STRING))
            msg += "Invocation of Hello Singeton EJB failed:msg=" + response;

        if (!rp.isInjectionSuccessful())
            msg += "Injection of a bean in lib directory into another " +
                            "Bean in lib directory failed";
        checkForNull(fb, "Injection of a bean that is placed in lib directory " +
                        "into a Servlet that is placed in a WAR failed");
        checkForNull(tmb, "Injection of a Managed bean that is placed in lib directory " +
        "into a Servlet that is placed in a WAR failed");

        if (!rp.isInjectionSuccessful())
            msg += "Injection of a bean in lib directory into another Bean " +
                            "in lib directory failed";

        if (!tmb.isInjectionSuccessful())
            msg += "Injection of a Bean placed in lib dir into a " +
                            "ManagedBean placed in lib dir failed";


        out.println(msg);
    }

    protected void checkForNull(Object o, String errorMessage){
        System.out.println("o=" + o);
        if (o == null) msg += " " + errorMessage;
    }
}
