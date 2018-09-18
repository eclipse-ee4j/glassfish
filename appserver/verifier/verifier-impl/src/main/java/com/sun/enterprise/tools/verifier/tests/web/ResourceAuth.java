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
import com.sun.enterprise.deployment.ResourceReferenceDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;


/**
 *  Must check for the correct authorization.
 *  @author Arun Jain
 */
public class ResourceAuth  extends WebTest implements WebCheck{
    

     /** 
     * Resource authority exists test.  Must be Container or Servlet.
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

        Set resourceRefs;
        ResourceReferenceDescriptor resrefDes;
        Iterator itr;
        String resAuth;
	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean oneFailed = false;
        
	if (!descriptor.getResourceReferenceDescriptors().isEmpty()) {
            
	    // get the Resource Reference Descriptors set
	    resourceRefs = descriptor.getResourceReferenceDescriptors();
	    itr = resourceRefs.iterator();
	    // test the Resource Reference Descriptor
	    while (itr.hasNext()) {
		resrefDes = (ResourceReferenceDescriptor)itr.next();
		resAuth = resrefDes.getAuthorization();
                if (resAuth.equals("Application") || resAuth.equals("Container")) {
		    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.addGoodDetails(smh.getLocalString
                                       (getClass().getName() + ".passed",
                                    "Resource reference has valid authorization."));
                }

                else {
                    oneFailed = true;
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addErrorDetails(smh.getLocalString
                                       (getClass().getName() + ".failed",
                                    "Error: Resource reference auth has to be either Container or Application."));

                }
            }
            
        } else {
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
                                 (getClass().getName() + ".notApplicable",
                                  "There are no resource references defined within the web archive [ {0} ]",
                                  new Object[] {descriptor.getName()}));
        }
        if (oneFailed) {
            result.setStatus(Result.FAILED);
        } else {
            result.setStatus(Result.PASSED);
        }
	return result;
    }
}
