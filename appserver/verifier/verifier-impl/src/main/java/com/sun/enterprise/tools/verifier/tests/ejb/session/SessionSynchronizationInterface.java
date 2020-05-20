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

package com.sun.enterprise.tools.verifier.tests.ejb.session;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbCheck;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

import java.util.logging.Level;

/**
 * Optionally implements the SessionSynchronization Interface test.  
 * The optional SessionSynchronization interface may be implemented only by 
 * a stateful Session Bean using container-managed transactions. The 
 * SessionSynchronization interface must not be implemented by a stateless 
 * Session Bean. 
 */
public class SessionSynchronizationInterface extends EjbTest implements EjbCheck {

    /**
     * Optionally implements the SessionSynchronization Interface test.  
     * The optional SessionSynchronization interface may be implemented only by 
     * a stateful Session Bean using container-managed transactions. The 
     * SessionSynchronization interface must not be implemented by a stateless 
     * Session Bean. 
     *   
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *   
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        if (descriptor instanceof EjbSessionDescriptor) {
            String stateType = ((EjbSessionDescriptor)descriptor).getSessionType();
            try {
                Class c = Class.forName(descriptor.getEjbClassName(), false, getVerifierContext().getClassLoader());
                // walk up the class tree
                do {
                    //Class[] interfaces = c.getInterfaces();
                    //for (int i = 0; i < interfaces.length; i++) {
                    for(Class interfaces : c.getInterfaces()) {
                        logger.log(Level.FINE, getClass().getName() + ".debug1",
                                new Object[] {interfaces.getName()});

                        if (interfaces.getName().equals("jakarta.ejb.SessionSynchronization") ) {
                            String transactionType = descriptor.getTransactionType();
                            if ((EjbSessionDescriptor.STATELESS.equals(stateType)) ||
                                    ((EjbSessionDescriptor.STATEFUL.equals(stateType))
                                            && EjbSessionDescriptor.BEAN_TRANSACTION_TYPE
                                            .equals(transactionType) )) {
                                addErrorDetails(result, compName);
                                result.failed(smh.getLocalString
                                        (getClass().getName() + ".failed",
                                        "Error: [ {0} ] does not properly implement the SessionSynchronization interface. " +
                                        " SessionSynchronization interface must not be implemented by a stateless Session Bean. " +
                                        "[ {1} ] is not a valid bean.",
                                        new Object[] {descriptor.getEjbClassName(),descriptor.getEjbClassName()}));
                                break;

                            }
                        }
                    }
                } while ((c=c.getSuperclass()) != null);

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

        if (result.getStatus()!=Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString(getClass().getName() + ".passed",
                    "The required interface is properly implemented"));
        }
        return result;
    }
}
