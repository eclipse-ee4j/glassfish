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

package com.sun.enterprise.tools.verifier.tests.ejb;

import com.sun.enterprise.tools.verifier.NameToken;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.security.common.Role;

import java.util.Iterator;

/**
 * The role-name element must conform to the lexical rules for an NMTOKEN
 */
public class RoleNameNMTOKEN extends EjbTest implements EjbCheck { 



    /** 
     * The role-name element must conform to the lexical rules for an NMTOKEN
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (!descriptor.getPermissionedRoles().isEmpty()) {
	    for (Iterator itr = descriptor.getPermissionedRoles().iterator();
		 itr.hasNext();) {

		Role nextRole = (Role) itr.next();
		if (NameToken.isNMTOKEN(nextRole.getName()))  {
		    result.addGoodDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
		    result.addGoodDetails
			(smh.getLocalString
			 (getClass().getName() + ".passed",
			  "Role name [ {0} ] conforms to the lexical rules of NMTOKEN within bean [ {1} ]",
			  new Object[] {nextRole.getName(), descriptor.getName()}));
		    if (result.getStatus()!= Result.FAILED)
			result.setStatus(Result.PASSED);
		} else {
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.failed
			(smh.getLocalString
			 (getClass().getName() + ".failed",
			  "Role name [ {0} ] does not conform to the lexical rules of NMTOKEN within bean [ {1} ]",
			  new Object[] {nextRole.getName(), descriptor.getName()}));
		}
	    } 
	} else {
	    result.addNaDetails(smh.getLocalString
				("tests.componentNameConstructor",
				 "For [ {0} ]",
				 new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "No permissioned roles defined for this bean [ {0} ]",
				  new Object[] {descriptor.getName()}));
	} 
	return result;
    }
}
