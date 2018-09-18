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

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.beans.TestBeanInterface;
import test.beans.artifacts.InjectViaAtEJB;
import test.beans.artifacts.InjectViaAtInject;
import test.beans.artifacts.LocalEJB;
import test.beans.artifacts.NoInterfaceBeanView;
import test.ejb.nointerfacebeanview.TestInterface;
import test.ejb.nointerfacebeanview.TestSuperClass;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class NoInterfaceEJBTestServlet extends HttpServlet {

//    @Inject
//    @InjectViaAtInject
//    @NoInterfaceBeanView
//    TestBeanInterface testBeanInject;


    @Inject
    FooBean fb;
//    @Inject
//    @InjectViaAtInject
//    @LocalEJB
//    TestBeanInterface testLocalBeanInject;

//    @Inject
//    @InjectViaAtEJB
//    @LocalEJB
//    TestBeanInterface testLocalBeanEJB;
    
    @Inject
    TestInterface ti;
    
    @Inject
    TestSuperClass ti1;
    
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException,
            IOException {
        PrintWriter writer = response.getWriter();
        writer.write("Hello from Servlet 3.0.");
        
        String msg = "";
        //test EJB injection via @EJB
        String m = fb.getBean().runTests();
        if (!m.equals(""))
            msg += "Invocation on no-interface EJB -- obtained through @EJB -- failed. Failed tests" + m;
        
        //test EJB injection via @Inject
//        m = testBeanInject.runTests();
//        if (!m.equals(""))
//            msg += "Invocation on no-interface EJB -- obtained through @Inject -- failed. Failed tests" + m;

        //test No-Interface EJB injection via @Inject of an interface the 
        //no-interface bean is implementing
        if (ti != null && !(ti.m1DefinedInInterface()))
            msg += "Invocation on no-interface EJB -- obtained through @Inject -- (method defined in super interface) failed";
        
        //test No-Interface EJB injection via @Inject of an interface the 
        //no-interface bean is implementing
        if (ti1 != null && !(ti1.m2DefinedInSuperClass()))
            msg += "Invocation on no-interface EJB -- obtained through @Inject -- (method defined in super class) failed";
        
        //test local EJB injection via @EJB
//        m = testLocalBeanEJB.runTests();
//        if (!m.equals(""))
//            msg += "Invocation on local EJB -- obtained through @EJB -- failed. Failed tests" + m;
        
        //test EJB injection via @Inject
//        m = testLocalBeanInject.runTests();
//        if (!m.equals(""))
//            msg += "Invocation on local EJB -- obtained through @Inject -- failed. Failed tests" + m;

        writer.write(msg + "\n");

    }
}
