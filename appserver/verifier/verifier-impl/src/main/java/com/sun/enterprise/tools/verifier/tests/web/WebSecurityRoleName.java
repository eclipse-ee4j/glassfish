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
import org.glassfish.web.deployment.descriptor.AuthorizationConstraintImpl;
import org.glassfish.web.deployment.descriptor.SecurityConstraintImpl;


/** 
 * The Web role-name element contains the name of a security role.
 */
public class WebSecurityRoleName extends WebTest implements WebCheck { 

    
    /** 
     * The Web role-name element contains the name of a security role.
     *
     * @param descriptor the Web deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(WebBundleDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor.getSecurityConstraints().hasMoreElements()) {
	    boolean oneFailed = false;
	    boolean foundIt = false;
            int naSr = 0;
            int naAci = 0;
            int noAci = 0;
            int noSc = 0;
	    // get the security role name's in this .war
	    for (Enumeration e = descriptor.getSecurityConstraints() ; e.hasMoreElements() ;) {
		foundIt = false;
                noSc++;
		SecurityConstraintImpl securityConstraintImpl = (SecurityConstraintImpl)
		    e.nextElement();
		AuthorizationConstraintImpl aci = (AuthorizationConstraintImpl) securityConstraintImpl.getAuthorizationConstraint();
		if (aci != null) {
                    noAci++;
                    if (aci.getSecurityRoles().hasMoreElements()) {
			for (Enumeration ee = aci.getSecurityRoles(); ee.hasMoreElements();) {
			    SecurityRoleDescriptor srd = (SecurityRoleDescriptor) ee.nextElement();
			    String roleName = srd.getName(); 
			    // jsb, nothing to test here...?
			    if (roleName.length() > 0) {
				foundIt = true;
			    } else {
				foundIt = false;
			    }
           
			    if (foundIt) {
				result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
				result.addGoodDetails(smh.getLocalString
						      (getClass().getName() + ".passed",
						       "The security role name [ {0} ] found within web application [ {1} ]",
						       new Object[] {roleName, descriptor.getName()}));
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
							"Error: The security role name [ {0} ] not found within web application [ {1} ]",
							new Object[] {roleName, descriptor.getName()}));
			    }
			}
		    } else {
			result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
			result.notApplicable(smh.getLocalString
					     (getClass().getName() + ".notApplicable1",
					      "Not Applicable: There are no security roles in this security constraint within [ {0} ]",
					      new Object[] {descriptor.getName()}));                    naSr++;
		    }
                } else {
		    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                    result.notApplicable(smh.getLocalString
                                         (getClass().getName() + ".notApplicable2",
                                          "Not Applicable: There is no authorization constraint in this security constraint within [ {0} ]",
                                          new Object[] {descriptor.getName()}));
                    naAci++;
                }
	    }
	    if (oneFailed) {
		result.setStatus(Result.FAILED);
	    } else if ((noSc == naAci) || (noAci == naSr)) {
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
				  "There are no role-name elements within the web archive [ {0} ]",
				  new Object[] {descriptor.getName()}));
	}

	return result;
    }
}
