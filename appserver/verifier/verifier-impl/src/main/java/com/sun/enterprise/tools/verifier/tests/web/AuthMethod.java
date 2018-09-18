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

import com.sun.enterprise.tools.verifier.tests.web.WebTest;
import java.util.*;
import java.io.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;


/** The auth-method element is used to configure the authentication mechanism 
 * for the web application. As a prerequisite to gaining access to any web 
 * resources which are protected by an authorization constraint, a user must 
 * have authenticated using the configured mechanism. Legal values for this 
 * element are "BASIC", "DIGEST", "FORM", or "CLIENT-CERT". 
 */
public class AuthMethod extends WebTest implements WebCheck { 

    
    /** The auth-method element is used to configure the authentication mechanism 
     * for the web application. As a prerequisite to gaining access to any web 
     * resources which are protected by an authorization constraint, a user must 
     * have authenticated using the configured mechanism. Legal values for this 
     * element are "BASIC", "DIGEST", "FORM", or "CLIENT-CERT". 
     *
     * @param descriptor the Web deployment descriptor 
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor.getLoginConfiguration() != null) {
	    boolean foundIt = false;
	    boolean na = false;
	    String authMethod = descriptor.getLoginConfiguration().getAuthenticationMethod();
	    if (authMethod.length() > 0) {
		// use hard-coded strings until DOL gets updated
		if ((authMethod.equals("BASIC")) || 
		    (authMethod.equals("FORM")) || 
		    (authMethod.equals("CLIENT-CERT")) || 
		    (authMethod.equals("DIGEST"))) {
		    foundIt = true;
		} else {
		    foundIt = false;
		}
	    } else {
		na = true;
	    }
     
	    if (foundIt) {
		result.addGoodDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
					
		result.passed(smh.getLocalString
			      (getClass().getName() + ".passed",
			       "The auth-method [ {0} ] is legal value within web application [ {1} ]",
			       new Object[] {authMethod, descriptor.getName()}));
	    } else if (na) {
		result.addNaDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
	        result.notApplicable(smh.getLocalString
			 (getClass().getName() + ".notApplicable",
			  "There are no auth-method elements within the web archive [ {0} ]",
			  new Object[] {descriptor.getName()}));
	    } else {
		result.addErrorDetails(smh.getLocalString
			("tests.componentNameConstructor",
			"For [ {0} ]",
			new Object[] {compName.toString()}));
		result.failed(smh.getLocalString
			      (getClass().getName() + ".failed",
			       "Error: The auth-method [ {0} ] is not legal value within web application [ {1} ].  It must be either [ {2} ], [ {3} ], [ {4} ] or [ {5} ].",
			       new Object[] {authMethod, descriptor.getName(),"BASIC","FORM","CLIENT-CERT","DIGEST"}));
	    }
	} else {
	    result.addNaDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no auth-method elements within the web archive [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}

	return result;
    }
}
