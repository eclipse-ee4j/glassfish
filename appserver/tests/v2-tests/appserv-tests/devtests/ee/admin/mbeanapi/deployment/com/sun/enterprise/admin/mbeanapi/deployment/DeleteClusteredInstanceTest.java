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

/**
 */
public class DeleteClusteredInstanceTest extends BaseTest
{
    private final Cmd target;

    public DeleteClusteredInstanceTest(final String user, final String password,
            final String host, final int port, final String instanceName)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final DeleteClusteredInstanceCmd deleteClusteredInstanceCmd =
                cmdFactory.createDeleteClusteredInstanceCmd(instanceName);

        target = new PipeCmd(connectCmd, deleteClusteredInstanceCmd);
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        new DeleteClusteredInstanceTest("admin", "password", "localhost",
            8686, args[0]).run();
    }
}
