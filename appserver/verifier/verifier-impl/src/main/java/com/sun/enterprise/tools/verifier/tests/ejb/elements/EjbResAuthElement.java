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

package com.sun.enterprise.tools.verifier.tests.ejb.elements;

import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.util.Iterator;

/** 
 * The resource-ref element res-auth subelement must be "Application" or
 * "Container".
 */
public class EjbResAuthElement extends EjbTest implements EjbCheck { 



    /**
     * The resource-ref element res-auth subelement must be "Application" or
     * "Container".
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	boolean oneFailed = false;
	if (!descriptor.getResourceReferenceDescriptors().isEmpty()) {
	    for (Iterator itr = descriptor.getResourceReferenceDescriptors().iterator(); itr.hasNext();) {
		ResourceReferenceDescriptor nextResourceReference = (ResourceReferenceDescriptor) itr.next();
		// The resource-ref element res-auth subelement must be "Application" or
		// "Container".
		if ((nextResourceReference.getAuthorization().equals(ResourceReferenceDescriptor.APPLICATION_AUTHORIZATION)) ||
		    (nextResourceReference.getAuthorization().equals(ResourceReferenceDescriptor.CONTAINER_AUTHORIZATION))) {
		    result.addGoodDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
		    result.addGoodDetails
			(smh.getLocalString
			 (getClass().getName() + ".passed",
			  "Resource-ref element res-auth sub-element value [ {0} ] is valid within bean [ {1} ]",
			  new Object[] {nextResourceReference.getAuthorization(),descriptor.getName()}));
		} else {
		    oneFailed = true;
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.addErrorDetails
			(smh.getLocalString
			 (getClass().getName() + ".failed",
			  "Error: Resource-ref element res-auth sub-element value [ {0} ] is not valid within bean [ {1} ]",
			  new Object[] {nextResourceReference.getAuthorization(),descriptor.getName()}));
		} 
	    }
	    if (!oneFailed){
		result.setStatus(Result.PASSED);
	    } else {
		result.setStatus(Result.FAILED);
	    }
	} else {
	    result.addNaDetails(smh.getLocalString
				("tests.componentNameConstructor",
				 "For [ {0} ]",
				 new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no resource reference elements defined within this bean [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}


	return result;

    }
}
