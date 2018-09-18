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
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

/**
 * Web name test.
 * The Web provider must assign a display-name to each web application 
 * 
 */
public class WebName extends WebTest implements WebCheck { 

      
    /** 
     * Web name test.
     * The Web provider must assign a display-name to each web application
     *
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
	String webName = descriptor.getName();

	if (webName.length() > 0) {
	    // as long as it's not blank, test should pass
	    result.addGoodDetails(smh.getLocalString
				  ("tests.componentNameConstructor",
				   "For [ {0} ]",
				   new Object[] {compName.toString()}));	
	    result.passed
	        (smh.getLocalString
	         (getClass().getName() + ".passed", 
	          "Web-App display name is : [ {0} ]", 
	          new Object [] {webName}));
	} else {
	    // it's blank, test should be N/A since <display-name> is optional
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable
	        (smh.getLocalString
	         (getClass().getName() + ".failed", 
	          "Not Applicable: Web-App display name is not defined."));
	} 
	return result;
    }
}
