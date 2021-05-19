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

package com.sun.enterprise.transaction.jts.iiop;

import java.util.logging.Logger;
import java.util.logging.Level;

import org.omg.CORBA.Any;
import org.omg.CORBA.ORB;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.INTERNAL;
import org.omg.CORBA.LocalObject;
import org.omg.CosTransactions.ADAPTS;
import org.omg.CosTransactions.SHARED;
import org.omg.CosTransactions.OTSPolicy;
import org.omg.CosTSInteroperation.TAG_OTS_POLICY;
import org.omg.CosTSInteroperation.TAG_INV_POLICY;
import org.omg.IOP.Codec;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.IORInterceptor;

import com.sun.logging.LogDomains;
import org.glassfish.enterprise.iiop.api.GlassFishORBHelper;
import org.glassfish.hk2.api.ServiceLocator;

public class TxIORInterceptor extends LocalObject implements IORInterceptor {


    private static Logger _logger =
           LogDomains.getLogger(com.sun.jts.pi.InterceptorImpl.class, LogDomains.TRANSACTION_LOGGER);

    private Codec codec;

    private ServiceLocator habitat;

    public TxIORInterceptor(Codec c, ServiceLocator h) {
        codec = c;
        habitat = h;
    }

    public void destroy() {
    }

    public String name() {
        return "TxIORInterceptor";
    }

    // Note: this is called for all remote refs created from this ORB,
    // including EJBs and COSNaming objects.
    public void establish_components(IORInfo iorInfo) {
        try {
            _logger.log(Level.FINE, "TxIORInterceptor.establish_components->:");

            // Add OTS tagged components. These are always the same for all EJBs
            OTSPolicy otsPolicy = null;
            try {
                otsPolicy = (OTSPolicy)iorInfo.get_effective_policy(
             habitat.getService(GlassFishORBHelper.class).getOTSPolicyType());
            } catch ( INV_POLICY ex ) {
                _logger.log(Level.FINE,
                        "TxIORInterceptor.establish_components: OTSPolicy not present");
            }
        addOTSComponents(iorInfo, otsPolicy);

        } catch (Exception e) {
            _logger.log(Level.WARNING,"Exception in establish_components", e);
        } finally {
            _logger.log(Level.FINE, "TxIORInterceptor.establish_components<-:");
        }
    }

    private void addOTSComponents(IORInfo iorInfo, OTSPolicy otsPolicy) {
        short invPolicyValue = SHARED.value;
        short otsPolicyValue = ADAPTS.value;

        if (otsPolicy != null) {
        otsPolicyValue = otsPolicy.value();
    }

        Any otsAny = ORB.init().create_any();
        Any invAny = ORB.init().create_any();

        otsAny.insert_short(otsPolicyValue);
        invAny.insert_short(invPolicyValue);

        byte[] otsCompValue = null;
        byte[] invCompValue = null;
        try {
            otsCompValue = codec.encode_value(otsAny);
            invCompValue = codec.encode_value(invAny);
        } catch (org.omg.IOP.CodecPackage.InvalidTypeForEncoding e) {
            throw new INTERNAL("InvalidTypeForEncoding "+e.getMessage());
        }

        TaggedComponent otsComp = new TaggedComponent(TAG_OTS_POLICY.value,
                                                      otsCompValue);
        iorInfo.add_ior_component(otsComp);

        TaggedComponent invComp = new TaggedComponent(TAG_INV_POLICY.value,
                                                      invCompValue);
        iorInfo.add_ior_component(invComp);
    }

}

// End of file.
