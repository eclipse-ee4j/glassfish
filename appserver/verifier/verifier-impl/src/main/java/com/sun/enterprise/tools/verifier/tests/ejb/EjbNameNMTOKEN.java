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

import com.sun.enterprise.tools.verifier.NameToken;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

/**
 * The ejb-name must conform to the lexical rules for an NMTOKEN
 */
public class EjbNameNMTOKEN extends EjbTest implements EjbCheck {



    /**
     * The ejb-name must conform to the lexical rules for an NMTOKEN
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        Result result = getInitializedResult();
        String ejbName = descriptor.getName();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        // The ejb-name must conform to the lexical rules for an NMTOKEN
        if (!(NameToken.isNMTOKEN(ejbName))) {
            // it's bad, test should not pass
            addErrorDetails(result, compName);
            result.failed
                    (smh.getLocalString
                    (getClass().getName() + ".failed",
                            "Error: [ {0} ] does not conform to the lexical rules of NMTOKEN within bean [ {1} ]",
                            new Object[] {ejbName, descriptor.getName()}));
        } else {
            addGoodDetails(result, compName);
            result.passed
                    (smh.getLocalString
                    (getClass().getName() + ".passed",
                            "[ {0} ] conforms to the lexical rules of NMTOKEN within bean [ {1} ]",
                            new Object[] {ejbName, descriptor.getName()}));
        }
        return result;
    }

}
