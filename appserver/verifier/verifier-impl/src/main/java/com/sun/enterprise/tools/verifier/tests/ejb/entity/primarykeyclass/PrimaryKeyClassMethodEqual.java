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
 * Primary key class provide implementation of and equals() methods test.  
 *
 * Enterprise Bean's primary key class 
 * The class must provide suitable implementation of the equals(Object other) 
 * methods to simplify the management of the primary keys by client code.
 *
 */
public class PrimaryKeyClassMethodEqual extends EjbTest implements EjbCheck { 


    /** 
     * Primary key class provide implementation of equals() methods test.  
     *
     * Enterprise Bean's primary key class 
     * The class must provide suitable implementation of the equals(Object other) 
     * methods to simplify the management of the primary keys by client code.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor instanceof EjbEntityDescriptor) {
	    String transactionType = descriptor.getTransactionType();
	    if (EjbDescriptor.CONTAINER_TRANSACTION_TYPE.equals(transactionType)) {
		boolean hasDefinedEqaulsMethod = false;
		boolean oneFailed = false;
		int lc = 0;

		// RULE: Primary key class must defined equals(Object other) method
		try {
		    VerifierTestContext context = getVerifierContext();
		ClassLoader jcl = context.getClassLoader();
		    // retrieve the EJB primary key class 
		    Class c = Class.forName(((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName(), false, getVerifierContext().getClassLoader());
		    Method methods[] = c.getDeclaredMethods();
		    for (int i=0; i< methods.length; i++) {
			if (methods[i].getName().equals("equals")){
			    // this is the right primary key class method equals()
			    hasDefinedEqaulsMethod = true;
			    // used in output below
			    lc = i;
			    break;
			}
		    }

		    if (hasDefinedEqaulsMethod) {
			result.addGoodDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
			result.addGoodDetails(smh.getLocalString
					      (getClass().getName() + ".debug1",
					       "For EJB primary key class [ {0} ]",
					       new Object[] {((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName()}));
			result.addGoodDetails(smh.getLocalString
					      (getClass().getName() + ".passed",
					       "Primary key class method [ {0} ] was defined in the primary key class.",
					       new Object[] {methods[lc].getName()}));
		    } else if (!hasDefinedEqaulsMethod) {
			oneFailed = true;
			result.addErrorDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
			result.addErrorDetails(smh.getLocalString
					       (getClass().getName() + ".debug1",
						"For EJB primary key class [ {0} ]",
						new Object[] {((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName()}));
			result.addErrorDetails(smh.getLocalString
					       (getClass().getName() + ".failed",
						"Error: Primary key class method equal() was not defined in the primary key class."));
		    } 
        
		} catch (ClassNotFoundException e) {
		    Verifier.debug(e);
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
				  (getClass().getName() + ".failedException",
				   "Error: Primary Key Class [ {0} ] not found within bean [ {1} ]",
				   new Object[] {((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName(), descriptor.getName()})
				  );
		}

		if (oneFailed)  {
		    result.setStatus(result.FAILED);
		} else {
		    result.setStatus(result.PASSED);
		}
  
	    } else {
		// not container managed, but is a entity bean
		result.addNaDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     (getClass().getName() + ".notApplicable2",
				      "Bean [ {0} ] is not [ {1} ] managed, it is [ {2} ]  managed.",
				      new Object[] {descriptor.getName(),EjbDescriptor.CONTAINER_TRANSACTION_TYPE,transactionType}));
	    }

	    return result;

	} else {
	    result.addNaDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "{0} expected {1} bean, but called with {2} bean.",
				  new Object[] {getClass(),"Entity","Session"}));
	    return result;
	}
    }
}
