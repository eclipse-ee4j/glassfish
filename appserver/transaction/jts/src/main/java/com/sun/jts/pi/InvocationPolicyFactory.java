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

import org.omg.CORBA.Any;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CORBA.PolicyError;
import org.omg.CosTransactions.EITHER;
import org.omg.CosTransactions.INVOCATION_POLICY_TYPE;
import org.omg.CosTransactions.InvocationPolicyValueHelper;
import org.omg.CosTransactions.SHARED;
import org.omg.CosTransactions.UNSHARED;
import org.omg.PortableInterceptor.PolicyFactory;

/**
 * This is the PolicyFactory to create an InvocationPolicy object.
 *
 * @author Ram Jeyaraman 11/11/2000
 * @version 1.0
 */
public class InvocationPolicyFactory
        extends LocalObject implements PolicyFactory {

    public InvocationPolicyFactory() {}

    public Policy create_policy(int type, Any value) throws PolicyError {

        if (type != INVOCATION_POLICY_TYPE.value) {
            throw new PolicyError("Invalid InvocationPolicyType", (short) 0);
        }

        short policyValue = InvocationPolicyValueHelper.extract(value);

        switch (policyValue) {
        case SHARED.value :
        case UNSHARED.value :
        case EITHER.value :
            break;
        default :
            throw new PolicyError("Invalid InvocationPolicyValue", (short) 1);
        }

        return new InvocationPolicyImpl(policyValue);
    }
}

