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

import javax.enterprise.inject.New;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import test.beans.TestRequestScopedBean;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class NewQualifierTestServlet extends HttpServlet {

    @Inject
    TestRequestScopedBean trsb;
    
    @Inject
    TestRequestScopedBean anotherRefToRequestScopedBean;

    @Inject
    @New
    TestRequestScopedBean newRequestScopedBean;

    @Inject
    @New
    Instance<TestRequestScopedBean> newRequestScopedBeanProgrammaticLookup;
    
    @Inject
    @New(TestRequestScopedBean.class)
    Instance<TestRequestScopedBean> newRequestScopedBeanProgrammaticLookup2;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        // 2 to account for the two injections in this servlet
        // XXX: For some weld instantiates the request scoped bean thrice(twice
        // during
        // the instantiation of the servlet and once during the servicing of the
        // servlet request
        if (!(trsb.getInstantiationCount() == 3))
            msg += "Request scoped bean created more than the expected number of times";

        if (!areInjectecedInstancesEqual(trsb, anotherRefToRequestScopedBean)) 
            msg += "Two references to the same request scoped bean are not equal";
        
        if (areInjectecedInstancesEqual(trsb, newRequestScopedBean))
            msg += "Request scoped Bean injected with @New qualifier must not be equal to the normal Request scoped bean";

        if (!testIsClientProxy(trsb, TestRequestScopedBean.class))
            msg += "Request scoped beans must be injected as a client proxy";
        
        if(newRequestScopedBeanProgrammaticLookup.get() == null) 
            msg += "A new instance of Request Scoped Bean obtained through programmatic lookup failed";
        
        if(newRequestScopedBeanProgrammaticLookup2.get() == null) 
            msg += "A new(complex type specification scenario) instance of Request Scoped Bean obtained through programmatic lookup failed";

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
