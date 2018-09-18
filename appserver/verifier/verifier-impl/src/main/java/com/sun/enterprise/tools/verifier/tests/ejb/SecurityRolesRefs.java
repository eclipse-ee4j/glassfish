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
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;
import org.glassfish.security.common.Role;

import java.util.Iterator;
import java.util.Set;

/**
 * Security role references test.
 * The Bean provider must declare all of the enterprise's bean references 
 * to security roles as specified in section 15.2.1.3 of the Moscone spec.
 * Role names must be mapped to names within the jar.
 */
public class SecurityRolesRefs extends EjbTest implements EjbCheck { 


    /** 
     * Security role references test.
     * The Bean provider must declare all of the enterprise's bean references
     * to security roles as specified in section 15.2.1.3 of the Moscone spec.
     * Role names must be mapped to names within the jar.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
	if ((descriptor instanceof EjbEntityDescriptor) ||
	    (descriptor instanceof EjbSessionDescriptor)) {
        
	    // RULE: Role names must be mapped to names within the ejb-jar
	    Set roleReferences = descriptor.getRoleReferences();
	    Iterator roleRefsIterator = roleReferences.iterator();
	    EjbBundleDescriptorImpl bundleDescriptor = descriptor.getEjbBundleDescriptor();
	    Set roles = bundleDescriptor.getRoles();
	    Iterator roleIterator = roles.iterator();
	    Role role = null;
	    RoleReference roleReference = null;
	    boolean found = false;
	    boolean oneFailed = false;
      
	    if (roleRefsIterator.hasNext()) {
		while (roleRefsIterator.hasNext()) {
		    found = false;
		    roleReference = (RoleReference)roleRefsIterator.next();

		    while (roleIterator.hasNext()) {
			role = (Role)roleIterator.next();
			if (role.getName().equals(roleReference.getValue())) {
			    found = true;
			    //reset this so next time it drop back into here
			    roleIterator = roles.iterator();
			    break;
			}
		    }

		    if (!found) {
			// print the roleReference with no corresponding env-prop
			result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
			result.addErrorDetails(smh.getLocalString
					       (getClass().getName() + ".failed",
						"Erro: The security role reference [ {0} ] has no corresponding linked security role name [ {1} ]",
						new Object[] {roleReference.getName(),roleReference.getValue()}));
			if (!oneFailed) {
			    oneFailed = true;
			}
		    } else {      
			result.addGoodDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
			result.addGoodDetails(smh.getLocalString
					      (getClass().getName() + ".passed",
					       "The security role reference [ {0} ] has corresponding linked security role name [ {1} ]",
					       new Object[] {roleReference.getName(),roleReference.getValue()}));
		    }
		}
	    } else { 
		result.addNaDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     (getClass().getName() + ".notApplicable1",
				      "There are no role references within this bean [ {0} ]",
				      new Object[] {descriptor.getName()}));
		return result;
	    }

	    // if one of 'em failed reset the status appropriately, in case
	    // status got stomped on within the while loop by the next env-prop
	    if (oneFailed) {
		result.setStatus(Result.FAILED);
	    } else {
		result.setStatus(Result.PASSED);
	    }

	    return result;
	} else {
	    result.addNaDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "[ {0} ] not called \n with a Session or Entity bean.",
				  new Object[] {getClass()}));
	    return result;
	}
    }
}
