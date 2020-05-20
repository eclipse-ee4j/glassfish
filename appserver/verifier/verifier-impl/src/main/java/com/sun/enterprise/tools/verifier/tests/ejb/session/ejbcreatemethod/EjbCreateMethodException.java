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

package com.sun.enterprise.tools.verifier.tests.ejb.session.ejbcreatemethod;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import java.lang.reflect.Method;

/**  
 * Session Bean's ejbCreate(...) methods exception test.
 * Each session Bean class must define one or more ejbCreate(...) methods. 
 * The number and signatures of a session Bean's create methods are specific 
 * to each EJB class. The method signatures must follow these rules: 
 * 
 * The method name must be ejbCreate. 
 *
 * Compatibility Note: EJB 1.0 allowed the ejbCreate method to throw the 
 * java.rmi.RemoteException to indicate a non-application exception. This 
 * practice is deprecated in EJB 1.1---an EJB 1.1 compliant enterprise bean 
 * should throw the jakarta.ejb.EJBException or another RuntimeException to 
 * indicate non-application exceptions to the Container (see Section 12.2.2). 
 * Note: Treat as a warning to user in this instance.
 */
public class EjbCreateMethodException extends EjbTest implements EjbCheck { 


    /** 
     * Session Bean's ejbCreate(...) methods exception test.
     * Each session Bean class must define one or more ejbCreate(...) methods. 
     * The number and signatures of a session Bean's create methods are specific 
     * to each EJB class. The method signatures must follow these rules: 
     * 
     * The method name must be ejbCreate. 
     *
     * Compatibility Note: EJB 1.0 allowed the ejbCreate method to throw the 
     * java.rmi.RemoteException to indicate a non-application exception. This 
     * practice is deprecated in EJB 1.1---an EJB 1.1 compliant enterprise bean 
     * should throw the jakarta.ejb.EJBException or another RuntimeException to 
     * indicate non-application exceptions to the Container (see Section 12.2.2). 
     * Note: Treat as a warning to user in this instance.
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
	    int foundWarning = 0;
	    try {
		VerifierTestContext context = getVerifierContext();
		ClassLoader jcl = context.getClassLoader();
		Class c = Class.forName(descriptor.getEjbClassName(), false, getVerifierContext().getClassLoader());

		Class [] ejbCreateMethodParameterTypes;
		int foundAtLeastOne = 0;
		boolean ejbCreateFound = false;
		boolean throwsRemoteException = false;
                // start do while loop here....
                do {
		    Method [] methods = c.getDeclaredMethods();
		    for (int i = 0; i < methods.length; i++) {
			// reset flags from last time thru loop
			ejbCreateFound = false;
			throwsRemoteException = false;

			// The method name must be ejbCreate. 
			if (methods[i].getName().startsWith("ejbCreate")) {
			    foundAtLeastOne++;
			    ejbCreateFound = true;

			    // Compatibility Note: EJB 1.0 allowed the ejbCreate method to throw
			    // the java.rmi.RemoteException to indicate a non-application 
			    // exception. This practice is deprecated in EJB 1.1---an EJB 1.1 
			    // compliant enterprise bean should throw the jakarta.ejb.EJBException
			    // or another RuntimeException to indicate non-application 
			    // exceptions to the Container (see Section 12.2.2). 
			    // Note: Treat as a warning to user in this instance.
			    Class [] exceptions = methods[i].getExceptionTypes();
			    for (int z = 0; z < exceptions.length; ++z) {
				if ((exceptions[z].getName().equals("java.rmi.RemoteException")) || 
				    (exceptions[z].getName().equals("RemoteException"))) {
				    throwsRemoteException = true;
				    break;
				}
			    }

			    // now display the appropriate results for this particular ejbCreate
			    // method
			    if (ejbCreateFound && (!throwsRemoteException) ) {
				result.addGoodDetails(smh.getLocalString
						      ("tests.componentNameConstructor",
						       "For [ {0} ]",
						       new Object[] {compName.toString()}));
				result.addGoodDetails(smh.getLocalString
						      (getClass().getName() + ".debug1",
						       "For EJB Class [ {0} ] method [ {1} ]",
						       new Object[] {descriptor.getEjbClassName(),methods[i].getName()}));
				result.addGoodDetails(smh.getLocalString
						      (getClass().getName() + ".passed",
						       " [ {0} ] properly declares [ {1} ] method which properly does not throw java.rmi.RemoteException.",
						       new Object[] {descriptor.getEjbClassName(),methods[i].getName()}));
			    } else if (ejbCreateFound && throwsRemoteException) {
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
							  "Error: Compatibility Note:" +
							  "\n An [ {0} ] method was found, but" +
							  "\n EJB 1.0 allowed the ejbCreate method to throw the " +
							  "\n java.rmi.RemoteException to indicate a non-application" +
							  "\n exception. This practice is deprecated in EJB 1.1" +
							  "\n ---an EJB 1.1 compliant enterprise bean should" +
							  "\n throw the jakarta.ejb.EJBException or another " +
							  "\n RuntimeException to indicate non-application exceptions" +
							  "\n to the Container",
							  new Object[] {methods[i].getName()}));
				foundWarning++;
			    } 
			}
		    }
                } while (((c = c.getSuperclass()) != null) && (foundAtLeastOne == 0));
        
		if (foundAtLeastOne == 0){
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
				  (getClass().getName() + ".failed",
				   "Error: [ {0} ] does not properly declare at least one ejbCreate(...) method.  [ {1} ] is not a valid bean.",
				   new Object[] {descriptor.getEjbClassName(),descriptor.getEjbClassName()}));
		    oneFailed = true;
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
	    } else if (foundWarning > 0) {
		result.setStatus(result.WARNING);
	    } else {
		result.setStatus(result.PASSED);
	    } 

	    return result;
 
	} else {
	    result.addNaDetails(smh.getLocalString
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
