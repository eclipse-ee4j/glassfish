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

import com.sun.enterprise.deployment.InjectionTarget;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.deployment.EntityManagerReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import java.util.Set;
import jakarta.servlet.Servlet;
import jakarta.servlet.SingleThreadModel;

/**
 * Assertion
 *
 *  EntityManager should not be injected into a web application that uses multithread model.
 *  EntityManager is not thread safe, hence it should not be injected into a web application
 *  that uses multithreaded model.
 *
 * @author bshankar@sun.com
 */
public class EntityManagerInjection extends WebTest implements WebCheck  {
    
    final static String className = EntityManagerInjection.class.getName();
    
    public Result check(WebBundleDescriptor descriptor) {
        
        Result result = getInitializedResult();
        addWarningDetails(result,
                getVerifierContext().getComponentNameConstructor());
        result.setStatus(Result.PASSED); //default status is PASSED
        
        for(EntityManagerReferenceDescriptor emRefDesc : descriptor.getEntityManagerReferenceDescriptors()) {
            Set<InjectionTarget> injectionTargets = emRefDesc.getInjectionTargets();
            if(injectionTargets != null) {
                for(InjectionTarget it : injectionTargets) {
                    String itClassName = it.getClassName();
                    String errMsg = smh.getLocalString(className + ".warning",
                            "Found a persistence unit by name [ {0} ] injected into [ {1} ].",
                            new Object[]{emRefDesc.getUnitName(), itClassName});
                    try {
                        Class c = Class.forName(itClassName, false, getVerifierContext().getClassLoader());
                        if(!(Servlet.class.isAssignableFrom(c))) {
                            result.warning(errMsg);
                        } else if (!(SingleThreadModel.class.isAssignableFrom(c))) {
                            result.warning(errMsg);
                        }
                    } catch(Exception ex) {
                        result.warning(errMsg);
                    }
                }
            }
        }
        return result;
    }
    
}
