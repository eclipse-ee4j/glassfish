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

import com.sun.enterprise.deployment.RoleReference;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.security.common.Role;

import java.util.Iterator;
import java.util.Set;

/**
 * If the Application assembler defines security roles in the deployment 
 * descriptor, the Application Assembler must bind security role references 
 * declared by the Bean Provider to the security roles. 
 */
public class SecurityRolesBind extends EjbTest implements EjbCheck { 



    /** 
     * If the Application assembler defines security roles in the deployment
     * descriptor, the Application Assembler must bind security role references
     * declared by the Bean Provider to the security roles.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	EjbBundleDescriptorImpl bundleDescriptor = descriptor.getEjbBundleDescriptor();
	Set ejbs = bundleDescriptor.getEjbs();
	Iterator ejbIterator = ejbs.iterator();
	EjbDescriptor ejb = null;
	Set roleReferences = null;
	Iterator roleRefsIterator = null;
	Set roles = bundleDescriptor.getRoles();
	Iterator rolesIterator = roles.iterator();
	RoleReference roleReference = null;
	Role role = null;
	boolean oneFailed = false;
        
	// check to see if there are any undefined roles being referenced
	while (ejbIterator.hasNext()) {
	    ejb = (EjbDescriptor)ejbIterator.next();
	    roleReferences = ejb.getRoleReferences();
	    roleRefsIterator = roleReferences.iterator();
	    if (roleRefsIterator.hasNext()) {
		while (roleRefsIterator.hasNext()) {
		    roleReference = (RoleReference)roleRefsIterator.next();
		    role = roleReference.getRole();
		    if (!role.getName().equals("")
			&& !bundleDescriptor.getRoles().contains(role) ) {
			// print the undefine role
			result.addErrorDetails(smh.getLocalString
					       ("tests.componentNameConstructor",
						"For [ {0} ]",
						new Object[] {compName.toString()}));
			result.addErrorDetails(smh.getLocalString
					       (getClass().getName() + ".failed",
						"Error: The role [ {0} ] for bean [ {1} ] is undefined.",
						new Object[] {role.getName(),ejb.getName()}));
			if (!oneFailed) {
			    oneFailed = true;
			}
		    } else {
			result.addGoodDetails(smh.getLocalString
					      ("tests.componentNameConstructor",
					       "For [ {0} ]",
					       new Object[] {compName.toString()}));
			result.passed(smh.getLocalString
				      (getClass().getName() + ".passed",
				       "The role [ {0} ] for bean [ {1} ] is defined.",
				       new Object[] {role.getName(),ejb.getName()}));
		    }
		}
	    } else {
		result.addNaDetails(smh.getLocalString
				    ("tests.componentNameConstructor",
				     "For [ {0} ]",
				     new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     (getClass().getName() + ".notApplicable",
				      "There are no role references which need to be bound to other security roles within this bean [ {0} ]",
				      new Object[] {ejb.getName()}));
	    }
	}

	if (oneFailed) {
	    result.setStatus(Result.FAILED);   
	}
        
	return result;
    }    
}
