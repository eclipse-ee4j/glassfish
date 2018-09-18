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

package com.sun.enterprise.tools.verifier.tests.ejb.homeintf.remotehomeintf;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import com.sun.enterprise.tools.verifier.tests.ejb.RmiIIOPUtils;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

/**
 * Home Interface follows the standard rules for RMI-IIOP remote interfaces 
 * test.  
 *
 * All enterprise beans home interface's must follow the standard rules 
 * for RMI-IIOP remote interfaces.
 */
public class RemoteHomeInterfaceRmiIIOP extends EjbTest implements EjbCheck { 


    /** 
     * Home Interface follows the standard rules for RMI-IIOP remote interfaces 
     * test.  
     *
     * All enterprise beans home interface's must follow the standard rules 
     * for RMI-IIOP remote interfaces.
     *   
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
	
	if(descriptor.getHomeClassName() == null || "".equals(descriptor.getHomeClassName())) {
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                       ("com.sun.enterprise.tools.verifier.tests.ejb.localinterfaceonly.notapp",
                        "Not Applicable because, EJB [ {0} ] has Local Interfaces only.",
                                          new Object[] {descriptor.getEjbClassName()}));

	    return result;
	}
	
	if ((descriptor instanceof EjbSessionDescriptor) ||
	    (descriptor instanceof EjbEntityDescriptor)) {
 
	    try {
		ClassLoader jcl = getVerifierContext().getClassLoader();
		Class c = Class.forName(descriptor.getHomeClassName(), false, jcl);

		// remote interface must be defined as valid Rmi-IIOP remote interface
		boolean isValidRmiIIOPInterface = false;
		if (RmiIIOPUtils.isValidRmiIIOPInterface(c) && RmiIIOPUtils.isValidRmiIIOPInterfaceMethods(c)) {
		    isValidRmiIIOPInterface = true;
		}
 
		// remote interface must be defined as valid Rmi-IIOP remote interface
		if (!isValidRmiIIOPInterface){
		    addErrorDetails(result, compName);
		    result.failed(smh.getLocalString
				  (getClass().getName() + ".failed",
				   "Error: [ {0} ] is not defined as valid RMI-IIOP remote interface.  All enterprise beans home interfaces must be defined as valid RMI-IIOP remote interface.  [ {1} ] is not a valid remote home interface.",
				   new Object[] {descriptor.getHomeClassName(),descriptor.getHomeClassName()}));
		} else {
		    addGoodDetails(result, compName);
		    result.passed(smh.getLocalString
				  (getClass().getName() + ".passed",
				   "[ {0} ] properly declares the home interface as valid RMI-IIOP remote interface.",
				   new Object[] {descriptor.getHomeClassName()}));
		}
	    } catch (ClassNotFoundException e) {
		Verifier.debug(e);
		addErrorDetails(result, compName);
		result.failed(smh.getLocalString
			      (getClass().getName() + ".failedException",
			       "Error: [ {0} ] class not found.",
			       new Object[] {descriptor.getHomeClassName()}));
	    }  
	    return result;
 
	} else {
	    addNaDetails(result, compName);
	    result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "[ {0} ] expected {1} bean or {2} bean, but called with {3}.",
				  new Object[] {getClass(),"Session","Entity",descriptor.getName()}));
	    return result;
	}
    }
}
