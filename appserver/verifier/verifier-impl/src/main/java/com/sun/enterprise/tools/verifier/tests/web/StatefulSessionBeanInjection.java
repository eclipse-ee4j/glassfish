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

package com.sun.enterprise.tools.verifier.tests.web;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.types.EjbReference;

import java.util.Set;

/**
 * Assertion
 *
 *  A stateful session bean should not be injected into a servlet.
 *
 * @author bshankar@sun.com
 */
public class StatefulSessionBeanInjection extends WebTest implements WebCheck  {
    
    final static String className = StatefulSessionBeanInjection.class.getName();
    
    public Result check(WebBundleDescriptor descriptor) {
        // initialize the result object
        Result result = getInitializedResult();
        addWarningDetails(result,
                getVerifierContext().getComponentNameConstructor());
        result.setStatus(Result.PASSED); //default status is PASSED
        
        Set<EjbReference> s = descriptor.getEjbReferenceDescriptors();
        if (s == null) return result;
        
        for(EjbReference ejbRefDesc : s) {
            EjbDescriptor ejbDescriptor = ejbRefDesc.getEjbDescriptor();
            if (ejbDescriptor instanceof EjbSessionDescriptor) { // instaceof returns false if ejbDescriptor=null.
                String stateType = ((EjbSessionDescriptor)ejbDescriptor).getSessionType();
                if(EjbSessionDescriptor.STATEFUL.equals(stateType)) {
                    Set<InjectionTarget> injectionTargets = ejbRefDesc.getInjectionTargets();
                    if(injectionTargets != null) {
                        for(InjectionTarget it : injectionTargets) {
                            String itClassName = it.getClassName();
                            result.warning(smh.getLocalString(className + ".warning",
                                    "Found a stateful session bean [ {0} ] injected into [ {1} ].",
                                    new Object[]{ejbDescriptor.getEjbClassName(), itClassName}));
                        }
                    }
                }
            }
        }
        return result;
    }
    
}
