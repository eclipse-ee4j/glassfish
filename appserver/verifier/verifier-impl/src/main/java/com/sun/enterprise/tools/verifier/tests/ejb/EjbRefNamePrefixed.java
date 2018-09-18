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

import com.sun.enterprise.deployment.EjbReferenceDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;

import java.util.Iterator;

/**
 * The ejb-ref-name element contains the name of an EJB reference. The EJB 
 * reference is an entry in the enterprise bean's environment. It is 
 * recommended that name is prefixed with "ejb/".
 */
public class EjbRefNamePrefixed extends EjbTest implements EjbCheck {

    /**
     * The ejb-ref-name element contains the name of an EJB reference. The EJB 
     * reference is an entry in the enterprise bean's environment. It is 
     * recommended that name is prefixed with "ejb/".
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        if (!descriptor.getEjbReferenceDescriptors().isEmpty()) {
            for (Iterator itr = descriptor.getEjbReferenceDescriptors().iterator();
                 itr.hasNext();) {
                EjbReferenceDescriptor nextEjbReference = (EjbReferenceDescriptor) itr.next();
                String ejbRefName = nextEjbReference.getName();
                if (!ejbRefName.startsWith("ejb/")) {
                    addWarningDetails(result, compName);
                    result.addWarningDetails(smh.getLocalString
                            (getClass().getName() + ".warning",
                            "Warning: [ {0} ] is not prefixed with recommended string " +
                            "ejb/ within bean [ {1} ]",
                            new Object[] {ejbRefName,descriptor.getName()}));
                }
            }
        }
        if (result.getStatus() != Result.WARNING) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                    "ejb-ref-name is properly defined within bean [ {0} ]",
                    new Object[] {descriptor.getName()}));
        }
        return result;
    }
}
