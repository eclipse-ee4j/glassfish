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
 * CmpEjbNoInvalidCreateMethod.java
 *
 * Created on November 13, 2001, 9:36 AM
 */

package com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.lang.reflect.Method;
/**
 * Per Ejb 2.0 spec, $14.1.8 create<METHOD> are not supported by 
 * CMP 1.1. EJBs.
 *
 * @author  Jerome Dochez
 * @version 
 */
public class CmpEjbNoInvalidCreateMethod extends EjbTest implements EjbCheck { 


    /** 
     * Entity beans with CMP 1.1 must not define create<METHOD>
     * 
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
        try {

	if (descriptor instanceof EjbCMPEntityDescriptor) {
	    String persistence =
		((EjbEntityDescriptor)descriptor).getPersistenceType();
	    if (EjbEntityDescriptor.CONTAINER_PERSISTENCE.equals(persistence) &&
                   ((EjbCMPEntityDescriptor)descriptor).getCMPVersion()==EjbCMPEntityDescriptor.CMP_1_1) {

                try {
		    Class c = Class.forName(descriptor.getHomeClassName(), false, getVerifierContext().getClassLoader());
		    Method [] methods = c.getDeclaredMethods();
                    boolean oneFailed = false;
                    for (int i=0;i<methods.length;i++) {
                        Method aMethod = methods[i];
                        if (aMethod.getName().startsWith("create")) {
                            if (!aMethod.getName().endsWith("create")) {
				    result.addErrorDetails(smh.getLocalString
							   (getClass().getName() + ".failed",
							    "CMP 1.1 entity beans are not authorized to define [ {0} ] method",
							    new Object[] {aMethod.getName()}));
                                     oneFailed = true;
                            } 
                        }
                    }
                    if (oneFailed) {
		        result.setStatus(Result.FAILED);
                    } else {
                        result.passed(smh.getLocalString
					(getClass().getName() + ".passed",
                                        "No create<METHOD> defined for this CMP 1.1 entity bean [ {0} ] ",
					 new Object[] {descriptor.getName()}));
                    }
                    return result;
                } catch(ClassNotFoundException cnfe) {
		    result.failed(smh.getLocalString
				  (getClass().getName() + ".failedException",
				   "Error: [ {0} ] class not found.",
				   new Object[] {descriptor.getHomeClassName()}));
                    return result;
                } 
            } 
        }
        } catch(Exception e) {
            e.printStackTrace();
        }
        result.notApplicable(smh.getLocalString
                             (getClass().getName() + ".notApplicable",
                              "[ {0} ] is not a CMP 1.1 Entity Bean.",
                              new Object[] {descriptor.getName()}));
       return result;
    }                        
}
