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

import test.beans.TestBeanInterface;
import test.beans.artifacts.Preferred;
import test.beans.artifacts.RequiresNewTransactionInterceptor;
import test.beans.artifacts.TransactionInterceptor;
import test.beans.mock.MockBean;
import test.beans.mock.MockShoppingCart;
import test.beans.nonmock.ShoppingCart;
import test.beans.nonmock.TestBean;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class AlternativeStereotypesTestServlet extends HttpServlet {
    @Inject
    @Preferred
    TestBeanInterface tb;
    
    @Inject
    @Preferred
    ShoppingCart sc;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

        //TestBean uses normal interceptors placed on it
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
        
        if (RequiresNewTransactionInterceptor.aroundInvokeCalled)
            msg += "RequiresNew TransactionInterceptor called when " +
            		"it shouldn't have been called";
        
        //test that the mocks are called instead of the actual beans
        if (TestBean.testBeanInvoked)
            msg += "Test Bean invoked when actually mock bean should " +
            		"have been invoked";
        
        if (!MockBean.mockBeanInvoked)
            msg += "Mock bean not invoked";
        
        TransactionInterceptor.clear();
        //invoke shopping cart bean. ShoppingCart bean uses a Stereotype to
        //assign the requires new transaction interceptor
        //This should result in an invocation on
        //the RequiresNewTransactional
        sc.addItem("Test Item");
        if (!RequiresNewTransactionInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke in requires new " +
            		"transaction interceptor not called";
        if (RequiresNewTransactionInterceptor.aroundInvokeInvocationCount != 1)
            msg += "Business method requires new interceptor invocation on " +
            		"method-level interceptor annotation count not expected. "
                    + "expected =1, actual="
                    + RequiresNewTransactionInterceptor.aroundInvokeInvocationCount;
        if (!RequiresNewTransactionInterceptor.errorMessage.trim().equals(""))
            msg += RequiresNewTransactionInterceptor.errorMessage;
        
        //TransactionInterceptor should not be called
        if (TransactionInterceptor.aroundInvokeCalled)
            msg += "TranscationInterceptor aroundInvoke called when a requiresnew" +
            		"transaction interceptor should have been called";
        
        //test that the mocks are called instead of the actual beans
        if (ShoppingCart.shoppingCartInvoked)
            msg += "Test shopping cart invoked when actually mock shopping cart " +
            		"should have been invoked";
        
        if (!MockShoppingCart.mockShoppingCartInvoked)
            msg += "Mock shopping cart not invoked";

        writer.write(msg + "\n");
    }

}
