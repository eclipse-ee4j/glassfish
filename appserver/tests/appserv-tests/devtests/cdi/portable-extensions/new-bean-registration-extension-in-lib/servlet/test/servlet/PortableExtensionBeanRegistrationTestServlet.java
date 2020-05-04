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
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.Set;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
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
import test.extension.ServiceFrameworkExtension;
import test.fwk.FrameworkService;
import test.fwk.SomeFwkServiceImpl;
import test.fwk.SomeFwkService;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class PortableExtensionBeanRegistrationTestServlet extends HttpServlet {
    @Inject
    @Preferred
    TestBean tb;
    
    @Inject
    BeanManager bm;
    
    @Inject
    @FrameworkService(serviceCriteria="TEST", dynamic=true)
    SomeFwkService sfc_proxy;

//    @Inject
//    @FrameworkService(serviceCriteria="FOO", dynamic=false)
//    SomeFwkService sfc_unproxied;

    String msg = "";

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");

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
        if (!ServiceFrameworkExtension.beforeBeanDiscoveryCalled)
            msg += "Portable Extension lifecycle observer method: " +
            		"beforeBeanDiscovery not called";

        if (!ServiceFrameworkExtension.afterBeanDiscoveryCalled)
            msg += "Portable Extension lifecycle observer method: " +
            		"afterBeanDiscovery not called or injection of BeanManager " +
            		"in an observer method failed";
        
        if (!ServiceFrameworkExtension.processAnnotatedTypeCalled)
            msg += "Portable Extension lifecycle observer method: process " +
            		"annotated type not called";

        //BeanManager lookup
        if (bm == null)
            msg += "Injection of BeanManager into servlet failed";
        
        //Get all beans and count the number of beans in the test package
        Set<Bean<?>> allBeans = bm.getBeans(Object.class, new AnnotationLiteral<Any>(){});

        if (sfc_proxy == null)
            msg += "Injection of (proxied) service bean added via extension not available";

//        if (sfc_unproxied == null)
//            msg += "Injection of (unproxied) service bean added via extension not available";

        //check if proxied service reference is indeed proxied
        if (!Proxy.isProxyClass(sfc_proxy.getClass()))
            msg += "Expected Proxied service reference, but got back an " +
            		"unproxied service reference ";
        //invoke a method o
        if(!sfc_proxy.fooMethod())
            msg += "invocation of method on proxied service failed";
        
//        //check if unproxied service reference is not proxied
//        if (Proxy.isProxyClass(sfc_unproxied.getClass()))
//            msg += "Expected unProxied service reference, but got back an " +
//                    "proxied service reference ";
//        //invoke a method o
//        if(!sfc_unproxied.fooMethod())
//            msg += "invocation of method on unproxied service failed";
        
        for (Iterator<Bean<?>> iterator = allBeans.iterator(); iterator.hasNext();) {
            Bean<?> bean = iterator.next();
            if (bean.getBeanClass().getName().contains("SomeFramework")){
                msg += testFrameworkBean(bean);
            }
        }
        
        if (!ServiceFrameworkExtension.afterProcessBeanCalled)
            msg += "Portable Extension lifecycle observer method: after " +
                    "ProcessBean not called for a bean registered by the portable" +
                    "extension";
        
        writer.write(msg + "\n");
    }


    private String testFrameworkBean(Bean<?> bean) {
        String msg = "";
        if (!bean.getQualifiers().contains(new AnnotationLiteral<Any>() {}) 
                || !bean.getQualifiers().contains(new AnnotationLiteral<Default>() {}))  
            msg += "FrameworkBean does not have the default expected qualifiers";
        
        if (!bean.getScope().equals(Dependent.class))
            msg += "FrameworkBean scope is not dependent";
        
        if(!bean.getBeanClass().getName().equals(SomeFwkServiceImpl.class.getName()))
            msg += "Framework Bean: Incorrect bean class";
        return msg;
    }

}
