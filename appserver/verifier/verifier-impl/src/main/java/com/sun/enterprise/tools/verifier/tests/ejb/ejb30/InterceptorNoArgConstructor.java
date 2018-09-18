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
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.util.Set;
/**
 * An interceptor class must have a public no-arg constructor.
 * 
 * @author Vikas Awasthi
 */
public class InterceptorNoArgConstructor extends EjbTest {

    public Result check(EjbDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        Set<EjbInterceptor> interceptors = descriptor.getInterceptorClasses();
        
        for (EjbInterceptor interceptor : interceptors) {
            try {
                Class interceptorClass = Class.forName(interceptor.getInterceptorClassName(),
                                                       false,
                                                       getVerifierContext().getClassLoader());
                try {
                    interceptorClass.getConstructor(new Class[]{});
                } catch (NoSuchMethodException e) {
                    result.getFaultLocation().setFaultyClass(interceptorClass);
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                                    (getClass().getName() + ".failed",
                                    "Interceptor class [ {0} ] does not have a " +
                                    "public constructor with no arguments.",
                                    new Object[] {interceptorClass}));
                }
            } catch (ClassNotFoundException e) {}// will be caught in other tests
        }
        if(result.getStatus()!=Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            (getClass().getName() + ".passed",
                            "Valid Interceptor(s) used."));
        } 

        return result;
    }
}
