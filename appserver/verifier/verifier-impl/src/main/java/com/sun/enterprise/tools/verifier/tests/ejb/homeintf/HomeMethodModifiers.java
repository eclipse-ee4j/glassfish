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

package com.sun.enterprise.tools.verifier.tests.ejb.homeintf;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/** 
 * Home methods must be public and not static
 *
 * @author Jerome Dochez
 * @version
 */
abstract public class HomeMethodModifiers extends HomeMethodTest {  
    
    /**
     * <p>
     * run an individual home method test 
     * </p>
     * 
     * @param method the home method to test
     * @param descriptor the deployment descriptor for the entity bean
     * @param result the result object
     */

    protected void runIndividualHomeMethodTest(Method method, EjbDescriptor descriptor, Result result) {
        
        Method m;
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	try {	  
	    // retrieve the remote interface methods
	    ClassLoader jcl = getVerifierContext().getClassLoader();
	    Class ejbClass = Class.forName(descriptor.getEjbClassName(), false, jcl);
                                    
            // Bug: 4952890. first character of this name should be converted to UpperCase. 
            String methodName = method.getName().replaceFirst(method.getName().substring(0,1),
                                                              method.getName().substring(0,1).toUpperCase());
            String expectedMethodName = "ejbHome" + methodName;
            do {
                // retrieve the EJB Class Methods
                m = getMethod(ejbClass, expectedMethodName, method.getParameterTypes());   
	    } while (((ejbClass = ejbClass.getSuperclass()) != null) && (m==null));

            if (m != null) {
                int modifiers = m.getModifiers();
                if (Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers)) {
                    addGoodDetails(result, compName);
                    result.addGoodDetails(smh.getLocalString
                        (getClass().getName() + ".passed",
                        "For method [ {1} ] in Home Interface [ {0} ], ejbHome method is public and not static",
                        new Object[] {method.getDeclaringClass().getName(), method.getName()})); 
		    result.setStatus(Result.PASSED);               
                    //return true;
                } else {
                    addErrorDetails(result, compName);
                    result.addErrorDetails(smh.getLocalString
                        (getClass().getName() + ".notApplicable",
                        "Error : For method [ {1} ] defined in Home Interface [ {0} ], the ejbHome method is either static or not public",
                        new Object[] {method.getDeclaringClass().getName(), method.getName()}));
		    result.setStatus(Result.FAILED);               
		    // return false;
                }                    
            } else {
                addErrorDetails(result, compName);
                result.addErrorDetails(smh.getLocalString
                    (getClass().getName() + ".failed",  
                    "Error : For method [ {1} ] defined in Home Interface [ {0} ], no ejbHome name matching method was found" ,
                    new Object[] {method.getDeclaringClass().getName(), method.getName()}));
		    result.setStatus(Result.FAILED);               
                //return true;
	    }
	} catch (ClassNotFoundException e) {
	    Verifier.debug(e);
        addErrorDetails(result, compName);
	    result.failed(smh.getLocalString(
					     getClass().getName() + ".failedException",
					     "Error: Home interface [ {0} ] does not exist or is not loadable within bean [ {1} ]",
					     new Object[] {getClassName(descriptor),descriptor.getName()}));
	    //return false;
	    result.setStatus(Result.FAILED);
	}
    }

  private String getClassName(EjbDescriptor descriptor) {
	return getHomeInterfaceName(descriptor);
    }  
}
