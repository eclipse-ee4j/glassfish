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

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

/**
 * Find Methods should have deployment descriptors associated with them
 * 
 * @author  Jerome Dochez
 * @version 
 */
public class FindMethodHasDescriptors extends QueryMethodTest {

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
        if (method.getName().equals("findByPrimaryKey")) {
	    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.addGoodDetails(smh.getLocalString
				  ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.FindMethodHasDescriptors.passed1",
				   "Passed: Found method findByPrimaryKey",
				   new Object[] {})); 
            return true;
	}
        
        // We don't use getQueryFor to free ourselfves from classloader issues.
        Set set = descriptor.getPersistenceDescriptor().getQueriedMethods();
        Iterator iterator = set.iterator();
	if (iterator.hasNext()) {
	    while(iterator.hasNext()) {
		MethodDescriptor queryMethod = (MethodDescriptor) iterator.next();
		if (queryMethod.getName().equals(method.getName())) {
		    Class mParms[] = method.getParameterTypes();
		    String queryParms[] = queryMethod.getParameterClassNames();
            int queryParamsLen;
            if(queryParms == null)
              queryParamsLen = 0;
            else
              queryParamsLen = queryParms.length;
		    if (queryParamsLen == mParms.length) {
			boolean same = true;
            if(queryParamsLen > 0)
            {
			for (int i=0;i<mParms.length;i++) {
			    if (!mParms[i].getName().equals(queryParms[i]))
				same=false;                    
			}
            }
			if (same) {  
			    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
			    result.addGoodDetails(smh.getLocalString
				("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.FindMethodHasDescriptors.passed",
				 "[ {0} ] has a query element associated with it",
				 new Object[] {method}));       
			    return true;
			}
		    }
		}
	    }
	    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.addErrorDetails(smh.getLocalString
			    ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.FindMethodHasDescriptors.failed",
			     "Error : [ {0} ] seems to be a finder method but has no query element associated with it",
			     new Object[] {method}));       
	    return false;               
	}
	else {
	    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.addGoodDetails(smh.getLocalString
	    ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.FindMethodHasDescriptors.notApplicable",
	     "NotApplicable : No Query methods found",
	     new Object[] {})); 
	    return true;
	}
       
    }
}
