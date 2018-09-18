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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.primarykeyclass;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.lang.reflect.Method;

/** 
 * Entity bean's Primary Key Class return test. 
 * If the enterprise bean is a Entity Bean, the Bean provider specifies
 * the fully qualified name of the Entity bean's primary key class in the 
 * "primary-class" element. The Bean provider 'must' specify the primary key
 * class for an Entity with bean managed persistence, and 'may' (but is not
 * required to) specify the primary key class for an Entity with 
 * Container-managed persistence. 
 *
 * Special case: Unknown primary key class
 * In special situations, the Bean Provider may choose not to specify the 
 * primary key class for an entity bean with container-managed persistence. This
 * case happens if the Bean Provider wants to allow the Deployer to select the 
 * primary key fields at deployment time. The Deployer uses instructions 
 * supplied by the Bean Provider (these instructions are beyond the scope of 
 * the EJB spec.) to define a suitable primary key class.
 *  
 * In this special case, the type of the argument of the findByPrimaryKey method
 * must be declared as java.lang.Object, and the return value of ejbCreate() 
 * must be declared as java.lang.Object. The Bean Provider must specify the 
 * primary key class in the deployment descriptor as of the type 
 * java.lang.Object.
 *  
 * The primary key class is specified at deployment time when the Bean Provider
 * develops enterprise beans that is intended to be used with multiple back-ends
 * that provide persistence, and when these multiple back-ends require different
 * primary key structures.
 */
public class PrimaryKeyClassOptReturn extends EjbTest implements EjbCheck { 


    /** 
     * Entity bean's Primary Key Class return test.
     * If the enterprise bean is a Entity Bean, the Bean provider specifies
     * the fully qualified name of the Entity bean's primary key class in the
     * "primary-class" element. The Bean provider 'must' specify the primary key
     * class for an Entity with bean managed persistence, and 'may' (but is not
     * required to) specify the primary key class for an Entity with
     * Container-managed persistence.
     *
     * Special case: Unknown primary key class
     * In special situations, the Bean Provider may choose not to specify the 
     * primary key class for an entity bean with container-managed persistence. This
     * case happens if the Bean Provider wants to allow the Deployer to select the 
     * primary key fields at deployment time. The Deployer uses instructions 
     * supplied by the Bean Provider (these instructions are beyond the scope of 
     * the EJB spec.) to define a suitable primary key class.
     *  
     * In this special case, the type of the argument of the findByPrimaryKey method
     * must be declared as java.lang.Object, and the return value of ejbCreate() 
     * must be declared as java.lang.Object. The Bean Provider must specify the 
     * primary key class in the deployment descriptor as of the type 
     * java.lang.Object.
     *  
     * The primary key class is specified at deployment time when the Bean Provider
     * develops enterprise beans that is intended to be used with multiple back-ends
     * that provide persistence, and when these multiple back-ends require different
     * primary key structures.
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
		String primkey = 
		    ((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName();

		// primkey can be not set, via setting xml element
                // <prim-key-class> to "java.lang.Object"
                if (primkey.equals("java.lang.Object")) {
		    try {
			VerifierTestContext context = getVerifierContext();
		ClassLoader jcl = context.getClassLoader();
			Class c = Class.forName(descriptor.getEjbClassName(), false, getVerifierContext().getClassLoader());
			boolean returnsJLO = false;
                        // start do while loop here....
                        do {
			    Method [] methods = c.getDeclaredMethods();
			    returnsJLO = false;
			    for (int j = 0; j < methods.length; ++j) {
				if (methods[j].getName().equals("ejbCreate")) {
				// The return type must be java.lang.Object.
				    Class rt = methods[j].getReturnType();
				    if (rt.getName().equals("java.lang.Object")) {
					returnsJLO = true;
					break;
				    }
				}
			    }
                        } while (((c = c.getSuperclass()) != null) && (!returnsJLO));

			if (returnsJLO) {
			    result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
			    result.passed(smh.getLocalString
					  (getClass().getName() + ".passed",
					   "ejbCreate() method properly defines method return type [ {0} ]",
					   new Object[] {"java.lang.Object"}));
			} else {
			    result.addErrorDetails(smh.getLocalString
						   ("tests.componentNameConstructor",
						    "For [ {0} ]",
						    new Object[] {compName.toString()}));
			    result.failed(smh.getLocalString
					  (getClass().getName() + ".failed",
					   "ejbCreate() method does not properly define method return type [ {0} ]",
					   new Object[] {"java.lang.Object"}));
			}
		    } catch (Exception e) {
			Verifier.debug(e);
			result.addErrorDetails(smh.getLocalString
					       ("tests.componentNameConstructor",
						"For [ {0} ]",
						new Object[] {compName.toString()}));
			result.failed(smh.getLocalString
				      (getClass().getName() + ".failedException",
				       "Error: Loading bean class [ {0} ]",
				       new Object[] {descriptor.getEjbClassName()}));
			return result;
		    }
		} else {
		    result.addNaDetails(smh.getLocalString
					("tests.componentNameConstructor",
					 "For [ {0} ]",
					 new Object[] {compName.toString()}));
		    result.notApplicable(smh.getLocalString
					 (getClass().getName() + ".notApplicable1",
					  "Primary Key Class is [ {0} ]",
					  new Object[] {primkey}));
		}

		return result;

	    } else if (EjbEntityDescriptor.BEAN_PERSISTENCE.equals(persistence)) {
		result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     (getClass().getName() + ".notApplicable2",
				      "Entity bean with [ {0} ] managed persistence, primkey mandatory.",
				      new Object[] {persistence}));
		return result;
	    } 
	    else {
		result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     (getClass().getName() + ".notApplicable3",
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
				  "[ {0} ] expected {1} bean, but called with {2} bean.",
				  new Object[] {getClass(),"Entity","Session"}));
	    return result;
	} 
    }
}
