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

import com.sun.enterprise.deployment.MethodDescriptor;
import com.sun.enterprise.tools.verifier.Result;
import com.sun.enterprise.tools.verifier.SpecVersionMapper;
import com.sun.enterprise.tools.verifier.tests.ComponentNameConstructor;
import com.sun.enterprise.tools.verifier.tests.ejb.EjbTest;
import org.glassfish.ejb.deployment.descriptor.ContainerTransaction;
import org.glassfish.ejb.deployment.descriptor.EjbDescriptor;


/**
 * Check that the transaction attributes for the ejbTimeout method are one
 * of the following -
 * RequiresNew or NotSupported
 *
 * @version 
 * @author Anisha Malhotra
 */
public class HasValidEjbTimeoutDescriptor extends EjbTest {
    Result result = null;
    ComponentNameConstructor compName = null;
    /**
     * Run a verifier test to check the transaction attributes of the
     * ejbTimeout method. The allowed attributes are -
     * RequiresNew or NotSupported.
     *
     * @param descriptor the Enterprise Java Bean deployment descriptor
     *
     * @return <code>Result</code> the results for this assertion
     */
    public Result check(EjbDescriptor descriptor) {

        result = getInitializedResult();
        compName = getVerifierContext().getComponentNameConstructor();

        if(descriptor.isTimedObject()) {

            if (descriptor.getTransactionType().equals
                    (EjbDescriptor.CONTAINER_TRANSACTION_TYPE)) {
                MethodDescriptor methodDesc = descriptor.getEjbTimeoutMethod();
                ContainerTransaction txAttr =
                        descriptor.getContainerTransactionFor(methodDesc);
                String version = getVerifierContext().getJavaEEVersion();
                if(txAttr != null) {
                    String ta = txAttr.getTransactionAttribute();
                    if ((version.compareTo(SpecVersionMapper.JavaEEVersion_5) >= 0) &&
                            !(ContainerTransaction.REQUIRES_NEW.equals(ta)
                            || ContainerTransaction.NOT_SUPPORTED.equals(ta)
                            || ContainerTransaction.REQUIRED.equals(ta))) {
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString
                                (getClass().getName()+".failed1",
                                "Error : Bean [ {0} ] Transaction attribute for timeout method" +
                                "must be Required, RequiresNew or NotSupported",
                                new Object[] {descriptor.getName()}));
                    } else if ((version.compareTo(SpecVersionMapper.JavaEEVersion_5) < 0) &&
                            !(ContainerTransaction.REQUIRES_NEW.equals(ta)
                            || ContainerTransaction.NOT_SUPPORTED.equals(ta))) {
                        addErrorDetails(result, compName);
                        result.failed(smh.getLocalString
                                (getClass().getName()+".failed2",
                                "Error : Bean [ {0} ] Transaction attribute for ejbTimeout " +
                                "must be RequiresNew or NotSupported",
                                new Object[] {descriptor.getName()}));

                    }
                } else if(version.compareTo(SpecVersionMapper.JavaEEVersion_5)<0) {
                    // Transaction attribute for ejbTimeout not specified in the DD
                    addErrorDetails(result, compName);
                    result.failed(smh.getLocalString
                            (getClass().getName()+".failed3",
                            "Transaction attribute for Timeout is not specified for [ {0} ]",
                            new Object[] {descriptor.getName()}));
                }
            }
        }

        if (result.getStatus() != Result.FAILED) {
            addGoodDetails(result, compName);
            result.passed(smh.getLocalString
                    (getClass().getName()+".passed",
                    "Transaction attributes are properly specified"));

        }
        return result;
    }
}
