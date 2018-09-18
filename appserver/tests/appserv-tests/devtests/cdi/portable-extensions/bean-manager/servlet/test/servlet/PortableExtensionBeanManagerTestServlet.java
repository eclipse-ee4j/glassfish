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

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.beans.Preferred;
import test.beans.TestBean;
import test.beans.TransactionInterceptor;
import test.beans.Transactional;
import test.extension.MyExtension;



@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class PortableExtensionBeanManagerTestServlet extends HttpServlet {
    @Inject
    @Preferred
    TestBean tb;
    
    @Inject
    BeanManager bm;
    
    String msg = "";


    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");

        if (tb == null)
            msg += "Injection of request scoped bean failed";

        tb.m1();
        if (!TransactionInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke not called";
        tb.m2();
        if (TransactionInterceptor.aroundInvokeInvocationCount != 2)
            msg += "Business method interceptor invocation on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =2, actual="
                    + TransactionInterceptor.aroundInvokeInvocationCount;
        if (!TransactionInterceptor.errorMessage.trim().equals(""))
            msg += TransactionInterceptor.errorMessage;
        
        //check if our portable extension was called
        if (!MyExtension.beforeBeanDiscoveryCalled)
            msg += "Portable Extension lifecycle observer method: " +
            		"beforeBeanDiscovery not called";

        if (!MyExtension.afterBeanDiscoveryCalled)
            msg += "Portable Extension lifecycle observer method: " +
            		"afterBeanDiscovery not called or injection of BeanManager " +
            		"in an observer method failed";
        
        if (!MyExtension.processAnnotatedTypeCalled)
            msg += "Portable Extension lifecycle observer method: process " +
            		"annotated type not called";

        //BeanManager lookup
        if (bm == null)
            msg += "Injection of BeanManager into servlet failed";
        
        try {
            BeanManager bm1 = (BeanManager) (new InitialContext()).lookup("java:comp/BeanManager");
            if (bm1 == null) 
                msg += "lookup of BeanManager via component context failed";
        } catch (NamingException e) {
            e.printStackTrace();
            msg += "NamingException during lookup of BeanManager via component context";
        }
        
        //Using BeanManager
        check((bm.getBeans("test_named_bean").size() == 1), "Invalid number of Named Beans");
        check((bm.getBeans("duplicate_test_bean").size() == 0), "Invalid number of Duplicate Test Beans");
        check(bm.getELResolver() != null, "ELResolver is null");
        check(bm.isInterceptorBinding(Transactional.class), "Transactional is not an interceptor binding");
        check(bm.isNormalScope(RequestScoped.class), "RequestScoped is not normal scope");
        check(bm.isPassivatingScope(SessionScoped.class), "SessionScoped is not passivating scope");
        check(bm.isQualifier(Preferred.class), "Preferred is not a Qualifier");
        check(!(bm.isScope(Preferred.class)), "Preferred is a Scope class");
        check(bm.isScope(ConversationScoped.class), "ConversationScoped is not a Scope class");
        writer.write(msg + "\n");
    }


    private void check(boolean condition, String errorMessage) {
        if(!condition){
            msg += errorMessage;
        }
    }

}
