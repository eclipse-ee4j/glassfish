/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.tools.verifier.tests.ejb.ejb30;

import com.sun.enterprise.deployment.EjbInterceptor;
import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Base class for all interceptor method tests.
 * @author Vikas Awasthi
 */
public abstract class InterceptorMethodTest extends EjbTest {
    Result result;
    ComponentNameConstructor compName;
    abstract void testInterceptorMethods(Set<LifecycleCallbackDescriptor> callbackDescs,
                                         String callbackMethodName,
                                         Boolean isBeanMethod);

    public Result check(EjbDescriptor descriptor) {
        result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor();

        testInterceptorMethods(
                descriptor.getAroundInvokeDescriptors(), "AroundInvoke", true);
        testInterceptorMethods(
                descriptor.getPreDestroyDescriptors(), "PreDestroy", true);
        testInterceptorMethods(
                descriptor.getPostConstructDescriptors(), "PostConstruct", true);

        if(descriptor instanceof EjbSessionDescriptor) {
            EjbSessionDescriptor sessionDescriptor = (EjbSessionDescriptor)descriptor;
            testInterceptorMethods(
                    sessionDescriptor.getPrePassivateDescriptors(), "PrePassivate", true);
            testInterceptorMethods(
                    sessionDescriptor.getPostActivateDescriptors(), "PostActivate", true);
        }

        descriptor.getInterceptorClasses();
        for (EjbInterceptor interceptor : descriptor.getInterceptorClasses()) {
            testInterceptorMethods(
                    interceptor.getAroundInvokeDescriptors(), "AroundInvoke", false);
            testInterceptorMethods(
                    interceptor.getPreDestroyDescriptors(), "PreDestroy", false);
            testInterceptorMethods(
                    interceptor.getPostConstructDescriptors(), "PostConstruct", false);
            testInterceptorMethods(
                    interceptor.getCallbackDescriptors(
                            LifecycleCallbackDescriptor.CallbackType.PRE_PASSIVATE), "PrePassivate", false);
            testInterceptorMethods(
                    interceptor.getCallbackDescriptors(
                            LifecycleCallbackDescriptor.CallbackType.POST_ACTIVATE), "PostActivate", false);
        }

        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            ("com.sun.enterprise.tools.verifier.tests.ejb.ejb30.InterceptorMethodTest.passed",
                            "Valid Interceptor methods."));
        }
        return result;
    }

    protected void logFailure(String methodName, Method method) {
        result.getFaultLocation().setFaultyClassAndMethod(method);
        addErrorDetails(result, compName);
        result.failed(smh.getLocalString
                        ("com.sun.enterprise.tools.verifier.tests.ejb.ejb30.InterceptorMethodTest.failed",
                        "Wrong {0} interceptor method [ {1} ]",
                        new Object[] {methodName, method}));
    }
}
