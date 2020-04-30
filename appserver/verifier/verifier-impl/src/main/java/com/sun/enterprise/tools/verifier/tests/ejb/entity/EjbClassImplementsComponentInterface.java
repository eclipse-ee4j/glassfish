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

package com.sun.enterprise.tools.verifier.tests.ejb.entity;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

import java.util.logging.Level;

/**
 * Optionally implements the enterprise Bean's remote interface test.  
 * The class may, but is not required to, implement the enterprise Bean's 
 * remote interface.  It is recommended that the enterprise bean class 
 * not implement the remote interface to prevent inadvertent passing of 
 * this as a method argument or result. 
 * Note: Display warning to user in this instance. 
 */
public class EjbClassImplementsComponentInterface extends EjbTest implements EjbCheck { 
    Result result = null;
    ComponentNameConstructor compName = null;
    /**
     * Optionally implements the enterprise Bean's remote interface test.  
     * The class may, but is not required to, implement the enterprise Bean's 
     * remote interface.  It is recommended that the enterprise bean class 
     * not implement the remote interface to prevent inadvertent passing of 
     * this as a method argument or result. 
     * Note: Display warning to user in this instance. 
     *   
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	result = getInitializedResult();
	compName = getVerifierContext().getComponentNameConstructor();

	if (descriptor instanceof EjbEntityDescriptor) {
	    if(descriptor.getRemoteClassName() != null && !"".equals(descriptor.getRemoteClassName()))
		commonToBothInterfaces(descriptor.getRemoteClassName(),(EjbEntityDescriptor)descriptor);  
	    if(descriptor.getLocalClassName() != null && !"".equals(descriptor.getLocalClassName()))
		commonToBothInterfaces(descriptor.getLocalClassName(),(EjbEntityDescriptor)descriptor);
	    
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

    private void commonToBothInterfaces(String component, EjbDescriptor descriptor) {
	try {
	    Class c = Class.forName(descriptor.getEjbClassName(), false, getVerifierContext().getClassLoader());
	    Class rc = Class.forName(component, false, getVerifierContext().getClassLoader());
	    boolean foundOne = false;
	    // walk up the class tree 
	    do {
		Class[] interfaces = c.getInterfaces();
		
		for (int i = 0; i < interfaces.length; i++) {
            logger.log(Level.FINE, getClass().getName() + ".debug1",
                    new Object[] {interfaces[i].getName()});   

		    if (interfaces[i].getName().equals(rc.getName()) &&
			descriptor instanceof EjbEntityDescriptor) {
			// display warning to user
			result.addWarningDetails(smh.getLocalString
						 ("tests.componentNameConstructor",
						  "For [ {0} ]",
						  new Object[] {compName.toString()}));
			result.warning(smh.getLocalString
				       (getClass().getName() + ".warning",
					"Warning: [ {0} ] class implments the enterprise Bean's remote interface [ {1} ].  It is recommended that the enterprise bean class not implement the remote interface to prevent inadvertent passing of this as a method argument or result.  The class must provide no-op implementations of the methods defined in the jakarta.ejb.EJBObject interface.",
					new Object[] {descriptor.getEjbClassName(),rc.getName()}));
			foundOne = true;
			break;
		    }
		}
	    } while ((c=c.getSuperclass()) != null);
	    
	    if (!foundOne) {
		// does not implement Bean's remote interface, test n/a
		result.addNaDetails(smh.getLocalString
				    ("tests.componentNameConstructor",
				     "For [ {0} ]",
				     new Object[] {compName.toString()}));
		result.notApplicable(smh.getLocalString
				     (getClass().getName() + ".notApplicable2",
				      "[ {0} ] does not optionally implement bean class remote interface  [ {1} ]",
				      new Object[] {descriptor.getEjbClassName(),rc.getName()}));
	    } else {
		// if the class implements the enterprise Bean's remote interface, 
		// the class must provide no-op implementations of the methods 
		// defined in the jakarta.ejb.EJBObject interface. 
		
		// i can check all methods defined within jakarta.ejb.EJBObject 
		// interface, but i can't tell whether they are no-ops
		// do nothing 
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
			   new Object[] {descriptor.getEjbClassName()}));
	}  
    }
}
