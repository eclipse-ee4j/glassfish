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

/*
 * SelectMethodTest.java
 *
 * Created on December 14, 2000, 4:36 PM
 */

package com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;

import java.lang.reflect.Method;

/**
 *
 * @author  dochez
 * @version 
 */
abstract public class SelectMethodTest extends CMPTest {

    protected abstract boolean runIndividualSelectTest(Method m, EjbCMPEntityDescriptor descriptor, Result result);
    
    /** 
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbCMPEntityDescriptor descriptor) {
        
        boolean allIsWell = true;
        Result result = getInitializedResult();
	boolean found = false;        
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        Class ejbClass = loadEjbClass(descriptor, result);
        if (ejbClass!=null) {
            Method[] methods = ejbClass.getDeclaredMethods();
	    if (methods != null) {
		for (int i=0;i<methods.length;i++) {
		    String methodName = methods[i].getName();
		    if (methodName.startsWith("ejbSelect")) {
			found = true;
			if (!runIndividualSelectTest(methods[i], (EjbCMPEntityDescriptor) descriptor, result))
			    allIsWell=false;
		    }
		}
		if (found == false) {
		    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.notApplicable(smh.getLocalString
					  ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.SelectMethodTest.nptApplicable",
					   "Not Applicable : No select methods found",
					   new Object[] {}));     
		}
        if (result.getStatus() != Result.NOT_APPLICABLE) {    
            if (allIsWell) 
                result.setStatus(Result.PASSED);
            else 
                result.setStatus(Result.FAILED);            
            }
        }
	}    
	return result;
    }    
}
