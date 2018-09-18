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

package com.sun.enterprise.tools.verifier.tests.ejb.elements;

import com.sun.enterprise.deployment.MethodPermission;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.util.Iterator;
import java.util.Map;

/** 
 * Security role used in method permission element must be defined in the roles
 * element of the deployment descriptor.
 */
public class MethodPermissionSecurityRoleExists extends EjbTest implements EjbCheck { 



    /** 
     * Security role used in method permission element must be defined in the 
     * roles element of the deployment descriptor.
     * 
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        Map permissionedMethods = descriptor.getPermissionedMethodsByPermission();
        boolean oneFailed = false;
        if (permissionedMethods.size() >0) {
	    for (Iterator e = permissionedMethods.keySet().iterator(); e.hasNext();) {            
                MethodPermission nextPermission = (MethodPermission) e.next();
                if (nextPermission.isRoleBased()) {
                    if (!descriptor.getEjbBundleDescriptor().getRoles().contains(nextPermission.getRole())) {
		        oneFailed =true;
			result.addErrorDetails(smh.getLocalString
					       ("tests.componentNameConstructor",
						"For [ {0} ]",
						new Object[] {compName.toString()}));
		        result.addErrorDetails
			    (smh.getLocalString
			    (getClass().getName() + ".failed",
			    "Error: Method permissions role [ {0} ] must be one of the roles defined in bean [ {1} ]",
			    new Object[] {nextPermission.getRole().getName(), descriptor.getName()}));
		    } else {
			result.addGoodDetails(smh.getLocalString
					      ("tests.componentNameConstructor",
					       "For [ {0} ]",
					       new Object[] {compName.toString()}));
		        result.addGoodDetails
			    (smh.getLocalString
			    (getClass().getName() + ".passed",
			    "Valid: Method permissions role [ {0} ] is defined as one of the roles defined in bean [ {1} ]",
			    new Object[] {nextPermission.getRole().getName(), descriptor.getName()}));
		    } 
                } else {
                    addNaDetails(result, compName);
                    result.notApplicable(smh.getLocalString
                             (getClass().getName() + ".notApplicable1",
                              "There are no role based method-permissions within this bean [ {0} ]",
                              new Object[] {descriptor.getName()}));
                }
	    } 
	    if (oneFailed) {
    	        result.setStatus(Result.FAILED);
    	    } else {
            if(result.getStatus() != Result.NOT_APPLICABLE)
	            result.setStatus(Result.PASSED);
    	    } 
	} else {
	    result.addNaDetails(smh.getLocalString
				("tests.componentNameConstructor",
				 "For [ {0} ]",
				 new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "There are no <method-permission> elements within this bean [ {0} ]",
				  new Object[] {descriptor.getName()}));
	} 
	return result;
    }
}

