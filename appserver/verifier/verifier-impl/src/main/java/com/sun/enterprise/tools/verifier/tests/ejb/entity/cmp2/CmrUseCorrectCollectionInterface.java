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
import org.glassfish.ejb.deployment.descriptor.RelationRoleDescriptor;

/**
 * Container managed relationship type field must use of the collection 
 * interface for on-to-many or many-to-many relationships and specify it in 
 * the Deployment Descriptor
 *
 * @author  Jerome Dochez
 * @version 
 */
public class CmrUseCorrectCollectionInterface extends CmrFieldTest {

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
    protected boolean runIndividualCmrTest(Descriptor descriptor, RelationRoleDescriptor rrd, Class c, Result result) {
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        if (rrd.getPartner().getIsMany()) {
            // must be one the collection interface 
            if (rrd.getCMRFieldType()==null) {
                addErrorDetails(result, compName);
              result.addErrorDetails(smh.getLocalString
		    (getClass().getName() + ".failed2",
                    "Error : CMR field [ {0} ]  cmr-field-type must be defined for one-to-many or many-to-many relationships and the value of the cmr-field-type element must be either: java.util.Collection or java.util.Set",
	            new Object[] {rrd.getCMRField()}));                
                return false;
            } else {
                CMRFieldInfo info = rrd.getCMRFieldInfo();
                if (rrd.getCMRFieldType().equals(info.type.getName())) {
                    result.addGoodDetails(smh.getLocalString
    		    (getClass().getName() + ".passed",
                        "CMR field [ {0} ] is the same type as declared in the deployment descriptors [ {1} ]",
        	            new Object[] {info.name, info.role.getCMRFieldType()}));                
                    return true;                
                } else {
                    addErrorDetails(result, compName);
                    result.addErrorDetails(smh.getLocalString
    		    (getClass().getName() + ".failed",
                        "Error : CMR field [ {0} ] is not the same type as declared in the deployment descriptors [ {1} ]",
    	            new Object[] {info.name, info.role.getCMRFieldType()}));                
                    return false;
                }            
            }
        }
       return true;        
    }
}
