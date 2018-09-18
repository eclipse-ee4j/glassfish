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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.ejbfindermethod;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbUtils;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.lang.reflect.Method;

/** 
 * Single-object ejbFind<METHOD>(...) methods throws ObjectNotFoundException 
 *  test.  
 *
 *   EJB class contains all ejbFind<METHOD>(...) methods declared in the bean 
 *   class.  
 *
 *   The signatures of the finder methods must follow the following rules: 
 *
 *     A finder method name must start with the prefix ``ejbFind'' 
 *     (e.g. ejbFindByPrimaryKey, ejbFindLargeAccounts, ejbFindLateShipments). 
 * 
 *     The ObjectNotFoundException is a subclass of FinderException. It is 
 *     thrown by the ejbFind<METHOD>(...) methods to indicate that the 
 *     requested entity object does not exist. 
 *
 *     Only single-object finders (see Subsection 9.1.8) may throw this 
 *     exception.   Multi-object finders must not throw this exception.
 * 
 */
public class EjbFinderMethodObjectNotFoundException extends EjbTest implements EjbCheck { 


    /** 
     * Single-object ejbFind<METHOD>(...) methods throws ObjectNotFoundException 
     *  test.  
     *
     *   EJB class contains all ejbFind<METHOD>(...) methods declared in the bean 
     *   class.  
     *
     *   The signatures of the finder methods must follow the following rules: 
     *
     *     A finder method name must start with the prefix ``ejbFind'' 
     *     (e.g. ejbFindByPrimaryKey, ejbFindLargeAccounts, ejbFindLateShipments). 
     *
     *     The ObjectNotFoundException is a subclass of FinderException. It is 
     *     thrown by the ejbFind<METHOD>(...) methods to indicate that the 
     *     requested entity object does not exist. 
     *
     *     Only single-object finders (see Subsection 9.1.8) may throw this 
     *     exception.   Multi-object finders must not throw this exception.
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
	    if (EjbEntityDescriptor.BEAN_PERSISTENCE.equals(persistence)) {

		boolean okayToThrowObjectNotFoundException = false;
		boolean throwsObjectNotFoundException = false;
		boolean oneFailed = false;
		int findMethodModifiers = 0;
		int foundAtLeastOne = 0;
		try {
		    // retrieve the EJB Class Methods
		    VerifierTestContext context = getVerifierContext();
		ClassLoader jcl = context.getClassLoader();
		    Class EJBClass = Class.forName(descriptor.getEjbClassName(), false, getVerifierContext().getClassLoader());
                    // start do while loop here....
                    do {
			Method [] ejbFinderMethods = EJBClass.getDeclaredMethods();
			String primaryKeyType = ((EjbEntityDescriptor)descriptor).getPrimaryKeyClassName();
    	  
			for (int j = 0; j < ejbFinderMethods.length; ++j) {
			    if (ejbFinderMethods[j].getName().startsWith("ejbFind")) {
				okayToThrowObjectNotFoundException = false;
				throwsObjectNotFoundException = false;
				Class returnByPrimaryKeyValue = ejbFinderMethods[j].getReturnType();
				foundAtLeastOne++;
				// Only single-object finders (see Subsection 9.1.8) may throw this 
				// exception.   Multi-object finders must not throw this exception.
				if (returnByPrimaryKeyValue.getName().equals(primaryKeyType))  {
				    okayToThrowObjectNotFoundException = true;
				} else if ((returnByPrimaryKeyValue.getName().equals("java.util.Collection"))  ||
					   (returnByPrimaryKeyValue.getName().equals("java.util.Enumeration"))) {
				    okayToThrowObjectNotFoundException = false;
				}
  
				// now check exceptions, if it's not okay to throw
				// ObjectNotFoundException, then flag this as an error, otherwise
				// test passes or is not applicable
				if (!okayToThrowObjectNotFoundException) {
				// get the exceptions and see if this method throw 
				// ObjectNotFoundException when it was not allowed to
				    Class [] exceptions = ejbFinderMethods[j].getExceptionTypes();
				    if (EjbUtils.isValidObjectNotFoundExceptionException(exceptions)) {
					throwsObjectNotFoundException = true;
				    }
				}
  
  
				if ((okayToThrowObjectNotFoundException) || ((!okayToThrowObjectNotFoundException) && (!throwsObjectNotFoundException))) {
				    result.addGoodDetails(smh.getLocalString
							  ("tests.componentNameConstructor",
							   "For [ {0} ]",
							   new Object[] {compName.toString()}));
				    result.addGoodDetails(smh.getLocalString
							  (getClass().getName() + ".debug1",
							   "For EJB Class [ {0} ] Finder Method [ {1} ]",
							   new Object[] {EJBClass.getName(),ejbFinderMethods[j].getName()}));
				    result.addGoodDetails(smh.getLocalString
							  (getClass().getName() + ".passed",
							   "An [ {0} ] method was found with valid exception types declared in throws clause.",
							   new Object[] {ejbFinderMethods[j].getName()}));
				} else if ((!okayToThrowObjectNotFoundException) && throwsObjectNotFoundException) {
				    oneFailed = true;
				    result.addErrorDetails(smh.getLocalString
							   ("tests.componentNameConstructor",
							    "For [ {0} ]",
							    new Object[] {compName.toString()}));
				    result.addErrorDetails(smh.getLocalString
							   (getClass().getName() + ".debug1",
							    "For EJB Class [ {0} ] Finder Method [ {1} ]",
							    new Object[] {EJBClass.getName(),ejbFinderMethods[j].getName()}));
				    result.addErrorDetails(smh.getLocalString
							   (getClass().getName() + ".failed",
							    "Error: An [ {0} ] method was found, but [ {1} ] throws ObjectNotFoundException.  Multi-object finder methods must not throw this exception.",
							    new Object[] {ejbFinderMethods[j].getName(),ejbFinderMethods[j].getName()}));
				}			 
			    }
			}
                    } while (((EJBClass = EJBClass.getSuperclass()) != null) && (foundAtLeastOne == 0));
  
		    if (foundAtLeastOne == 0) {
			result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
			result.notApplicable(smh.getLocalString
					     (getClass().getName() + ".notApplicable1",
					      "[ {0} ] does not declare any ejbFind<METHOD>(...) methods.",
					      new Object[] {descriptor.getEjbClassName()}));
		    }
  
		} catch (ClassNotFoundException e) {
		    Verifier.debug(e);
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
				  (getClass().getName() + ".failedException",
				   "Error: EJB Class [ {1} ] does not exist or is not loadable.",
				   new Object[] {descriptor.getEjbClassName()}));
		    oneFailed = true;
		}
    
		if (oneFailed) {
		    result.setStatus(result.FAILED);
                } else if (foundAtLeastOne == 0) {
                    result.setStatus(result.NOT_APPLICABLE);
		} else {
		    result.setStatus(result.PASSED);
		}
    
		return result;

	    } else  { //if (CONTAINER_PERSISTENCE.equals(persistence))
		result.addNaDetails(smh.getLocalString
				    ("tests.componentNameConstructor",
				     "For [ {0} ]",
				     new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     (getClass().getName() + ".notApplicable2",
				      "Expected [ {0} ] managed persistence, but [ {1} ] bean has [ {2} ] managed persistence.",
				      new Object[] {EjbEntityDescriptor.BEAN_PERSISTENCE,descriptor.getName(),persistence}));
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
