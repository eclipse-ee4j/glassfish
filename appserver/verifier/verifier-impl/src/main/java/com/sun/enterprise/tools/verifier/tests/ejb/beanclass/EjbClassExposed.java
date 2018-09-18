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

package com.sun.enterprise.tools.verifier.tests.ejb.beanclass;


import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import java.lang.reflect.Method;

/**
 * Enterprise Java Bean class exposed test.  
 * The class must not be exposed through remote or local interfaces.
 * @author Sheetal Vartak
 */
public class EjbClassExposed extends EjbTest { 

    Result result = null;
    ComponentNameConstructor compName = null;
    /**
     * Enterprise Java Bean class exposed test.  
     *   
     * @param descriptor the Enterprise Java Bean deployment descriptor   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	result = getInitializedResult();
	compName = getVerifierContext().getComponentNameConstructor();

    if (descriptor instanceof EjbSessionDescriptor ||
            descriptor instanceof EjbEntityDescriptor) {
        if (descriptor.getRemoteClassName() != null && 
                !((descriptor.getRemoteClassName()).equals(""))) 
            commonToBothInterfaces(descriptor.getRemoteClassName(),descriptor); 
        if (descriptor.getLocalClassName() != null && 
                !((descriptor.getLocalClassName()).equals(""))) 
            commonToBothInterfaces(descriptor.getLocalClassName(),descriptor); 
    }

    if(result.getStatus() != Result.FAILED) {
        addGoodDetails(result, compName);
        result.passed(smh.getLocalString(
                getClass().getName() + ".passed",
                "Ejb Bean Class [{0}] is valid.",
                new Object[] {descriptor.getEjbClassName()}));
    }
    return result;

    }

    /** 
     * This method is responsible for the logic of the test. It is called for 
     * both local and remote interfaces.
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @param remote for the Remote/Local interface of the Ejb. 
     */

    private void commonToBothInterfaces(String remote, EjbDescriptor descriptor) {
	try { 
        Class c = Class.forName(remote, 
                                false, 
                                getVerifierContext().getClassLoader());

        for (Method method : c.getDeclaredMethods()) {
            String ejbClassName = descriptor.getEjbClassName();
            if(((method.getReturnType()).getName()).equals(ejbClassName)) {
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString(
                        getClass().getName() + ".failed",
                        "Error: Ejb Bean Class [{0}] is exposed through interface [{1}]",
                        new Object[] {ejbClassName, remote}));
            }
        }
	}catch (ClassNotFoundException e) {
        addErrorDetails(result, compName);
	    result.failed(smh.getLocalString(
					     getClass().getName() + ".failedException",
					     "Error: interface class [{0}] not found",
					     new Object[] {remote}));
	}
    }
}
