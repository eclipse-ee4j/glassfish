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


/**
 *  Param value  should not be empty string
 *  @author Arun Jain
 */
public class ParamValue extends WebTest implements WebCheck {
    

     /** 
     * 
     * Param value should not be empty string
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

        Set contextParameters;
        Iterator itr;
        String epValue;
	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        EnvironmentProperty ep;
        boolean passed = true;
        boolean oneFailed = false;
        boolean notApp = false;
        
	if (!descriptor.getContextParametersSet().isEmpty()) {
            
	    // get the context parameters
	    contextParameters = descriptor.getContextParametersSet();
	    itr = contextParameters.iterator();
	    // test the environment property
	    while (itr.hasNext()) {
		ep = (EnvironmentProperty)itr.next();
		epValue = ep.getValue();
                if ( epValue.length() != 0) {
		    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                    result.addGoodDetails(smh.getLocalString
                                          (getClass().getName() + ".passed",
                                           "Param named/value exists for in the web archive [ {0} ].",
                                           new Object[] {descriptor.getName()}));                              
                } else {
                    oneFailed = true;
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                    result.addErrorDetails(smh.getLocalString
                                           (getClass().getName() + ".failed",
                                            "Error: Param name/value should not be empty strings"));
                    
                }
            } 
            
        } else {
            notApp = true;
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
                                 (getClass().getName() + ".notApplicable",
                                  "There are no context parameters within the web archive [ {0} ]",
                                  new Object[] {descriptor.getName()}));
        }
        if (oneFailed) {
            result.setStatus(Result.FAILED);
        } else if (notApp) {
            result.setStatus(Result.NOT_APPLICABLE);
        } else {
            result.setStatus(Result.PASSED);
        }
	return result;
    }
}
