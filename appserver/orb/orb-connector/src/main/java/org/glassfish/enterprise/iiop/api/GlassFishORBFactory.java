/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2021 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.enterprise.iiop.api;

import com.sun.enterprise.deployment.EjbDescriptor;

import java.util.Properties;

import org.jvnet.hk2.annotations.Contract;
import org.omg.CORBA.ORB;
import org.omg.PortableInterceptor.IORInfo;
import org.omg.PortableInterceptor.ServerRequestInfo;

/**
 * @author Mahesh Kannan Date: Jan 17, 2009
 */
@Contract
public interface GlassFishORBFactory {

    // This is ORBConstants.ENV_IS_SERVER_PROPERTY. We cannot
    // reference ORBConstants from this class.
    String ENV_IS_SERVER_PROPERTY = "com.sun.corba.ee.ORBEnvironmentIsGlassFishServer";

    ORB createORB(Properties props);

    int getOTSPolicyType();

    int getCSIv2PolicyType();

    Properties getCSIv2Props();

    void setCSIv2Prop(String name, String value);

    int getORBInitialPort();

    String getORBHost(ORB orb);

    int getORBPort(ORB orb);

    boolean isEjbCall(ServerRequestInfo sri);

    String getIIOPEndpoints();

    EjbDescriptor getEjbDescriptor(IORInfo iorInfo);
}
