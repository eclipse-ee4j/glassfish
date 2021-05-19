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

/**
 */
public class CreateClusteredInstanceTest extends BaseTest
{
    private final Cmd target;

    public CreateClusteredInstanceTest(final String user, final String password,
            final String host, final int port, final String instanceName,
            final String clusterName, final String nodeAgentName,
            final Map optional)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final CreateClusteredInstanceCmd createClusteredInstanceCmd =
                cmdFactory.createCreateClusteredInstanceCmd(
                    instanceName, clusterName, nodeAgentName, optional);

        target = new PipeCmd(connectCmd, createClusteredInstanceCmd);
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        final String instanceName   = args[0];
        final String clusterName    = args[1];
        final String nodeAgentName  = args[2];

        new CreateClusteredInstanceTest("admin", "password", "localhost", 8686,
            instanceName, clusterName, nodeAgentName, null).run();
    }
}
