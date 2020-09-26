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
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import test.beans.Preferred;
import test.beans.SecondShoppingCart;
import test.beans.SecureInterceptor;
import test.beans.ShoppingCart;
import test.beans.TestBean;
import test.beans.TransactionInterceptor;
import test.beans.TransactionalSecureInterceptor;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class BindingTypeInheritanceTestServlet extends HttpServlet {

    @Inject
    ShoppingCart sc;

    @Inject
    @Preferred
    SecondShoppingCart sc2;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        // Now use the two shopping cart to test TransactionalSecureInterceptor
        sc.checkout();
        if (!TransactionalSecureInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke on "
                    + "TransactionSecureInterceptor not called";

        if (!TransactionInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke on "
                    + "TransactionInterceptor not called";

        if (!SecureInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke on "
                    + "SecureInterceptor not called";

        if (TransactionalSecureInterceptor.aroundInvokeInvocationCount != 1)
            msg += "Business method interceptor invocation on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =1, actual="
                    + TransactionalSecureInterceptor.aroundInvokeInvocationCount;
        if (!TransactionalSecureInterceptor.errorMessage.trim().equals(""))
            msg += TransactionalSecureInterceptor.errorMessage;

        // reset all counts
        TransactionInterceptor.clear();
        SecureInterceptor.clear();
        TransactionalSecureInterceptor.clear();

        // try on the second shopping cart bean
        sc2.checkout();
        if (!TransactionalSecureInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke on "
                    + "TransactionSecureInterceptor not called";

        if (!TransactionInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke on "
                    + "TransactionInterceptor not called";

        if (!SecureInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke on "
                    + "SecureInterceptor not called";

        if (TransactionalSecureInterceptor.aroundInvokeInvocationCount != 1)
            msg += "Business method interceptor invocation on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =1, actual="
                    + TransactionalSecureInterceptor.aroundInvokeInvocationCount;

        writer.write(msg + "\n");
    }

}
