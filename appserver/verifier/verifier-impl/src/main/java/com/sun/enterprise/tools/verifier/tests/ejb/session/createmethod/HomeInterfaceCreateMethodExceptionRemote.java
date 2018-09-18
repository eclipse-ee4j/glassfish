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

package com.sun.enterprise.tools.verifier.tests.ejb.session.createmethod;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbUtils;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import java.lang.reflect.Method;

/** 
 * Session beans home interface create method throws RemoteException test.
 * 
 * The following are the requirements for the enterprise Bean's home interface 
 * signature: 
 * 
 * An Session Bean's home interface defines zero or more create(...) methods. 
 * 
 * The throws clause must include java.rmi.RemoteException. 
 */
public class HomeInterfaceCreateMethodExceptionRemote extends EjbTest implements EjbCheck { 


    /** 
     * Session beans home interface create method throws RemoteException test.
     * 
     * The following are the requirements for the enterprise Bean's home interface 
     * signature: 
     * 
     * An Session Bean's home interface defines zero or more create(...) methods. 
     * 
     * The throws clause must include java.rmi.RemoteException. 
     *    
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor instanceof EjbSessionDescriptor) {
	    boolean oneFailed = false;
	    boolean foundAtLeastOneRemote = false;
	    // RULE: Session home interface are only allowed to have create 
	    //       methods which must throw java.rmi.RemoteException
	    if(descriptor.getHomeClassName() == null || "".equals(descriptor.getHomeClassName())) {
		result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.addNaDetails(smh.getLocalString
				    (getClass().getName() + ".notApplicable1",
				     "No Remote Home Interface for this ejb",
				     new Object[] {}));
		return result;
	    }
	    try {
		VerifierTestContext context = getVerifierContext();
		ClassLoader jcl = context.getClassLoader();
		Class c = Class.forName(descriptor.getHomeClassName(), false, getVerifierContext().getClassLoader());
		Method methods[] = c.getDeclaredMethods();
		Class [] methodExceptionTypes;
		boolean throwsRemoteException = false;


		for (int i=0; i< methods.length; i++) {
		    // clear these from last time thru loop
		    throwsRemoteException = false;

		    if (methods[i].getName().startsWith("create")) {
			// set this once to indicate that test is applicable, if you didn't
			// find any create methods, that's an error, as session beans can
			// have  one or more create methods, & when you have zero, test
			// is failed
			if (!foundAtLeastOneRemote) {
			    foundAtLeastOneRemote = true;
			}  

			methodExceptionTypes = methods[i].getExceptionTypes();
               
			// methods must also throw java.rmi.RemoteException
			if (EjbUtils.isValidRemoteException(methodExceptionTypes)) {
			    throwsRemoteException = true;
			}

			//report for this particular create method found in home interface
			// now display the appropriate results for this particular create
			// method
			if (throwsRemoteException ) {
			    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
			    result.addGoodDetails(smh.getLocalString
						  (getClass().getName() + ".debug1",
						   "For Home Interface [ {0} ] Method [ {1} ]",
						   new Object[] {c.getName(),methods[i].getName()}));
			    result.addGoodDetails(smh.getLocalString
						  (getClass().getName() + ".passed",
						   "The create method which must throw java.rmi.RemoteException was found."));
			} else if (!throwsRemoteException) {
			    oneFailed = true;
			    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
			    result.addErrorDetails(smh.getLocalString
						   (getClass().getName() + ".debug1",
						    "For Home Interface [ {0} ] Method [ {1} ]",
						    new Object[] {c.getName(),methods[i].getName()}));
			    result.addErrorDetails(smh.getLocalString
						   (getClass().getName() + ".failed",
						    "Error: A create method was found, but did not throw java.rmi.RemoteException." ));
			}  // end of reporting for this particular 'create' method
		    } // if the home interface found a "create" method
		} // for all the methods within the home interface class, loop
             

	    } catch (ClassNotFoundException e) {
		Verifier.debug(e);
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.failed(smh.getLocalString
			      (getClass().getName() + ".failedException",
			       "Error: Home interface [ {0} ] does not exist or is not loadable within bean [ {1} ]",
			       new Object[] {descriptor.getHomeClassName(), descriptor.getName()}));
	    }

	    if (!foundAtLeastOneRemote) {
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.addErrorDetails(smh.getLocalString
				       (getClass().getName() + ".debug3",
					"For Home Interface [ {0} ]",
					new Object[] {descriptor.getHomeClassName()}));
		result.addErrorDetails(smh.getLocalString
				       (getClass().getName() + ".failed1",
					"No create method was found in [ {0} ]", 
					new Object[] {descriptor.getHomeClassName()}));
		result.setStatus(result.FAILED);
	    } else {
		if (oneFailed) {
		    result.setStatus(result.FAILED);
		} else {
		    result.setStatus(result.PASSED);
		}
	    }
         
	    return result;
        
	} else {
	    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "[ {0} ] expected {1} bean, but called with {2} bean.",
				  new Object[] {getClass(),"Session","Entity"}));
	    return result;
	} 
    }
}
