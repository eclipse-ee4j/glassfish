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

import java.lang.reflect.Modifier;

/** Enterprise Java Bean class modifiers test.  The class must not be final.
 */
public class EjbClassModifiersFinal extends EjbTest { 

    /** Enterprise Java Bean class modifiers test.  The class must not be final.
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
            boolean isFinal = false;
            int modifiers = c.getModifiers();
            if (Modifier.isFinal(modifiers)) {
                isFinal = true;
            }

            if (!isFinal) {
		result.addGoodDetails(smh.getLocalString
				      ("tests.componentNameConstructor",
				       "For [ {0} ]",
				       new Object[] {compName.toString()}));
                result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                    "[ {0} ] properly declares non-final class modifier.",
                    new Object[] {descriptor.getEjbClassName()}));
            } else if (isFinal) {
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                result.failed(smh.getLocalString
                    (getClass().getName() + ".failed",
                    "Error: Ejb Class [ {0} ] was found, but was declared as final.  The class  [ {1} ] must not be defined as final.",
                    new Object[] {descriptor.getEjbClassName(),descriptor.getEjbClassName()}));
            }
        }
        return result;        
    }
}
