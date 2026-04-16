/*
 * Copyright (c) 2023, 2026 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.iiop.security;

import com.sun.enterprise.deployment.EjbDescriptor;

import java.lang.System.Logger;
import java.util.List;

import org.glassfish.enterprise.iiop.api.GlassFishORBFactory;
import org.glassfish.enterprise.iiop.api.GlassFishORBLocator;
import org.glassfish.enterprise.iiop.util.IIOPUtils;
import org.glassfish.gms.bootstrap.GMSAdapter;
import org.glassfish.gms.bootstrap.GMSAdapterService;
import org.glassfish.internal.api.Globals;
import org.glassfish.orb.admin.config.IiopListener;
import org.omg.IOP.Codec;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.WARNING;

class SecIORInterceptor extends org.omg.CORBA.LocalObject implements org.omg.PortableInterceptor.IORInterceptor {

    private static final Logger LOG = System.getLogger(SecIORInterceptor.class.getName());

    private final GMSAdapter gmsAdapter;
    private final GlassFishORBLocator orbLocator;

    SecIORInterceptor(Codec c, GlassFishORBLocator orbLocator) {
        this.orbLocator = orbLocator;
        final GMSAdapterService gmsAdapterService = Lookups.getGMSAdapterService();
        if (gmsAdapterService == null) {
            this.gmsAdapter = null;
        } else {
            this.gmsAdapter = gmsAdapterService.getGMSAdapter();
        }
    }

    @Override
    public void destroy() {
    }

    @Override
    public String name() {
        return "SecIORInterceptor";
    }

    // Note: this is called for all remote refs created from this ORB,
    // including EJBs and COSNaming objects.
    @Override
    public void establish_components(IORInfo iorInfo) {
        try {
            addCSIv2Components(iorInfo);
        } catch (Exception e) {
            // The corba-orb library is swallowing the exception!
            LOG.log(WARNING, "Failed to add CSIv2 components for " + iorInfo, e);
            throw new IllegalStateException("Failed to add CSIv2 components!", e);
        }
    }

    private void addCSIv2Components(IORInfo iorInfo) {
        LOG.log(DEBUG, "addCSIv2Components(iorInfo={0})", iorInfo);
        if (gmsAdapter != null) {
            // If this app server instance is part of a dynamic cluster (that is,
            // one that supports RMI-IIOP failover and load balancing, DO NOT
            // create the CSIv2 components here. Instead, handle this in the
            // ORB's ServerGroupManager, in conjunctions with the
            // CSIv2SSLTaggedComponentHandler.
            return;
        }

        // ORB orb = helper.getORB();
        int sslMutualAuthPort = getServerPort("SSL_MUTUALAUTH");
        LOG.log(DEBUG, "sslMutualAuthPort: {0}", sslMutualAuthPort);

        GlassFishORBFactory orbFactory = orbLocator.getOrbFactory();
        EjbDescriptor desc = orbFactory.getEjbDescriptor(iorInfo);
        int sslport = getServerPort("SSL");
        LOG.log(DEBUG, "sslport: {0}", sslport);

        CSIV2TaggedComponentInfo ctc = new CSIV2TaggedComponentInfo(sslMutualAuthPort, orbLocator.getORB());
        final TaggedComponent csiv2Comp;
        if (desc == null) {
            // this is not an EJB object, must be a non-EJB CORBA object
            csiv2Comp = ctc.createSecurityTaggedComponent(sslport, orbFactory.getCSIv2Props());
        } else {
            csiv2Comp = ctc.createSecurityTaggedComponent(sslport, desc);
        }
        iorInfo.add_ior_component(csiv2Comp);
    }

    private int getServerPort(String mech) {
        List<IiopListener> listenersList = Globals.get(IIOPUtils.class).getIiopService().getIiopListener();
        IiopListener[] iiopListenerBeans = listenersList.toArray(new IiopListener[listenersList.size()]);

        for (IiopListener ilisten : iiopListenerBeans) {
            if ("SSL".equalsIgnoreCase(mech)) {
                if ("true".equalsIgnoreCase(ilisten.getSecurityEnabled()) && ilisten.getSsl() != null
                        && !"true".equalsIgnoreCase(ilisten.getSsl().getClientAuthEnabled())) {
                    return Integer.parseInt(ilisten.getPort());
                }
            } else if (mech.equalsIgnoreCase("SSL_MUTUALAUTH")) {
                if ("true".equalsIgnoreCase(ilisten.getSecurityEnabled()) && ilisten.getSsl() != null
                        && "true".equalsIgnoreCase(ilisten.getSsl().getClientAuthEnabled())) {
                    return Integer.parseInt(ilisten.getPort());
                }
            } else if (!"true".equalsIgnoreCase(ilisten.getSecurityEnabled())) {
                return Integer.parseInt(ilisten.getPort());
            }
        }
        return -1;
    }
}
