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
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;

import java.lang.reflect.Method;

/**
 * All finder methods should throw the jakarta.ejb.FinderException
 * 
 * @author  Jerome Dochez
 * @version 
 */
public class FindMethodException extends QueryMethodTest {

 
    /**
     * <p>
     * Run an individual test against a finder method (single or multi)
     * </p>
     * 
     * @param method is the finder method reference
     * @param descriptor is the entity bean descriptor
     * @param targetClass is the class to apply to tests to
     * @param result is where to place the result
     * 
     * @return true if the test passes
     */
    protected boolean runIndividualQueryTest(Method method, EjbCMPEntityDescriptor descriptor, Class targetClass, Result result) {
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();    
        if (methodThrowException(method, "jakarta.ejb.FinderException")) {
	    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
            result.addGoodDetails(smh.getLocalString
		("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.FindMethodException.passed",
                "[ {0} ] throws the jakarta.ejb.FinderException",
		new Object[] {method.getName()}));                   
            return true;
        } else {
	    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
            result.addErrorDetails(smh.getLocalString
		("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.FindMethodException.failed",
                "Error : [ {0} ] does not throw the jakarta.ejb.FinderException",
		new Object[] {method.getName()}));                   
            return false;
        }
    }
}
