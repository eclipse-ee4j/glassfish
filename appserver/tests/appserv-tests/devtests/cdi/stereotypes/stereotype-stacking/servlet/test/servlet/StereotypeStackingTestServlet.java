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

import test.beans.TestSecureBean;
import test.beans.TestTxBean;
import test.beans.TestTxSecBean;
import test.beans.interceptors.SecurityInterceptor;
import test.beans.interceptors.TransactionInterceptor;
import test.beans.qualifiers.Preferred;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class StereotypeStackingTestServlet extends HttpServlet {
    @Inject
    @Preferred
    TestTxBean tb_tx;

    @Inject
    @Preferred
    TestSecureBean tb_sec;

    @Inject
    @Preferred
    TestTxSecBean tb_tx_sec;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        // Just Transactional Interceptor
        if (tb_tx == null)
            msg += "Injection of transactional bean failed";

        tb_tx.m1();
        if (!TransactionInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke not called";
        tb_tx.m2();
        if (TransactionInterceptor.aroundInvokeInvocationCount != 2)
            msg += "Business method interceptor [TransactionInterceptor] " +
            		"invocation on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =2, actual="
                    + TransactionInterceptor.aroundInvokeInvocationCount;
        if (!TransactionInterceptor.errorMessage.trim().equals(""))
            msg += TransactionInterceptor.errorMessage;

        if (SecurityInterceptor.aroundInvokeCalled)
            msg += "Security Interceptor called when "
                    + "it shouldn't have been called";

        clearInterceptors();
        
        //Just security interceptor
        if (tb_sec == null)
            msg += "Injection of @secure bean failed";

        tb_sec.m1();
        if (!SecurityInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke not called";
        tb_sec.m2();
        if (SecurityInterceptor.aroundInvokeInvocationCount != 2)
            msg += "Business method interceptor invocation [SecurityInterceptor]" +
            		"on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =2, actual="
                    + SecurityInterceptor.aroundInvokeInvocationCount;
        if (!SecurityInterceptor.errorMessage.trim().equals(""))
            msg += SecurityInterceptor.errorMessage;

        if (TransactionInterceptor.aroundInvokeCalled)
            msg += "Security Interceptor called when "
                    + "it shouldn't have been called";

        clearInterceptors();

        //Both transaction and security interceptors
        if (tb_tx_sec == null)
            msg += "Injection of @transactional and @secure bean failed";

        tb_tx_sec.m1();
        if (!SecurityInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor [Security Interceptor] aroundInvoke not called";
        if (!TransactionInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor [Transaction Interceptor] aroundInvoke not called";
        
        tb_tx_sec.m2();
        if (SecurityInterceptor.aroundInvokeInvocationCount != 2)
            msg += "Business method interceptor invocation [SecurityInterceptor]" +
                    "on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =2, actual="
                    + SecurityInterceptor.aroundInvokeInvocationCount;
        if (TransactionInterceptor.aroundInvokeInvocationCount != 2)
            msg += "Business method interceptor [TransactionInterceptor] " +
                    "invocation on method-level "
                    + "interceptor annotation count not expected. "
                    + "expected =2, actual="
                    + TransactionInterceptor.aroundInvokeInvocationCount;
        
        if (!SecurityInterceptor.errorMessage.trim().equals(""))
            msg += SecurityInterceptor.errorMessage;
        if (!TransactionInterceptor.errorMessage.trim().equals(""))
            msg += TransactionInterceptor.errorMessage;


        writer.write(msg + "\n");
    }

    private void clearInterceptors() {
        //clear interceptors
        TransactionInterceptor.clear();
        SecurityInterceptor.clear();
    }

}
