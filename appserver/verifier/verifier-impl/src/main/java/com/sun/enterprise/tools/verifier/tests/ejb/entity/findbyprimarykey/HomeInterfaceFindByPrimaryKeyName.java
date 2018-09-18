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
 * Define findByPrimaryKey method test.  
 *
 *     Every entity enterprise Bean class must define the findByPrimaryKey 
 *     method. 
 * 
 */
public class HomeInterfaceFindByPrimaryKeyName extends EjbTest implements EjbCheck { 
    Result result = null;
    ComponentNameConstructor compName = null;

    /** 
     * Define findByPrimaryKey method test.  
     *
     *     Every entity enterprise Bean class must define the findByPrimaryKey 
     *     method. 
     *  
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	result = getInitializedResult();
	boolean oneFailed = false;
	compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor instanceof EjbEntityDescriptor) {
	    if(descriptor.getLocalHomeClassName() != null && !"".equals(descriptor.getLocalHomeClassName()))
		oneFailed = commonToBothInterfaces(descriptor.getLocalHomeClassName(),descriptor);	   
	    
	    if(oneFailed == false) {
		if(descriptor.getHomeClassName() != null && !"".equals(descriptor.getHomeClassName()))
		    oneFailed = commonToBothInterfaces(descriptor.getHomeClassName(),descriptor);	
	    }
	    if (oneFailed) {
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
     * @return boolean the results for this assertion i.e if a test has failed or not
     */

    private boolean commonToBothInterfaces(String home, EjbDescriptor descriptor) {
	boolean findByPrimaryKeyMethodFound = false;
	boolean oneFailed = false;
	try {
	    // retrieve the home interface methods
	    VerifierTestContext context = getVerifierContext();
		ClassLoader jcl = context.getClassLoader();
	    Class homeInterfaceClass = Class.forName(home, false, getVerifierContext().getClassLoader());
	    Method [] ejbFinderMethods = homeInterfaceClass.getDeclaredMethods();
	    for (int j = 0; j < ejbFinderMethods.length; j++) {
		if (ejbFinderMethods[j].getName().equals("findByPrimaryKey")) {
		    // Every entity enterprise Bean class must define the 
		    // findByPrimaryKey method. 
	
		    findByPrimaryKeyMethodFound = true;
			    
		    // report for this particular findByPrimaryKey(...)
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
					   "A findByPrimaryKey method was found."));
		    return oneFailed;
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
				       (getClass().getName() + ".failed",
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
