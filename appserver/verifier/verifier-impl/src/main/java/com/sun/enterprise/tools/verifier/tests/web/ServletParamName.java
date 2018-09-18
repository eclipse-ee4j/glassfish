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
import com.sun.enterprise.deployment.WebComponentDescriptor;

/**
 *  
 *  @author      Arun Jain
 */
public class ServletParamName extends WebTest implements WebCheck {
    

     /** 
     *  Servlet Param Name exists test.
     * 
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

        Set servlets;
        Iterator servItr;
        String epName = null;
	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        WebComponentDescriptor servlet = null;
        Enumeration en;
        EnvironmentProperty ep = null;
        boolean oneFailed = false;
        boolean duplicate = false;
        
	if (!descriptor.getServletDescriptors().isEmpty()) {
            
	    // get the servlets in this .war
	    servlets = descriptor.getServletDescriptors();
	    servItr = servlets.iterator();
	    // test the servlets in this .war
	    while (servItr.hasNext()) {
		servlet = (WebComponentDescriptor)servItr.next();
                HashSet<String> envSet = new HashSet<String>();            
                for ( en = servlet.getInitializationParameters(); en.hasMoreElements();) {
                    ep = (EnvironmentProperty)en.nextElement();
                    epName = ep.getName();
                    
                    if (epName.length() != 0) {
                        // Do duplicate name test.
                        duplicate = checkDuplicate(epName, envSet);
                        
                    } else {
                        oneFailed = true;
			result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                        result.addErrorDetails(smh.getLocalString
                                               (getClass().getName() + ".failed",
                                                "Error: Param name/value entry should of finite length."));
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
            }
	    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
            result.addGoodDetails(smh.getLocalString
                                  (getClass().getName() + ".passed",
                                   "Param named/value exists for in the servlet [ {0} ].",
                                   new Object[] {servlet.getName()}));          
            
        } else {
            result.setStatus(Result.NOT_APPLICABLE);
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
            result.notApplicable(smh.getLocalString
                                 (getClass().getName() + ".notApplicable",
                                  "There are no initialization parameters for the servlet within the web archive [ {0} ]",
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

