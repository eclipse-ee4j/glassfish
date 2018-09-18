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

package com.sun.enterprise.tools.verifier.tests.ejb.businessmethod;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import com.sun.enterprise.tools.verifier.tests.ejb.MethodUtils;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import java.lang.reflect.Method;
import java.util.Set;

/** 
 * Enterprise Bean's business(...) methods public test.
 * Each enterprise Bean class must define zero or more business(...) methods. 
 * The method signatures must follow these rules: 
 * 
 * The method must be declared as public. 
 */
public class BusinessMethodPublic extends EjbTest implements EjbCheck { 
    Result result = null;
    ComponentNameConstructor compName = null;
    
    /** 
     * Enterprise Bean's business(...) methods public test.
     * Each enterprise Bean class must define zero or more business(...) methods. 
     * The method signatures must follow these rules: 
     * 
     * The method must be declared as public. 
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {
        
        result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor();
        
        if ((descriptor instanceof EjbSessionDescriptor)  ||
                (descriptor instanceof EjbEntityDescriptor)) {
            
            if(descriptor.getRemoteClassName() != null && !"".equals(descriptor.getRemoteClassName())) 
                commonToBothInterfaces(descriptor.getRemoteClassName(),descriptor);  
            if(descriptor.getLocalClassName() != null && !"".equals(descriptor.getLocalClassName())) 
                commonToBothInterfaces(descriptor.getLocalClassName(),descriptor);
            Set<String> localAndRemoteInterfaces = descriptor.getLocalBusinessClassNames();
            localAndRemoteInterfaces.addAll(descriptor.getRemoteBusinessClassNames());
            
            for (String localOrRemoteIntf : localAndRemoteInterfaces) {
                commonToBothInterfaces(localOrRemoteIntf, descriptor);
            }
        } 
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                            (getClass().getName() + ".passed",
                            "Proper declaration of business method(s) found."));
        }
        return result;
    }
    
    /** 
     * This method is responsible for the logic of the test. It is called for both local and remote interfaces.
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @param intf or component for the Remote/Local interface of the Ejb. 
     * This parameter may be optional depending on the test 
     */
    private void commonToBothInterfaces(String intf,EjbDescriptor descriptor) {
        try {
            Class intfClass = Class.forName(intf, false, getVerifierContext().getClassLoader());
            
            for (Method remoteMethod : intfClass.getMethods()) {
                // we don't test the EJB methods
                if (remoteMethod.getDeclaringClass().getName().equals("javax.ejb.EJBObject")||
                        remoteMethod.getDeclaringClass().getName().equals("javax.ejb.EJBLocalObject")) 
                    continue;
                
                Class beanClass = Class.forName(descriptor.getEjbClassName(), 
                                                false, 
                                                getVerifierContext().getClassLoader());
                boolean foundOne = false;
                for (Method method : beanClass.getMethods()) {//getMethods returns only public methods
                    if(MethodUtils.methodEquals(method, remoteMethod)) {
                        foundOne = true;
                        break;
                    }
                }
                if (!foundOne) {
                    String methodToString = remoteMethod.toString().replace("abstract ","");
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                            (getClass().getName() + ".failed",
                            "Error: public business method [ {0} ] not found in [ {1} ].",
                            new Object[] {methodToString, beanClass.getName()}));
                }
            }
            
        } catch (ClassNotFoundException e) {
            Verifier.debug(e);
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                            (getClass().getName() + ".failedException",
                            "Error: Remote interface [ {0} ] or bean class [ {1} ] " +
                            "does not exist or is not loadable within bean [ {2} ].",
                            new Object[] {intf,descriptor.getEjbClassName(),descriptor.getName()}));
            
        }  
    }
}
