/*
 * Copyright (c) 2010, 2020 Oracle and/or its affiliates. All rights reserved.
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

import test.beans.AnotherTestBean;
import test.beans.Preferred;
import test.beans.TestBean;
import test.beans.TestInterceptor;


@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class BusinessMethodInterceptorTestServlet extends HttpServlet {

    @Inject
    TestBean tb;


    @Inject @Preferred
    String echoMessage;


    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        if (tb == null) {
            msg += "Injection of request scoped bean failed ";
        }

        // Violate the constraint on the echo method
        try {
            tb.echo(null);
            msg += "; Expected ConstraintViolationException not thrown ";
        } catch (Exception e) {
            // Expected exception
            if (!"jakarta.validation.ConstraintViolationException".equals(e.getClass().getName())) {
                msg += "; Unexpected exception: " + e.getClass().getName();
            }
        }

        // Since validation failed, the aroundConstruct interceptor method should still have been called
        if (!TestInterceptor.aroundConstructCalled) {
            msg += "; Business method interceptor aroundConstruct was not called ";
        }

        // Since validation failed, the interceptor should not have been called
        if (TestInterceptor.aroundInvokeCalled || TestInterceptor.aroundInvokeInvocationCount != 0) {
            msg += "; Business method interceptor aroundInvoke should not have been called ";
        }

        tb.echo("Test Echo Request Message");

        if (!TestInterceptor.aroundConstructCalled) {
            msg += "; Business method interceptor aroundConstruct not called ";
        }

        if (!TestInterceptor.aroundInvokeCalled || TestInterceptor.aroundInvokeInvocationCount != 1) {
            msg += "; Business method interceptor aroundInvoke not called ";
        }

        tb.hello("Client");

        if (TestInterceptor.aroundInvokeInvocationCount != 2) {
            msg += "Business method interceptor invocation on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected=2, actual="
                    + TestInterceptor.aroundInvokeInvocationCount;
        }

        if (TestInterceptor.aroundConstructInvocationCount != 1) {
            msg += "Bean construct interceptor invocation count not expected. "
                    + "expected=1, actual="
                    + TestInterceptor.aroundConstructInvocationCount;
        }

        if (!TestInterceptor.errorMessage.trim().equals("")) {
            msg += TestInterceptor.errorMessage;
        }

        writer.write(msg + "\n");
    }

}
