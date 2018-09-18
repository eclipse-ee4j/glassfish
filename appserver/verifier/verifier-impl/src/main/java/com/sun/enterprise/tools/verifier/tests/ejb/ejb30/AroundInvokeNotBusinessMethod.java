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
import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * An AroundInvoke method must not be a business method.
 * 
 * @author Vikas Awasthi
 */
public class AroundInvokeNotBusinessMethod extends EjbTest {

    public Result check(EjbDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        ClassLoader cl = getVerifierContext().getClassLoader();

        if(descriptor.hasAroundInvokeMethod()) {
            Set<MethodDescriptor> businessMethods = descriptor.getMethodDescriptors();
            Set<LifecycleCallbackDescriptor> aiDescriptors = 
                                        descriptor.getAroundInvokeDescriptors();
            
            for (LifecycleCallbackDescriptor aiDesc : aiDescriptors) {
                try {
                    Method interceptorMethod = aiDesc.getLifecycleCallbackMethodObject(cl);
                    MethodDescriptor interceptorMD = new MethodDescriptor(interceptorMethod);
                    if(businessMethods.contains(interceptorMD)) {
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString
                                (getClass().getName() + ".failed",
                                "AroundInvoke method [ {0} ] is a business method.",
                                new Object[] {interceptorMethod}));
                    }
                } catch (Exception e) {}// will be caught in other tests
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
