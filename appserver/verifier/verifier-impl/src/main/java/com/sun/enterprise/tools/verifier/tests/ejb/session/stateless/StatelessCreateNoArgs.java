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

package com.sun.enterprise.tools.verifier.tests.ejb.session.stateless;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import java.lang.reflect.Method;

/**
 * Stateless session are only allowed to have create methods with no arguments.
 */
public class StatelessCreateNoArgs extends EjbTest implements EjbCheck { 
    Result result = null;
    ComponentNameConstructor compName = null;

    /**
     * Stateless session are only allowed to have create methods with no arguments.
     *    
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor();

        if (descriptor instanceof EjbSessionDescriptor) {
            String stateType = ((EjbSessionDescriptor)descriptor).getSessionType();
            if (EjbSessionDescriptor.STATELESS.equals(stateType)) {
                // RULE: Stateless session are only allowed to have create
                //       methods with no arguments.
                if(descriptor.getHomeClassName() != null && !"".equals(descriptor.getHomeClassName()))
                    commonToBothInterfaces(descriptor.getHomeClassName(),(EjbSessionDescriptor)descriptor);
                if(descriptor.getLocalHomeClassName() != null && !"".equals(descriptor.getLocalHomeClassName()))
                    commonToBothInterfaces(descriptor.getLocalHomeClassName(),(EjbSessionDescriptor)descriptor);
            }
        }
        if (result.getStatus()!=Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                            "The bean's Home Interface properly defines one create Method with no args"));
        }
        return result;
    }

    /**
     * This method is responsible for the logic of the test. It is called for both local and remote interfaces.
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @param home for the Home interface of the Ejb. 
     *
     */


    private void commonToBothInterfaces(String home, EjbSessionDescriptor descriptor) {
        try {

            Class c = Class.forName(home, false, getVerifierContext().getClassLoader());
            Method m = null;
            for(Method methods : c.getDeclaredMethods()) {
                if (methods.getName().equals("create")) {
                    m = methods;
                    break;
                }
            }
            //check if this method m does not have any paramenter
            if (m != null) {
                Class cc[] = m.getParameterTypes();
                if (cc.length > 0) {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                            (getClass().getName() + ".failed",
                            "Error: The create method has one or more parameters \n" +
                            "within bean [ {0} ].  Stateless session are only allowed \n" +
                            "to have create methods with no arguments.",
                             new Object[] {home}));
                }
            } else {
                // set status to FAILED, 'cause there is not even
                // a create method to begin with, regardless of its parameters
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                        (getClass().getName() + ".failed1",
                                "Error: No Create method exists within bean [ {0} ]",
                                new Object[] {home}));
            }
        } catch (ClassNotFoundException e) {
            Verifier.debug(e);
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                    (getClass().getName() + ".failedException",
                            "Error: Class [ {0} ] not found within bean [ {1} ]",
                            new Object[] {home, descriptor.getName()}));
        }
    }
}
