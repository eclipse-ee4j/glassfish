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

import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.lang.reflect.Method;

/**
 * Stateless session beans home interface create method test.
 * The home interface of a stateless session Bean must have a create method.
 * The home interface must not have any other create methods. 
 */
public class StatelessCreateOnlyOne extends EjbTest implements EjbCheck { 
    Result result = null;
    ComponentNameConstructor compName = null;

    /**
     * Stateless session beans home interface create method test.
     * The home interface of a stateless session Bean must have a create method.
     * The home interface must not have any other create methods. 
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
                //       methods with no arguments, and returns the session Bean's
                //       remote interface. The home interface must not have any
                //       other create methods.
                if(descriptor.getHomeClassName() != null && !"".equals(descriptor.getHomeClassName()))
                    commonToBothInterfaces(descriptor.getHomeClassName(),(EjbSessionDescriptor)descriptor);
                if(descriptor.getLocalHomeClassName() != null && !"".equals(descriptor.getLocalHomeClassName()))
                    commonToBothInterfaces(descriptor.getLocalHomeClassName(),(EjbSessionDescriptor)descriptor);
            }
        }
        if (result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                            "The bean's Home Interface has exactly one create Method defined"));
        }
        return result;
    }

    /**
     * This method is responsible for the logic of the test. It is called for both local and remote interfaces.
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @param home for the Home interface of the Ejb. 
     */

    private void commonToBothInterfaces(String home, EjbSessionDescriptor descriptor) {
        try {
            int count = 0;
            Class c = Class.forName(home, false, getVerifierContext().getClassLoader());
            for(Method methods : c.getDeclaredMethods()) {
                if (methods.getName().equals("create"))
                    count++;
            }
            if(count!=1) {
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                        (getClass().getName() + ".failed",
                                "Error: [ {0} ] Create methods exists within bean [ {1} ]. " +
                        "The home interface must have only one create method for stateless session bean.",
                                new Object[] {new Integer(count),home}));
            }
        } catch (ClassNotFoundException e) {
            Verifier.debug(e);
            result.addErrorDetails(smh.getLocalString
                    ("tests.componentNameConstructor",
                            "For [ {0} ]",
                            new Object[] {compName.toString()}));
            result.failed(smh.getLocalString
                    (getClass().getName() + ".failedException",
                            "Error: Class [ {0} ] not found within bean [ {1} ]",
                            new Object[] {home, descriptor.getName()}));
        }
    }
}
