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
public class ListDeployedAppsTest extends BaseTest
{
    private final Cmd targetCmd;

    public ListDeployedAppsTest(final String user, final String password,
            final String host, final int port, final String target,
            final String appType)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final ListDeployedAppsCmd cmd = cmdFactory.createListDeployedAppsCmd(
                target, appType);

        targetCmd = new PipeCmd(connectCmd, cmd);
    }

    protected void runInternal() throws Exception
    {
        Set[] sets = (Set[])targetCmd.execute();

        //System.out.println("Set1: " + sets[0]);
        //System.out.println("Set2: " + sets[1]);

        if(sets.length > 1)
            sets[0].retainAll(sets[1]);

        results = new String[sets[0].size()];
        sets[0].toArray(results);
    }

    String[] getResults()
    {
        return results;
    }
    public static void main(String[] args) throws Exception
    {
        new ListDeployedAppsTest("admin", "password", "localhost", 8686,
                args[0], args[1]).run();
    }

    private String[] results;
}
