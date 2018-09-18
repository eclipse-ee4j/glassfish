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

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.util.Iterator;
import java.util.logging.Level;


/**
 * The ejb-name element within the method element must be the name of one 
 * of the enterprise beans declared in the deployment descriptor.
 */
public class EjbNameMethodElement extends EjbTest implements EjbCheck { 


    /**
     * The ejb-name element within the method element must be the name of one 
     * of the enterprise beans declared in the deployment descriptor.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	// get ejb's methods

	// DOL doesn't save "ejb-name" element inside of method element
	// so i can't seem to get at raw representation of XML data needed
	// for this test, 
        // <ejb-name> within <method> element is the name of the ejb 
        // descriptor where you got the method descriptor from,
        // so you can't trip negative assertion and test should always pass
        // once plugged into new DOL, where access to raw XML is
        // available, then this test can be properly modified,
	// then i would use DOL similar to this:
	//Set methods = descriptor.getMethodDescriptors();

	//for (Iterator itr = methods.iterator(); itr.hasNext();) {

	//MethodDescriptor methodDescriptor = (MethodDescriptor) itr.next();

	boolean found = false;
	for (Iterator itr2 = 
		 descriptor.getEjbBundleDescriptor().getEjbs().iterator();
	     itr2.hasNext();) {
	    EjbDescriptor ejbDescriptor = (EjbDescriptor) itr2.next();
        logger.log(Level.FINE, getClass().getName() + ".debug1",
                new Object[] {ejbDescriptor.getName()});
	    
	    // now iterate over all methods to ensure that ejb-name exist
	    //if (methodDescriptor.getName().equals(ejbDescriptor.getName())) {

	    // for now, do this test, which should always pass, since DOL lacks
	    // raw XML data representation
            // <ejb-name> within <method> element is the name of the ejb
            // descriptor where you got the method descriptor from
	    if (descriptor.getName().equals(ejbDescriptor.getName())) {
		found = true;
		if (result.getStatus() != Result.FAILED){
		    result.setStatus(Result.PASSED);
		    // for now, pass in details string via addGoodDetails
		    // until DOL raw data issue gets resolved
		    result.addGoodDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
		    result.addGoodDetails
			(smh.getLocalString
			 (getClass().getName() + ".passed",
			  "[ {0} ] is valid and contained within jar.",
			  new Object[] {descriptor.getName()}));
		}
	    }
	}
	if (!found) {
	    result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
	    result.addErrorDetails(smh.getLocalString
			("tests.componentNameConstructor",
			"For [ {0} ]",
			new Object[] {compName.toString()}));
		result.failed
		(smh.getLocalString
		 (getClass().getName() + ".failed",
		  "Error: [ {0} ] is not the name of one of the EJB's within jar.",
		  new Object[] {descriptor.getName()}));
	    //(methodDescriptor.getName() pending DOL update
	}
	//}
	return result;

    }

}
