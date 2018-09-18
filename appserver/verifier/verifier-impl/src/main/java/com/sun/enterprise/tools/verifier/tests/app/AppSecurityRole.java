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
import java.io.*;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.security.acl.*;
import com.sun.enterprise.tools.verifier.*;
import org.glassfish.security.common.Role;


/** 
 * The Application role-name element contains the name of a security role.
 */
public class AppSecurityRole extends ApplicationTest implements AppCheck { 

    

    /** 
     * The Application role-name element contains the name of a security role.
     *
     * @param descriptor the Application deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(Application descriptor) {

	Result result = getInitializedResult();


	if (!descriptor.getRoles().isEmpty()) {
	    boolean oneFailed = false;
	    boolean foundIt = false;
	    // get the security role name's in this app
	    Set sc = descriptor.getRoles();
	    Iterator itr = sc.iterator();
	    while (itr.hasNext()) {
		foundIt = false;
		Role role = (Role) itr.next();
		String roleName = role.getName(); 
		if (roleName.length() > 0) {
		    foundIt = true;
		} else {
		    foundIt = false;
		}

      
		if (foundIt) {
		    result.addGoodDetails(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "The security role name [ {0} ] found within application [ {1} ]",
					   new Object[] {roleName, descriptor.getName()}));
		} else {
		    if (!oneFailed) {
			oneFailed = true;
		    }
		    result.addErrorDetails(smh.getLocalString
					   (getClass().getName() + ".failed",
					    "Error: The security role name [ {0} ] not found within application [ {1} ]",
					    new Object[] {roleName, descriptor.getName()}));
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
				  "There are no role-name elements within the application [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}

	return result;
    }
}
