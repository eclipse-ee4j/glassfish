/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package test.beans;

import jakarta.enterprise.inject.Any;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;

import test.artifacts.Asynchronous;
import test.artifacts.MockPaymentProcessor;
import test.artifacts.PaymentProcessor;
import test.artifacts.Synchronous;

//Alternatives injection and test for availability of other beans via @Any Failed
public class BeanToTestAlternatives {
    private int numberOfPaymentProcessors = 0;

    @Inject
    @Asynchronous
    @Synchronous
    private PaymentProcessor pp;

    @Inject
    public void init(@Any Instance<PaymentProcessor> payInstances) {
        for (PaymentProcessor p : payInstances) {
            System.out.println("Payment Processor #"
                    + numberOfPaymentProcessors + ":" + p);
            numberOfPaymentProcessors++;
        }
    }

    public boolean testInjection() {
        System.out.println("# of Payment processors:"
                + numberOfPaymentProcessors);
        System.out.println("mock payment procssor:" + pp);
        return (numberOfPaymentProcessors == 5)
                && (pp instanceof MockPaymentProcessor); // Async, Sync, Cheque,
                                                         // ReliableCash,
                                                         // MockPaymentProcessor
    }
}
