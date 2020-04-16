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
import javax.inject.Inject;
import javax.naming.InitialContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import test.beans.TestDependentScopedBean;
import test.beans.TestRequestScopedBean;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class DependentScopeTestServlet extends HttpServlet {
    @Inject
    TestDependentScopedBean tb;
    @Inject
    TestDependentScopedBean anotherInjectedDependentInstance;

    @Inject
    TestRequestScopedBean trsb;
    @Inject
    BeanManager bm;
    BeanManager bm1;

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
        if (areInjectecedInstancesEqual(tb, anotherInjectedDependentInstance))
            msg += "Dependent scoped beans across two different injections points must not point to the same bean instance";
        if (areInjectecedInstancesEqual(tb, trsb.getDependentScopedBean()))
            msg += "Dependent scoped beans across two different clients must not point to the same bean instance";

        // 2 to account for the two injections
        // in this servlet and 1 more for the injection in TestRequestScopedBean
        if (tb.getInstancesCount() != 3)
            msg += "Dependent scoped bean created more than the expected number of times";

        if (testIsClientProxy(tb, TestDependentScopedBean.class))
            msg += "Beans with dependent pseudo-scope should not have a proxy";

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
