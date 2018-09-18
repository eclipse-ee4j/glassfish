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
 * Stateless session beans home interface create method return test.
 * The home interface of a stateless session Bean must have a create method that
 * takes no arguments, and returns the session Bean's remote interface. 
 */
public class StatelessCreateReturn extends EjbTest implements EjbCheck { 
    boolean foundAtLeastOneCreate = false;
    Result result = null;
    ComponentNameConstructor compName = null;

    /**
     * Stateless session beans home interface create method return test.
     * The home interface of a stateless session Bean must have a create method that
     * takes no arguments, and returns the session Bean's remote interface. 
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
                if (descriptor.getHomeClassName() != null && !"".equals(descriptor.getHomeClassName()) &&
                        descriptor.getRemoteClassName() != null && !"".equals(descriptor.getRemoteClassName()) )
                    commonToBothInterfaces(descriptor.getRemoteClassName(),descriptor.getHomeClassName(),(EjbSessionDescriptor)descriptor);

                if (descriptor.getLocalHomeClassName() != null && !"".equals(descriptor.getLocalHomeClassName())&&
                        descriptor.getLocalClassName() != null && !"".equals(descriptor.getLocalClassName()))
                    commonToBothInterfaces(descriptor.getLocalClassName(),descriptor.getLocalHomeClassName() ,(EjbSessionDescriptor)descriptor);

                if(result.getStatus() != Result.FAILED) {
                    addGoodDetails(result, compName);
                    result.passed(smh.getLocalString
                            (getClass().getName() +".passed",
                                    "create method is properly defined in the remote/local home interface"));
                }
                return result;
            }
        }
        return result;
    }

    /**
     * This method is responsible for the logic of the test. It is called for both local and remote interfaces.
     * @param descriptor the Enterprise Java Bean deployment descriptor
     * @param home for the Home interface of the Ejb. 
     * @param remote Remote/Local interface
     */

    private void commonToBothInterfaces(String remote, String home, EjbSessionDescriptor descriptor) {
        try {
            Class c = Class.forName(home, false, getVerifierContext().getClassLoader());
            for (Method methods : c.getDeclaredMethods()) {
                if (methods.getName().startsWith("create")) {
                    Class methodReturnType = methods.getReturnType();
                    if (!(methodReturnType.getName().equals(remote))) {
                        addErrorDetails(result, compName);
                        result.addErrorDetails(smh.getLocalString
                                (getClass().getName() + ".debug1",
                                        "For Home Interface [ {0} ] Method [ {1} ]",
                                        new Object[] {home,methods.getName()}));
                        result.addErrorDetails(smh.getLocalString
                                (getClass().getName() + ".failed",
                                        "Error: A Create method was found, but the " +
                                "return type [ {0} ] was not the Remote/Local interface" ,
                                        new Object[] {methodReturnType.getName()}));

                    }
                }
            }
        } catch (ClassNotFoundException e) {
            Verifier.debug(e);
            addErrorDetails(result, compName);
            result.failed(smh.getLocalString
                    (getClass().getName() + ".failedException",
                            "Error: Home/Local Home interface [ {0} ] does not exist or is not loadable within bean [ {1} ]",
                            new Object[] {home, descriptor.getName()}));
        }
    }
}
