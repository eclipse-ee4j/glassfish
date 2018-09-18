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

package com.sun.enterprise.tools.verifier.tests.ejb.homeintf;

import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.lang.reflect.Modifier;

/**
 * All enterprise beans home interface's must be declared as public.
 */
abstract public class HomeInterfacePublic extends EjbTest implements EjbCheck { 
 
    /** Method tells the name of the home interface class that called this test
     */
    abstract protected String getHomeInterfaceName(EjbDescriptor descriptor);
    
    /** 
     * All enterprise beans home interface's must be declared as public.
     *   
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */

   
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	if(getHomeInterfaceName(descriptor) == null || "".equals(getHomeInterfaceName(descriptor))){
            result.addNaDetails(smh.getLocalString
                        ("tests.componentNameConstructor", "For [ {0} ]",
                         new Object[] {compName.toString()}));
            result.notApplicable(smh.getLocalString
                       ("com.sun.enterprise.tools.verifier.tests.ejb.localinterfaceonly.notapp",
                        "Not Applicable because, EJB [ {0} ] has Local Interfaces only.",
                                          new Object[] {descriptor.getEjbClassName()}));

	    return result;
	}


	if ((descriptor instanceof EjbSessionDescriptor) ||
	    (descriptor instanceof EjbEntityDescriptor)) {
 
	    try {
		VerifierTestContext context = getVerifierContext();
		ClassLoader jcl = context.getClassLoader();
		Class c = Class.forName(getClassName(descriptor), false, jcl);

		// remote interface must be defined as public
		boolean isPublic = false;
		int modifiers = c.getModifiers();
		if (Modifier.isPublic(modifiers)) {
		    isPublic = true;
		}
 
		// it extends the proper EJBHome, but is it's modifier public
		if (!isPublic){
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
				  (getClass().getName() + ".failed",
				   "Error: [ {0} ] is not defined as public.  All enterprise beans home interfaces must be defined as public.  [ {1} ] is not a valid home interface.",
				   new Object[] {getClassName(descriptor),getClassName(descriptor)}));
		} else {
		    result.addGoodDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));
			
		result.passed(smh.getLocalString
				  (getClass().getName() + ".passed",
				   "[ {0} ] properly declares the home interface as public.",
				   new Object[] {getClassName(descriptor)}));
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
			       new Object[] {getClassName(descriptor)}));
	    }  
	    return result;
 
	} else {
	    result.addNaDetails(smh.getLocalString
				("tests.componentNameConstructor",
				 "For [ {0} ]",
				 new Object[] {compName.toString()}));
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "[ {0} ] expected {1} bean or {2} bean, but called with {3}.",
				  new Object[] {getClass(),"Session","Entity",descriptor.getName()}));
	    return result;
	}
    }

    private String getClassName(EjbDescriptor descriptor) {
	return getHomeInterfaceName(descriptor);
    } 
}
