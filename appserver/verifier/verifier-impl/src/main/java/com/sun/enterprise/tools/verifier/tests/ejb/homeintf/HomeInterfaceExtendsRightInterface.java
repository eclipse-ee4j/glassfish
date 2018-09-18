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

/** 
 * Extends the EJBHome Interface test.  
 * All enterprise beans home interface's must extend the EJBHome interface.
 */
abstract public class HomeInterfaceExtendsRightInterface extends EjbTest implements EjbCheck { 
 
    /** Method tells the name of the home interface class that called this test
     */
    abstract protected String getHomeInterfaceName(EjbDescriptor descriptor);
    abstract protected String getSuperInterface();
    
    /** 
     * Extends the EJBHome Interface test.  
     * All enterprise beans home interface's must extend the EJBHome interface.
     *   
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */

  
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
	String str = null;

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
		str = getSuperInterface();
                if (isImplementorOf(c, str)) {
		    // it extends the proper EJBHome
		    result.addGoodDetails(smh.getLocalString
					  ("tests.componentNameConstructor",
					   "For [ {0} ]",
					   new Object[] {compName.toString()}));	
		    result.passed(smh.getLocalString
				  (getClass().getName() + ".passed",
				   "[ {0} ] properly extends the " + str + "interface.",
				   new Object[] {getClassName(descriptor)}));
                } else {
		    result.addErrorDetails(smh.getLocalString
					   ("tests.componentNameConstructor",
					    "For [ {0} ]",
					    new Object[] {compName.toString()}));
		    result.failed(smh.getLocalString
				  (getClass().getName() + ".failed",
				   "Error: [ {0} ] does not properly extend the  " + str +
				   " interface.  All enterprise beans home interfaces must extend the  " + str + 
				   " interface.  [ {1} ] is not a valid home interface.",
				   new Object[] {getClassName(descriptor),getClassName(descriptor)}));
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
