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
import test.beans.SecondTransactionInterceptor;
import test.beans.TestBean;
import test.beans.TestRequestScopedBean;
import test.beans.ThirdTransactionInterceptor;
import test.beans.TransactionInterceptor;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class MultipleInterceptorImplementationsTestServlet extends HttpServlet {
    @Inject
    @Preferred
    TestBean tb;

    @Inject
    TestRequestScopedBean trsb;
    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        if (tb == null)
            msg += "Injection of bean (that is being intercepted) failed";

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


        if (!SecondTransactionInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke in the second " +
                            "transaction interceptor not called";
        tb.m2(); //calling m2 again
        if (SecondTransactionInterceptor.aroundInvokeInvocationCount != 3)
            msg += "Business method second interceptor invocation on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =3, actual="
                    + SecondTransactionInterceptor.aroundInvokeInvocationCount;

        //test ordering of interceptors in the system
        boolean expectedOrdering =
            ((trsb.interceptorInvocationOrder.get(0).
                    equals(SecondTransactionInterceptor.class.getCanonicalName()))
                && (trsb.interceptorInvocationOrder.get(1).
                        equals(TransactionInterceptor.class.getCanonicalName())));
        if (!expectedOrdering)
            msg += "Interceptor invocation order does not match with the expected order";

        //test disabling of interceptors via beans.xml
        if (ThirdTransactionInterceptor.aroundInvokeCalled)
            msg += "Disabled interceptor called";

        writer.write(msg + "\n");
    }

}
