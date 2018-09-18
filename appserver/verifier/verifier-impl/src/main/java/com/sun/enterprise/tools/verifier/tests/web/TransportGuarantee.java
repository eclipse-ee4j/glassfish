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

package com.sun.enterprise.tools.verifier.tests.web;

import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;
import org.glassfish.web.deployment.descriptor.SecurityConstraintImpl;
import org.glassfish.web.deployment.descriptor.UserDataConstraintImpl;

/** 
 * The transport-guarantee element specifies that the communication between 
 * client and server should be "SECURE", "NONE", or "CONFIDENTIAL". 
 */
public class TransportGuarantee extends WebTest implements WebCheck { 

    
    /**
     * The transport-guarantee element specifies that the communication between 
     * client and server should be "SECURE", "NONE", or "CONFIDENTIAL". 
     *
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor.getSecurityConstraints().hasMoreElements()) {
	    boolean oneFailed = false;
	    boolean foundIt = false;
	    int na = 0;
	    int noSc = 0;
	    // get the errorpage's in this .war
	    for (Enumeration e = descriptor.getSecurityConstraints() ; e.hasMoreElements() ;) {
		foundIt = false;
                noSc++;
		SecurityConstraintImpl securityConstraintImpl = (SecurityConstraintImpl) e.nextElement();
		UserDataConstraintImpl userDataConstraint = (UserDataConstraintImpl) securityConstraintImpl.getUserDataConstraint();
		if (userDataConstraint != null) {
                    String transportGuarantee = userDataConstraint.getTransportGuarantee(); 
		    if (transportGuarantee.length() > 0) {
		        if ((transportGuarantee.equals("NONE")) ||
			    (transportGuarantee.equals("INTEGRAL")) ||
			    (transportGuarantee.equals("CONFIDENTIAL"))) {
			    foundIt = true;
		        } else {
			    foundIt = false;
		        }
		    } else {
		        foundIt = false;
		    }
         
		    if (foundIt) {
			result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		        result.addGoodDetails(smh.getLocalString
					      (getClass().getName() + ".passed",
					       "transport-guarantee [ {0} ] specifies that the communication between client and server should be one of \"SECURE\", \"NONE\", or \"CONFIDENTIAL\" within web application [ {1} ]",
					       new Object[] {transportGuarantee, descriptor.getName()}));
		    } else {
		        if (!oneFailed) {
			    oneFailed = true;
		        }
			result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		        result.addErrorDetails(smh.getLocalString
					       (getClass().getName() + ".failed",
					        "Error: transport-guarantee [ {0} ] does not specify that the communication between client and server is one of \"SECURE\", \"NONE\", or \"CONFIDENTIAL\" within web application [ {1} ]",
					        new Object[] {transportGuarantee, descriptor.getName()}));
		    }
	        } else {
		    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	            result.addNaDetails(smh.getLocalString
			 (getClass().getName() + ".notApplicable1",
			  "There are no transport-guarantee elements within the web application [ {0} ]",
			  new Object[] {descriptor.getName()}));
                    na++;
	        }
	    }
	    if (oneFailed) {
		result.setStatus(Result.FAILED);
	    } else if (na == noSc) {
		result.setStatus(Result.NOT_APPLICABLE);
	    } else {
		result.setStatus(Result.PASSED);
	    }
	} else {result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no transport-guarantee elements within the web archive [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}

	return result;
    }
}
