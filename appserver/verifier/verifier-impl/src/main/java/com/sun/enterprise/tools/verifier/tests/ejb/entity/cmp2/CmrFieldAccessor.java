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
import org.glassfish.ejb.deployment.descriptor.CMRFieldInfo;
import org.glassfish.ejb.deployment.descriptor.RelationRoleDescriptor;

/**
 * Container-managed fields declaration test.
 * CMR fields accessor methods names must the field name with the first
 * letter uppercased and prefixed with get and set
 * 
 * @author  Jerome Dochez
 * @author Sheetal Vartak
 * @version 
 */
public class CmrFieldAccessor extends CmrFieldTest {

    /**
     * run an individual verifier test of a declated cmr field of the class
     *
     * @param entity the descriptor for the entity bean containing the cmp-field    
     * @param rrd the descriptor for the declared cmr field
     * @param c the class owning the cmp field
     * @param result the result object to use to put the test results in
     * 
     * @return true if the test passed
     */            
    protected boolean runIndividualCmrTest(Descriptor entity, RelationRoleDescriptor rrd, Class c, Result result) {
	boolean oneFailed = false;
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
	    CMRFieldInfo info = rrd.getCMRFieldInfo();
	    if (info == null) {
		 addErrorDetails(result, compName);
		result.addErrorDetails(smh.getLocalString
		            ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CMPTest.isAccessorDeclared.failed1",
                            "Error : no CMR Field  declared ",
		            new Object[] {}));  
		return false;
	    }
	    oneFailed = isAccessorDeclared(info.name, info.type, c, result);
	    if (oneFailed == false) {
            // do nothing, appropriate message has been added in
            // isAccessorDeclared().
	    }else {
		 result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.addGoodDetails(smh.getLocalString
		            ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CMPTest.isAccessorDeclared.passed",
                            "CMR Field is properly declared ",
		            new Object[] {}));     
	    }
	    return oneFailed;
    }
}
