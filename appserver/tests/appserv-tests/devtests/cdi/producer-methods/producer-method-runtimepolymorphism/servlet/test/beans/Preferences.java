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

import java.io.Serializable;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.New;
import javax.enterprise.inject.Produces;

public class Preferences implements Serializable {

    private PaymentStrategyType paymentStrategy;

    @Produces
    @Preferred_CreatedProgrammatically
    //In this producer method, we are manually creating the
    //bean instance
    public PaymentStrategy getPaymentStrategy() {
        if (true)
            paymentStrategy = PaymentStrategyType.CREDIT_CARD;
        // set CREDIT_CARD as preferred always

        switch (paymentStrategy) {
        case CREDIT_CARD:
            return new CreditCardPaymentStrategy();
        case CHECK:
            return new ChequePaymentStrategy();
        case PAYPAL:
            return new PayPalPaymentStrategy();
        default:
            return null;
        }
    }

    @Produces
    @Preferred_CreatedViaInjection
    @SessionScoped
    // This class is SessionScoped, so if we inject CreditCardPaymentStrategy
    // (which is RequestScoped)
    // we would incorrectly be promoting a request scoped object to a
    // session-scoped object
    // so we use @New for CreditCardPaymentStrategy alone. The rest of the
    // payment
    // strategies are Dependent anyway as they are not scoped explicitly in
    // their
    // classes
    public PaymentStrategy getPaymentStrategy(
            @New CreditCardPaymentStrategy ccps, ChequePaymentStrategy cps,
            PayPalPaymentStrategy pps) {
        // set CREDIT_CARD as preferred always
        paymentStrategy = PaymentStrategyType.CREDIT_CARD;

        switch (paymentStrategy) {
        case CREDIT_CARD:
            return ccps;
        case CHECK:
            return cps;
        case PAYPAL:
            return pps;
        default:
            return null;
        }
    }

}
