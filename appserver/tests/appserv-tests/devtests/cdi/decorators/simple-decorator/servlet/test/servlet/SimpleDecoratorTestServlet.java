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
import java.math.BigDecimal;

import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.beans.Account;
import test.beans.LargeTransactionDecorator;
import test.beans.Preferred;
import test.beans.RequiresNewTransactionInterceptor;
import test.beans.ShoppingCart;
import test.beans.TestBean;
import test.beans.TransactionInterceptor;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class SimpleDecoratorTestServlet extends HttpServlet {
    @Inject
    @Preferred
    TestBean tb;
    
    @Inject
    @Preferred
    ShoppingCart sc;
    
    @Inject
    Account testAccount;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");
        String msg = "";

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
        
        TransactionInterceptor.clear();
        //invoke shopping cart bean. This should result in an invocation on
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
        
        //TransactionInterceptor should not have been called
        if (TransactionInterceptor.aroundInvokeCalled)
            msg += "TranscationInterceptor aroundInvoke called when a requiresnew" +
            		"transaction interceptor should have been called";
        
        //Test decorators
        System.out.println(testAccount.getBalance());
        if (testAccount.getBalance().compareTo(new BigDecimal(100)) != 0)            
                msg += "Decorators:Invalid initial balance";
        
        testAccount.deposit(new BigDecimal(10));
        if (testAccount.getBalance().compareTo(new BigDecimal(115)) != 0) //5 as bonus by the decorator
            msg += "Decorators:Invalid balance after deposit";
            
        
        testAccount.withdraw(new BigDecimal(10));
        if (testAccount.getBalance().compareTo(new BigDecimal(105)) != 0)
            msg += "Decorators:Invalid balance after withdrawal";

        
        if (!LargeTransactionDecorator.depositCalled)
            msg += "deposit method in Decorator not called";
        if (!LargeTransactionDecorator.withDrawCalled)
            msg += "deposit method in Decorator not called";
        
        writer.write(msg + "\n");
    }

}
