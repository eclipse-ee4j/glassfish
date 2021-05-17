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
public class ClusterLifecycleTest extends BaseTest
{
    private final Cmd target;

    public ClusterLifecycleTest(final String user, final String password,
            final String host, final int port, final String clusterName,
            final String instanceName, final String nodeAgentName,
            final String configName, final Map optional)
    {
        final CmdChainCmd chain = new CmdChainCmd();

        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final CreateClusterCmd createClusterCmd =
                cmdFactory.createCreateClusterCmd(
                    clusterName, configName, optional);

        final CreateClusteredInstanceCmd createClusteredInstanceCmd =
                cmdFactory.createCreateClusteredInstanceCmd(instanceName,
                        clusterName, nodeAgentName, optional);

        final StartClusterCmd startClusterCmd =
                cmdFactory.createStartClusterCmd(clusterName);

        final StopClusterCmd stopClusterCmd =
                cmdFactory.createStopClusterCmd(clusterName);

        final DeleteClusteredInstanceCmd deleteClusteredInstanceCmd =
                cmdFactory.createDeleteClusteredInstanceCmd(instanceName);

        final DeleteClusterCmd deleteClusterCmd = cmdFactory.
                createDeleteClusterCmd(clusterName);

        chain.addCmd(new PipeCmd(connectCmd, createClusterCmd));
        chain.addCmd(new PipeCmd(connectCmd, createClusteredInstanceCmd));
        chain.addCmd(new PipeCmd(connectCmd, startClusterCmd));
        chain.addCmd(new PipeCmd(connectCmd, stopClusterCmd));
        chain.addCmd(new PipeCmd(connectCmd, deleteClusteredInstanceCmd));
        chain.addCmd(new PipeCmd(connectCmd, deleteClusterCmd));

        target = chain;
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        final String clusterName    = args[0];
        final String instanceName   = args[1];
        final String nodeAgentName  = args[2];
        final String configName     = args.length == 4 ? args[3] : null;

        new ClusterLifecycleTest("admin", "password", "localhost", 8686,
            clusterName, instanceName, nodeAgentName, configName, null).run();
    }
}
