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

import jakarta.enterprise.context.ConversationScoped;
import jakarta.enterprise.context.RequestScoped;
import jakarta.enterprise.context.SessionScoped;
import jakarta.enterprise.context.spi.CreationalContext;
import jakarta.enterprise.inject.spi.AnnotatedType;
import jakarta.enterprise.inject.spi.BeanManager;
import jakarta.enterprise.inject.spi.InjectionTarget;
import jakarta.enterprise.inject.spi.InjectionTargetFactory;
import jakarta.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import test.beans.Preferred;
import test.beans.TestBean;
import test.beans.TransactionInterceptor;
import test.beans.Transactional;
import test.extension.MyExtension;
import test.framework.TestFrameworkClassWithConstructorInjection;
import test.framework.TestFrameworkClassWithSetterAndFieldInjection;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class PortableExtensionInjectionTargetTestServlet extends HttpServlet {

    @Inject
    @Preferred
    TestBean tb;

    @Inject
    BeanManager bm;

    String msg = "";

    public void service(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0.");

        if (tb == null)
            msg += "Injection of request scoped bean failed";

        tb.m1();
        if (!TransactionInterceptor.aroundInvokeCalled)
            msg += "Business method interceptor aroundInvoke not called";

        tb.m2();
        if (TransactionInterceptor.aroundInvokeInvocationCount != 2)
            msg += "Business method interceptor invocation on method-level " + "interceptor annotation count not expected. " + "expected =2, actual="
                    + TransactionInterceptor.aroundInvokeInvocationCount;
        if (!TransactionInterceptor.errorMessage.trim().equals(""))
            msg += TransactionInterceptor.errorMessage;

        // check if our portable extension was called
        if (!MyExtension.beforeBeanDiscoveryCalled)
            msg += "Portable Extension lifecycle observer method: " + "beforeBeanDiscovery not called";

        if (!MyExtension.afterBeanDiscoveryCalled)
            msg += "Portable Extension lifecycle observer method: " + "afterBeanDiscovery not called or injection of BeanManager "
                    + "in an observer method failed";

        if (!MyExtension.processAnnotatedTypeCalled)
            msg += "Portable Extension lifecycle observer method: process " + "annotated type not called";

        // BeanManager lookup
        if (bm == null)
            msg += "Injection of BeanManager into servlet failed";

        AnnotatedType<TestFrameworkClassWithConstructorInjection> atfc = bm.createAnnotatedType(TestFrameworkClassWithConstructorInjection.class);

        // First: Constructor Injection Framework class
        CreationalContext ctx = bm.createCreationalContext(null);
        InjectionTargetFactory<TestFrameworkClassWithConstructorInjection> injectionTargetFactory = bm.getInjectionTargetFactory(atfc);
        InjectionTarget<TestFrameworkClassWithConstructorInjection> it = injectionTargetFactory.createInjectionTarget(null);
        
        TestFrameworkClassWithConstructorInjection ctorInstance = it.produce(ctx);

        // Since this framework class needs to support constructor based injection
        // we need to ask the CDI runtime to produce the instance.
        it.inject(ctorInstance, ctx);
        it.postConstruct(ctorInstance);
        msg += ctorInstance.getInitialTestResults();
        it.preDestroy(ctorInstance);
        it.dispose(ctorInstance);
        msg += ctorInstance.getFinalTestResults();

        // Second: Setter and Field based Injection into a Framework class
        AnnotatedType<TestFrameworkClassWithSetterAndFieldInjection> atsfi = bm.createAnnotatedType(TestFrameworkClassWithSetterAndFieldInjection.class);
        InjectionTargetFactory<TestFrameworkClassWithSetterAndFieldInjection> ahdgf = bm.getInjectionTargetFactory(atsfi);
        InjectionTarget<TestFrameworkClassWithSetterAndFieldInjection> it_set = ahdgf.createInjectionTarget(null);
        TestFrameworkClassWithSetterAndFieldInjection setterInstance = new TestFrameworkClassWithSetterAndFieldInjection("test");
        it_set.inject(setterInstance, ctx);
        it_set.postConstruct(setterInstance);
        msg += setterInstance.getInitialTestResults();
        it_set.preDestroy(setterInstance);
        it_set.dispose(setterInstance);
        msg += setterInstance.getFinalTestResults();

        writer.write(msg + "\n");
    }

    private void check(boolean condition, String errorMessage) {
        if (!condition) {
            msg += errorMessage;
        }
    }

}
