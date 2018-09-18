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

import com.sun.enterprise.deployment.LifecycleCallbackDescriptor;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * Lifecycle callback interceptor methods defined on an interceptor class have 
 * the following signature: 
 * void <METHOD> (InvocationContext)
 *  
 * Lifecycle callback interceptor methods defined on a bean class have the 
 * following signature: 
 * void <METHOD>()
 * 
 * @author Vikas Awasthi
 */
public class CallbackMethodArgument extends InterceptorMethodTest {

    void testInterceptorMethods(Set<LifecycleCallbackDescriptor> callbackDescs,
                                String callbackMethodName,
                                Boolean isBeanMethod) {
        if(callbackMethodName.equals("AroundInvoke"))
            return; // this test applies only to lifecycle callback methods

        ClassLoader cl = getVerifierContext().getClassLoader();
        for (LifecycleCallbackDescriptor callbackDesc : callbackDescs) {
            try {
                Method method = callbackDesc.getLifecycleCallbackMethodObject(cl);
                Class<?>[] args = method.getParameterTypes();
                if(isBeanMethod && args.length!=0) {
                    logFailure(callbackMethodName, method);
                } else if(!isBeanMethod && 
                        (args.length!=1 || 
                                !javax.interceptor.InvocationContext.class.isAssignableFrom(args[0]))) {
                    logFailure(callbackMethodName, method);
                }
            } catch (Exception e) {}//ignore as it will be caught in other tests
        }
    }
}
