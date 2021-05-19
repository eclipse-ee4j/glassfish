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

package org.glassfish.enterprise.iiop.impl;


import org.omg.CORBA.LocalObject;
import org.omg.CORBA.Policy;
import org.omg.CosTransactions.ADAPTS;
import org.omg.CosTransactions.FORBIDS;
import org.omg.CosTransactions.OTSPolicy;
import org.omg.CosTransactions.OTS_POLICY_TYPE;
import org.omg.CosTransactions.REQUIRES;

public class OTSPolicyImpl extends LocalObject implements OTSPolicy {

    // class constants

    public static final OTSPolicyImpl _ADAPTS = new OTSPolicyImpl(ADAPTS.value);
    public static final OTSPolicyImpl _FORBIDS = new OTSPolicyImpl(FORBIDS.value);
    public static final OTSPolicyImpl _REQUIRES = new OTSPolicyImpl(REQUIRES.value);

    // instance variables

    private short value = FORBIDS.value;

    // constructor

    public OTSPolicyImpl() {
        this.value = ADAPTS.value;
    }


    public OTSPolicyImpl(short value) {
        this.value = value;
    }

    // org.omg.CosTransactions.OTSPolicyOperations implementation


    @Override
    public short value() {
        return this.value;
    }

    // org.omg.CORBA.PolicyOperations implementation


    @Override
    public int policy_type() {
        return OTS_POLICY_TYPE.value;
    }


    @Override
    public Policy copy() {
        return new OTSPolicyImpl(this.value);
    }


    @Override
    public void destroy() {
        value = FORBIDS.value;
    }


    @Override
    public String toString() {
        return "OTSPolicy[" + this.value + "]";
    }
}
