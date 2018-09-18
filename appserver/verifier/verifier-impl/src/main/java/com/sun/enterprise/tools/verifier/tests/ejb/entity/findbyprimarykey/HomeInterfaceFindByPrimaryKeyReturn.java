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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.findbyprimarykey;

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
 * Define findByPrimaryKey method which returns the primary key type test.  
 *
 *     Every entity enterprise Bean class must define the findByPrimaryKey 
 *     method. The return type for this method must be the primary key type.
 *     (i.e. the findByPrimaryKey method must be a single-object finder). 
 * 
 */
public class HomeInterfaceFindByPrimaryKeyReturn extends EjbTest implements EjbCheck { 
    Result result = null;
    ComponentNameConstructor compName = null;

    /**
     * Define findByPrimaryKey method test.  
     *
     *     Every entity enterprise Bean class must define the findByPrimaryKey 
     *     method. The return type for this method must be the primary key type.
     *     (i.e. the findByPrimaryKey method must be a single-object finder). 
     *  
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	result = getInitializedResult();
	compName = getVerifierContext().getComponentNameConstructor();

	boolean oneFailed = false;
	if (descriptor instanceof EjbEntityDescriptor) {
	    if(descriptor.getHomeClassName() != null && !"".equals(descriptor.getHomeClassName())&&
	       descriptor.getRemoteClassName() != null && !"".equals(descriptor.getRemoteClassName()))
		oneFailed = commonToBothInterfaces(descriptor.getHomeClassName(), descriptor.getRemoteClassName(),descriptor);
	    if(oneFailed == false) {
		if(descriptor.getLocalHomeClassName() != null && !"".equals(descriptor.getLocalHomeClassName())&&
		   descriptor.getLocalClassName() != null && !"".equals(descriptor.getLocalClassName()))
		    oneFailed = commonToBothInterfaces(descriptor.getLocalHomeClassName(),descriptor.getLocalClassName(),descriptor);
	    }	   
	    if (oneFailed)  {
		result.setStatus(result.FAILED);
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
				  new Object[] {getClass(),"Entity","Session"}));
	    return result;
	}
    }

    /** 
     * This method is responsible for the logic of the test. It is called for both local and remote interfaces.
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @param home for the Home interface of the Ejb. 
     * @param remote for Remote/Local interface
     * @return boolean the results for this assertion i.e if a test has failed or not
     */


    private boolean commonToBothInterfaces(String home, String remote, EjbDescriptor descriptor) {
	boolean findByPrimaryKeyMethodFound = false;
	boolean oneFailed = false;
	boolean returnValueValid = false;
	try {
	    // retrieve the home interface methods
	    VerifierTestContext context = getVerifierContext();
	    ClassLoader jcl = context.getClassLoader();
	    Class homeInterfaceClass = Class.forName(home, false, getVerifierContext().getClassLoader());
	    Method [] ejbFinderMethods = homeInterfaceClass.getDeclaredMethods();
	    Class rc = Class.forName(remote, false, getVerifierContext().getClassLoader());
	    
	    for (int j = 0; j < ejbFinderMethods.length; ++j) {
		// reset all booleans for next method within the loop
		returnValueValid = false;
		
		if (ejbFinderMethods[j].getName().equals("findByPrimaryKey")) {
		    // Every entity enterprise Bean class must define the 
		    // findByPrimaryKey method. The return type for this method must 
		    // be the primary key type (i.e. the findByPrimaryKey method 
		    // must be a single-object finder). 
		    if (!findByPrimaryKeyMethodFound) {
			findByPrimaryKeyMethodFound = true;
		    }
		    Class returnByPrimaryKeyValue = ejbFinderMethods[j].getReturnType();
		    // as long as this returns a single object finder, then return type
		    // is valid
		    if (((returnByPrimaryKeyValue.getName().equals(rc.getName())) && 
			 (!((returnByPrimaryKeyValue.getName().equals("java.util.Collection")) || 
			    (returnByPrimaryKeyValue.getName().equals("java.util.Enumeration")))))) {
			returnValueValid = true;
		    }
		    
		    
		    // report for this particular findByPrimaryKey(...)
		    if (findByPrimaryKeyMethodFound && returnValueValid) {
			result.addGoodDetails(smh.getLocalString
					      ("tests.componentNameConstructor",
					       "For [ {0} ]",
					       new Object[] {compName.toString()}));
			result.addGoodDetails(smh.getLocalString
					      (getClass().getName() + ".debug1",
					       "For Home interface [ {0} ] Finder Method [ {1} ]",
					       new Object[] {homeInterfaceClass.getName(),ejbFinderMethods[j].getName()}));
			result.addGoodDetails(smh.getLocalString
					      (getClass().getName() + ".passed",
					       "A findByPrimaryKey method was found with valid return type."));
		    } else if (findByPrimaryKeyMethodFound && !returnValueValid) {
			oneFailed = true;
			result.addErrorDetails(smh.getLocalString
					       ("tests.componentNameConstructor",
						"For [ {0} ]",
						new Object[] {compName.toString()}));
			result.addErrorDetails(smh.getLocalString
					       (getClass().getName() + ".debug1",
						"For Home interface [ {0} ] Finder Method [ {1} ]",
						new Object[] {homeInterfaceClass.getName(),ejbFinderMethods[j].getName()}));
			result.addErrorDetails(smh.getLocalString
					       (getClass().getName() + ".failed",
						"Error: A findByPrimaryKey method was found, but with invalid return type."));
		    }			 
		    // found findByPrimaryKey, so break out of loop
		    break;
		}
	    }
	    if (!findByPrimaryKeyMethodFound) {
		oneFailed = true;
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.addErrorDetails(smh.getLocalString
				       (getClass().getName() + ".debug3",
					"For Home interface [ {0} ] ",
					new Object[] {homeInterfaceClass.getName()}));
		result.addErrorDetails(smh.getLocalString
				       (getClass().getName() + ".failed1",
					"Error: No findByPrimaryKey method was found in home interface class [ {0} ].",
					new Object[] {homeInterfaceClass.getName()}));
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
			   "Error: Home interface [ {0} ] does not exist or is not loadable.",
			   new Object[] {home}));
	    oneFailed = true;
	    return oneFailed;
	}
	
    }
}
