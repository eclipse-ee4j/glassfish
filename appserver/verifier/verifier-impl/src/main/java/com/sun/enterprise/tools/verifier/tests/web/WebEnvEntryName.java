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
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

/** 
 * The environment entry name must be of finite length.
 */
public class WebEnvEntryName extends WebTest implements WebCheck { 


    /** 
     * The environment entry name must be of finite length.
     *
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean oneFailed = false;
        EnvironmentProperty nextEnvironmentProperty;
        String envName;
	if (!descriptor.getEnvironmentProperties().isEmpty()) {

	    for (Iterator itr = descriptor.getEnvironmentProperties().iterator(); 
		 itr.hasNext();) {
		nextEnvironmentProperty = (EnvironmentProperty) itr.next();
                envName = nextEnvironmentProperty.getName();
		if (envName.length() > 0) {
		    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addGoodDetails
			(smh.getLocalString
			 (getClass().getName() + ".passed",
			  "Environment entry value [ {0} ] has valid name within web archive [ {1} ]",
			  new Object[] {nextEnvironmentProperty.getName(),descriptor.getName()}));
		} else {
		    oneFailed = true;
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addErrorDetails
			(smh.getLocalString
			 (getClass().getName() + ".failed",
			  "Error: Environment entry name must be of finite length"));
		} 
	    }
	    if (!oneFailed){
		result.setStatus(Result.PASSED);
	    } else {
		result.setStatus(Result.FAILED);
	    }
	} else {
            result.setStatus(Result.NOT_APPLICABLE);
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no environment entry elements defined within this web archive [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}

	return result;
    }
}
