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
import java.util.HashSet;
import java.util.Set;

/**
 * Lifecycle callback interceptor methods must not throw application exceptions. 
 * 
 * Any exception other than derived from java.lang.RuntimeException or 
 * java.rmi.RemoteException is an application exception.
 * 
 * @author Vikas Awasthi
 */
public class CallbackMethodException extends EjbTest {

    public Result check(EjbDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        ClassLoader cl = getVerifierContext().getClassLoader();

        Set<LifecycleCallbackDescriptor> callbackDescs = 
                                        new HashSet<LifecycleCallbackDescriptor>();
        
        for (EjbInterceptor interceptor : descriptor.getInterceptorClasses()) {
            callbackDescs.addAll(interceptor.getPostConstructDescriptors());
            callbackDescs.addAll(interceptor.getPreDestroyDescriptors());
            callbackDescs.addAll(interceptor.getCallbackDescriptors(
                        LifecycleCallbackDescriptor.CallbackType.PRE_PASSIVATE));
            callbackDescs.addAll(interceptor.getCallbackDescriptors(
                        LifecycleCallbackDescriptor.CallbackType.POST_ACTIVATE));
        }

        if(descriptor.hasPostConstructMethod())
            callbackDescs.addAll(descriptor.getPostConstructDescriptors());
        if(descriptor.hasPreDestroyMethod())
            callbackDescs.addAll(descriptor.getPreDestroyDescriptors());
        
        // session descriptor has two extra interceptor methods.
        if(descriptor instanceof EjbSessionDescriptor) {
            EjbSessionDescriptor ejbSessionDescriptor = ((EjbSessionDescriptor)descriptor);
            if(ejbSessionDescriptor.hasPostActivateMethod())
                callbackDescs.addAll(ejbSessionDescriptor.getPostActivateDescriptors());
            if(ejbSessionDescriptor.hasPrePassivateMethod())
                callbackDescs.addAll(ejbSessionDescriptor.getPrePassivateDescriptors());
        }

        for (LifecycleCallbackDescriptor callbackDesc : callbackDescs) {
            try {
                Method method = callbackDesc.getLifecycleCallbackMethodObject(cl);
                Class[] excepClasses = method.getExceptionTypes();
                for (Class exception : excepClasses) {
                    if(!(RuntimeException.class.isAssignableFrom(exception) ||
                            java.rmi.RemoteException.class.isAssignableFrom(exception))) {
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString
                                        (getClass().getName() + ".failed",
                                        "Method [ {0} ] throws an application exception.",
                                        new Object[] {method}));
                    }
                }
            } catch (Exception e) {}// will be caught in other tests
        }
        
        if(result.getStatus()!=Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            (getClass().getName() + ".passed",
                            "Valid Callback methods."));
        }
        
        return result;
    }
}
