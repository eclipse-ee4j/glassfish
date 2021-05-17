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

package com.acme;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.annotation.Resource;
import jakarta.ejb.EJB;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import javax.naming.InitialContext;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/HelloServlet", loadOnStartup = 1)
@EJB(name = "java:module/m1", beanName = "HelloSingleton", beanInterface = Hello.class)
public class ProgrammaticLookupInEARServlet extends HttpServlet {

    @Resource(name = "java:app/env/myString")
    protected String myString;

    private Hello singleton1;

    @Inject
    Instance<TestBean> programmaticLookup;

    private String msg = "";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        System.out.println("In HelloServlet::init");
        System.out.println("myString = '" + myString + "'");
        if ((myString == null) || !(myString.equals("myString"))) {
            msg += "@Resource lookup of myString failed";
            throw new RuntimeException("Invalid value " + myString
                    + " for myString");
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        System.out.println("In HelloServlet::doGet");
        resp.setContentType("text/html");

        PrintWriter out = resp.getWriter();
        try {
            InitialContext ic = new InitialContext();
            String appName = (String) ic.lookup("java:app/AppName");
            String moduleName = (String) ic.lookup("java:module/ModuleName");
            checkForNull(appName, "AppName lookup returned null");
            checkForNull(moduleName, "ModuleName lookup returned null");

            singleton1 = (Hello) ic.lookup("java:module/m1");
            checkForNull(singleton1,
                    "programmatic lookup of module-level singleton EJB failed");

            System.out.println("My AppName = "
                    + ic.lookup("java:app/AppName"));

            System.out.println("My ModuleName = "
                    + ic.lookup("java:module/ModuleName"));

        } catch (Exception e) {
            msg += "Exception occurred during test Exception: "
                    + e.getMessage();
            e.printStackTrace();
        }

        singleton1.hello();

        checkForNull(programmaticLookup.get(), "programmatic lookup of session scoped bean in war failed");

        out.println(msg);

    }

    protected void checkForNull(Object o, String errorMessage) {
        if (o == null)
            msg += " " + errorMessage;
    }
}
