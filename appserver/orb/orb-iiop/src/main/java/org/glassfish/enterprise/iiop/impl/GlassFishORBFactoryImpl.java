/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.deployment.EjbDescriptor;

import jakarta.inject.Inject;

import java.lang.System.Logger;
import java.util.Properties;

import org.glassfish.enterprise.iiop.api.GlassFishORBFactory;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;
import org.omg.CORBA.INV_POLICY;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.ServerRequestInfo;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;

/**
 * @author Mahesh Kannan 2009
 */
@Service
public class GlassFishORBFactoryImpl implements GlassFishORBFactory, PostConstruct {
    private static final Logger LOG = System.getLogger(GlassFishORBFactoryImpl.class.getName());

    @Inject
    private ServiceLocator serviceLocator;

    private GlassFishORBManager gfORBManager;

    @Override
    public void postConstruct() {
        this.gfORBManager = new GlassFishORBManager(serviceLocator);
    }

    @Override
    public int getOTSPolicyType() {
        return POARemoteReferenceFactory.OTS_POLICY_TYPE;
    }

    @Override
    public int getCSIv2PolicyType() {
        return POARemoteReferenceFactory.CSIv2_POLICY_TYPE;
    }

    @Override
    public ORB createORB(Properties props) {
        return gfORBManager.createOrb(props);
    }

    @Override
    public Properties getCSIv2Props() {
        return gfORBManager.getCSIv2Props();
    }

    @Override
    public void setCSIv2Prop(String name, String value) {
        gfORBManager.setCSIv2Prop(name, value);
    }

    @Override
    public int getORBInitialPort() {
        return gfORBManager.getORBInitialPort();
    }

    @Override
    public String getORBHost(ORB orb) {
        return ((com.sun.corba.ee.spi.orb.ORB) orb).getORBData().getORBInitialHost();
    }

    @Override
    public int getORBPort(ORB orb) {
        return ((com.sun.corba.ee.spi.orb.ORB) orb).getORBData().getORBInitialPort();
    }

    /**
     * Returns true, if the incoming call is a EJB method call.
     * This checks for is_a calls and ignores those calls. In callflow analysis
     * when a component looks up another component, this lookup should be
     * considered part of the same call coming in.
     * Since a lookup triggers the iiop codebase, it will fire a new request start.
     * With this check, we consider the calls that are only new incoming ejb
     * method calls as new request starts.
     */
    @Override
    public boolean isEjbCall(ServerRequestInfo sri) {
        return gfORBManager.isEjbAdapterName(sri.adapter_name()) && !"_is_a".equals(sri.operation());
    }

    @Override
    public String getIIOPEndpoints() {
        return gfORBManager.getIIOPEndpoints();
    }

    public EjbDescriptor getEjbDescriptor(IORInfo iorInfo) {
        CSIv2Policy csiv2Policy = null;
        try {
            csiv2Policy = (CSIv2Policy) iorInfo.get_effective_policy(getCSIv2PolicyType());
        } catch (INV_POLICY e) {
            LOG.log(TRACE, "CSIv2Policy cound not be loaded", e);
        }
        LOG.log(DEBUG, "CSIv2Policy: {0}", csiv2Policy);

        // Add CSIv2 tagged component for this EJB type.

        if (csiv2Policy != null) {
            return csiv2Policy.getEjbDescriptor();
        }
        return null;
    }
}
