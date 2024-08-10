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
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.ORB;
import org.omg.CosTSInteroperation.TAG_INV_POLICY;
import org.omg.CosTSInteroperation.TAG_OTS_POLICY;
import org.omg.CosTransactions.EITHER;
import org.omg.CosTransactions.FORBIDS;
import org.omg.CosTransactions.INVOCATION_POLICY_TYPE;
import org.omg.CosTransactions.InvocationPolicy;
import org.omg.CosTransactions.OTSPolicy;
import org.omg.CosTransactions.OTS_POLICY_TYPE;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecPackage.InvalidTypeForEncoding;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;

/**
 * This class implements the IORInterceptor for JTS. When an instance of this
 * class is called by the ORB (during POA creation), it supplies appropriate
 * IOR TaggedComponents for the OTSPolicy / InvocationPolicy associated
 * with the POA, which will be used by the ORB while publishing IORs.
 *
 * @author Ram Jeyaraman 11/11/2000
 * @version 1.0
 */
public class IORInterceptorImpl extends LocalObject implements IORInterceptor {

    // class attributes

    private static final String name = "com.sun.jts.pi.IORInterceptor";

    // Instance attributes

    private Codec codec = null;

    public IORInterceptorImpl(Codec codec) {
        this.codec = codec;
    }

    // org.omg.PortableInterceptors.IORInterceptorOperations implementation

    public void establish_components (IORInfo info) {

        // get the OTSPolicy and InvocationPolicy objects

        OTSPolicy otsPolicy = null;

        try {
            otsPolicy = (OTSPolicy)
                info.get_effective_policy(OTS_POLICY_TYPE.value);
        } catch (INV_POLICY e) {
            // ignore. This implies an policy was not explicitly set.
            // A default value will be used instead.
        }

        InvocationPolicy invPolicy = null;
        try {
            invPolicy = (InvocationPolicy)
                info.get_effective_policy(INVOCATION_POLICY_TYPE.value);
        } catch (INV_POLICY e) {
            // ignore. This implies an policy was not explicitly set.
            // A default value will be used instead.
        }

        // get OTSPolicyValue and InvocationPolicyValue from policy objects.

        short otsPolicyValue = FORBIDS.value; // default value
        short invPolicyValue = EITHER.value;  // default value

        if (otsPolicy != null) {
            otsPolicyValue = otsPolicy.value();
        }

        if (invPolicy != null) {
            invPolicyValue = invPolicy.value();
        }

        // use codec to encode policy value into an CDR encapsulation.

        Any otsAny = ORB.init().create_any();
        Any invAny = ORB.init().create_any();

        otsAny.insert_short(otsPolicyValue);
        invAny.insert_short(invPolicyValue);

        byte[] otsCompValue = null;
        byte[] invCompValue = null;
        try {
            otsCompValue = this.codec.encode_value(otsAny);
            invCompValue = this.codec.encode_value(invAny);
        } catch (InvalidTypeForEncoding e) {
            throw new INTERNAL();
        }

        // create IOR TaggedComponents for OTSPolicy and InvocationPolicy.

        TaggedComponent otsComp = new TaggedComponent(TAG_OTS_POLICY.value,
                                                      otsCompValue);
        TaggedComponent invComp = new TaggedComponent(TAG_INV_POLICY.value,
                                                      invCompValue);

        // add ior components.

        info.add_ior_component(otsComp);
        info.add_ior_component(invComp);
    }

    // org.omg.PortableInterceptors.InterceptorOperations implementation

    public String name(){
        return IORInterceptorImpl.name;
    }

    public void destroy() {}
}


