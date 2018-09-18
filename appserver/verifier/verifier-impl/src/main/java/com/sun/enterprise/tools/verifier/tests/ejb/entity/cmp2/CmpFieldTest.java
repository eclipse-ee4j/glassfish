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
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;

import java.util.Iterator;
import java.util.Set;


/**
 * Container-managed persistent fields related tests superclass 
 *
 * @author  Jerome Dochez
 * @version 
 */
abstract public class CmpFieldTest extends CMPTest {


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
    protected abstract boolean runIndividualCmpFieldTest(Descriptor entity, Descriptor f, Class c, Result r);
    
    /** 
     *
     * Container-managed persistent fields test, iterates over all declared
     * cmp fields and invoke the runIndividualCmpFieldTest nethod
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbCMPEntityDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        Class c = loadEjbClass(descriptor, result);
        if (c!=null) {
            Descriptor persistentField;
	    boolean oneFailed = false;
            
            Set persistentFields = descriptor.getPersistenceDescriptor().getCMPFields();
            Iterator iterator = persistentFields.iterator();
	    if (iterator.hasNext()) {	  	
		while (iterator.hasNext()) {
		    persistentField = (Descriptor)iterator.next();
		    boolean status  = runIndividualCmpFieldTest(descriptor, persistentField, c, result);
		    if (!status) 
			oneFailed=true;                       
		    
		}
		if (oneFailed) {
		    result.setStatus(Result.FAILED);
		} else { 
		    result.setStatus(Result.PASSED);
		}
	    }	    
	    else { 
		result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CmpFieldTest.notApplicable",
				      "Not Applicable : The EJB has no CMP fields declared",
				      new Object[] {})); 
	    }
	} 
        return result;
    }
}
