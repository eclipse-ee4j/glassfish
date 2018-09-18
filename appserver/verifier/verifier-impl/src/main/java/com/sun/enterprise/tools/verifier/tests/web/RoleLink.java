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
import java.util.logging.Level;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.*;
import com.sun.enterprise.tools.verifier.tests.*;


/**
 * The role-link element is used to link a security role reference to a
 * defined security role.  The role-link element must contain the name of
 * one of the security roles defined in the security-role elements.
 */
public class RoleLink extends WebTest implements WebCheck {

    /**
     * The role-link element is used to link a security role reference to a
     * defined security role.  The role-link element must contain the name of
     * one of the security roles defined in the security-role elements.
     *
     * @param descriptor the Web deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (!descriptor.getWebComponentDescriptors().isEmpty()) {
	    boolean oneFailed = false;
            int na = 0;
            int noWd = 0;
	    for (WebComponentDescriptor next : descriptor.getWebComponentDescriptors()) {
                noWd++;
		boolean foundIt = false;
		// get the security role-link's in this .war
		if (next.getSecurityRoleReferences().hasMoreElements()) {
		    for (Enumeration ee = next.getSecurityRoleReferences(); ee.hasMoreElements();) {
			RoleReference rr = (RoleReference) ee.nextElement();
			foundIt = false;
			String linkName = rr.getValue();
                        logger.log(Level.FINE, "servlet linkName: " + linkName);
			// now check to see if role-link exist in security role names
			if (descriptor.getSecurityRoles().hasMoreElements()) {
			    for (Enumeration eee = descriptor.getSecurityRoles(); eee.hasMoreElements();) {
				SecurityRoleDescriptor srdNext = (SecurityRoleDescriptor) eee.nextElement();

				if (linkName.equals(srdNext.getName())) {
				    foundIt = true;
				    break;
				} else {
				    continue;
				}
			    }
			} else {
			    // if descriptor.getSecurityRoles().hasMoreElements())
			    foundIt = false;
			}

			if (foundIt) {
			    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
			    result.addGoodDetails(smh.getLocalString
						  (getClass().getName() + ".passed",
						   "role-link [ {0} ] links security role reference to a defined security role within web application [ {1} ]",
						   new Object[] {linkName, descriptor.getName()}));
			} else {
			    if (!oneFailed) {
				oneFailed = true;
			    }
			    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
			    result.addErrorDetails(smh.getLocalString
						   (getClass().getName() + ".failed",
						    "Error: role-link [ {0} ] does not link security role reference to a defined security role within web application [ {1} ]",
						    new Object[] {linkName, descriptor.getName()}));
			}
		    } // for loop next.getSecurityRoleReferences() has more elements
		} else {
		    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addNaDetails(smh.getLocalString
					(getClass().getName() + ".notApplicable1",
					 "[ {0} ] has no role-link element defined within the web archive [ {1} ]",
					 new Object[] {next.getName(),descriptor.getName()}));
                    na++;
		}
	    } // for loop descriptor.getWebComponentDescriptors(); e.hasMoreElements()
	    if (oneFailed) {
		result.setStatus(Result.FAILED);
            } else if (na == noWd) {
                result.setStatus(Result.NOT_APPLICABLE);
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
				  "There are no location elements within the web archive [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}

	return result;
    }
}
