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
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.tools.verifier.tests.*;
import org.glassfish.web.deployment.descriptor.ServletFilterDescriptor;

/**
 *  
 *  @author      Arun Jain
 */
public class FilterInitParamName extends WebTest implements WebCheck {
    
     /** 
     *  Filter Param Name exists test.
     * 
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean oneFailed = false;

        Enumeration filterEnum = descriptor.getServletFilterDescriptors().elements();
        
    	if (filterEnum.hasMoreElements()) {        
            
	    // get the filters in this .war
	    while (filterEnum.hasMoreElements()) {
		ServletFilterDescriptor filter = (ServletFilterDescriptor)filterEnum.nextElement();
                HashSet<String> envSet = new HashSet<String>(); 
                Vector epVector = filter.getInitializationParameters();              
                for ( int i = 0; i < epVector.size(); i++) {
                    
                    EnvironmentProperty ep = (EnvironmentProperty)epVector.elementAt(i);
                    String epName = ep.getName();
                    
                    if (epName.length() == 0) {
                        oneFailed = true;
			result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));

                        result.addErrorDetails(smh.getLocalString
                                               (getClass().getName() + ".failed1",
                                                "Error: Param name/value entry should of finite length."));
                    }
                    else {
                        // Do duplicate name test.
                        if (!envSet.contains(epName)) {                        
                            envSet.add(epName);
                        }  else {
                            oneFailed = true;
			    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));

                            result.addErrorDetails(smh.getLocalString
                                               (getClass().getName() + ".failed2",
                                                "Error: Duplicate param names are not allowed."));
                        }
                    }
                }
            }
            if (oneFailed) {
                result.setStatus(Result.FAILED);
            } else {
		result.addGoodDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
					
		result.passed(smh.getLocalString
                                 (getClass().getName() + ".passed",
                                  "All init parameter names are unique"));                
            }            
        } else {
	    result.addNaDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
            result.notApplicable(smh.getLocalString
                                 (getClass().getName() + ".notApplicable",
                                  "There are no initialization parameters for the filter within the web archive [ {0} ]",
                                  new Object[] {descriptor.getName()}));
        }        
        return result;
    }
    
}

