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
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Interceptor methods may throw runtime exceptions or application exceptions 
 * that are allowed in the throws clause of the business method.
 * The methods of the business interface should not throw the 
 * java.rmi.RemoteException, even if the interface is a remote business 
 * interface or the bean class is annotated WebService or the method as 
 * WebMethod.
 * 
 * These statements from the specification indicate that the interceptor method
 * must not throw java.rmi.RemoteException
 * 
 * @author Vikas Awasthi
 */
public class InterceptorMethodException extends EjbTest {

    public Result check(EjbDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        Set<Method> interceptorMethods = new HashSet<Method>();

        if(descriptor.hasAroundInvokeMethod()) {
//XXX
/*
            Method method = descriptor.getAroundInvokeMethod().getMethod(descriptor);
            interceptorMethods.add(method);
*/
        }
        
        List<EjbInterceptor> interceptors = descriptor.getInterceptorChain();
        
        for (EjbInterceptor interceptor : interceptors) {
            try {
                Class interceptorClass = 
                        Class.forName(interceptor.getInterceptorClassName(),
                                      false,
                                      getVerifierContext().getClassLoader());
//XXX
/*
                Method method = interceptor.getAroundInvokeMethod().getMethod(interceptorClass);
                interceptorMethods.add(method);
*/
            } catch (ClassNotFoundException e) {
                Verifier.debug(e);
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                                (getClass().getName() + ".failed1",
                                "[ {0} ] not found.",
                                new Object[] {interceptor.getInterceptorClassName()}));
            }
        }
        
        for (Method method : interceptorMethods) {
            Class[] exceptions = method.getExceptionTypes();
            for (Class excepClass : exceptions) {
                if(java.rmi.RemoteException.class.isAssignableFrom(excepClass)) {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                                    (getClass().getName() + ".failed",
                                    "Method [ {0} ] throws java.rmi.RemoteException.",
                                    new Object[] {method}));
                }
            }
        }
        
        if(result.getStatus()!=Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            (getClass().getName() + ".passed",
                            "Valid Interceptor methods."));
        }

        return result;
    }
}
