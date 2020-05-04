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

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.beans.Preferred;
import test.beans.TestBean;
import test.beans.TransactionInterceptor;
import test.extension.PackagePrivateConstructorExtension;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class PackagePrivateConstructorInPortableExtensionTestServlet extends HttpServlet {
    @Inject
    @Preferred
    TestBean tb;
    
    @Inject
    BeanManager bm;

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
        
        //check if our portable extension was called
        if (!PackagePrivateConstructorExtension.packagePrivateConstructorCalled)
            msg += "Portable Extension package private constructor:  not called";
        
        if (!PackagePrivateConstructorExtension.beforeBeanDiscoveryCalled)
            msg += "Portable Extension lifecycle observer method: " +
            		"beforeBeanDiscovery not called";

        if (!PackagePrivateConstructorExtension.afterBeanDiscoveryCalled)
            msg += "Portable Extension lifecycle observer method: " +
            		"afterBeanDiscovery not called or injection of BeanManager " +
            		"in an observer method failed";
        
        if (!PackagePrivateConstructorExtension.processAnnotatedTypeCalled)
            msg += "Portable Extension lifecycle observer method: process " +
            		"annotated type not called";

        if((bm.getBeans(PackagePrivateConstructorExtension.class, new AnnotationLiteral<Any>(){}).iterator().next().getClass()) == null) 
            msg += "Portable Extension not available for lookup through BeanManager";

        writer.write(msg + "\n");
    }

}
