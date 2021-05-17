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

import com.sun.appserv.management.config.StandaloneServerConfig;

/**
 */
public class InstanceLifecycleTest extends BaseTest
{
    private final Cmd target;

    public InstanceLifecycleTest(final String user, final String password,
            final String host, final int port, final String instanceName,
            final String nodeAgentName, final String configName,
            final Map optional)
    {
        final CmdChainCmd chain = new CmdChainCmd();

        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);
        final CreateInstanceCmd createInstanceCmd = cmdFactory.
                createCreateInstanceCmd(instanceName, nodeAgentName,
                        configName, optional);
        final StartInstanceCmd startInstanceCmd = cmdFactory.
                createStartInstanceCmd(instanceName);
        final StopInstanceCmd stopInstanceCmd = cmdFactory.
                createStopInstanceCmd(instanceName);
        final DeleteInstanceCmd deleteInstanceCmd = cmdFactory.
                createDeleteInstanceCmd(instanceName);

        chain.addCmd(new PipeCmd(connectCmd, createInstanceCmd));
        chain.addCmd(new PipeCmd(connectCmd, startInstanceCmd));
        chain.addCmd(new PipeCmd(connectCmd, stopInstanceCmd));
        chain.addCmd(new PipeCmd(connectCmd, deleteInstanceCmd));

        target = chain;
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        new InstanceLifecycleTest("admin", "password", "localhost", 8686,
            args[0], "n1", null, null).run();
    }
}
