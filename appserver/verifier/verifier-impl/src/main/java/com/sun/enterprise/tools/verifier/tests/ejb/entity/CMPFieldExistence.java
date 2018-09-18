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

package com.sun.enterprise.tools.verifier.tests.ejb.entity;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.PersistenceDescriptor;

/** 
 * Any CMP entity bean should have at least one cmp field defined in the DDs
 */
public class CMPFieldExistence extends EjbTest implements EjbCheck { 


    /** 
     * Any CMP entity bean should have at least one cmp field defined in the DDs
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
        if (descriptor instanceof EjbEntityDescriptor) {
	    String persistentType = 
		((EjbEntityDescriptor)descriptor).getPersistenceType();
	    if (EjbEntityDescriptor.CONTAINER_PERSISTENCE.equals(persistentType)) {
                EjbCMPEntityDescriptor cmpDesc = (EjbCMPEntityDescriptor) descriptor;
                PersistenceDescriptor persDesc = cmpDesc.getPersistenceDescriptor();
                if (persDesc.getCMPFields().size()==0) {
                    result.failed(smh.getLocalString
                                           (getClass().getName()+"failed",
                                            "For CMP entity bean [ {0} ], no cmp field are defined",
                                            new Object[] {descriptor.getName()}));
                } else {
                    result.passed(smh.getLocalString
                                           (getClass().getName() + ".passed",
                                            "For CMP entity bean [ {0} ], some cmp fields are defined",
                                            new Object[] {descriptor.getName()}));                    
                }
                return result;
            } 
        }
        
        // everything else is NA
        result.notApplicable(smh.getLocalString
				(getClass().getName() + ".notApplicable",
				 "The EJB [ {0} ] is not an CMP entity bean",
				 new Object[] {descriptor.getName()}));    
         return result;
    }
}
