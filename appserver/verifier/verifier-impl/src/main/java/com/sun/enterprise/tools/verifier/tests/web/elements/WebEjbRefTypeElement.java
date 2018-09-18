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

package com.sun.enterprise.tools.verifier.tests.web.elements;

import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.web.WebCheck;
import com.sun.enterprise.tools.verifier.tests.web.WebTest;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.util.Iterator;

/** 
 * The web archive ejb-ref-type element must be one of the following:
 *   Entity
 *   Session
 */
public class WebEjbRefTypeElement extends WebTest implements WebCheck { 


    /** 
     * The web archive ejb-ref-type element must be one of the following:
     *   Entity
     *   Session
     *
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	boolean failed = false;

	// The web archive ejb-ref-type element must be one of the following:
	//  Entity
	//  Session
	if (!descriptor.getEjbReferenceDescriptors().isEmpty()) {
	    for (Iterator itr = descriptor.getEjbReferenceDescriptors().iterator(); 
		 itr.hasNext();) {
		EjbReferenceDescriptor nextEjbReference = (EjbReferenceDescriptor) itr.next();
		String ejbRefTypeStr = nextEjbReference.getType();
		if (!((ejbRefTypeStr.equals(EjbSessionDescriptor.TYPE)) ||
		      (ejbRefTypeStr.equals(EjbEntityDescriptor.TYPE)))) {
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
				  (getClass().getName() + ".failed",
				   "Error: ejb-ref-type [ {0} ] within \n web archive [ {1} ] is not valid.  \n Must be [ {2} ] or [ {3} ]",
				   new Object[] {ejbRefTypeStr,descriptor.getName(),EjbEntityDescriptor.TYPE,EjbSessionDescriptor.TYPE}));
		    failed = true;
		}
	    }
	} else {
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no ejb references to other beans within this web archive [ {0} ]",
				  new Object[] {descriptor.getName()}));
	    return result;
	}

	if (failed)
	    {
		result.setStatus(Result.FAILED);
	    } else {
		result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));	
		result.passed
		    (smh.getLocalString
		     (getClass().getName() + ".passed",
		      "All ejb-ref-type elements are valid.  They are all [ {0} ] or [ {1} ] within this web archive [ {2} ]",
		      new Object[] {EjbEntityDescriptor.TYPE,EjbSessionDescriptor.TYPE,descriptor.getName()}));
	    } 
	return result;
    }
}
