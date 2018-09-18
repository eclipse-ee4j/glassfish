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
import org.glassfish.ejb.deployment.descriptor.RelationRoleDescriptor;
import org.glassfish.ejb.deployment.descriptor.RelationshipDescriptor;

import java.util.Iterator;
import java.util.Set;

/**
 * Container managed relationship fields tests superclass, iterates over all
 * declated cmr fields and delegate actual tests to subclasses
 *
 * @author  Jerome Dochez
 * @version 
 */
abstract public class CmrFieldTest extends CMPTest {

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
    protected abstract boolean runIndividualCmrTest(Descriptor entity, RelationRoleDescriptor rrd, Class c, Result r);
    
    /** 
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbCMPEntityDescriptor descriptor) {

        Result result = getInitializedResult();
        addErrorDetails(result,
            getVerifierContext().getComponentNameConstructor());

	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        boolean oneFailed = false;
	boolean found = false;

        Class c = loadEjbClass(descriptor, result);

        if (c!=null) {
            Set cmrFields = ((EjbCMPEntityDescriptor)descriptor).getPersistenceDescriptor().getRelationships();
            Iterator cmrIterator = cmrFields.iterator();

	    if (cmrIterator.hasNext()) {
		while (cmrIterator.hasNext()) {
		    RelationshipDescriptor cmfDescriptor = (RelationshipDescriptor) cmrIterator.next();
            {
                // test if this bean is the source in this relationship
                RelationRoleDescriptor role = cmfDescriptor.getSource();
                if (role.getOwner().equals(descriptor) && role.getCMRField()!=null) {
                found = true;
                if (!runIndividualCmrTest(descriptor, role, c, result)) {
                    oneFailed = true;
                }
                }
            }
            // we need to test for both source and sink because of self references
            {
                // test if this bean is the sink in this relationship
                RelationRoleDescriptor role = cmfDescriptor.getSink();
                if (role.getOwner().equals(descriptor) && role.getCMRField()!=null) {
                found = true;
                if (!runIndividualCmrTest(descriptor, role, c, result)) {
                    oneFailed = true;
                }
                }
            }
		}
		if (oneFailed) 
		    result.setStatus(Result.FAILED);
		else if (found == false) {
		     result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.notApplicable(smh.getLocalString
				 ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CmrFieldTest.notApplicable",
				  "Not Applicable : The EJB has no CMR fields declared",
				  new Object[] {})); 
		}
		else 
		    result.setStatus(Result.PASSED);
	    }
	    else { 
		 result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				 ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CmrFieldTest.notApplicable",
				  "Not Applicable : The EJB has no CMR fields declared",
				  new Object[] {})); 
	    } 
	}
        return result;
    }   
}
