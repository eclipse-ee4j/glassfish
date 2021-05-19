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

import com.sun.appserv.management.config.DeployedItemRefConfigCR;

/**
 */
public class CreateAppRefCmd extends DeployCmd implements SourceCmd
{
    public static String kName      = "Name";
    public static String kEnabled   = "Enabled";
    public static String kLBEnabled = "LBEnabled";
    public static String kVirtualServers = "VirtualServers";
    public static String kDisableTimeoutInMinutes = "DisableTimeoutInMinutes";

    public CreateAppRefCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        return getDeployedItemRefConfigCR().
            createDeployedItemRefConfig(getEnabled(), getName(),
                getVirtualServers(), getLBEnabled(),
                getDisableTimeoutInMinutes());
    }

    private boolean getEnabled()
    {
        return ((Boolean)getCmdEnv().get(kEnabled)).booleanValue();
    }

    private boolean getLBEnabled()
    {
        return ((Boolean)getCmdEnv().get(kLBEnabled)).booleanValue();
    }

    private String getVirtualServers()
    {
        return (String)getCmdEnv().get(kVirtualServers);
    }

    private int getDisableTimeoutInMinutes()
    {
        return ((Integer)getCmdEnv().get(kDisableTimeoutInMinutes)).
            intValue();
    }

    private String getName()
    {
        return (String)getCmdEnv().get(kName);
    }
}
