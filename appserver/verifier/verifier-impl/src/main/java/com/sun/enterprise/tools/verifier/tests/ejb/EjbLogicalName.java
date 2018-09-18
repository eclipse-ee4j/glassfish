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

import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/**
 * Enterprise bean's name test.
 * The Bean provider must assign a logical name to each enterprise bean in 
 * the ejb-jar file.
 */
public class EjbLogicalName extends EjbTest implements EjbCheck {

    /**
     * Enterprise bean's name test.
     * The Bean provider must assign a logical name to each enterprise bean in 
     * the ejb-jar file.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor 
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        Result result = getInitializedResult();
        String ejbName = descriptor.getName();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();
        if (!ejbName.equals("")) {
            // as long as it's not blank, test should pass
            //result.passed("EJB logical name is : "+ejbName);
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                            "EJB logical name is: [ {0} ]",
                            new Object[] {ejbName}));
        } else {
            addErrorDetails(result, compName);
            // it's blank, test should not pass
            result.failed(smh.getLocalString
                    (getClass().getName() + ".failed",
                            "Error: EJB logical name cannot be blank."));
        }
        return result;
    }
}

