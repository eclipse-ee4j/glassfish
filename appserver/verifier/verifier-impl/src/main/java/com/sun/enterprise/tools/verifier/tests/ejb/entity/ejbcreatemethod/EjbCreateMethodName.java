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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.ejbcreatemethod;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.lang.reflect.Method;
import java.util.Vector;

/**  
 * Entity Bean's ejbCreate(...) methods name test.
 * Each entity Bean class may define zero or more ejbCreate(...) methods. 
 * The number and signatures of a entity Bean's create methods are specific 
 * to each EJB class. The method signatures must follow these rules: 
 * 
 * The method name must be ejbCreate. 
 */
public class EjbCreateMethodName extends EjbTest implements EjbCheck { 

    Result result = null;
    ComponentNameConstructor compName = null;
    int foundAtLeastOne = 0;
  
    /** 
     * Entity Bean's ejbCreate(...) methods name test.
     * Each entity Bean class may define zero or more ejbCreate(...) methods. 
     * The number and signatures of a entity Bean's create methods are specific 
     * to each EJB class. The method signatures must follow these rules: 
     * 
     * The method name must be ejbCreate. 
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
	    if(descriptor.getHomeClassName() != null && !"".equals(descriptor.getHomeClassName()))
		oneFailed = commonToBothInterfaces(descriptor.getHomeClassName(),(EjbEntityDescriptor)descriptor);  
	    if (oneFailed == false) {
		if(descriptor.getLocalHomeClassName() != null && !"".equals(descriptor.getLocalHomeClassName()))
		    oneFailed = commonToBothInterfaces(descriptor.getLocalHomeClassName(),(EjbEntityDescriptor)descriptor);
	    }
	    if (oneFailed) {
		result.setStatus(result.FAILED);
	    } else if (foundAtLeastOne == 0) {
		result.setStatus(result.NOT_APPLICABLE);
	    } else {
		result.setStatus(result.PASSED);
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
    
    private boolean commonToBothInterfaces(String component, EjbEntityDescriptor descriptor) {
	
	boolean oneFailed = false;
	boolean createExists = false;
	try {
	    VerifierTestContext context = getVerifierContext();
	    ClassLoader jcl = context.getClassLoader();
	    Class c = Class.forName(descriptor.getEjbClassName(), false, getVerifierContext().getClassLoader());
	    Class home = Class.forName(component, false, getVerifierContext().getClassLoader());
	    Method [] homeMethods = home.getDeclaredMethods();
	    Vector<String> createMethodSuffix = new Vector<String>();
	    for (int i = 0; i < homeMethods.length; i++) {
		// The method name must start with create. 
		if (homeMethods[i].getName().startsWith("create")) {
		    createMethodSuffix.addElement( homeMethods[i].getName().substring(6));
		    createExists = true;
		}
	    }
	    
	    // start do while loop here....
	    do {
		boolean found = false;
		Method [] methods = c.getDeclaredMethods();
		for (int j = 0; j < methods.length; j++) {
		    // The method name must start with ejbCreate. 
		    if (methods[j].getName().startsWith("ejbCreate")) {
			String matchSuffix = methods[j].getName().substring(9);
			for (int k = 0; k < createMethodSuffix.size(); k++) {
			    found = false;
			    if (matchSuffix.equals(createMethodSuffix.elementAt(k))) {
				found = true;
				foundAtLeastOne++;
				// now display the appropriate results for this particular ejbCreate
				// method
				result.addGoodDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
				result.addGoodDetails(smh.getLocalString
						      (getClass().getName() + ".debug1",
						       "For EJB Class [ {0} ] method [ {1} ]",
						       new Object[] {descriptor.getEjbClassName(),methods[j].getName()}));
				result.addGoodDetails(smh.getLocalString
						      (getClass().getName() + ".passed",
						       "[ {0} ] declares [ {1} ] method.",
						       new Object[] {descriptor.getEjbClassName(),methods[j].getName()}));
				break;
			    }
			}
			if (found == false) {
			    result.addErrorDetails(smh.getLocalString
						   ("tests.componentNameConstructor",
						    "For [ {0} ]",
						    new Object[] {compName.toString()}));
			    result.failed(smh.getLocalString
					  (getClass().getName() + ".failedException1",
					   "Error: no create{0}() method found corresponding to ejbCreate{1}() method ",
					   new Object[] {matchSuffix, matchSuffix}));
			    oneFailed = true;
			    break;
			}
		    }
		}
		if (oneFailed == true)
		    break;
	    } while (((c = c.getSuperclass()) != null) && (foundAtLeastOne == 0));
	    
	    if ( createExists == false){
		result.addNaDetails(smh.getLocalString
						  ("tests.componentNameConstructor",
						   "For [ {0} ]",
						   new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     (getClass().getName() + ".notApplicable1",
				      "[ {0} ] does not declare any ejbCreate(...) methods.",
				      new Object[] {descriptor.getEjbClassName()}));
		oneFailed = false;
	    }
	    if (foundAtLeastOne == 0 && createExists == true){
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.failed(smh.getLocalString
			      (getClass().getName() + ".failedException1",
			       "Error: no ejbCreate<Method> method for corresponding create<Method> method found!",
			       new Object[] {}));
		oneFailed = false;
	    }

	    return oneFailed;
	} catch (ClassNotFoundException e) {
	    Verifier.debug(e);
	    result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
	    result.failed(smh.getLocalString
			  (getClass().getName() + ".failedException",
			   "Error: [ {0} ] class not found.",
			   new Object[] {descriptor.getEjbClassName()}));
	    oneFailed = true;
	    return oneFailed;
	}  
    }
    
}
