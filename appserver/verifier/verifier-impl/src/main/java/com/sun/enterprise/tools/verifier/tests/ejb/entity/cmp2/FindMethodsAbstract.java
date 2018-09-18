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
 * Finder method implementation test
 * EJB 2.0 Spec 9.7.1 Entity bean class must not implement the ejbFind<METHOD>
 *
 * @author  Jerome Dochez
 * @version 
 */
public class FindMethodsAbstract extends QueryMethodTest {
    
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
        // single or multi finder
        String methodName = method.getName();
        String ejbMethodName = "ejb" + Character.toUpperCase(methodName.charAt(0)) + methodName.substring(1);
        Method ejbMethod = getMethod(targetClass, methodName, method.getParameterTypes());
        if (ejbMethod != null) {
	    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
            result.addErrorDetails(smh.getLocalString
		("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.FindMethodsAbstract.failed",
                "Error : [ {0} ] is defined in bean class [ {1} ]",
		new Object[] {ejbMethodName , targetClass.getName()}));       
            return false;
        } else {
	    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
            result.addGoodDetails(smh.getLocalString
		("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.FindMethodsAbstract.passed",
                "[ {0} ] is not defined in bean class [ {1} ]",
		new Object[] {ejbMethodName , targetClass.getName()}));       
            return true;
        }
    }
}
