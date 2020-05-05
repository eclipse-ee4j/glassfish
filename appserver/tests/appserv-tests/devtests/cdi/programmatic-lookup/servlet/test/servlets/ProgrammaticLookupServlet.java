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

package test.servlets;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.enterprise.util.AnnotationLiteral;
import jakarta.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebInitParam;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import test.artifacts.Asynchronous;
import test.artifacts.ChequePaymentProcessor;
import test.artifacts.MockPaymentProcessor;
import test.artifacts.PayBy;
import test.artifacts.PaymentMethod;
import test.artifacts.PaymentProcessor;
import test.artifacts.Synchronous;
import test.artifacts.TestApplicationScopedBean;
import test.artifacts.TestSessionScopedBean;
import test.artifacts.UnproxyableType;
import test.beans.BeanToTestProgrammaticLookup;

@WebServlet(name = "mytest", urlPatterns = { "/myurl" }, initParams = {
        @WebInitParam(name = "n1", value = "v1"),
        @WebInitParam(name = "n2", value = "v2") })
public class ProgrammaticLookupServlet extends HttpServlet {

    @Inject
    BeanToTestProgrammaticLookup tb;

    @Inject
    @Synchronous
    Instance<PaymentProcessor> synchronousPaymentProcessor;

    @Inject
    @Asynchronous
    Instance<PaymentProcessor> asynchronousPaymentProcessor;

    @Inject
    @Any
    Instance<PaymentProcessor> anyPaymentProcessors;

    @Inject
    @Any
    Instance<UnproxyableType> unpnc;

    @Inject
    TestApplicationScopedBean tasb;

    @Inject
    TestSessionScopedBean tssb;

    public void service(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {
        PrintWriter writer = res.getWriter();
        writer.write("Hello from Servlet 3.0. ");
        String msg = "n1=" + getInitParameter("n1") + ", n2="
                + getInitParameter("n2");

        if (!tb.testInjection())
            msg += "Alternatives injection and test for availability of other "
                    + "beans via @Any Failed";

        boolean specifiedAtInjectionPoint = synchronousPaymentProcessor.get() instanceof MockPaymentProcessor
                && asynchronousPaymentProcessor.get() instanceof MockPaymentProcessor;
        if (!specifiedAtInjectionPoint)
            msg += "Qualifier based(specified at injection point) programmatic "
                    + "injection into Servlet Failed";

        PaymentProcessor pp1, pp2;
        // using anonymous inner classes
        pp1 = anyPaymentProcessors.select(
                new AnnotationLiteral<Asynchronous>() {
                }).get();
        pp2 = anyPaymentProcessors.select(
                new AnnotationLiteral<Synchronous>() {
                }).get();
        boolean specifiedQualifierDynamically = pp1 instanceof MockPaymentProcessor
                && pp2 instanceof MockPaymentProcessor;
        if (!specifiedQualifierDynamically)
            msg += "Qualifier based(specified dynamically through "
                    + "instance.select()) programmatic injection into Servlet Failed";

        // using concrete implementations of the annotation type
        pp1 = anyPaymentProcessors.select(new AsynchronousQualifier()).get();
        pp2 = anyPaymentProcessors.select(new SynchronousQualifier()).get();
        boolean specifiedQualifierDynamicallyThroughConcreteTypes = pp1 instanceof MockPaymentProcessor
                && pp2 instanceof MockPaymentProcessor;
        if (!specifiedQualifierDynamicallyThroughConcreteTypes)
            msg += "Qualifier based(specified dynamically through "
                    + "instance.select() through a concrete annotation type "
                    + "implementation) programmatic injection into Servlet Failed";

        PaymentProcessor chequePaymentProcessor = anyPaymentProcessors
                .select(new ChequeQualifier()).get();
        boolean specifiedQualifierWithMembersDynamicallyThroughConcreteTypes = chequePaymentProcessor instanceof ChequePaymentProcessor;
        if (!specifiedQualifierWithMembersDynamicallyThroughConcreteTypes)
            msg += "Qualifier with members based(specified dynamically through "
                    + "instance.select() through a concrete annotation type "
                    + "implementation) programmatic injection into Servlet Failed";

        // Ensure unproxabletypes are injectable
        for (Iterator iterator = unpnc.iterator(); iterator.hasNext();) {
            UnproxyableType type = (UnproxyableType) iterator.next();
            if (type == null) {
                System.out.println("UnproxyableType is null "
                        + type.getClass());
                msg += "Unproxyable type (class with non-public null constructor) Injection failed";
            }
        }

        // Ensure application-scoped bean has a reference to a session-scoped
        // bean
        // via a client proxy
        TestSessionScopedBean tsb = tasb.getSessionScopedBean();
        boolean isAClientProxy = testIsClientProxy(tsb,
                TestSessionScopedBean.class);
        if (!isAClientProxy)
            msg += "Session scoped bean is not a proxy class";

        writer.write("initParams: " + msg + "\n");
    }

    // Tests if the bean instance is a client proxy
    private boolean testIsClientProxy(Object beanInstance, Class beanType) {
        boolean isSameClass = beanInstance.getClass().equals(beanType);
        boolean isProxyAssignable = beanType.isAssignableFrom(beanInstance
                .getClass());
        System.out.println(beanInstance + "whose class is "
                + beanInstance.getClass() + " is same class of " + beanType
                + " = " + isSameClass);
        System.out.println(beanType + " is assignable from " + beanInstance
                + " = " + isProxyAssignable);
        boolean isAClientProxy = !isSameClass && isProxyAssignable;
        return isAClientProxy;
    }

    // concrete implementations of the qualifier annotation types to be used in
    // Instance.select()
    class AsynchronousQualifier extends AnnotationLiteral<Asynchronous>
            implements Asynchronous {
    }

    class SynchronousQualifier extends AnnotationLiteral<Synchronous>
            implements Synchronous {
    }

    class ChequeQualifier extends AnnotationLiteral<PayBy> implements PayBy {
        @Override
        public PaymentMethod value() {
            return PaymentMethod.CHEQUE;
        }

        @Override
        public String comment() {
            return "";
        }

    }

}
