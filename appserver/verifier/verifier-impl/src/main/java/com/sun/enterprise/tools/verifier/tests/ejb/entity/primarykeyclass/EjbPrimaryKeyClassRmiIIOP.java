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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.primarykeyclass;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import com.sun.enterprise.tools.verifier.tests.ejb.RmiIIOPUtils;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

/** 
 * Define primary key class which must be legal Value Type in RMI-IIOP test.  
 *
 * Enterprise Bean's primary key class 
 * The Bean provider must specify a primary key class in the deployment 
 * descriptor. The primary key class must be a legal Value Type in RMI-IIOP. 
 *
 */
public class EjbPrimaryKeyClassRmiIIOP extends EjbTest implements EjbCheck { 


    /** 
     * Define primary key class which must be legal Value Type in RMI-IIOP test.  
     *
     * Enterprise Bean's primary key class 
     * The Bean provider must specify a primary key class in the deployment 
     * descriptor. The primary key class must be a legal Value Type in RMI-IIOP. 
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor instanceof EjbEntityDescriptor) {
	    boolean isLegalRMIIIOPValueType = false;
	    boolean oneFailed = false;
  
	    // retrieve the EJB primary key class 
	    String primaryKeyType = ((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName();
  	  
	    if (!primaryKeyType.equals("")) {
		try {
		    VerifierTestContext context = getVerifierContext();
		    ClassLoader jcl = context.getClassLoader();
		    Class c = Class.forName(((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName(), false, getVerifierContext().getClassLoader());
		    if (RmiIIOPUtils.isValidRmiIIOPValueType(c)) {
			// this is the right primary key class 
			isLegalRMIIIOPValueType = true;
		    } // return valid
  
		    if (isLegalRMIIIOPValueType) {
			result.addGoodDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
			result.addGoodDetails(smh.getLocalString
					      (getClass().getName() + ".debug1",
					       "For EJB primary key class [ {0} ]",
					       new Object[] {primaryKeyType}));
			result.addGoodDetails(smh.getLocalString
					      (getClass().getName() + ".passed",
					       "A primary key class which must be legal Value Type in RMI-IIOP was defined in the deployment descriptor."));
		    } else if (!isLegalRMIIIOPValueType) {
			oneFailed = true;
			result.addErrorDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
			result.addErrorDetails(smh.getLocalString
					       (getClass().getName() + ".debug1",
						"For EJB primary key class [ {0} ]",
						new Object[] {primaryKeyType}));
			result.addErrorDetails(smh.getLocalString
					       (getClass().getName() + ".failed",
						"Error: A primary key class which must be legal Value Type in RMI-IIOP was not defined in the deployment descriptor."));
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
				   new Object[] {primaryKeyType}));
		    oneFailed = true;
		}
	    } else {
		oneFailed = true;
		result.addErrorDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
		result.addErrorDetails(smh.getLocalString
				       (getClass().getName() + ".debug1",
					"For EJB primary key class [ {0} ]",
					new Object[] {primaryKeyType}));
		result.addErrorDetails(smh.getLocalString
				       (getClass().getName() + ".failed1",
					"Error: A primary key class was not defined in the deployment descriptor."));
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
}
