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

package com.sun.enterprise.tools.verifier.tests.app;

import com.sun.enterprise.tools.verifier.tests.app.ApplicationTest;
import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;

/** 
 * All web modules in the application have a non-blank context-root 
 */

public class AppWebContext extends ApplicationTest implements AppCheck { 


    /** 
     * All web modules in the application have a non-blank context-root
     *
     * @param descriptor the Application deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(Application descriptor) {

	Result result = getInitializedResult();

  
	if (descriptor.getBundleDescriptors(WebBundleDescriptor.class).size() > 0) {
	    boolean oneWarning = false;
	    for (Iterator itr = descriptor.getBundleDescriptors(WebBundleDescriptor.class).iterator(); itr.hasNext();) {
		WebBundleDescriptor wbd = (WebBundleDescriptor) itr.next();
		if (wbd.getContextRoot().equals("")) {
		    // fail test can't be blank , 
		    if (!oneWarning) {
			oneWarning =true;
		    }
		    result.addWarningDetails
			(smh.getLocalString
			 (getClass().getName() + ".warning",
			  "Warning: [ {0} ] has blank context root defined within application [ {1} ]",
			  new Object[] {wbd.getName(), descriptor.getName()}));
		} else {
		    result.addGoodDetails
			(smh.getLocalString
			 (getClass().getName() + ".passed",
			  "[ {0} ] has context root defined as [ {1} ] within application[ {2} ].",
			  new Object[] {wbd.getName(), wbd.getContextRoot(), descriptor.getName()}));
		}
	    }

	    if (oneWarning) {
		result.setStatus(Result.WARNING);
	    } else {
		result.setStatus(Result.PASSED);
	    }
	} else {
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no web components in application [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}
  
	return result;
    }
}
