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

import com.sun.enterprise.deployment.EnvironmentProperty;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.util.Iterator;

/** 
 * The environment entry value type must be one of the following Java types:
 * String, Integer, Boolean, Double, Byte, Short, Long, and Float.
 */
public class EjbEnvEntryValueType extends EjbTest implements EjbCheck { 



    /** 
     * The environment entry value type must be one of the following Java types:
     * String, Integer, Boolean, Double, Byte, Short, Long, and Float.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        if (!descriptor.getEnvironmentProperties().isEmpty()) {
            // environment entry value type must be one of the following Java types:
            // String, Integer, Boolean, Double, Byte, Short, Long, and Float.
            for (Iterator itr = descriptor.getEnvironmentProperties().iterator();
                 itr.hasNext();) {
                EnvironmentProperty nextEnvironmentProperty =
                        (EnvironmentProperty) itr.next();
                String envType = nextEnvironmentProperty.getType();
                if (!((envType.equals("java.lang.String")) ||
                        (envType.equals("java.lang.Integer")) ||
                        (envType.equals("java.lang.Boolean")) ||
                        (envType.equals("java.lang.Double")) ||
                        (envType.equals("java.lang.Byte")) ||
                        (envType.equals("java.lang.Short")) ||
                        (envType.equals("java.lang.Long")) ||
                        (envType.equals("java.lang.Character")) ||
                        (envType.equals("java.lang.Float")))) {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                            (getClass().getName() + ".failed",
                            "Error: Environment entry value [ {0} ] does not have" +
                            " valid value type [ {1} ] within bean [ {2} ]",
                            new Object[] {nextEnvironmentProperty.getName(),envType, descriptor.getName()}));
                }
            }
        }
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                    "Environment entry value has valid value type"));
        }
        return result;
    }
}
