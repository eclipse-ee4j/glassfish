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

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import test.beans.CreditCardPaymentStrategy;
import test.beans.PaymentStrategy;
import test.beans.Preferred_CreatedProgrammatically;
import test.beans.Preferred_CreatedViaInjection;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class ProducerMethodRuntimePolymorhpismTestServlet extends HttpServlet {

    @Inject
    @Preferred_CreatedProgrammatically
    PaymentStrategy payCreate;

    @Inject
    @Preferred_CreatedViaInjection
    PaymentStrategy payInject;

    @Inject
    @Preferred_CreatedViaInjection
    PaymentStrategy payInject2;
    
    @Inject
    CreditCardPaymentStrategy ccps;// This should be the request-scoped
                                   // instance.

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        if (payCreate == null)
            msg += "Bean injection into Servlet of Bean created through "
                    + "programmatic instantiation in producer method failed";

        if (payInject == null)
            msg += "Bean injection into Servlet of Bean created through "
                    + "injection into producer method failed";

        if (!(payCreate instanceof PaymentStrategy))
            msg += "Bean runtime polymorphism in producer method " +
            		"in Preferences failed";
        if (!(payInject instanceof PaymentStrategy))
            msg += "Bean runtime polymorphism in producer method(dep injection " +
            		"in method parameters in Preferences failed";

        if (areInjectedInstancesEqual(ccps, payInject))
            msg += "Use of @New to create new Dependent object while injecting " +
            		"in producer method failed";

        if (!areInjectedInstancesEqual(payInject, payInject2))
            msg += "Session-scoped producer method created Bean injected in " +
            		"different injection points are not equal";

        writer.write(msg + "\n");
    }

    private boolean areInjectedInstancesEqual(Object o1, Object o2) {
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
