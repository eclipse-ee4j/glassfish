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

import java.util.Iterator;

/**
 * The ejb-name must be unique amoung the names of the enterprise beans within
 * the same ejb-jar file.
 */
public class EjbNameUnique extends EjbTest implements EjbCheck {

    /**
     * The ejb-name must be unique amoung the names of the enterprise beans within
     * the same ejb-jar file.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        Result result = getInitializedResult();
        String ejbName = descriptor.getName();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        // initialize needed by ejb loop
        int found = 0;

        // The ejb-name must be unique amoung the names of the enterprise beans
        // within the same ejb-jar file.
        // i.e. need to loop through all ejb's within this jar and get their
        // respective ejbName's, then do a string compare and make sure their are
        // no duplicates.
        for (Iterator itr =descriptor.getEjbBundleDescriptor().getEjbs().iterator();
             itr.hasNext();) {
            EjbDescriptor ejbDescriptor = (EjbDescriptor) itr.next();
            if (ejbDescriptor.getName().equals(ejbName)) {
                found++;
                if (found > 1) {
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                            (getClass().getName() + ".failed",
                            "Error: [ {0} ] has found [ {1} ] duplicate ejb name(s) within the same jar.",
                            new Object[] {ejbName, new Integer((found - 1))}));
                }
            }
        }
        
        if (result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                    "Valid: [ {0} ] was found once within jar, ejb-name is unique.",
                    new Object[] {ejbName}));
        }
        return result;
    }
}
