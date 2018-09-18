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

/** ejb [1,n]
 *    name [String]
 *
 * This is the name of the enterprise java bean.
 * @author
 */
public class ASEjbName extends EjbTest implements EjbCheck {

    /**
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        boolean oneFailed = false;
	Result result = getInitializedResult();
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        try{
            String ejbName = descriptor.getName();
            if(ejbName.length()==0){
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                    (getClass().getName() + ".failed",
                    "failed [AS-EJB ejb] : ejb-name cannot not be empty. It should be a valid ejb-name as defined in ejb-jar.xml"));
            } else {
                EjbDescriptor testDesc = descriptor.getEjbBundleDescriptor().getEjbByName(ejbName);
                if(testDesc!=null && testDesc.getName().equals(ejbName))
                {
                    addGoodDetails(result, compName);
                    result.passed(smh.getLocalString(getClass().getName() + ".passed",
                        "PASSED [AS-EJB ejb] :  ejb-name is {0} and verified with ejb-jar.xml",
                        new Object[] {ejbName}));
                }
                else
                {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString(getClass().getName() + ".failed1",
                        "FAILED [AS-EJB ejb] :  ejb-name {0} is not found in ejb-jar.xml. It should exist in ejb-jar.xml also.",
                        new Object[] {ejbName}));
                }
            }
        } catch(Exception ex){
            oneFailed = true;
            addErrorDetails(result, compName);
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create descriptor object"));
        }
        return result;
    }
}
