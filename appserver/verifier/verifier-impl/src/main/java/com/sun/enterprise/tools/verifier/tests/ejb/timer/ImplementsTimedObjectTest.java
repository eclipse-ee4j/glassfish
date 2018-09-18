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

package com.sun.enterprise.tools.verifier.tests.ejb.timer;

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbSessionDescriptor;

/**
 * Checks if the EJB class implements the TimedObject interface 
 *
 * @version
 * @author Anisha Malhotra
 */
public class ImplementsTimedObjectTest extends EjbTest {

    ComponentNameConstructor compName = null;
    /**
     * Checks if the EJB class implements the TimedObject interface
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    @Override
    public Result check(EjbDescriptor descriptor) {
        Result result = getInitializedResult();
        ComponentNameConstructor compName =
                getVerifierContext().getComponentNameConstructor();
        boolean isEjb30 = descriptor.getEjbBundleDescriptor().getSpecVersion().equalsIgnoreCase("3.0");
        if(descriptor.isTimedObject()) {
            //Timers can be created for stateless session beans, message-driven beans,
            //and 2.1 entity beans.Timers cannot be created for stateful session beans or EJB 3.0 entities.
            if(((descriptor instanceof EjbEntityDescriptor) && isEjb30)
                    || ( (descriptor instanceof EjbSessionDescriptor) &&
                    ((((EjbSessionDescriptor)descriptor).getSessionType()).equals
                    (EjbSessionDescriptor.STATEFUL)) )) {
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString(getClass().getName()+
                        ".failed1", "[ {0} ] must not implement the TimedObject interface." +
                        "Only 2.1 entity beans or stateless session beans may " +
                        "implement the TimedObject interface" ,
                        new Object[] {descriptor.getEjbClassName()}));
            }
        }
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString (getClass().getName()+".passed",
                    "[ {0} ] properly implements the TimedObject interface",
                    new Object[] {descriptor.getEjbClassName()}));

        }
        return result;
    }
}
