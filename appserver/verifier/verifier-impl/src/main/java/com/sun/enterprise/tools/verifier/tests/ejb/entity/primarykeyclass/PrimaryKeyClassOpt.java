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
 * Entity bean's Primary Key Class test. 
 * If the enterprise bean is a Entity Bean, the Bean provider specifies
 * the fully qualified name of the Entity bean's primary key class in the 
 * "<prim-key-class>" element. The Bean provider 'must' specify the primary key
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
 * primary key class in the deployment descriptor as the type 
 * java.lang.Object.
 *  
 * The primary key class is specified at deployment time when the Bean Provider
 * develops enterprise beans that is intended to be used with multiple back-ends
 * that provide persistence, and when these multiple back-ends require different
 * primary key structures.
 */
public class PrimaryKeyClassOpt extends EjbTest implements EjbCheck { 

    Result result = null;
    ComponentNameConstructor compName = null;
    /** 
     * Entity bean's Primary Key Class test.
     * If the enterprise bean is a Entity Bean, the Bean provider specifies
     * the fully qualified name of the Entity bean's primary key class in the
     * "<prim-key-class>" element. The Bean provider 'must' specify the primary 
     * key class for an Entity with bean managed persistence, and 'may' (but is 
     * not required to) specify the primary key class for an Entity with
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

	result = getInitializedResult();
	compName = getVerifierContext().getComponentNameConstructor();

	boolean oneFailed = false;

	if (descriptor instanceof EjbEntityDescriptor) {
	    String persistence = 
		((EjbEntityDescriptor)descriptor).getPersistenceType();
	    if (EjbEntityDescriptor.CONTAINER_PERSISTENCE.equals(persistence)) {
		String primkey = 
		    ((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName();

		// primkey can be not set, via setting xml element
                // <prim-key-class> to "java.lang.Object"
		if (primkey.equals("java.lang.Object")) {
		    if(descriptor.getHomeClassName() != null && !"".equals(descriptor.getHomeClassName())) {
			oneFailed = commonToBothInterfaces(descriptor.getHomeClassName(), descriptor);
		    }
		    if(oneFailed == false) {
			if(descriptor.getLocalHomeClassName() != null && !"".equals(descriptor.getLocalHomeClassName())) {
			    oneFailed = commonToBothInterfaces(descriptor.getLocalHomeClassName(), descriptor);
			}
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
				      "Entity Bean with [ {0} ] managed persistence, primkey mandatory.",
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

    /** 
     * This method is responsible for the logic of the test. It is called for both local and remote interfaces.
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @param home for the Home interface of the Ejb. 
     * @return boolean the results for this assertion i.e if a test has failed or not
     */


    private boolean commonToBothInterfaces(String home,EjbDescriptor descriptor) {
	boolean oneFailed = false;
	try {
	    VerifierTestContext context = getVerifierContext();
	    ClassLoader jcl = context.getClassLoader();
	    Class c = Class.forName(home, false, getVerifierContext().getClassLoader());
	    Method [] ejbFinderMethods = c.getDeclaredMethods();
	    boolean paramValid = false;
	    for (int j = 0; j < ejbFinderMethods.length; ++j) {
		// reset all booleans for next method within the loop
		if (ejbFinderMethods[j].getName().equals("findByPrimaryKey")) {
		    Class [] ejbFinderMethodParameterTypes;
		    ejbFinderMethodParameterTypes = ejbFinderMethods[j].getParameterTypes();
		    for (int k = 0; k < ejbFinderMethodParameterTypes.length; ++k) {
			if (ejbFinderMethodParameterTypes[k].getName().equals("java.lang.Object")) {
			    paramValid = true;
			    break;
			}
		    }
		}
	    }
	    
	    if (paramValid) {
		result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));	
		result.passed(smh.getLocalString
			      (getClass().getName() + ".passed",
			       "findByPrimaryKey method properly defines method parameter [ {0} ]",
			       new Object[] {"java.lang.Object"}));
	    } else {
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.failed(smh.getLocalString
			      (getClass().getName() + ".failed",
			       "Error: findByPrimaryKey method does not properly define method parameter [ {0} ]",
			       new Object[] {"java.lang.Object"}));
	    }
	    return oneFailed;
	} catch (Exception e) {
	    Verifier.debug(e);
	    result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
	    result.failed(smh.getLocalString
			  (getClass().getName() + ".failedException",
			   "Error: Loading Home interface class [ {0} ]",
			   new Object[] {home}));
	    return oneFailed;
	}
    }
}
