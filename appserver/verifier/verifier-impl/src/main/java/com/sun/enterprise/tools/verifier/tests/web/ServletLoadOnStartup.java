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
 * The load-on-startup element contains an integer indicating the order
 * in which the servlet should be loaded. 
 */
public class ServletLoadOnStartup extends WebTest implements WebCheck { 

      
    /**
     * The load-on-startup element contains an integer indicating the order
     * in which the servlet should be loaded. 
     * 
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
  

	boolean oneFailed = false;
	if (!descriptor.getServletDescriptors().isEmpty()) {
	    for (Iterator itr = descriptor.getServletDescriptors().iterator();
		 itr.hasNext();) {

		WebComponentDescriptor nextServletDescriptor = (WebComponentDescriptor) itr.next();
		// DOL only allows int's to be stored, test will always pass as written, so need to check against -1 placeholder
		Integer loadOnStartUp = new Integer(nextServletDescriptor.getLoadOnStartUp());
		if (loadOnStartUp.intValue() >= 0) {
		    // DOL needs to store string value representing load-on-startup value
		    result.addGoodDetails
			(smh.getLocalString
			 (getClass().getName() + ".passed",
			  "load-on-startup [ {0} ] value found in [ {1} ]",
			  new Object[] {loadOnStartUp,nextServletDescriptor.getName()}));
		} else {
                    if (!oneFailed) {
	                oneFailed = true;
                    }
		    result.addErrorDetails
			(smh.getLocalString
			 (getClass().getName() + ".failed",
			  "Error: load-on-startup [ {0} ] invalid value found in [ {1} ]",
			  new Object[] {loadOnStartUp,nextServletDescriptor.getName()}));
		}
	    }
	    if (oneFailed) {
	        result.setStatus(Result.FAILED);
    	    } else {
	        result.setStatus(Result.PASSED);
	    }
	} else {
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no servlets within this web archive [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}

	return result;
    }
}
