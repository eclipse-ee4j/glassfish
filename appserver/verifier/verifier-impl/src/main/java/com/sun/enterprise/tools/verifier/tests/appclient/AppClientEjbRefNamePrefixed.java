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

package com.sun.enterprise.tools.verifier.tests.appclient;

import java.util.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;

/** 
 * The application client ejb-ref-name element contains the name of an EJB 
 * reference. The EJB reference is an entry in the enterprise bean's 
 * environment.  It is recommended that name is prefixed with "ejb/".
 */
public class AppClientEjbRefNamePrefixed extends AppClientTest implements AppClientCheck { 


    /** 
     * The application client ejb-ref-name element contains the name of an EJB 
     * reference. The EJB reference is an entry in the enterprise bean's 
     * environment.  It is recommended that name is prefixed with "ejb/".
     *
     * @param descriptor the app-client deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(ApplicationClientDescriptor descriptor) {
	Result result = getInitializedResult();
ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	boolean oneWarning = false;
	if (!descriptor.getEjbReferenceDescriptors().isEmpty()) {
	    for (Iterator itr = descriptor.getEjbReferenceDescriptors().iterator();
		 itr.hasNext();) {
		EjbReferenceDescriptor nextEjbReference = (EjbReferenceDescriptor) itr.next();
		String ejbRefName = nextEjbReference.getName();

		if (ejbRefName.startsWith("ejb/")) {
		    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addGoodDetails
			(smh.getLocalString
			 (getClass().getName() + ".passed",
			  "[ {0} ] is prefixed with recommended string \"ejb/\" within application client [ {1} ]",
			  new Object[] {ejbRefName,descriptor.getName()}));
		}  else {
		    if (!oneWarning) {
			oneWarning = true;
		    }
		    result.addWarningDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addWarningDetails
			(smh.getLocalString
			 (getClass().getName() + ".warning",
			  "Warning: [ {0} ] is not prefixed with recommended string \"ejb/\" within application client [ {1} ]",
			  new Object[] {ejbRefName,descriptor.getName()}));
		}
	    }
	    if (oneWarning) {
		result.setStatus(Result.WARNING);
	    } else {
		result.setStatus(Result.PASSED);
	    }
	} else {
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no ejb references to other beans within this application client [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}
	return result;
    }
}
