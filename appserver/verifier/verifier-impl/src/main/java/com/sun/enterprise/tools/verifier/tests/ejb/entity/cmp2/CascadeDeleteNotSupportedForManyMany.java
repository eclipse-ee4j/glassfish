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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.ejb.deployment.descriptor.RelationRoleDescriptor;


/**
 * cascade-delete tag is not supported for many-many relationships
 *
 * @author  Sheetal Vartak
 * @version 
 */
public class CascadeDeleteNotSupportedForManyMany extends CmrFieldTest {

    /**
     * run an individual verifier test of a declated cmr field of the class
     *
     * @param entity the descriptor for the entity bean containing the cmp-field    
     * @param info the descriptor for the declared cmr field
     * @param c the class owning the cmp field
     * @parma r the result object to use to put the test results in
     * 
     * @return true if the test passed
     */            
    protected boolean runIndividualCmrTest(Descriptor descriptor, RelationRoleDescriptor role, Class c, Result result) {
	boolean isMany = false;
	boolean isPartnerMany = false;
	boolean cascadeDelete = false;
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	isMany = role.getIsMany();
	isPartnerMany = (role.getPartner()).getIsMany();
	cascadeDelete = role.getCascadeDelete();
	if(isMany && isPartnerMany && cascadeDelete) {
	    addErrorDetails(result, compName);
	    result.addErrorDetails(smh.getLocalString
		   (getClass().getName() + ".failed",
		    "Error: cascade-delete should not be supported for many-many relationships. Please check Relationship Role [{0}]",
	            new Object[] {role.getName()}));
	    return false;
	} else {
	     result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.addGoodDetails(smh.getLocalString
	           (getClass().getName() + ".passed",
		    "cascade-delete is not supported for many-many relationships. Test passed.",
	            new Object[] {}));
	    return true;
	}
    }   
}
