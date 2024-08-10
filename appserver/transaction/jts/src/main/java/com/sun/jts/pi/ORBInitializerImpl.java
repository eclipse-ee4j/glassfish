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

import com.sun.jts.CosTransactions.MinorCode;

import org.omg.CORBA.BAD_INV_ORDER;
import org.omg.CORBA.CompletionStatus;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.LocalObject;
import org.omg.CORBA.TSIdentification;
import org.omg.CosTransactions.INVOCATION_POLICY_TYPE;
import org.omg.CosTransactions.OTS_POLICY_TYPE;
import org.omg.IOP.Codec;
import org.omg.IOP.CodecFactory;
import org.omg.IOP.ENCODING_CDR_ENCAPS;
import org.omg.IOP.Encoding;
import org.omg.IOP.CodecFactoryPackage.UnknownEncoding;
import org.omg.PortableInterceptor.Current;
import org.omg.PortableInterceptor.ORBInitInfo;
import org.omg.PortableInterceptor.ORBInitializer;
import org.omg.PortableInterceptor.ORBInitInfoPackage.DuplicateName;
//import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.PortableInterceptor.ORBInitInfoPackage.InvalidName;

/**
 * This class implements the ORBInitializer for JTS. When an instance of this
 * class is called during ORB initialization, it registers the IORInterceptors
 * and the JTS request/reply interceptors with the ORB.
 *
 * @author Ram Jeyaraman 11/11/2000
 * @version 1.0
 */
public class ORBInitializerImpl extends LocalObject implements ORBInitializer {

    public ORBInitializerImpl() {}

    public void pre_init(ORBInitInfo info) {}

    public void post_init(ORBInitInfo info) {

        // get hold of the codec instance to pass onto interceptors.

        CodecFactory codecFactory = info.codec_factory();
        Encoding enc = new Encoding(
                            ENCODING_CDR_ENCAPS.value, (byte) 1, (byte) 2);
        Codec codec = null;
        try {
            codec = codecFactory.create_codec(enc);
        } catch (UnknownEncoding e) {
            throw new INTERNAL(MinorCode.TSCreateFailed,
                               CompletionStatus.COMPLETED_NO);
        }

        // get hold of PICurrent to allocate a slot for JTS service.

        Current pic = null;
        try {
            pic = (Current) info.resolve_initial_references("PICurrent");
        } catch (InvalidName e) {
            throw new INTERNAL(MinorCode.TSCreateFailed,
                               CompletionStatus.COMPLETED_NO);
        }

        // allocate a PICurrent slotId for the transaction service.

        int[] slotIds = new int[2];
        try {
            slotIds[0] = info.allocate_slot_id();
            slotIds[1] = info.allocate_slot_id();
        } catch (BAD_INV_ORDER e) {
            throw new INTERNAL(MinorCode.TSCreateFailed,
                               CompletionStatus.COMPLETED_NO);
        }

        // get hold of the TSIdentification instance to pass onto interceptors.

        TSIdentification tsi = null;
        try {
            tsi = (TSIdentification)
                    info.resolve_initial_references("TSIdentification");
        } catch (InvalidName e) {
            // the TransactionService is unavailable.
        }

        // register the policy factories.

        try {
            info.register_policy_factory(OTS_POLICY_TYPE.value,
                                         new OTSPolicyFactory());
        } catch (BAD_INV_ORDER e) {
            // ignore, policy factory already exists.
        }

        try {
            info.register_policy_factory(INVOCATION_POLICY_TYPE.value,
                                         new InvocationPolicyFactory());
        } catch (BAD_INV_ORDER e) {
            // ignore, policy factory already exists.
        }

        // register the interceptors.

        try {
            info.add_ior_interceptor(new IORInterceptorImpl(codec));
            InterceptorImpl interceptor =
                new InterceptorImpl(pic, codec, slotIds, tsi);
            info.add_client_request_interceptor(interceptor);
            info.add_server_request_interceptor(interceptor);
        } catch (DuplicateName e) {
            throw new INTERNAL(MinorCode.TSCreateFailed,
                               CompletionStatus.COMPLETED_NO);
        }
    }
}
