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

import jakarta.ejb.EJB;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.annotation.WebServlet;
import jakarta.annotation.Resource;
import javax.naming.*;

@WebServlet(urlPatterns = "/HelloServlet", loadOnStartup = 1)
@EJB(name = "java:module/m1", beanName = "HelloSingleton", beanInterface = Hello.class)
public class HelloServlet extends HttpServlet {

    @EJB(name = "java:module/env/m2")
    private Hello m1;

    @EJB(name = "java:app/a1")
    private HelloRemote a1;

    @EJB(name = "java:app/env/a2")
    private HelloRemote a2;

    @Resource(name = "java:app/env/myString")
    protected String myString;

    private Hello singleton1;
    private Hello singleton2;
    private Hello singleton3;
    private Hello singleton4;
    private Hello singleton5;
    private HelloRemote stateless1;
    private HelloRemote stateless2;

    private String msg = "";

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        System.out.println("In HelloServlet::init");
        System.out.println("myString = '" + myString + "'");
        if ((myString == null) || !(myString.equals("myString"))) {
            msg += "@Resource lookup of myString failed";
            throw new RuntimeException("Invalid value " + myString + " for myString");
        }

    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("In HelloServlet::doGet");
        resp.setContentType("text/html");

        PrintWriter out = resp.getWriter();
        try {
            InitialContext ic = new InitialContext();
            String appName = (String) ic.lookup("java:app/AppName");
            String moduleName = (String) ic.lookup("java:module/ModuleName");
            checkForNull(appName, "AppName lookup returned null");
            checkForNull(moduleName, "ModuleName lookup returned null");

            // lookup via intermediate context
            Context appCtx = (Context) ic.lookup("java:app");
            Context appCtxEnv = (Context) appCtx.lookup("env");
            stateless2 = (HelloRemote) appCtxEnv.lookup("AS2");
            checkForNull(stateless2, "lookup of stateless EJB via java:app in intermediate context failed");
            NamingEnumeration<Binding> bindings = appCtxEnv.listBindings("");
            System.out.println("java:app/env/ bindings ");
            while (bindings.hasMore()) {
                System.out.println("binding : " + bindings.next().getName());
            }

            singleton1 = (Hello) ic.lookup("java:module/m1");
            checkForNull(singleton1, "programmatic lookup of module-level singleton EJB failed");

            // standard java:app name for ejb
            singleton2 = (Hello) ic.lookup("java:app/cdi-full-ear-ejb/HelloSingleton");
            checkForNull(singleton2, "programmatic lookup of module-level singleton EJB through app reference failed");

            singleton3 = (Hello) ic.lookup("java:global/" + appName + "/cdi-full-ear-ejb/HelloSingleton");
            checkForNull(singleton3, "programmatic lookup of module-level singleton EJB through global reference failed");

            // lookup some java:app defined by ejb-jar
            singleton4 = (Hello) ic.lookup("java:app/env/AS1");
            checkForNull(singleton4, "programmatic lookup of module-level singleton EJB through EJB name failed");

            // global dependency
            singleton5 = (Hello) ic.lookup("java:global/GS1");
            checkForNull(singleton5, "programmatic lookup of singleton EJB through global name failed");

            stateless1 = (HelloRemote) ic.lookup("java:app/env/AS2");
            checkForNull(stateless1, "programmatic lookup of app-level stateless EJB failed");

            System.out.println("My AppName = " + ic.lookup("java:app/AppName"));

            System.out.println("My ModuleName = " + ic.lookup("java:module/ModuleName"));

            try {
                org.omg.CORBA.ORB orb = (org.omg.CORBA.ORB) ic.lookup("java:module/MORB1");
                msg += " Not getting naming exception when we try to see ejb-jar module-level dependency";
                throw new RuntimeException("Should have gotten naming exception");
            } catch (NamingException ne) {
                System.out.println("Successfully was *not* able to see ejb-jar module-level dependency");
            }

        } catch (Exception e) {
            msg += "Exception occurred during test Exception: " + e.getMessage();
            e.printStackTrace();
        }

        m1.hello();
        a1.hello();
        a2.hello();
        singleton1.hello();
        singleton2.hello();
        singleton3.hello();
        singleton4.hello();
        singleton5.hello();

        stateless1.hello();
        stateless2.hello();

        out.println(msg);
    }

    protected void checkForNull(Object o, String errorMessage) {
        if (o == null)
            msg += " " + errorMessage;
    }
}
