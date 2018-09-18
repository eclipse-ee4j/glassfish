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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.pksinglefield;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.FieldDescriptor;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Set;

/** 
 * The primkey-field must be one of the fields declared in the cmp-field
 * elements.
 */
public class PrimekeyFieldPersistentFields extends EjbTest implements EjbCheck { 


    /** 
     * The primkey-field must be one of the fields declared in the cmp-field
     * elements.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	// The primkey-field must be one of the fields declared in the 
	// cmp-field elements
	if (descriptor instanceof EjbEntityDescriptor) {
	    String persistence =
		((EjbEntityDescriptor)descriptor).getPersistenceType();

	    if (EjbEntityDescriptor.CONTAINER_PERSISTENCE.equals(persistence)) {
		try {
		    // do i need to use this to help determine single vs. multiple 
		    // object finders, etc.
		    String primkey =
			((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName();
                    if (primkey.equals("java.lang.String")) {
                        try {
  
		            FieldDescriptor primField =
			        ((EjbCMPEntityDescriptor)descriptor).getPrimaryKeyFieldDesc();
  
		            // primField must exist in order to be valid & pass test
		            Descriptor persistentField;
		            Field field;
		            Set persistentFields =
			        ((EjbCMPEntityDescriptor)descriptor).getPersistenceDescriptor().getCMPFields();
		            Iterator iterator = persistentFields.iterator();
		            boolean foundMatch = false;
		            while (iterator.hasNext()) {
			        persistentField = (Descriptor)iterator.next();
			        if (primField != null) {
			            if (primField.getName().equals(persistentField.getName())) {
			                foundMatch = true;
			                break;
			            } else {
			                continue;
			            }
		                } else {
                                    // should already be set, can't ever be in cmp 
                                    // fields if primField doesn't exist
			            foundMatch = false;
			            break;
		                }
		            }
		            if (foundMatch) {
			        result.addGoodDetails(smh.getLocalString
						      ("tests.componentNameConstructor",
						       "For [ {0} ]",
						       new Object[] {compName.toString()}));
				result.passed(smh.getLocalString
				              (getClass().getName() + ".passed",
				               "Primary key field [ {0} ] is defined within set of container managed fields for bean [ {1} ]",
				               new Object[] {primField.getName(),descriptor.getName()}));
		            } else {
			        if (primField != null) {
			            result.addErrorDetails(smh.getLocalString
							   ("tests.componentNameConstructor",
							    "For [ {0} ]",
							    new Object[] {compName.toString()}));
				    result.failed(smh.getLocalString
					          (getClass().getName() + ".failed",
					           "Primary key field [ {0} ] is not defined within set of container managed fields for bean [ {1} ]",
					           new Object[] {primField.getName(),descriptor.getName()}));
			        } else {
                                    // unless special case, where primary key class
                                    // is java.lang.Object, then test should be N/A
                                    // not failed
                                    try {
                                        if (((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName().equals("java.lang.Object")) {

					    result.addNaDetails(smh.getLocalString
								("tests.componentNameConstructor",
								 "For [ {0} ]",
								 new Object[] {compName.toString()}));
		                            result.notApplicable(smh.getLocalString
								 (getClass().getName() + ".notApplicable2",
								  "Primkey field not defined for [ {0} ] bean.",
								  new Object[] {descriptor.getName()}));
                                    
                                        } else {
			                    result.addErrorDetails(smh.getLocalString
								   ("tests.componentNameConstructor",
								    "For [ {0} ]",
								    new Object[] {compName.toString()}));
					    result.failed(smh.getLocalString
							  (getClass().getName() + ".failed1",
							   "Primary key field is not defined within set of container managed fields for bean [ {0} ]",
							   new Object[] {descriptor.getName()}));
			                }		       
		                    } catch (NullPointerException e) {
					result.addNaDetails(smh.getLocalString
							    ("tests.componentNameConstructor",
							     "For [ {0} ]",
							     new Object[] {compName.toString()}));
		                        result.notApplicable(smh.getLocalString
							     (getClass().getName() + ".notApplicable2",
							      "Primkey field not defined for [ {0} ] bean.",
							      new Object[] {descriptor.getName()}));
		                    }
			        }		       
		            }
                        } catch (NullPointerException e) {
                            result.addErrorDetails(smh.getLocalString
						("tests.componentNameConstructor",
						 "For [ {0} ]",
						 new Object[] {compName.toString()}));
			    result.failed
                                (smh.getLocalString
                                 (getClass().getName() + ".failed2",
                                  "Error: Primary Key Field must be defined for bean [ {0} ] with primary key class set to [ {1} ]",
                                  new Object[] {descriptor.getName(),primkey}));
                        }
                    } else {
			result.addNaDetails(smh.getLocalString
					    ("tests.componentNameConstructor",
					     "For [ {0} ]",
					     new Object[] {compName.toString()}));
                        result.notApplicable(smh.getLocalString
					     (getClass().getName() + ".notApplicable3",
					      "primkey [ {0} ] is not java.lang.String for bean [ {1} ]",
					      new Object[] {primkey,descriptor.getName()}));
                    }
		} catch (NullPointerException e) {
		    result.addNaDetails(smh.getLocalString
					("tests.componentNameConstructor",
					 "For [ {0} ]",
					 new Object[] {compName.toString()}));
		    result.notApplicable(smh.getLocalString
					 (getClass().getName() + ".notApplicable2",
					  "Primkey field not defined for [ {0} ] bean.",
					  new Object[] {descriptor.getName()}));
		}
	    } else {
		result.addNaDetails(smh.getLocalString
				    ("tests.componentNameConstructor",
				     "For [ {0} ]",
				     new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     (getClass().getName() + ".notApplicable1",
				      "Expected [ {0} ] managed persistence, but [ {1} ] bean has [ {2} ] managed persistence",
				      new Object[] {EjbEntityDescriptor.CONTAINER_PERSISTENCE,descriptor.getName(),persistence}));
	    }
	} else {
	    result.addNaDetails(smh.getLocalString
				("tests.componentNameConstructor",
				 "For [ {0} ]",
				 new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "{0} expected \n {1} bean, but called with {2} bean",
				  new Object[] {getClass(),"Entity","Session"}));
	}

	return result;
    }
}
