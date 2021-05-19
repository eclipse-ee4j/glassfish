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
import java.util.Set;

/**
 */
class DeployedAppInspector
{
    DeployedAppInspector(ConnectCmd ccmd, String target)
    {
        CmdFactory cmdFactory = Env.getCmdFactory();
        ListDeployedAppsCmd cmd = cmdFactory.createListDeployedAppsCmd(target, "All");
        targetCmd = new PipeCmd(ccmd, cmd);
    }

    ////////////////////////////////////////////////////////////////////////////

    DeployedAppInspector(String user, String password, String host,
            int port, String target)
    {
        CmdFactory cmdFactory = Env.getCmdFactory();
        ConnectCmd connectCmd = cmdFactory.createConnectCmd(user, password, host, port);
        ListDeployedAppsCmd cmd = cmdFactory.createListDeployedAppsCmd(target, "All");
        targetCmd = new PipeCmd(connectCmd, cmd);
    }

    ////////////////////////////////////////////////////////////////////////////

    boolean isDeployed(String id) throws DeploymentTestsException
    {
        try
        {
            if(results == null)
                refresh();

            return results.contains(id);
        }
        catch(Exception e)
        {
            throw new DeploymentTestsException("Exception caught in DeployedAppInspector.isDeployed().", e);
        }
    }

    ////////////////////////////////////////////////////////////////////////////

    void refresh()
    {
        Set[] sets = null;

        try
        {
            sets = (Set[])targetCmd.execute();
        }
        catch(Exception e)
        {
            // note: the called method literally declares 'throws Exception' !!!
            throw new RuntimeException(e);
        }

        // get the intersection of the 2 sets
        if(sets.length > 1)
            sets[0].retainAll(sets[1]);

        results = sets[0];
    }

    ////////////////////////////////////////////////////////////////////////////

    private Set results;
    private Cmd targetCmd;
}
