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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2;

import com.sun.enterprise.deployment.EjbDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;

import java.lang.reflect.Method;

/**
 * Superclass for all finder method test
 *
 * @author  Jerome Dochez
 * @version 
 */
abstract public class QueryMethodTest extends CMPTest {
    ComponentNameConstructor compName = null;
    /**
     * <p>
     * Run an individual test against a finder method (single or multi)
     * </p>
     * 
     * @param method is the finder method reference
     * @param descriptor is the entity bean descriptor
     * @param targetClass is the class to apply to tests to
     * @param result is where to place the result
     * 
     * @return true if the test passes
     */
    protected abstract boolean runIndividualQueryTest(Method method, EjbCMPEntityDescriptor descriptor, Class targetClass, Result result);
    
     /**
     * check if a field has been declared in a class
     * 
     * @param fieldName the field name to look for declaration
     * @param c the class to look into
     * @param result where to place the test result
     */
    public Result check(EjbCMPEntityDescriptor descriptor) {
        
        boolean allIsWell = true;
        Result result = getInitializedResult();
	compName = getVerifierContext().getComponentNameConstructor();
        
	if (descriptor.getHomeClassName() != null && !((descriptor.getHomeClassName()).equals("")) &&
	    descriptor.getRemoteClassName() != null && !((descriptor.getRemoteClassName()).equals(""))) {
	    allIsWell = commonToBothInterfaces(descriptor.getHomeClassName(),descriptor.getRemoteClassName(),descriptor, result);
	}   
	if(allIsWell == true) {
	    if (descriptor.getLocalHomeClassName() != null && !((descriptor.getLocalHomeClassName()).equals("")) &&
		descriptor.getLocalClassName() != null && !((descriptor.getLocalClassName()).equals(""))) {
		allIsWell = commonToBothInterfaces(descriptor.getLocalHomeClassName(),descriptor.getLocalClassName(),descriptor, result);
	    } 
	}    
     
        if (allIsWell) 
            result.setStatus(Result.PASSED);
        else 
            result.setStatus(Result.FAILED);
            
        return result;
    }
  /** 
     * This method is responsible for the logic of the test. It is called for both local and remote interfaces.
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @param ejbHome for the Home interface of the Ejb. 
     * @param result Result of the test
     * @param remote Remote/Local interface
     * @return boolean the results for this assertion i.e if a test has failed or not
     */


    private boolean commonToBothInterfaces(String ejbHome, String remote, EjbDescriptor descriptor, Result result) {
	boolean allIsWell = true;
	boolean found = false;
	String ejbClassName = descriptor.getEjbClassName();
	VerifierTestContext context = getVerifierContext();
		ClassLoader jcl = context.getClassLoader();
        try {
            Class ejbClass = Class.forName(ejbClassName, false,
                                getVerifierContext().getClassLoader());
            Method[] methods = Class.forName(ejbHome, false,
                                getVerifierContext().getClassLoader()).getMethods();
            for (int i=0;i<methods.length;i++) {
                String methodName = methods[i].getName();
                // get the expected return type
                String methodReturnType = methods[i].getReturnType().getName();
                if (methodName.startsWith("find")) {
		    found = true;
                    if (methodReturnType.equals(remote) ||                     
			isSubclassOf(Class.forName(methodReturnType, false,
                    getVerifierContext().getClassLoader()), "java.util.Collection") ||
			isImplementorOf(Class.forName(methodReturnType, false,
                    getVerifierContext().getClassLoader()), "java.util.Collection")) {
                        
                        if (!runIndividualQueryTest(methods[i], (EjbCMPEntityDescriptor) descriptor, ejbClass, result)) 
                            allIsWell=false;
                    }
                }
	    }
	    if (found == false) {
		result.addGoodDetails(smh.getLocalString
			  ("com.sun.enterprise.tools.verifier.tests.ejb.EjbTest.passed",
			   "Not Applicable : No find methods found",
                new Object[] {}));  
	    }   
            
	    return allIsWell;
        } catch (ClassNotFoundException e) {
            Verifier.debug(e);
       		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.failed(smh.getLocalString
			  ("com.sun.enterprise.tools.verifier.tests.ejb.EjbTest.failedException",
			   "Error: [ {0} ] class not found.",
                new Object[] {descriptor.getEjbClassName()}));                    
            allIsWell= false;
	    return allIsWell;
        }
    }
}
