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

import com.sun.enterprise.deployment.MethodDescriptor;
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
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**  
 * Enterprise Bean's business(...) methods name test.
 * Each enterprise Bean class must define zero or more business(...) methods. 
 * The method signatures must follow these rules: 
 * 
 * The methods in the remote/local interface should be present in the deployment 
 * descriptor
 *
 */
public class BusinessMethodMatchesWithDD extends EjbTest implements EjbCheck { 

    Result result = null;
    ComponentNameConstructor compName = null;

    /** 
     * Enterprise Bean's business(...) methods name test.
     * Each enterprise Bean class must define zero or more business(...) methods. 
     * The method signatures must follow these rules: 
     * 
     * The methods in the remote/local interface should be present in the 
     * deployment descriptor
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	result = getInitializedResult();
	compName = getVerifierContext().getComponentNameConstructor();

	if ((descriptor instanceof EjbSessionDescriptor)  ||
	    (descriptor instanceof EjbEntityDescriptor)) {
         
        if(descriptor.getRemoteClassName() != null && 
                !"".equals(descriptor.getRemoteClassName())) 
            commonToBothInterfaces(descriptor.getRemoteClassName(),
                                   descriptor, 
                                   MethodDescriptor.EJB_REMOTE);
        
        if(descriptor.getLocalClassName() != null && 
                !"".equals(descriptor.getLocalClassName())) 
            commonToBothInterfaces(descriptor.getLocalClassName(),
                                   descriptor, 
                                   MethodDescriptor.EJB_LOCAL);
	}
        
    if(result.getStatus() != Result.FAILED) {
        addGoodDetails(result, compName);
        result.passed(smh.getLocalString
                        (getClass().getName() + ".passed",
                        "Business method(s) are valid."));
    }
    return result;
    }
    
    /** 
     * This method is responsible for the logic of the test. It is called for 
     * both local and remote interfaces.
     * @param intf for the Remote/Local interface of the Ejb. 
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * This parameter may be optional depending on the test 
     * @param methodIntf for the interface type
     */

    private void commonToBothInterfaces(String intf, 
                                        EjbDescriptor descriptor, 
                                        String methodIntf) {
        try {
            Class intfClass = Class.forName(intf, false, getVerifierContext().getClassLoader());
            
            boolean found = false;
            Set allMethods = new HashSet();
            
            for (Iterator e = 
                    descriptor.getPermissionedMethodsByPermission().values().iterator();e.hasNext();) {
                Set methodDescriptors = (Set) e.next();
                if (methodDescriptors != null)
                    allMethods.addAll(methodDescriptors);
            }
            for (Enumeration e = 
                    descriptor.getMethodContainerTransactions().keys();e.hasMoreElements();) {
                allMethods.add(e.nextElement());
            }
            
            for (Method remoteMethod : intfClass.getMethods()) {
                found = false;
                
                // we don't test the EJB methods
                if (remoteMethod.getDeclaringClass().getName().equals("javax.ejb.EJBObject")) 
                    continue;
                if (!remoteMethod.getName().startsWith("ejb") &&
                        !remoteMethod.getName().equals("class$") &&
                        !remoteMethod.getName().equals("setSessionContext")) {
                    
                    Iterator methods = allMethods.iterator();
                    while (methods.hasNext()) {
                        MethodDescriptor methodDescriptor = (MethodDescriptor)methods.next();
                        
                        if (methodDescriptor.getName().equals(remoteMethod.getName())) {
                            if (MethodUtils.stringArrayEquals(methodDescriptor.getParameterClassNames(), 
                                    (new MethodDescriptor(remoteMethod,methodIntf)).getParameterClassNames())) {
                                found = true;
                                break;
                            }
                        }
                    }
                }
                if (!found) {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                                    (getClass().getName() + ".failed",
                                    "Error: Business method [ {0} ] is not defined " +
                                    "in the deployment descriptor.",
                                    new Object[] {remoteMethod.getName()}));
                } 
            }
            
        } catch (Exception e) {
            Verifier.debug(e);
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                            (getClass().getName() + ".failedException",
                            "Error: Component interface [ {0} ] does not exist " +
                            "or is not loadable within bean [ {1} ].",
                            new Object[] {intf,descriptor.getName()}));
        }  
    }
}
