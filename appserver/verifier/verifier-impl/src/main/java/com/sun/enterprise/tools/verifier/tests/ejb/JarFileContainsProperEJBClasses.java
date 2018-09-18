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

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;

/** 
 * ejb-jar file must contain the java class file of the enterprise bean 
 * implementation class, and any of the classes that it depends on.
 */
public class JarFileContainsProperEJBClasses extends EjbTest implements EjbCheck { 



    /** 
     * ejb-jar file must contain the java class file of the enterprise bean 
     * implementation class, and any of the classes that it depends on.
     *  
     * @param descriptor the Enterprise Java Bean deployment descriptor 
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

	try {
	    VerifierTestContext context = getVerifierContext();
        Class c = Class.forName(descriptor.getEjbClassName(), false,
                             getVerifierContext().getClassLoader());
            // if we are dealing with a CMP2 entity bean, the class is abstract..
            if (descriptor instanceof EjbEntityDescriptor) {
	        String persistentType =
		    ((EjbEntityDescriptor)descriptor).getPersistenceType();
	        if (EjbEntityDescriptor.CONTAINER_PERSISTENCE.equals(persistentType)) {
                    if (EjbCMPEntityDescriptor.CMP_1_1!=((EjbCMPEntityDescriptor) descriptor).getCMPVersion()) {
			result.addGoodDetails(smh.getLocalString
					      ("tests.componentNameConstructor",
					       "For [ {0} ]",
					       new Object[] {compName.toString()}));

		        result.passed(smh.getLocalString
				      (getClass().getName() + ".passed",
				       "Bean class [ {0} ] exists and it's supporting classes exist.",
				       new Object[] {descriptor.getEjbClassName()}));
        	        return result;
                    }
                }
            }

            try {
		c.newInstance();
		result.addGoodDetails(smh.getLocalString
				      ("tests.componentNameConstructor",
				       "For [ {0} ]",
				       new Object[] {compName.toString()}));

		result.passed(smh.getLocalString
			      (getClass().getName() + ".passed",
			       "Bean class [ {0} ] exists and it's supporting classes exist.",
			       new Object[] {descriptor.getEjbClassName()}));
	    } catch (InstantiationException e) {
		Verifier.debug(e);
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));

		result.failed(smh.getLocalString
			      (getClass().getName() + ".failedException",
			       "Error: Could not instantiate [ {0} ] within bean [ {1} ]",
			       new Object[] {descriptor.getEjbClassName(),descriptor.getName()}));
	    } catch (IllegalAccessException e) {
		Verifier.debug(e);
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));

		result.failed(smh.getLocalString
			      (getClass().getName() + ".failedException1",
			       "Error: Illegal Access while trying to instantiate [ {0} ] within bean [ {1} ]",
			       new Object[] {descriptor.getEjbClassName(),descriptor.getName()}));
	    }
	} catch (ClassNotFoundException e) {
	    Verifier.debug(e);
	    result.addErrorDetails(smh.getLocalString
				   ("tests.componentNameConstructor",
				    "For [ {0} ]",
				    new Object[] {compName.toString()}));
	    
	    result.failed(smh.getLocalString
			  (getClass().getName() + ".failedException2",
			   "Error: Can't find class [ {0} ] within bean [ {1} ]",
			   new Object[] {descriptor.getEjbClassName(),descriptor.getName()}));
        } catch (Throwable t) {
	    Verifier.debug(t);
	    result.addNaDetails(smh.getLocalString
				("tests.componentNameConstructor",
				 "For [ {0} ]",
				 new Object[] {compName.toString()}));
	    
            result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "Not Applicable: [ {0} ] class encountered [ {1} ]. Cannot create instance of class [ {2} ] becuase [ {3} ] is not accessible within [ {4} ].",
				  new Object[] {(descriptor).getEjbClassName(),t.toString(), descriptor.getEjbClassName(), t.getMessage(), descriptor.getEjbBundleDescriptor().getModuleDescriptor().getArchiveUri()}));
	}
	return result;
    }
}
