/*
 * Copyright (c) 2025, 2026 Contributors to the Eclipse Foundation
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

import com.sun.corba.ee.impl.folb.CSIv2SSLTaggedComponentHandler;
import com.sun.corba.ee.spi.folb.ClusterInstanceInfo;
import com.sun.corba.ee.spi.ior.IOR;
import com.sun.corba.ee.spi.orb.DataCollector;
import com.sun.corba.ee.spi.orb.ORB;
import com.sun.corba.ee.spi.orb.ORBConfigurator;
import com.sun.corba.ee.spi.transport.SocketInfo;

import java.lang.System.Logger;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.enterprise.iiop.api.IIOPSSLUtil;
import org.glassfish.internal.api.Globals;
import org.omg.CORBA.ORBPackage.InvalidName;
import org.omg.IOP.TaggedComponent;
import org.omg.PortableInterceptor.IORInfo;

import static com.sun.corba.ee.spi.misc.ORBConstants.CSI_V2_SSL_TAGGED_COMPONENT_HANDLER;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;

/**
 * @author Harold Carr, 2005
 */
public class CSIv2SSLTaggedComponentHandlerImpl extends org.omg.CORBA.LocalObject
    implements CSIv2SSLTaggedComponentHandler, ORBConfigurator {

    private static final Logger LOG = System.getLogger(CSIv2SSLTaggedComponentHandlerImpl.class.getName());

    @Override
    public TaggedComponent insert(IORInfo iorInfo, List<ClusterInstanceInfo> clusterInstanceInfo) {
        LOG.log(DEBUG, "insert(iorInfo={0}, clusterInstanceInfo={1})", iorInfo, clusterInstanceInfo);

        List<com.sun.corba.ee.spi.folb.SocketInfo> socketInfos = new ArrayList<>();
        for (ClusterInstanceInfo clInstInfo : clusterInstanceInfo) {
            for (com.sun.corba.ee.spi.folb.SocketInfo sinfo : clInstInfo.endpoints()) {
                if (sinfo.type().equals("SSL") || sinfo.type().equals("SSL_MUTUALAUTH")) {
                    socketInfos.add(sinfo);
                }
            }
        }
        if (Globals.getDefaultHabitat() == null) {
            return null;
        }
        IIOPSSLUtil sslUtil = Globals.getDefaultHabitat().getService(IIOPSSLUtil.class);
        return sslUtil.createSSLTaggedComponent(iorInfo, socketInfos);
    }


    @Override
    public List<SocketInfo> extract(IOR ior) {
        LOG.log(DEBUG, "extract(ior={0})", ior);
        List<SocketInfo> socketInfo = null;
        try {
            if (Globals.getDefaultHabitat() != null) {
                IIOPSSLUtil sslUtil = Globals.getDefaultHabitat().getService(IIOPSSLUtil.class);
                socketInfo = sslUtil.getSSLPortsAsSocketInfo(ior);
            }
            LOG.log(DEBUG, "Extracted socketInfo: {0}", socketInfo);
            return socketInfo;
        } catch (Exception e) {
            throw new IllegalStateException("Exception getting SocketInfo for IOR: " + ior, e);
        }
    }

    @Override
    public void configure(DataCollector collector, ORB orb) {
        LOG.log(TRACE, "configure(dc, orb)");
        try {
            orb.register_initial_reference(CSI_V2_SSL_TAGGED_COMPONENT_HANDLER, this);
        } catch (InvalidName e) {
            throw new IllegalStateException(
                "Failed to register initial reference " + CSI_V2_SSL_TAGGED_COMPONENT_HANDLER + ". " + e.getMessage(),
                e);
        }
    }
}
