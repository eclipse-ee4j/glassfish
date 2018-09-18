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
import com.sun.enterprise.tools.verifier.Verifier;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.util.Iterator;

/**
 * If the Bean Provider provides a value for an environment entry using the 
 * env-entry-value element, the value can be changed later by the Application 
 * Assembler or Deployer. The value must be a string that is valid for the 
 * constructor of the specified type that takes a single String parameter.
 */
public class EjbEnvEntryValue extends EjbTest implements EjbCheck {


    /**
     *If the Bean Provider provides a value for an environment entry using the 
     * env-entry-value element, the value can be changed later by the Application 
     * Assembler or Deployer. The value must be a string that is valid for the 
     * constructor of the specified type that takes a single String parameter.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        if (!descriptor.getEnvironmentProperties().isEmpty()) {
            // The value must be a string that is valid for the
            // constructor of the specified type that takes a single String parameter
            for (Iterator itr = descriptor.getEnvironmentProperties().iterator();
                 itr.hasNext();) {
                EnvironmentProperty nextEnvironmentProperty =
                        (EnvironmentProperty) itr.next();
                if ((nextEnvironmentProperty.getValue() != null)
                        && (nextEnvironmentProperty.getValue().length() > 0)) {
                    if(!validEnvType(nextEnvironmentProperty)) {
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString
                                (getClass().getName() + ".failed",
                                "Error: Environment entry name [ {0} ] does not have" +
                                " valid value [ {1} ] for constructor of the specified type" +
                                " [ {2} ] that takes a single String parameter within bean [ {3} ]",
                                new Object[] {nextEnvironmentProperty.getName(),
                                nextEnvironmentProperty.getValue(), nextEnvironmentProperty.getType(),
                                descriptor.getName()}));
                    }
                }
            }
        }
        if(result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                    "Environment entry name has valid value"));

            }
        return result;
    }

    private boolean validEnvType(EnvironmentProperty nextEnvironmentProperty) {

        try {
            if (nextEnvironmentProperty.getType().equals("java.lang.String"))  {
                new String(nextEnvironmentProperty.getValue());

            } else if (nextEnvironmentProperty.getType().equals("java.lang.Integer")) {
                new Integer(nextEnvironmentProperty.getValue());

            } else if  (nextEnvironmentProperty.getType().equals("java.lang.Boolean")) {
                // don't need to do anything in this case, since any string results
                // in a valid object creation
                new Boolean(nextEnvironmentProperty.getValue());

            } else if  (nextEnvironmentProperty.getType().equals("java.lang.Double")) {

                new Double(nextEnvironmentProperty.getValue());

            } else if  (nextEnvironmentProperty.getType().equals("java.lang.Character")
                    && (nextEnvironmentProperty.getValue().length() == 1)) {
                char c = (nextEnvironmentProperty.getValue()).charAt(0);
                new Character(c);
            } else if  (nextEnvironmentProperty.getType().equals("java.lang.Byte")) {
                new Byte(nextEnvironmentProperty.getValue());

            } else if  (nextEnvironmentProperty.getType().equals("java.lang.Short")) {
                new Short(nextEnvironmentProperty.getValue());

            } else if  (nextEnvironmentProperty.getType().equals("java.lang.Long")) {
                new Long(nextEnvironmentProperty.getValue());

            } else if  (nextEnvironmentProperty.getType().equals("java.lang.Float")) {
                new Float(nextEnvironmentProperty.getValue());

            } else {
                return false;
            }
        } catch (Exception ex) {
            Verifier.debug(ex);
            return false;
        }
        return true;
    }

}
