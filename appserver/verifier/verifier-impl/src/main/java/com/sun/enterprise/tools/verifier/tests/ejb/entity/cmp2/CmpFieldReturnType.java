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

import java.lang.reflect.Method;
import com.sun.enterprise.deployment.*;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.*;
import org.glassfish.deployment.common.Descriptor;


/**
 * Container-managed fields declaration test.
 * CMP fields accessor methods should not return local interface type
 *
 * @author  Sheetal Vartak
 * @version 
 */
public class CmpFieldReturnType extends CmpFieldTest {

    /**
     * run an individual verifier test of a declated cmp field of the class
     *
     * @param entity the descriptor for the entity bean containing the cmp-field    
     * @param f the descriptor for the declared cmp field
     * @param c the class owning the cmp field
     * @parma r the result object to use to put the test results in
     * 
     * @return true if the test passed
     */    
    protected boolean runIndividualCmpFieldTest(Descriptor entity, Descriptor persistentField, Class c, Result result) {
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
	String fieldName = persistentField.getName();
	String getMethodName = "get" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        String setMethodName = "set" + Character.toUpperCase(fieldName.charAt(0)) + fieldName.substring(1);
        Method getMethod = getMethod(c, getMethodName, null);
        if (getMethod != null) {
	    if (((EjbDescriptor)entity).getLocalClassName() != null) {
		if ((((EjbDescriptor)entity).getLocalClassName()).equals(getMethod.getReturnType().getName())) {
		     result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addErrorDetails(smh.getLocalString
			        ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CmpFieldReturnType.failed",
				"Error : cmp-field accessor method [{0}] cannot return local interface [{1}] ",
				 new Object[] { getMethod.toString(),((EjbDescriptor)entity).getLocalClassName() }));         
		    return false;
		} else {
		     result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.addGoodDetails(smh.getLocalString
			     ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CmpFieldReturnType.passed",
			     "cmp-field accessor method [{0}] does not return local interface [{1}]. Test passed.",
		            new Object[] { getMethod.toString(),((EjbDescriptor)entity).getLocalClassName() })); 
		    return true;        
		}
	    } else {
		 result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.addGoodDetails(smh.getLocalString
			    ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CmpFieldReturnType.failed2",
                            "Not Applicable :  no local interface found.",
		            new Object[] {})); 
		return true; 
	    }
	}else {
	     result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.addErrorDetails(smh.getLocalString
			    ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CmpFieldReturnType.failed1",
                            "Error : cmp-field accessor method [{0}] not found.",
		            new Object[] {getMethodName})); 
	    return false;
	}
    }
}
