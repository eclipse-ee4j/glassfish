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
import org.glassfish.ejb.deployment.descriptor.EjbBundleDescriptorImpl;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.RelationRoleDescriptor;

import java.util.Iterator;
import java.util.Set;

/**
 * Container managed relationship type field must be :
 *      a reference to a local interface of a entity bean
 *      a collection interface for oneToMany or manyToMany relationships
 *
 * @author  Jerome Dochez
 * @author Sheetal Vartak
 * @version 
 */
public class CmrFields extends CmrFieldTest {

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
    protected boolean runIndividualCmrTest(Descriptor descriptor, RelationRoleDescriptor role, Class c, Result result) {
     
	boolean foundIt = false;
	CMRFieldInfo info = null;
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	try { 
	    info  = role.getCMRFieldInfo();
        }catch (Exception e) {
        addErrorDetails(result, compName);
	    result.addErrorDetails(smh.getLocalString
		   (getClass().getName() + ".failed1",
		    "Error: No Local interfaces defined for EJB [ {0} ]",
	            new Object[] {descriptor.getName()}));
	    return false;
	    
	}   
        if (role.getPartner().getIsMany()) {
            // must be one the collection interface 
            if (info.type.getName().equals("java.util.Collection") ||
                info.type.getName().equals("java.util.Set")) {
                foundIt = true;
            } 
        } else {
	    EjbBundleDescriptorImpl bundle = ((EjbDescriptor) descriptor).getEjbBundleDescriptor();
	    if(((EjbDescriptor) descriptor).getLocalClassName() != null && 
	       !"".equals(((EjbDescriptor) descriptor).getLocalClassName())) {
		if (isValidInterface(info.type, bundle.getEjbs())) {
		    foundIt = true;
		}
	    }
	    else {
		if ((role.getRelationshipDescriptor()).getIsBidirectional()) {
		    result.addErrorDetails(smh.getLocalString
			   (getClass().getName() + ".failed",
			    "Error: Invalid type assigned for container managed relationship [ {0} ] in bean [ {1} ]",
			    new Object[] {info.name , descriptor.getName()}));
		    return false;
		}
		else foundIt = true;
	    }
        }
        if (foundIt) {
	     result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
            result.addGoodDetails(smh.getLocalString
	           (getClass().getName() + ".passed",
		    "Valid type assigned for container managed relationship [ {0} ] in bean [ {1} ]",
	                    new Object[] {info.name , descriptor.getName()}));
        } else {
            result.addErrorDetails(smh.getLocalString
		   (getClass().getName() + ".failed",
		    "Error: Invalid type assigned for container managed relationship [ {0} ] in bean [ {1} ]",
	            new Object[] {info.name , descriptor.getName()}));
        }
        return foundIt;
  
    }
    
    private boolean isValidInterface(Class fieldType, Set<EjbDescriptor> entities) {
        String component = "";
        if (entities==null)
            return false;
	// only local interface can be a valid interface
        Iterator<EjbDescriptor> iterator = entities.iterator();
        while (iterator.hasNext()) {
            EjbDescriptor entity = iterator.next();
	    if (fieldType.getName().equals(entity.getLocalClassName()))
		return true;
	}
        return false;
    }
   
}
