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
public class UndeploymentTest extends BaseTest
{
    private Cmd cmd;

    public UndeploymentTest(String user, String password, String host,
            int port, String name, String target)
    {
        CmdFactory cmdFactory = getCmdFactory();

        ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        UndeployCmd undeployCmd = cmdFactory.
                createUndeployCmd(name, target);

        cmd = new PipeCmd(connectCmd, undeployCmd);
    }

    protected void runInternal() throws Exception
    {
        cmd.execute();
    }

    public static void main(String[] args) throws Exception
    {
        final String appName            = args[0];
        final String appserverTarget    = args.length == 2 ? args[1] : null;

        new UndeploymentTest("admin", "password", "localhost", 8686,
            appName, appserverTarget).run();
    }
}
