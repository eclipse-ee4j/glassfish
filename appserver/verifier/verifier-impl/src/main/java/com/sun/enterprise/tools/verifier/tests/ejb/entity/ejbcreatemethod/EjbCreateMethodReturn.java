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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.ejbcreatemethod;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.lang.reflect.Method;

/**  
 * Entity Bean's ejbCreate(...) methods return test.
 * Each entity Bean class may define zero or more ejbCreate(...) methods. 
 * The number and signatures of a entity Bean's create methods are specific 
 * to each EJB class. The method signatures must follow these rules: 
 * 
 * The method name must be ejbCreate. 
 *
 * The return type must be primary key type. 
 * 
 */
public class EjbCreateMethodReturn extends EjbTest implements EjbCheck { 


    /** 
     * Entity Bean's ejbCreate(...) methods return test.
     * Each entity Bean class may define zero or more ejbCreate(...) methods. 
     * The number and signatures of a entity Bean's create methods are specific 
     * to each EJB class. The method signatures must follow these rules: 
     * 
     * The method name must be ejbCreate. 
     *
     * The return type must be primary key type. 
     * 
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor instanceof EjbEntityDescriptor) {
	    String persistence =
		((EjbEntityDescriptor)descriptor).getPersistenceType();
	    if (EjbEntityDescriptor.BEAN_PERSISTENCE.equals(persistence)) {
		boolean oneFailed = false;
                boolean oneWarning = false;
		int foundAtLeastOne = 0;
		try {
		    VerifierTestContext context = getVerifierContext();
		    ClassLoader jcl = context.getClassLoader();
		    Class c = Class.forName(descriptor.getEjbClassName(), false, getVerifierContext().getClassLoader());
  
		    String primaryKeyType = ((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName();
		    boolean ejbCreateFound = false;
		    boolean returnsPrimaryKeyType = false;
                    // start do while loop here....
                    do {
			Method [] methods = c.getDeclaredMethods();
			for (int i = 0; i < methods.length; i++) {
			    // reset flags from last time thru loop
			    ejbCreateFound = false;
			    returnsPrimaryKeyType = false;
			    // The method name must be ejbCreate. 
			    if (methods[i].getName().startsWith("ejbCreate")) {
				foundAtLeastOne++;
				ejbCreateFound = true;
  
				// The return type must be primary key type. 
				Class rt = methods[i].getReturnType();
				if (rt.getName().equals(primaryKeyType)) {
				    returnsPrimaryKeyType = true;
				}
  
				// now display the appropriate results for this particular ejbCreate
				// method
				if (ejbCreateFound && !returnsPrimaryKeyType) {
                                    if (primaryKeyType.equals("java.lang.Object")) {
                                        oneWarning = true;
					result.addWarningDetails(smh.getLocalString
								 ("tests.componentNameConstructor",
								  "For [ {0} ]",
								  new Object[] {compName.toString()}));
				        result.addWarningDetails(smh.getLocalString
								 (getClass().getName() + ".debug1",
								  "For EJB Class [ {0} ] method [ {1} ]",
								  new Object[] {descriptor.getEjbClassName(),methods[i].getName()}));
				        result.addWarningDetails(smh.getLocalString
								 (getClass().getName() + ".warning",
								  "Warning: An [ {0} ] method was found, but [ {1} ] method has [ {2} ] return type.   Deployment descriptor primary key type [ {3} ]. Definition of the primary key type is deferred to deployment time ?",
								  new Object[] {methods[i].getName(),methods[i].getName(),methods[i].getReturnType().getName(),primaryKeyType}));
                                    } else {
				        oneFailed = true;
					result.addErrorDetails(smh.getLocalString
							       ("tests.componentNameConstructor",
								"For [ {0} ]",
								new Object[] {compName.toString()}));
				        result.addErrorDetails(smh.getLocalString
							       (getClass().getName() + ".debug1",
								"For EJB Class [ {0} ] method [ {1} ]",
								new Object[] {descriptor.getEjbClassName(),methods[i].getName()}));
				        result.addErrorDetails(smh.getLocalString
							       (getClass().getName() + ".failed",
								"Error: An [ {0} ] method was found, but [ {1} ] method has illegal return value.   [ {2} ] methods must return primary key type [ {3} ].",
								new Object[] {methods[i].getName(),methods[i].getName(),methods[i].getName(),primaryKeyType}));
					break;
                                    }
				} 
			    }
			}
			if (oneFailed == true)
			    break;
                    } while (((c = c.getSuperclass()) != null) && (foundAtLeastOne == 0));
          
		    if (foundAtLeastOne == 0) {
			result.addNaDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
			result.notApplicable(smh.getLocalString
					     (getClass().getName() + ".notApplicable0",
					      "[ {0} ] does not declare any ejbCreate(...) methods.",
					      new Object[] {descriptor.getEjbClassName()}));
		    }
		    if (oneFailed == false && foundAtLeastOne > 0) {
			result.addGoodDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
			result.addGoodDetails(smh.getLocalString
					      (getClass().getName() + ".debug1",
					       "For EJB Class [ {0} ]",
					       new Object[] {descriptor.getEjbClassName()}));
			result.addGoodDetails(smh.getLocalString
					      (getClass().getName() + ".passed",
					       "[ {0} ] properly declares ejbCreate<method> method to return primary key type [ {1} ].",
					       new Object[] {descriptor.getEjbClassName(), primaryKeyType}));
		    }
		} catch (ClassNotFoundException e) {
		    Verifier.debug(e);
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
				  (getClass().getName() + ".failedException",
				   "Error: [ {0} ] class not found.",
				   new Object[] {descriptor.getEjbClassName()}));
		    oneFailed = true;
		}  
  
		if (oneFailed) {
                    result.setStatus(result.FAILED);
                } else if (foundAtLeastOne == 0) {
                    result.setStatus(result.NOT_APPLICABLE);
                } else {
                    if (oneWarning) {
                        result.setStatus(result.WARNING);
                    } else { 
			
                        result.setStatus(result.PASSED);
                    }
                }
  
		return result;
 
	    } else { // if (CONTAINER_PERSISTENCE.equals(persistence)) {
		result.addNaDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     (getClass().getName() + ".notApplicable1",
				      "Expected [ {0} ] managed persistence, but [ {1} ] bean has [ {2} ] managed persistence.",
				      new Object[] {EjbEntityDescriptor.BEAN_PERSISTENCE,descriptor.getName(),persistence}));
		return result;
	    }

	} else {
	    result.addNaDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "[ {0} ] expected {1} bean, but called with {2} bean.",
				  new Object[] {getClass(),"Entity","Session"}));
	    return result;
	}
    }
}
