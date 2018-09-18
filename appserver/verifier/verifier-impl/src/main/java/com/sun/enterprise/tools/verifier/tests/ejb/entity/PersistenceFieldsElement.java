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
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.deployment.common.Descriptor;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.lang.reflect.Field;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;

/** 
 * Container-managed fields test.
 * If the enterprise bean is a Entity Bean  w/Container managed persistence
 * the Bean provider must specify container managed fields in the 
 * "persistent-fields" element.
 */
public class PersistenceFieldsElement extends EjbTest implements EjbCheck { 

    /**
     * Container-managed fields test.
     * If the enterprise bean is a Entity Bean  w/Container managed persistence
     * the Bean provider must specify container managed fields in the
     * "persistent-fields" element.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor instanceof EjbEntityDescriptor) {
	    String persistentType = 
		((EjbEntityDescriptor)descriptor).getPersistenceType();
	    if (EjbEntityDescriptor.CONTAINER_PERSISTENCE.equals(persistentType)) {

                // this test apply only to 1.x cmp beans, in 2.x fields are virtual fields only
                if (EjbCMPEntityDescriptor.CMP_1_1!=((EjbCMPEntityDescriptor) descriptor).getCMPVersion()) {
		    result.addNaDetails(smh.getLocalString
					("tests.componentNameConstructor",
					 "For [ {0} ]",
					 new Object[] {compName.toString()}));
	            result.notApplicable(smh.getLocalString
				 ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.CMPTest.notApplicable3",
				  "Test do not apply to this cmp-version of container managed persistence EJBs"));
        	    return result;
                }
            logger.log(Level.FINE, getClass().getName() + ".debug1",
                    new Object[] {persistentType});
		// RULE: Entity w/Container managed persistence bean provider must
		//       specify container managed fields in the persistent-fields
		//       element
		Set persistentFields =
		    ((EjbCMPEntityDescriptor)descriptor).getPersistenceDescriptor().getCMPFields();
		Iterator iterator = persistentFields.iterator();

		// check class to see if fields actually exist
		try {
		    VerifierTestContext context = getVerifierContext();
		    ClassLoader jcl = context.getClassLoader();
		    Class c = Class.forName(descriptor.getEjbClassName(), false, getVerifierContext().getClassLoader());

		    Descriptor persistentField;
		    Field field;
		    boolean oneFailed = false;
		    while (iterator.hasNext()) {
			persistentField = (Descriptor)iterator.next();
			try {
			    field = c.getField(persistentField.getName());
			    result.addGoodDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
			    result.passed(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "[ {0} ] field found in [ {1} ]",
					   new Object[] {((Descriptor)persistentField).getName(),descriptor.getEjbClassName()}));
			} catch (NoSuchFieldException e) {
			    result.addErrorDetails(smh.getLocalString
						   ("tests.componentNameConstructor",
						    "For [ {0} ]",
						    new Object[] {compName.toString()}));
			    result.failed(smh.getLocalString
					  (getClass().getName() + ".failedException1",
					   "Error: NoSuchFieldException: [ {0} ] not found in [ {1} ]",
					   new Object[] {((Descriptor)persistentField).getName(),descriptor.getEjbClassName()}));
			    if (!oneFailed) {
				oneFailed = true;
			    }

			} catch (SecurityException e) {
			    result.addErrorDetails(smh.getLocalString
						   ("tests.componentNameConstructor",
						    "For [ {0} ]",
						    new Object[] {compName.toString()}));
			    result.failed(smh.getLocalString
					  (getClass().getName() + ".failedException2",
					   "Error: SecurityException: [ {0} ] not found in [ {1} ]",
					   new Object[] {((Descriptor)persistentField).getName(),descriptor.getEjbClassName()}));
			    if (!oneFailed) {
				oneFailed = true;
			    }
			}
		    }
		    if (oneFailed) {
			result.setStatus(Result.FAILED);
		    } else {
			result.setStatus(Result.PASSED);
		    }
		} catch (ClassNotFoundException e) {
		    Verifier.debug(e);
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
				  (getClass().getName() + ".failedException3",
				   "Error: Fields don't exist or are not loadable within bean [ {0} ]",
				   new Object[] {descriptor.getName()}));
		}
	    } else if (EjbEntityDescriptor.BEAN_PERSISTENCE.equals(persistentType))
		{
		    result.addNaDetails(smh.getLocalString
					("tests.componentNameConstructor",
					 "For [ {0} ]",
					 new Object[] {compName.toString()}));
		    result.notApplicable(smh.getLocalString
					 (getClass().getName() + ".notApplicable1",
					  "Expected persistence type [ {0} ], but [ {1} ] bean has persistence type [ {2} ]",
					  new Object[] {EjbEntityDescriptor.CONTAINER_PERSISTENCE,descriptor.getName(),persistentType}));
		} else {
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
				  (getClass().getName() + ".failed",
				   "Error: [ {0} ] is not valid persistentType within bean [ {1} ]",
				   new Object[] {persistentType, descriptor.getName()}));
		} 
	    return result;
	} else {
	    result.addNaDetails(smh.getLocalString
				("tests.componentNameConstructor",
				 "For [ {0} ]",
				 new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "[ {0} ] expected {1} bean, but called with {2} bean.",
				  new Object[] {getClass(),"Entity","Session"}));
	    return result;
	} 
    }
}
