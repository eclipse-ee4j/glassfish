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
 * The ejb element specifies the URI of a ejb-jar, relative to
 *  the top level of the application package.
 *
 */

public class EjbURI extends ApplicationTest implements AppCheck { 


    /** The ejb element specifies the URI of a ejb-jar, relative to
     *  the top level of the application package.
     *
     * @param descriptor the Application deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(Application descriptor) {

	Result result = getInitializedResult();

  
	if (descriptor.getBundleDescriptors(EjbBundleDescriptor.class).size() > 0) {
	    boolean oneFailed = false;
	    for (Iterator itr = descriptor.getBundleDescriptors(EjbBundleDescriptor.class).iterator(); itr.hasNext();) {
		EjbBundleDescriptor ejbd = (EjbBundleDescriptor) itr.next();
    

		// not sure what we can do to test this string?
		if (ejbd.getModuleDescriptor().getArchiveUri().endsWith(".jar")) {
		    result.passed
			(smh.getLocalString
			 (getClass().getName() + ".passed",
			  "[ {0} ] specifies the URI [ {1} ] of an ejb-jar, relative to the top level of the application package [ {2} ].",
			  new Object[] {ejbd.getName(), ejbd.getModuleDescriptor().getArchiveUri(), descriptor.getName()}));
		} else {
		    if (!oneFailed) {
			oneFailed =true;
		    }
		    result.addErrorDetails
			(smh.getLocalString
			 (getClass().getName() + ".failed",
			  "Error: [ {0} ] does not specify the URI [ {1} ] of an ejb-jar, relative to the top level of the application package [ {2} ], or does not end with \".jar\"",
			  new Object[] {ejbd.getName(), ejbd.getModuleDescriptor().getArchiveUri(), descriptor.getName()}));
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
				  "There are no ejb components in application [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}

	return result;
    }
}
