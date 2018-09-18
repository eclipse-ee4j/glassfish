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
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.lang.reflect.Field;

/** 
 * The type of the primkey-field must be the same as the primary key type.
 */
public class PrimekeyFieldPrimaryKeyType extends EjbTest implements EjbCheck { 


    /** 
     * The type of the primkey-field must be the same as the primary key type.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	// The type of the primkey-field must be the same as the primary key type.
	if (descriptor instanceof EjbEntityDescriptor) {
	    String persistence =
		((EjbEntityDescriptor)descriptor).getPersistenceType();

	    if (EjbEntityDescriptor.CONTAINER_PERSISTENCE.equals(persistence)) {
		try {
		    VerifierTestContext context = getVerifierContext();
		ClassLoader jcl = context.getClassLoader();
		    Class c = Class.forName(descriptor.getEjbClassName(), false, getVerifierContext().getClassLoader());
		    try {
			if (((EjbCMPEntityDescriptor)descriptor).getPrimaryKeyFieldDesc() != null) {
			    Field pkf = c.getDeclaredField(((EjbCMPEntityDescriptor)descriptor).getPrimaryKeyFieldDesc().getName());
			    Class pkfType = pkf.getType();
			    try {
				String primkey =
				    ((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName();
    
				boolean foundMatch = false;
				if (primkey.equals(pkfType.getName())) {
				    foundMatch = true;
				} else {
				    foundMatch = false;
				}
      
				if (foundMatch) {
				    result.addGoodDetails(smh.getLocalString
							  ("tests.componentNameConstructor",
							   "For [ {0} ]",
							   new Object[] {compName.toString()}));
				    result.passed(smh.getLocalString
						  (getClass().getName() + ".passed",
						   "The type of the primkey-field [ {0} ] is the same as the primary key type [ {1} ] for bean [ {2} ]",
						   new Object[] {((EjbCMPEntityDescriptor)descriptor).getPrimaryKeyFieldDesc().getName(),primkey,descriptor.getName()}));
				} else {
				    result.addErrorDetails(smh.getLocalString
							   ("tests.componentNameConstructor",
							    "For [ {0} ]",
							    new Object[] {compName.toString()}));
				    result.failed(smh.getLocalString
						  (getClass().getName() + ".failed",
						   "The type of the primkey-field [ {0} ] is not the same as the primary key type [ {1} ] for bean [ {2} ]",
						   new Object[] {((EjbCMPEntityDescriptor)descriptor).getPrimaryKeyFieldDesc().getName(),primkey,descriptor.getName()}));
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
						 (getClass().getName() + ".notApplicable2",
						  "Primkey field not defined for [ {0} ] bean.",
						  new Object[] {descriptor.getName()}));
			}
		    } catch (NullPointerException e) {
			result.addNaDetails(smh.getLocalString
					    ("tests.componentNameConstructor",
					     "For [ {0} ]",
					     new Object[] {compName.toString()}));
			result.notApplicable(smh.getLocalString
					     (getClass().getName() + ".notApplicable3",
					      "Primkey field not defined within [ {0} ] bean.",
					      new Object[] {descriptor.getName()}));
		    } catch (NoSuchFieldException e) {
			result.addNaDetails(smh.getLocalString
					    ("tests.componentNameConstructor",
					     "For [ {0} ]",
					     new Object[] {compName.toString()}));
			result.notApplicable(smh.getLocalString
					     (getClass().getName() + ".notApplicable2",
					      "Primkey field [ {0} ] not defined within [ {1} ] bean.",
					      new Object[] {((EjbCMPEntityDescriptor)descriptor).getPrimaryKeyFieldDesc().getName(),descriptor.getName()}));
		    }
		} catch (ClassNotFoundException e) {
		    Verifier.debug(e);
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
				  (getClass().getName() + ".failedException",
				   "Error: EJB class [ {0} ] does not exist or is not loadable within bean [ {1} ]",
				   new Object[] {descriptor.getEjbClassName(),descriptor.getName()}));
		}
	    } else {
		result.addNaDetails(smh.getLocalString
				    ("tests.componentNameConstructor",
				     "For [ {0} ]",
				     new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     (getClass().getName() + ".notApplicable1",
				      "Expected persistence type [ {0} ], but bean [ {1} ] has persistence type [ {2} ]",
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
