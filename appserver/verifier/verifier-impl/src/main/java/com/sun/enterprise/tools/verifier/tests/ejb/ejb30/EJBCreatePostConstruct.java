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
import com.sun.enterprise.tools.verifier.Result;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import java.lang.reflect.Method;

/**
 * If the stateless session bean instance has an ejbCreate method, the container
 * treats the ejbCreate method as the instance’s PostConstruct method, and, in
 * this case, the PostConstruct annotation (or deployment descriptor metadata)
 * can only be applied to the bean’s ejbCreate method.
 * 
 * @author Vikas Awasthi
 */
public class EJBCreatePostConstruct extends SessionBeanTest {

    public Result check(EjbSessionDescriptor descriptor) {

        if(descriptor.isStateless() && 
                descriptor.hasPostConstructMethod() && 
                hasEJBCreateMethod(descriptor.getEjbClassName())) {
            for (LifecycleCallbackDescriptor callbackDesc : 
                    descriptor.getPostConstructDescriptors()) {
                String cmName = callbackDesc.getLifecycleCallbackMethod();
                if(!cmName.contains("ejbCreate")) {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                            (getClass().getName()+".failed",
                                    "Wrong postconstruct method [ {0} ]",
                                    new Object[] {cmName}));
                }
            }
        }
        
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            (getClass().getName()+".passed",
                            "Valid postcontruct method(s) in Bean"));
        }
        
        return result;
    }
    
    private boolean hasEJBCreateMethod(String beanClassName) {
        try {
            ClassLoader jcl = getVerifierContext().getClassLoader();
            Class bean = Class.forName(beanClassName, false, jcl);
            Method[] methods = bean.getMethods();
            for (Method method : methods) 
                if(method.getName().contains("ejbCreate"))
                    return true;
        } catch (ClassNotFoundException e) {}// will be caught in other tests
        return false;
    }
}
