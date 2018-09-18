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

package com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.VerifierTestContext;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbCMPEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.lang.reflect.Method;

/**
 * Select methods must not be exposed through the bean home or remote interface
 *
 * @author  Jerome Dochez
 * @version 
 */
public class SelectMethodExposition extends SelectMethodTest {

     /**
     * <p>
     * run an individual test against a declared ejbSelect method
     * </p>
     * 
     * @param m is the ejbSelect method
     * @param descriptor is the entity declaring the ejbSelect
     * @param result is where to put the result
     * 
     * @return true if the test passes
     */
    ComponentNameConstructor compName = null;

    protected boolean runIndividualSelectTest(Method m, EjbCMPEntityDescriptor descriptor, Result result) {
        boolean allIsWell = true;
	compName = getVerifierContext().getComponentNameConstructor();
	//  String methodReturnType = m.getReturnType().getName();
    	if(descriptor.getRemoteClassName() != null && !"".equals(descriptor.getRemoteClassName()) &&
	   descriptor.getHomeClassName() != null && !"".equals(descriptor.getHomeClassName()))
	    allIsWell =  commonToBothInterfaces(descriptor.getHomeClassName(),descriptor.getRemoteClassName(),descriptor, result, m);
	if(allIsWell == true) {
	    if(descriptor.getLocalClassName() != null && !"".equals(descriptor.getLocalClassName()) &&
	       descriptor.getLocalHomeClassName() != null && !"".equals(descriptor.getLocalHomeClassName()))
		allIsWell =  commonToBothInterfaces(descriptor.getLocalHomeClassName(),descriptor.getLocalClassName(),descriptor, result, m);
	}
	return allIsWell;
    }  

 /** 
     * This method is responsible for the logic of the test. It is called for both local and remote interfaces.
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @param ejbHome for the Home interface of the Ejb. 
     * @param result Result of the test
     * @param remote Remote/Local interface
     * @param m Method
     * @return boolean the results for this assertion i.e if a test has failed or not
     */

    private boolean commonToBothInterfaces(String home,String remote,EjbDescriptor descriptor, Result result, Method m) {
	try {
            // we must not find this method exposed in the home or remote interface
	    VerifierTestContext context = getVerifierContext();
	    ClassLoader jcl = context.getClassLoader();
            Method m1 = getMethod(Class.forName(home, false,
                                 getVerifierContext().getClassLoader()),m.getName(), m.getParameterTypes());
            Method m2 = getMethod(Class.forName(remote, false,
                                 getVerifierContext().getClassLoader()), m.getName(), m.getParameterTypes());
            if (m1 == null && m2 == null) {
		result.addGoodDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.addGoodDetails(smh.getLocalString
			  ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.SelectMethodExposition.passed",
			   "[ {0} ] is not declared in the home or remote interface",
			   new Object[] {m.getName()}));
                return true;
            } else {
		result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
		result.addErrorDetails(smh.getLocalString
                    ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.SelectMethodExposition.failed",
                    "Error : [ {0} ] is declared in the home or remote interface",
                    new Object[] {m.getName()}));
                return false;
            }
        } catch (ClassNotFoundException e) {
	    result.addErrorDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
	    result.addErrorDetails(smh.getLocalString
		     ("com.sun.enterprise.tools.verifier.tests.ejb.entity.cmp2.SelectMethodExposition.failedException",
		      "Error: home or remote interface not found.",
		      new Object[] {}));
            Verifier.debug(e);
            return false;
        }
    }         
}
