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

package com.sun.enterprise.tools.verifier.tests.ejb.runtime;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/** enterprise-bean
 *    name ? [String]
 *
 * This is the name of the enterprise bean module
 * @author Irfan Ahmmed
 */
public class ASEntBeanName extends EjbTest implements EjbCheck { 

    /**
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        String entBeanName = null;
        String ejbName = null;
        try{
            ejbName = descriptor.getName();
            entBeanName = getXPathValue("sun-ejb-jar/enterprise-beans/name");
            if(entBeanName == null){
                result.setStatus(Result.NOT_APPLICABLE);
                result.addNaDetails(smh.getLocalString
				       ("tests.componentNameConstructor",
					"For [ {0} ]",
					new Object[] {compName.toString()}));
                result.notApplicable(smh.getLocalString
				 (getClass().getName() + ".notApplicable",
				  "{0} Does not define any enterprise bean name",
				  new Object[] {ejbName}));
                return result;
            }
            
            if(entBeanName!=null && entBeanName.length()==0){
                result.addErrorDetails(smh.getLocalString
                                   ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
                result.warning(smh.getLocalString
                     (getClass().getName() + ".warning",
                      "WARNING [AS-EJB enterprise-beans] : name should not be empty."));
                return result;
            }else{
                result.addGoodDetails(smh.getLocalString
				  ("tests.componentNameConstructor",
				   "For [ {0} ]",
				   new Object[] {compName.toString()}));
                result.passed(smh.getLocalString
		 (getClass().getName() + ".passed",
		  "PASSED [AS-EJB enterprise-beans] :  name is {0}",
		  new Object[] {entBeanName}));
                return result;
            }
        }catch(Exception ex){
            result.addErrorDetails(smh.getLocalString
                                   ("tests.componentNameConstructor",
                                    "For [ {0} ]",
                                    new Object[] {compName.toString()}));
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create a descriptor object"));
            return result;
        }
    }
}
