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

import com.sun.enterprise.deployment.RunAsIdentityDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/** ejb [0,n]
 *    principal ?
 *        name [String]
 *
 * The principal tag defines a username on the platform
 * @author Irfan Ahmed
 */
public class ASEjbPrincipal extends EjbTest implements EjbCheck { 


    public Result check(EjbDescriptor descriptor)
    {
	Result result = getInitializedResult();
        
	ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        
        boolean oneFailed = false;
        try{
            if (descriptor.getUsesCallerIdentity() == false){
                RunAsIdentityDescriptor runAsIdDesc = descriptor.getRunAsIdentity();
                if (runAsIdDesc != null){
                    String principal = runAsIdDesc.getPrincipal();
                    if (principal == null){
                        addNaDetails(result, compName);
                        result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                        "NOT APPLICABLE [AS-EJB ejb] : principal element not defined"));
                    }else{
                        if(principal.length()==0){
                            addErrorDetails(result, compName);
                            result.failed(smh.getLocalString(getClass().getName()+".failed",
                            "FAILED [AS-EJB principal] : name cannot be an empty String"));
                        }else{
                            addGoodDetails(result, compName);
                            result.passed(smh.getLocalString(getClass().getName()+".passed",
                            "PASSED [AS-EJB principal] : name is {0}", new Object[]{principal}));
                        }
                    }
                }
            }else{
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString(getClass().getName()+".notApplicable",
                    "NOT APPLICABLE [AS-EJB ejb] run-as Element is not defined"));
            }
        }catch(Exception ex){
            addErrorDetails(result, compName);
            result.addErrorDetails(smh.getLocalString
                 (getClass().getName() + ".notRun",
                  "NOT RUN [AS-EJB] : Could not create an SunEjbJar object"));
        }

        return result;
    }
}
