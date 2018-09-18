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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.createmethod;

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
 * create<Method> method tests
 * Entity beans home interface create method return type test.
 * 
 * The following are the requirements for the enterprise Bean's home interface 
 * signature: 
 * 
 * An Entity Bean's home interface defines zero or more create(...) methods. 
 * 
 * The return type for a create method must be the enterprise Bean's remote 
 * interface type. 
 * 
 */
public class HomeInterfaceCreateMethodReturn extends EjbTest implements EjbCheck { 
    boolean foundAtLeastOneCreate = false;
    Result result = null;
    ComponentNameConstructor compName = null;
    boolean remote_exists = false;
    boolean local_exists = false;
    /**
     * Entity beans home interface create method return type test.
     * 
     * The following are the requirements for the enterprise Bean's home interface 
     * signature: 
     * 
     * An Entity Bean's home interface defines zero or more create(...) methods. 
     * 
     * The return type for a create method must be the enterprise Bean's remote 
     * interface type. 
     * 
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        result = getInitializedResult();
	compName = getVerifierContext().getComponentNameConstructor();
	String local = null;
	String localHome = null;
	String remote = null;
	String home = null;
	if (descriptor instanceof EjbEntityDescriptor) {
	    boolean oneFailed = false;
	 
	    // RULE: Entity home interface are only allowed to have create 
	    //       methods which returns the entity Bean's
	    //       remote interface. 
	 
	    if (descriptor.getHomeClassName() != null && !"".equals(descriptor.getHomeClassName()) &&
		descriptor.getRemoteClassName() != null && !"".equals(descriptor.getRemoteClassName()) ) {
		remote_exists = true;
		home = descriptor.getHomeClassName();
		remote = descriptor.getRemoteClassName();
	    }
	    if (descriptor.getLocalHomeClassName() != null && !"".equals(descriptor.getLocalHomeClassName())&&
		descriptor.getLocalClassName() != null && !"".equals(descriptor.getLocalClassName())) {
		local_exists = true;
		localHome = descriptor.getLocalHomeClassName();
		local = descriptor.getLocalClassName();
	    }
	    oneFailed = commonToBothInterfaces(remote,home,local,localHome,(EjbEntityDescriptor)descriptor);
	    
	    if (!foundAtLeastOneCreate) {
		result.addNaDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
		result.addNaDetails(smh.getLocalString
				      (getClass().getName() + ".debug3",
				       "In Home Interface ",
				       new Object[] {}));
		result.addNaDetails(smh.getLocalString
				      (getClass().getName() + ".notApplicable1",
				       "No create method was found, test not applicable." ));
		result.setStatus(result.NOT_APPLICABLE);
	    } else {
		if (oneFailed) {
		    result.setStatus(result.FAILED);
		} else {
		    result.setStatus(result.PASSED);
		}
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
				  new Object[] {getClass(),"Entity","Session"}));
	    return result;
	} 
    }

    /** 
     * This method is responsible for the logic of the test. It is called for both local and remote interfaces.
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @param home for the Home interface of the Ejb. 
     * @param remote Remote/Local interface
     * @return boolean the results for this assertion i.e if a test has failed or not
     */
    
      private boolean commonToBothInterfaces(String remote, String home, String local, String localHome, EjbEntityDescriptor descriptor) {
	boolean oneFailed = false;
	Class c,rc,lc,hc;
	Method localMethods[],methods[];
	try {
	    VerifierTestContext context = getVerifierContext();
	    ClassLoader jcl = context.getClassLoader();
	    if (remote_exists) {
		c = Class.forName(home, false, getVerifierContext().getClassLoader());
		rc = Class.forName(remote, false, getVerifierContext().getClassLoader());
		methods = c.getDeclaredMethods();
		oneFailed = findReturnType(methods,home,local,remote);
	    }
	    if (oneFailed == false) {
		if (local_exists) {
		    hc = Class.forName(localHome, false, getVerifierContext().getClassLoader());
		    lc = Class.forName(local, false, getVerifierContext().getClassLoader());
		    localMethods = hc.getDeclaredMethods();
		    oneFailed = findReturnType(localMethods,localHome,local,remote);
		}
	    }
	    return oneFailed;	    
	  
	} catch (ClassNotFoundException e) {
	    Verifier.debug(e);
	    result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
	    result.failed(smh.getLocalString
			  (getClass().getName() + ".failedException",
			   "Error: Home interface [ {0} ] or [ {1} ]or Component interface [ {2} ] or [ {3} ] does not exist or is not loadable within bean [ {4} ]",
			   new Object[] {home, localHome, remote, local,  descriptor.getName()}));
	    return false;
	}
    }


    private boolean findReturnType(Method[] methods, String home, String local, String remote) {
	Class methodReturnType;
	boolean validReturn, oneFailed = false;
	
	for (int i=0; i< methods.length; i++) {
	    // clear these from last time thru loop
	    validReturn = false;
	    if (methods[i].getName().startsWith("create")) {
        foundAtLeastOneCreate=true;
		// return type must be the remote interface
		methodReturnType = methods[i].getReturnType();
		if (remote_exists) {
		    if (methodReturnType.getName().equals(remote)) {
			// this is the right ejbCreate method
			validReturn = true;
			result.addGoodDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
			result.addGoodDetails(smh.getLocalString
					      (getClass().getName() + ".debug1",
					       "For Home Interface [ {0} ] Method [ {1} ]",
					       new Object[] {home ,methods[i].getName()}));
			result.addGoodDetails(smh.getLocalString
					      (getClass().getName() + ".passed",
					       "The create method which returns [ {0} ] interface was found.",
					       new Object[] {"remote"}));
		    }
		}
		if (local_exists) {
		    if (methodReturnType.getName().equals(local)) {
			// this is the right ejbCreate method
			validReturn = true;
			result.addGoodDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
			result.addGoodDetails(smh.getLocalString
					      (getClass().getName() + ".debug1",
					       "For Home Interface [ {0} ] Method [ {1} ]",
					       new Object[] {home ,methods[i].getName()}));
			result.addGoodDetails(smh.getLocalString
					      (getClass().getName() + ".passed",
					       "The create method which returns [ {0} ] interface was found.",
					       new Object[] {"local"}));
		    }
		}
		
		//report for this particular create method found in home interface
		// now display the appropriate results for this particular create
		// method
		if (!validReturn) {
		    oneFailed = true;
		    result.addErrorDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
		    result.addErrorDetails(smh.getLocalString
					   (getClass().getName() + ".debug1",
					    "For Home Interface [ {0} ] Method [ {1} ]",
					    new Object[] {home,methods[i].getName()}));
		    result.addErrorDetails(smh.getLocalString
					   (getClass().getName() + ".failed",
					    "Error: A Create method was found, but the return type [ {0} ] was not the Remote/Local interface" ,
					    new Object[] {methodReturnType.getName()}));
            return oneFailed;
		}  // end of reporting for this particular 'create' method
	    } // if the home interface found a "create" method
	} // for all the methods within the home interface class, loop
	
	return oneFailed;  
	
    }
}
