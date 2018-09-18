/*
 * Copyright (c) 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.admin.mbeanapi.deployment;

import java.util.Map;

import com.sun.appserv.management.j2ee.J2EEServer;
import com.sun.appserv.management.j2ee.J2EECluster;

/**
 */
public abstract class BaseInstanceCmd extends BaseCmd
{
    public static final String kInstanceName    = "InstanceName";
    public static final String kClusterName     = "ClusterName";
    public static final String kConfigName      = "ConfigName";
    public static final String kNodeAgentName   = "NodeAgentName";
    public static final String kOptional        = "Optional";

    protected BaseInstanceCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    protected J2EEServer getJ2EEServer() throws Exception
    {
        final Map servers = getJ2EEDomain().getServerMap();
        return (J2EEServer)servers.get(getInstanceName());
    }

    protected J2EECluster getJ2EECluster() throws Exception
    {
        final Map clusters = getJ2EEDomain().getClusterMap();
        return (J2EECluster)clusters.get(getClusterName());
    }

    public String getInstanceName()
    {
        return (String)getCmdEnv().get(kInstanceName);
    }

    public String getClusterName()
    {
        return (String)getCmdEnv().get(kClusterName);
    }

    public String getNodeAgentName()
    {
        return (String)getCmdEnv().get(kNodeAgentName);
    }

    public String getConfigName()
    {
        return (String)getCmdEnv().get(kConfigName);
    }

    public Map getOptional()
    {
        return (Map)getCmdEnv().get(kOptional);
    }
}
