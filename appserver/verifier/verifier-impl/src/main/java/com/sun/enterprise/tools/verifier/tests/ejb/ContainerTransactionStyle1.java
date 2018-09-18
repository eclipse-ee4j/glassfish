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

package com.sun.enterprise.tools.verifier.tests.ejb;

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.util.Enumeration;

/** 
 * ContainerTransaction Style 1 - Each container transaction element consists 
 * of a list of one or more method elements, and the trans-attribute element. 
 * The container transaction element specifies that all the listed methods are 
 * assigned the specified transaction attribute value.
 *
 * Style 1: 
 *    <method> 
 *      <ejb-name> EJBNAME</ejb-name> 
 *      <method-name>*</method-name> 
 *    </method> 
 * This style is used to specify a default value of the transaction attribute 
 * for the methods for which there is no Style 2 or Style 3 element specified. 
 * There must be at most one container transaction element that uses the Style 1
 * method element for a given enterprise bean.
 */
public class ContainerTransactionStyle1 extends EjbTest implements EjbCheck { 


    /**
     * Each container transaction element consists of a list of one or more 
     * method elements, and the trans-attribute element. The container transaction 
     * element specifies that all the listed methods are assigned the specified 
     * transaction attribute value.
     *
     * Style 1: 
     *    <method> 
     *      <ejb-name> EJBNAME</ejb-name> 
     *      <method-name>*</method-name> 
     *    </method> 
     * This style is used to specify a default value of the transaction attribute 
     * for the methods for which there is no Style 2 or Style 3 element specified. 
     * There must be at most one container transaction element that uses the Style 1
     * method element for a given enterprise bean.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	// hack try/catch block around test, to exit gracefully instead of
	// crashing verifier on getMethodDescriptors() call, XML mods cause
	// java.lang.ClassNotFoundException: verifier.ejb.hello.BogusEJB
	// Replacing <ejb-class>verifier.ejb.hello.HelloEJB with
	//  <ejb-class>verifier.ejb.hello.BogusEJB...
	try  {
	    boolean oneFailed = false;
	    boolean na = false;
	    int foundWildCard = 0;
            if (!descriptor.getMethodContainerTransactions().isEmpty()) {
		for (Enumeration ee = descriptor.getMethodContainerTransactions().keys(); ee.hasMoreElements();) {
		    MethodDescriptor methodDescriptor = (MethodDescriptor) ee.nextElement();
  
		    if (methodDescriptor.getName().equals(MethodDescriptor.ALL_METHODS)) {
			foundWildCard++;
		    }
		}

		// report for this particular set of Container tx's
                // DOL only saves one container tx with "*", so can't fail...
		if (foundWildCard == 1) {
		    result.addGoodDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
		    result.passed(smh.getLocalString
				  (getClass().getName() + ".passed",
				   "Container Transaction method name [ {0} ] defined only once in [ {1} ] bean.",
				   new Object[] {MethodDescriptor.ALL_METHODS, descriptor.getName()}));
		} else if (foundWildCard > 1) {
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
				  (getClass().getName() + ".failed",
				   "Error: Container Transaction method name [ {0} ] is defined [ {1} ] times in [ {2} ] bean.  Method name container transaction style [ {3} ] is allowed only once per bean.",
				   new Object[] {MethodDescriptor.ALL_METHODS, new Integer(foundWildCard), descriptor.getName(),MethodDescriptor.ALL_METHODS}));
		} else {
		    result.addNaDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
		    result.notApplicable(smh.getLocalString
					 (getClass().getName() + ".notApplicable1",
					  "Container Transaction method name [ {0} ] not defined in [ {1} ] bean.",
					  new Object[] {MethodDescriptor.ALL_METHODS, descriptor.getName()}));
		} 
		
	    } else {  // if (methodDescriptorsIterator.hasNext())
		result.addNaDetails(smh.getLocalString
				      ("tests.componentNameConstructor",
				       "For [ {0} ]",
				       new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     (getClass().getName() + ".notApplicable",
				      "There are no method permissions within this bean [ {0} ]", 
				      new Object[] {descriptor.getName()}));
	    }
	    return result; 
	} catch (Throwable t) {
	    result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
	    result.failed(smh.getLocalString
			  (getClass().getName() + ".failedException2",
			   "Error: [ {0} ] does not contain class [ {1} ] within bean [ {2} ]",
			   new Object[] {descriptor.getName(), t.getMessage(), descriptor.getName()}));
	    return result;
	}
    }
}
