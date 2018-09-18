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

import java.util.Set;

/**
 * If the PostConstruct lifecycle callback interceptor method is the ejbCreate 
 * method, if the PreDestroy lifecycle callback interceptor method is the 
 * ejbRemove method, if the PostActivate lifecycle callback interceptor method 
 * is the ejbActivate method, or if the Pre-Passivate lifecycle callback 
 * interceptor method is the ejbPassivate method, these callback methods must 
 * be implemented on the bean class itself (or on its superclasses).
 * 
 * @author Vikas Awasthi
 */
public class CallbacksOnBeanClass extends EjbTest {

    Result result = null;
    ComponentNameConstructor compName = null;
    
    public Result check(EjbDescriptor descriptor) {
        result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor();
        
        Set<EjbInterceptor> interceptors = descriptor.getInterceptorClasses();
        for (EjbInterceptor interceptor : interceptors) {
            if (interceptor.hasCallbackDescriptor(
                    LifecycleCallbackDescriptor.CallbackType.POST_ACTIVATE)) {
                Set<LifecycleCallbackDescriptor> callBackDescs = 
                        interceptor.getCallbackDescriptors(
                                LifecycleCallbackDescriptor.CallbackType.POST_ACTIVATE);
                reportError(callBackDescs, "ejbActivate",interceptor.getInterceptorClassName());
            }
            if (interceptor.hasCallbackDescriptor(
                    LifecycleCallbackDescriptor.CallbackType.PRE_PASSIVATE)) {
                Set<LifecycleCallbackDescriptor> callBackDescs = 
                        interceptor.getCallbackDescriptors(
                                LifecycleCallbackDescriptor.CallbackType.PRE_PASSIVATE);
                reportError(callBackDescs, "ejbPassivate",interceptor.getInterceptorClassName());
            }
            if (interceptor.hasCallbackDescriptor(
                    LifecycleCallbackDescriptor.CallbackType.POST_CONSTRUCT)) {
                Set<LifecycleCallbackDescriptor> callBackDescs = 
                        interceptor.getCallbackDescriptors(
                                LifecycleCallbackDescriptor.CallbackType.POST_CONSTRUCT);
                reportError(callBackDescs, "ejbCreate",interceptor.getInterceptorClassName());
            }
            if (interceptor.hasCallbackDescriptor(
                    LifecycleCallbackDescriptor.CallbackType.PRE_DESTROY)) {
                Set<LifecycleCallbackDescriptor> callBackDescs = 
                        interceptor.getCallbackDescriptors(
                                LifecycleCallbackDescriptor.CallbackType.PRE_DESTROY);
                reportError(callBackDescs, "ejbRemove",interceptor.getInterceptorClassName());
            }
        }
        
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            (getClass().getName()+".passed",
                            "Valid lifecycle callback method(s)"));
        }
        return result;
    }
    
    private void reportError(Set<LifecycleCallbackDescriptor> callBackDescs, 
                             String callbackMethodName,
                             String interceptorClassName) {
        for (LifecycleCallbackDescriptor callbackDesc : callBackDescs) {
            String callbackMethod = callbackDesc.getLifecycleCallbackMethod();
            if(callbackMethod.contains(callbackMethodName)) {
                result.getFaultLocation().setFaultyClassName(interceptorClassName);
                result.getFaultLocation().setFaultyMethodName(callbackMethod);
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                        (getClass().getName()+".failed",
                        "Wrong method [ {0} ] in class [ {1} ]",
                        new Object[] {callbackMethod, interceptorClassName}));
            }
        }
    }
}
