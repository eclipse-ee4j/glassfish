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

package com.sun.jts.pi;

import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CosTransactions.EITHER;
import org.omg.CosTransactions.INVOCATION_POLICY_TYPE;
import org.omg.CosTransactions.InvocationPolicy;

/**
 * This is the InvocationPolicy object which holds an appropriate policy value.
 *
 * @author Ram Jeyaraman 11/11/2000
 * @version 1.0
 */
public class InvocationPolicyImpl extends LocalObject implements InvocationPolicy {

    private short value = EITHER.value;

    public InvocationPolicyImpl() {
        this.value = EITHER.value;
    }


    public InvocationPolicyImpl(short value) {
        this.value = value;
    }

    // org.omg.CosTransactions.InvocationPolicyOperations implementation


    @Override
    public short value() {
        return this.value;
    }

    // org.omg.CORBA.PolicyOperations implementation


    @Override
    public int policy_type() {
        return INVOCATION_POLICY_TYPE.value;
    }


    @Override
    public Policy copy() {
        return new InvocationPolicyImpl(this.value);
    }


    @Override
    public void destroy() {
        value = EITHER.value;
    }


    @Override
    public String toString() {
        return "InvocationPolicy[" + this.value + "]";
    }
}
