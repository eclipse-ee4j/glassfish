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
import java.lang.annotation.Annotation;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.beans.DuplicateTestBean;
import test.beans.Preferred;
import test.beans.TestBean;
import test.beans.TransactionInterceptor;
import test.beans.Transactional;
import test.extension.MyExtension;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" })
public class PortableExtensionBeanInterfaceTestServlet extends HttpServlet {
    @Inject
    @Preferred
    TestBean tb;

    @Inject
    BeanManager bm;

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

        // check if our portable extension was called
        if (!MyExtension.beforeBeanDiscoveryCalled)
            msg += "Portable Extension lifecycle observer method: "
                    + "beforeBeanDiscovery not called";

        if (!MyExtension.afterBeanDiscoveryCalled)
            msg += "Portable Extension lifecycle observer method: "
                    + "afterBeanDiscovery not called or injection of BeanManager "
                    + "in an observer method failed";

        if (!MyExtension.processAnnotatedTypeCalled)
            msg += "Portable Extension lifecycle observer method: process "
                    + "annotated type not called";

        // BeanManager lookup
        if (bm == null)
            msg += "Injection of BeanManager into servlet failed";

        try {
            BeanManager bm1 = (BeanManager) (new InitialContext())
                    .lookup("java:comp/BeanManager");
            if (bm1 == null)
                msg += "lookup of BeanManager via component context failed";
        } catch (NamingException e) {
            e.printStackTrace();
            msg += "NamingException during lookup of BeanManager via component context";
        }

        testBeanManager(bm);
        testBeanInterface(bm);

        writer.write(msg + "\n");
    }

    private void testBeanInterface(BeanManager bm2) {
        // all beans in the application
        System.out.println(bm.getBeans(Object.class,
                new AnnotationLiteral<Any>() {
                }).size());
        Set<Bean<?>> s = bm.getBeans(Object.class,
                new AnnotationLiteral<Any>() {
                });
        boolean foundInAllBeansInApplication = false;
        for (Iterator iterator = s.iterator(); iterator.hasNext();) {
            Bean<?> bean = (Bean<?>) iterator.next();
            if (bean.getBeanClass().equals(TestBean.class)) {
                // found
                foundInAllBeansInApplication = true;
                testTestBeanMetadata(bean,
                        "Testing TestBean obtained through a lookup for all beans in application");
            }
        }
        if (!foundInAllBeansInApplication)
            msg += "TestBean was not found among all the beans in the application";
        Bean<?> testBeanThroughType = bm
                .getBeans(TestBean.class, new AnnotationLiteral<Any>() {
                }).iterator().next();
        testTestBeanMetadata(
                testBeanThroughType,
                "Testing TestBean obtained through a lookup of TestBean.class with @Any Qualifier");
        Bean<?> testBeanThroughQualifier = bm
                .getBeans(Object.class, new AnnotationLiteral<Preferred>() {
                }).iterator().next();
        testTestBeanMetadata(
                testBeanThroughQualifier,
                "Testing TestBean obtained through a lookup of all beans with @Preferred Qualifier");

        //There should be no Bean for DuplicateTestBean
        try {
            Bean<?> duplicateTestBeanThroughType = bm.getBeans(
                    DuplicateTestBean.class, new AnnotationLiteral<Any>() {}).
                    iterator().next();
            if (duplicateTestBeanThroughType != null)
                msg += "Duplicate test bean that has been vetoed by the portable " +
                        "extension is still present as a valid Bean";
        } catch(NoSuchElementException nsee){
            //Expected.
        }

    }

    private void testTestBeanMetadata(Bean<?> bean, String message) {
        System.out.println("++++++" + message + "++++++");
        System.out.println("EL Name:" + bean.getName());
        check((bean.getName() == null),
                "TestBean(whose EL name was unspecified)'s ELName is not null");
        System.out.println(bean.getBeanClass());
        check((bean.getBeanClass().equals(TestBean.class)),
                "TestBean(whose EL name was unspecified) Bean's class is not TestBean");
        System.out.println(bean.getScope());
        check((bean.getScope().equals(RequestScoped.class)),
                "TestBean(whose EL name was unspecified) Bean's scope is not RequestScoped");
        System.out.println(bean.getTypes()); // Object, TestBean
        check((bean.getTypes().size() == 2),
                "TestBean(whose EL name was unspecified) Bean's types unexpected. Should have been Object and TestBean, instead got "
                        + bean.getTypes());
        System.out.println(bean.getQualifiers()); // Any, Preferred
        check((bean.getQualifiers().size() == 2),
                "TestBean(whose EL name was unspecified) Bean's qualifiers unexpected. Should have been Any and Preferred, instead got "
                        + bean.getTypes());
        Set<Annotation> x = bean.getQualifiers();
        boolean qualifierFound = false;
        for (Iterator iterator = x.iterator(); iterator.hasNext();) {
            Annotation annotation = (Annotation) iterator.next();
            if (annotation.annotationType().equals(Preferred.class)) {
                qualifierFound = true;
            }
        }
        check(qualifierFound,
                "TestBean's qualifiers does not have Preferred.");
    }

    private void testBeanManager(BeanManager bm) {
        // Using BeanManager
        check((bm.getBeans("test_named_bean").size() == 1),
                "Invalid number of Named Beans");
        check((bm.getBeans("duplicate_test_bean").size() == 0),
                "Invalid number of Duplicate Test Bean");
        check(bm.getELResolver() != null, "ELResolver is null");
        check(bm.isInterceptorBinding(Transactional.class),
                "Transactional is not an interceptor binding");
        check(bm.isNormalScope(RequestScoped.class),
                "RequestScoped is not normal scope");
        check(bm.isPassivatingScope(SessionScoped.class),
                "SessionScoped is not passivating scope");
        check(bm.isQualifier(Preferred.class), "Preferred is not a Qualifier");
        check(!(bm.isScope(Preferred.class)), "Preferred is a Scope class");
        check(bm.isScope(ConversationScoped.class),
                "ConversationScoped is not a Scope class");

    }

    private void check(boolean condition, String errorMessage) {
        if (!condition) {
            System.out.println("PROBLEM");
            msg += errorMessage;
        }
    }

}
