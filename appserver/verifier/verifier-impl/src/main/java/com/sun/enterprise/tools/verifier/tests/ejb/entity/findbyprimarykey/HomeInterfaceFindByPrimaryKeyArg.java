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
 * Define findByPrimaryKey method parameter test.  
 *
 *     Every entity enterprise home interface must define the findByPrimaryKey 
 *     method. The method must declare the primary key class as the method 
 *     argument.
 * 
 */
public class HomeInterfaceFindByPrimaryKeyArg extends EjbTest implements EjbCheck { 
    Result result = null;
    ComponentNameConstructor compName = null;

    /** 
     * Define findByPrimaryKey method parameter test.  
     *
     *     Every entity enterprise home interface must define the findByPrimaryKey 
     *     method. The method must declare the primary key class as the method 
     *     argument.
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

	    if(descriptor.getHomeClassName() != null && !"".equals(descriptor.getHomeClassName()))
		oneFailed = commonToBothInterfaces(descriptor.getHomeClassName(),descriptor);
	    if(oneFailed == false) {
		if(descriptor.getLocalHomeClassName() != null && !"".equals(descriptor.getLocalHomeClassName()))
		    oneFailed = commonToBothInterfaces(descriptor.getLocalHomeClassName(),descriptor);
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
	Class [] ejbFinderMethodParameterTypes;
	boolean findByPrimaryKeyMethodFound = false;
	boolean foundOne = false;
	boolean oneFailed = false;
	boolean paramValid = false;
	boolean onlyOneParam = false;
	try {
	    // retrieve the home interface methods
	    VerifierTestContext context = getVerifierContext();
	    ClassLoader jcl = context.getClassLoader();
	    Class homeInterfaceClass = Class.forName(home, false, getVerifierContext().getClassLoader());
	    Method [] ejbFinderMethods = homeInterfaceClass.getDeclaredMethods();
	    
	    String primaryKeyType = ((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName();
	    
	    for (int j = 0; j < ejbFinderMethods.length; ++j) {
		// reset all booleans for next method within the loop
		paramValid = false;
		onlyOneParam = false;
		findByPrimaryKeyMethodFound = false;
		
		if (ejbFinderMethods[j].getName().equals("findByPrimaryKey")) {
		    // Every entity enterprise Bean class must define the
		    // findByPrimaryKey method. The result type for this method must
		    // be the primary key type (i.e. the findByPrimaryKey method
		    // must be a single-object finder).
		    findByPrimaryKeyMethodFound = true;
		    
		    ejbFinderMethodParameterTypes = ejbFinderMethods[j].getParameterTypes();
		    if (ejbFinderMethodParameterTypes.length == 1) {
			onlyOneParam = true;
			for (int k = 0; k < ejbFinderMethodParameterTypes.length; ++k) {
			    if (ejbFinderMethodParameterTypes[k].getName().equals(primaryKeyType)) {
				paramValid = true;
				break;
			    }
			}
		    } else {
			// should already be set...
			onlyOneParam = false;
			paramValid = false;
		    }
		    
		    // report for this particular findByPrimaryKey(...)
		    if (findByPrimaryKeyMethodFound && paramValid) {
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
					       "A findByPrimaryKey method with valid parameter type was found."));
			foundOne = true;
			break;
		    } else if (findByPrimaryKeyMethodFound && onlyOneParam && !paramValid) {
			result.addNaDetails(smh.getLocalString
					    ("tests.componentNameConstructor",
					     "For [ {0} ]",
					     new Object[] {compName.toString()}));
			result.addNaDetails(smh.getLocalString
					    (getClass().getName() + ".debug1",
					     "For home interface [ {0} ] Finder Method [ {1} ]",
					     new Object[] {homeInterfaceClass.getName(),ejbFinderMethods[j].getName()}));
			result.addNaDetails(smh.getLocalString
					    (getClass().getName() + ".notApplicable2",
					     "A findByPrimaryKey method was found, but with non-PrimaryKeyClass arg parameter type."));
		    } else if (findByPrimaryKeyMethodFound && !onlyOneParam) {
			result.addNaDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
			result.addNaDetails(smh.getLocalString
					    (getClass().getName() + ".debug1",
					     "For home interface [ {0} ] Finder Method [ {1} ]",
					     new Object[] {homeInterfaceClass.getName(),ejbFinderMethods[j].getName()}));
			result.addNaDetails(smh.getLocalString
					    (getClass().getName() + ".notApplicable1",
					     "A findByPrimaryKey method was found, but with non-single arg parameters."));
		    }			 
		}
	    }
	    if (!foundOne) {
		oneFailed = true;
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.addErrorDetails(smh.getLocalString
				       (getClass().getName() + ".debug3",
					"For home interface [ {0} ]",
					new Object[] {homeInterfaceClass.getName()}));
		result.addErrorDetails(smh.getLocalString
				       (getClass().getName() + ".failed",
					"Error: No single arg findByPrimaryKey(PrimaryKeyClass) method was found in home interface class [ {0} ].",
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
	    return oneFailed;
	}
	
    }
}
