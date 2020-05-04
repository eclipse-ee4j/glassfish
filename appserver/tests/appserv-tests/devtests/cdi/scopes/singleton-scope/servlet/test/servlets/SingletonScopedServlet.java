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

package test.servlets;

import java.io.IOException;
import java.io.PrintWriter;

import javax.enterprise.inject.spi.BeanManager;
import jakarta.inject.Inject;
import javax.naming.InitialContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.beans.TestSessionScopedBean;
import test.beans.TestSingletonScopedBean;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class SingletonScopedServlet extends HttpServlet {
    @Inject
    TestSingletonScopedBean tb;
    @Inject
    TestSingletonScopedBean anotherInjectedSingletonInstance;

    @Inject
    BeanManager bm;
    BeanManager bm1;

    @Inject
    TestSessionScopedBean tssb;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";
        if (tb == null)
            msg += "Singleton pseudo-scope Bean injection into Servlet failed";
        if (bm == null)
            msg += "BeanManager Injection via @Inject failed";
        try {
            bm1 = (BeanManager) ((new InitialContext())
                    .lookup("java:comp/BeanManager"));
        } catch (Exception ex) {
            ex.printStackTrace();
            msg += "BeanManager Injection via component environment lookup failed";
        }
        if (bm1 == null)
            msg += "BeanManager Injection via component environment lookup failed";
        if (tb.getInstancesCount() > 1) 
            msg += "Singleton scoped bean must be created only once";
        if (!areInjectecedInstancesEqual(tb, anotherInjectedSingletonInstance))
            msg += "Two different injection of a Singleton pseudo-scoped Bean must point to the same bean instance";
        if (testIsClientProxy(tb, TestSingletonScopedBean.class))
            msg += "Beans with Singleton pseudo-scope should not have a proxy";
        if (testIsClientProxy(tssb.getSingletonScopedBean(),
                TestSingletonScopedBean.class))
            msg += "Non-serializable Beans with Singleton pseudo-scope injected via Instance<T> into a session scoped bean should not have a proxy";

        writer.write(msg + "\n");
    }

    private boolean areInjectecedInstancesEqual(Object o1, Object o2) {
        return (o1.equals(o2)) & (o1 == o2);
    }

    // Tests if the bean instance is a client proxy
    private boolean testIsClientProxy(Object beanInstance, Class beanType) {
        boolean isSameClass = beanInstance.getClass().equals(beanType);
        boolean isProxyAssignable = beanType.isAssignableFrom(beanInstance
                .getClass());
        System.out.println(beanInstance + "whose class is "
                + beanInstance.getClass() + " is same class of " + beanType
                + " = " + isSameClass);
        System.out.println(beanType + " is assignable from " + beanInstance
                + " = " + isProxyAssignable);
        boolean isAClientProxy = !isSameClass && isProxyAssignable;
        return isAClientProxy;
    }

}
