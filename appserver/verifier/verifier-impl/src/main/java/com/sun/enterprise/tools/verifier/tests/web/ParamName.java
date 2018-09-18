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
 *  Check the context param's name.  It should not be duplicate.
 *  @author Arun Jain
 */
public class ParamName extends WebTest implements WebCheck {
    

     /** 
     * Param name should not be duplicate.
     *      
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

        String epName;
	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        Enumeration en;
        HashSet<String> envSet = new HashSet<String>();
        EnvironmentProperty ep;
        boolean oneFailed = false;
        boolean duplicate = false;
        
	if (!descriptor.getContextParametersSet().isEmpty()) {
            
	    // get the context parameters
            for ( en = descriptor.getContextParameters(); en.hasMoreElements(); ) {
                ep = (EnvironmentProperty)en.nextElement();
                epName = ep.getName();
                if (epName.length() != 0)
                    duplicate = checkDuplicate(epName, envSet);
                else {
                    oneFailed = true;
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                    result.addErrorDetails(smh.getLocalString
                                           (getClass().getName() + ".failed",
                                            "Error: Param name/value entry should not be empty strings."));
                }
                if ( !duplicate) {
                    envSet.add(epName);
                }
                else {
                    oneFailed = true;
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                    result.addErrorDetails(smh.getLocalString
                                           (getClass().getName() + ".failed",
                                            "Error: Duplicate param names are not allowed."));
                }
            } 
	    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
            result.addGoodDetails(smh.getLocalString
                                  (getClass().getName() + ".passed",
                                   "Param named/value exists for in the web app [ {0} ].",
                                   new Object[] {descriptor.getName()}));                
            
        } else {
            result.setStatus(Result.NOT_APPLICABLE);
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
                                 (getClass().getName() + ".notApplicable",
                                  "There are no context parameters within the web archive [ {0} ]",
                                  new Object[] {descriptor.getName()}));
            return result;
        }
        if (oneFailed) {
            result.setStatus(Result.FAILED);
        } else {
            result.setStatus(Result.PASSED);
        }
	return result;
    }
    private boolean checkDuplicate(String epName, HashSet theSet) {
        
             return theSet.contains(epName);
    }
    
}

