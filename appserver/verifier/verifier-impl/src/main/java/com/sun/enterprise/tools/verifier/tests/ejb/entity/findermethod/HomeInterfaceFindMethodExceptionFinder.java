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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.findermethod;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbUtils;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.lang.reflect.Method;

/** 
 * Entity beans home interface find<METHOD> method throws 
 * javax.ejb.FinderException test.
 * 
 * The following are the requirements for the enterprise Bean's home interface 
 * find<METHOD> signature: 
 * 
 * An Entity Bean's home interface defines one or more find<METHOD>(...) 
 * methods. 
 * 
 * The throws clause must include javax.ejb.FinderException. 
 */
public class HomeInterfaceFindMethodExceptionFinder extends EjbTest implements EjbCheck { 
    Result result = null;
    ComponentNameConstructor compName = null;

    /**
     * Entity beans home interface find<METHOD> method throws 
     * javax.ejb.FinderException test.
     * 
     * The following are the requirements for the enterprise Bean's home interface 
     * find<METHOD> signature: 
     * 
     * An Entity Bean's home interface defines one or more find<METHOD>(...) 
     * methods. 
     * 
     * The throws clause must include javax.ejb.FinderException. 
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
	    String persistence =
		((EjbEntityDescriptor)descriptor).getPersistenceType();
	    if (EjbEntityDescriptor.BEAN_PERSISTENCE.equals(persistence)) {
		if(descriptor.getHomeClassName() != null && !"".equals(descriptor.getHomeClassName())) {
		    oneFailed = commonToBothInterfaces(descriptor.getHomeClassName(),descriptor);
		}
		if(oneFailed == false) {
		    if(descriptor.getLocalHomeClassName() != null && !"".equals(descriptor.getLocalHomeClassName())) {
			oneFailed = commonToBothInterfaces(descriptor.getLocalHomeClassName(),descriptor);
		    }
		}
		if (oneFailed) {
		    result.setStatus(result.FAILED);
		} else {
		    result.setStatus(result.PASSED);
		}
		return result;

	    } else { //if (CONTAINER_PERSISTENCE.equals(persistence))
		result.addNaDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     (getClass().getName() + ".notApplicable2",
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

   /** 
     * This method is responsible for the logic of the test. It is called for both local and remote interfaces.
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @param home for the Home interface of the Ejb. 
     * @return boolean the results for this assertion i.e if a test has failed or not
     */

    private boolean commonToBothInterfaces(String home, EjbDescriptor descriptor) {
	boolean oneFailed = false;
		// RULE: Entity home interface are only allowed to have find<METHOD> 
		//       methods which must throw javax.ejb.FinderException
		try {
		    VerifierTestContext context = getVerifierContext();
		ClassLoader jcl = context.getClassLoader();
		    Class c = Class.forName(home, false, getVerifierContext().getClassLoader());
		    Method methods[] = c.getDeclaredMethods();
		    Class [] methodExceptionTypes;
		    boolean throwsFinderException = false;
  
		    for (int i=0; i< methods.length; i++) {
			// clear these from last time thru loop
			throwsFinderException = false;
			if (methods[i].getName().startsWith("find")) {
			    // reset from last time thru loop
			    throwsFinderException = false;
			    methodExceptionTypes = methods[i].getExceptionTypes();
                 
			    // methods must throw javax.ejb.FinderException
			    if (EjbUtils.isValidFinderException(methodExceptionTypes)) {
				throwsFinderException = true;
			    }
  
			    // report for this particular find<METHOD> method found in home 
			    // interface , now display the appropriate results for this 
			    // particular find<METHOD> method
			    if (throwsFinderException ) {
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
						       "The [ {0} ] method which must throw javax.ejb.FinderException was found.",
						       new Object[] {methods[i].getName()}));
			    } else if (!throwsFinderException) {
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
							"Error: A [ {0} ] method was found, but did not throw javax.ejb.FinderException.",
							new Object[] {methods[i].getName()}));
			    }  // end of reporting for this particular 'find' method
			} // if the home interface found a "find" method
		    } // for all the methods within the home interface class, loop
		    return oneFailed;
  
		} catch (ClassNotFoundException e) {
		    Verifier.debug(e);
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
				  (getClass().getName() + ".failedException",
				   "Error: Home interface [ {0} ] does not exist or is not loadable within bean [ {1} ]",
				   new Object[] {home, descriptor.getName()}));
		    return oneFailed;
		}
  
    }
}
