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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.pkmultiplefield;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.FieldDescriptor;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Enterprise Java Bean primary key class public fields test.  
 * The primary key class must declare all fields within the class as public.
 */
public class PrimaryKeyClassFieldsPublic extends EjbTest implements EjbCheck { 


    /**
     * Enterprise Java Bean primary key class public fields test.  
     * The primary key class must declare all fields within the class as public.
     *   
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor instanceof EjbEntityDescriptor) {
	    String persistence =
		((EjbEntityDescriptor)descriptor).getPersistenceType();
	    if (EjbEntityDescriptor.CONTAINER_PERSISTENCE.equals(persistence)) { 
		// do we have  primekey that maps to single or multiple fields in entity        // bean class?  if primekey-field exist, then primekey maps to single
		// field in entity bean class and this test in notApplicable
		try {
                    FieldDescriptor fd = ((EjbCMPEntityDescriptor)descriptor).getPrimaryKeyFieldDesc();
                    if (fd != null) {
                        String pkf = fd.getName();
                        if (pkf.length() > 0) {
			    // N/A case
			    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
			    result.notApplicable(smh.getLocalString
					         (getClass().getName() + ".notApplicable2",
					          "Entity Bean [ {0} ] with primekey-field non-blank, test not applicable.",
					          new Object[] {descriptor.getEjbClassName()}));
                        }
		    } else {
			try {
			    VerifierTestContext context = getVerifierContext();
			    ClassLoader jcl = context.getClassLoader();
			    Class c = Class.forName(((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName(), false, getVerifierContext().getClassLoader());
      
			    boolean oneFailed = false;
			    boolean badField = false;
			    Field [] fields = c.getDeclaredFields();
			    for (int i = 0; i < fields.length; i++) {
				badField = false;
				int modifiers = fields[i].getModifiers();
				if (Modifier.isPublic(modifiers)) {
				    continue;
				} else {
				    if (!oneFailed) {
					oneFailed = true;
				    }
				    badField = true;
				}
          
				if (badField) {
				    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
				    result.failed(smh.getLocalString
						  (getClass().getName() + ".failed",
						   "Error: Field [ {0} ] defined within primary key class [ {1} ] is not defined as public.",
						   new Object[] {fields[i].getName(),((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName()}));
				}
			    }
			    if (!oneFailed) {
				result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
				result.passed(smh.getLocalString
					      (getClass().getName() + ".passed",
					       "This primary key class [ {0} ] has defined all fields as public.",
					       new Object[] {((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName()}));
			    }
			} catch (ClassNotFoundException e) {
			    Verifier.debug(e);
			    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
			    result.failed(smh.getLocalString
					  (getClass().getName() + ".failedException",
					   "Error: [ {0} ] class not found.",
					   new Object[] {((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName()}));
                        } catch (Throwable t) {
			    result.addWarningDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                            result.warning(smh.getLocalString
                                (getClass().getName() + ".warningException",
                                 "Warning: [ {0} ] class encountered [ {1} ]. Cannot access fields of class [ {2} ] which is external to [ {3} ].",
                                 new Object[] {(descriptor).getEjbClassName(),t.toString(), t.getMessage(), descriptor.getEjbBundleDescriptor().getModuleDescriptor().getArchiveUri()}));
			}  
		    }  
		} catch (NullPointerException e) {
		    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
					 (getClass().getName() + ".failedException2",
					  "Error: Primkey field not defined within [ {0} ] bean.",
					  new Object[] {descriptor.getName()}));
		}
		return result;

	    } else { //if (BEAN_PERSISTENCE.equals(persistence)
		result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     (getClass().getName() + ".notApplicable1",
				      "Expected [ {0} ] managed persistence, but [ {1} ] bean has [ {2} ] managed persistence.", 
				      new Object[] {EjbEntityDescriptor.CONTAINER_PERSISTENCE,descriptor.getName(),persistence}));
		return result;
	    } 
 
	} else {
	    result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "{0} expected {1} bean, but called with {2}.",
				  new Object[] {getClass(),"Entity","Session"}));
	    return result;
	}
    }
}
