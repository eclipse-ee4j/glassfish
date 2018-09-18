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

package com.sun.enterprise.tools.verifier.tests.ejb.beanclass;


import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * Enterprise Java Bean class constuctor test.  
 * The class must have a public constructor that takes no parameters.
 */
public class EjbClassConstructor extends EjbTest { 


    /**
     * Enterprise Java Bean class constuctor test.  
     * The class must have a public constructor that takes no parameters.
     *   
     * @param descriptor the Enterprise Java Bean deployment descriptor   
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        Class c = loadEjbClass(descriptor, result);
        if (c!=null) {

            boolean foundOne = false;
            Constructor [] constructors = c.getConstructors();
            for (int i = 0; i < constructors.length; i++) {
                int modifiers = constructors[i].getModifiers();
                if (Modifier.isPublic(modifiers)) {
                    Class [] constructorParameterTypes;
                    constructorParameterTypes = constructors[i].getParameterTypes();
                    if (constructorParameterTypes.length > 0) {
                        continue;
                    } else {
                        foundOne = true;
                        break;
                    }
                }
            }

            if (foundOne) {
		result.addGoodDetails(smh.getLocalString
				      ("tests.componentNameConstructor",
				       "For [ {0} ]",
				       new Object[] {compName.toString()}));
		result.passed(smh.getLocalString
			      (getClass().getName() + ".passed",
			       "Valid: This bean [ {0} ] has a public constructor method with no "
			       + " \n parameters.  Enterprise beans must have a public constructor "
			       + " \n method with no parameters.",
			       new Object[] {descriptor.getEjbClassName()}));
            } else {
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                result.failed(smh.getLocalString
                    (getClass().getName() + ".failed",
                    "Error: There is no public constructor method with no parameters"
                    + "\n defined within bean [ {0} ].  Enterprise beans must have a "
                    + "\n public constructor methods with no parameters.",
                    new Object[] {descriptor.getEjbClassName()}));
            }
        }
        return result;

    }
}
