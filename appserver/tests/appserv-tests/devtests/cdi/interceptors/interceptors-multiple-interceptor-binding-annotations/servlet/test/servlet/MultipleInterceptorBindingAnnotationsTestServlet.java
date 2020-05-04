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
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.beans.Preferred;
import test.beans.SecondShoppingCart;
import test.beans.ShoppingCart;
import test.beans.TestBean;
import test.beans.TestRequestScopedBean;
import test.beans.ThirdShoppingCart;
import test.beans.TransactionalSecureInterceptor;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class MultipleInterceptorBindingAnnotationsTestServlet extends HttpServlet {
    @Inject
    @Preferred
    TestBean tb;
    
    @Inject
    ShoppingCart sc;
    
    @Inject
    @Preferred
    SecondShoppingCart sc2;

    @Inject
    @Preferred
    ThirdShoppingCart sc3;
    
    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        if (tb == null)
            msg += "Injection of bean (that is being intercepted) failed";

        tb.m1();
        if (TransactionalSecureInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke on " +
            		"TransactionSecureInterceptor called when it shouldn't have";
        tb.m2();
        if (TransactionalSecureInterceptor.aroundInvokeInvocationCount != 0)
            msg += "Business method interceptor invocation on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =0, actual="
                    + TransactionalSecureInterceptor.aroundInvokeInvocationCount;
        if (!TransactionalSecureInterceptor.errorMessage.trim().equals(""))
            msg += TransactionalSecureInterceptor.errorMessage;
        
        
        //Now use the two shopping cart to test TransactionalSecureInterceptor
        sc.checkout();
        if (!TransactionalSecureInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke on " +
                    "TransactionSecureInterceptor not called";
        if (TransactionalSecureInterceptor.aroundInvokeInvocationCount != 1)
            msg += "Business method interceptor invocation on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =1, actual="
                    + TransactionalSecureInterceptor.aroundInvokeInvocationCount;
        if (!TransactionalSecureInterceptor.errorMessage.trim().equals(""))
            msg += TransactionalSecureInterceptor.errorMessage;
        
        sc2.checkout();
        if (TransactionalSecureInterceptor.aroundInvokeInvocationCount != 2)
            msg += "Business method interceptor invocation on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =2, actual="
                    + TransactionalSecureInterceptor.aroundInvokeInvocationCount;

        sc3.checkout();
        if (TransactionalSecureInterceptor.aroundInvokeInvocationCount != 3)
            msg += "Business method interceptor invocation on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =3, actual="
                    + TransactionalSecureInterceptor.aroundInvokeInvocationCount;

        writer.write(msg + "\n");
    }

}
