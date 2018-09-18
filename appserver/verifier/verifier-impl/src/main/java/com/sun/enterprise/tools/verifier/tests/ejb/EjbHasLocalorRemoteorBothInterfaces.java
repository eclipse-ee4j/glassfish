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

import com.sun.enterprise.deployment.EjbSessionDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;
import org.glassfish.ejb.deployment.descriptor.EjbEntityDescriptor;


/**
 * Bean interface type test.  
 * The bean provider must provide either Local or Remote or Both interfaces
 *
 * @author Sheetal Vartak
 */
public class EjbHasLocalorRemoteorBothInterfaces extends EjbTest implements EjbCheck {

    /**
     * Bean interface type test.  
     * The bean provider must provide either Local or Remote or Both interfaces
     *   
     * @param descriptor the Enterprise Java Bean deployment descriptor   
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        Result result = getInitializedResult();
        ComponentNameConstructor compName = getVerifierContext().getComponentNameConstructor();

        if (!(descriptor instanceof EjbSessionDescriptor) &&
                !(descriptor instanceof EjbEntityDescriptor)) {
            addNaDetails(result, compName);
            result.notApplicable(smh.getLocalString
                    ("com.sun.enterprise.tools.verifier.tests.ejb.intf.InterfaceClassExist.notApplicable1",
                            "Test apply only to session or entity beans."));
            return result;
        }
        if ((descriptor.getRemoteClassName() == null || "".equals(descriptor.getRemoteClassName()))&&
                (descriptor.getLocalClassName() == null || "".equals(descriptor.getLocalClassName()))) {

            if (implementsEndpoints(descriptor)) {
                addNaDetails(result, compName);
                result.notApplicable(smh.getLocalString
                        ("com.sun.enterprise.tools.verifier.tests.ejb.webservice.notapp",
                                "Not Applicable because, EJB [ {0} ] implements a Service Endpoint Interface.",
                                new Object[] {compName.toString()}));
                return result;
            }
            else {
                addErrorDetails(result, compName);
                result.failed(smh.getLocalString
                        (getClass().getName() + ".failed",
                                "Ejb [ {0} ] does not have local or remote interfaces",
                                new Object[] {descriptor.getEjbClassName()}));
                return result;
            }
        }
        else {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName() + ".passed",
                            "Ejb [ {0} ] does have valid local and/or remote interfaces",
                            new Object[] {descriptor.getEjbClassName()}));
            return result;
        }
    }
}

