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
public class DeploymentTest extends BaseTest
{
    private final Cmd target;

    public DeploymentTest(final String user, final String password,
            final String host, final int port, final String archive,
            final String name, final String contextRoot,
            final boolean enable)
    {
        this(user, password, host, port, archive, name, contextRoot, enable,
                "server");
    }

    public DeploymentTest(final String user, final String password,
            final String host, final int port, final String archive,
            final String name, final String contextRoot,
            final boolean enable, String appservTarget)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final DeployCmd deployCmd = cmdFactory.createDeployCmd(archive, name,
                contextRoot, enable, appservTarget);

        target = new PipeCmd(connectCmd, deployCmd);
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }

    public static void main(String[] args) throws Exception
    {
        final String archive            = args[0];
        final String name               = args.length >= 2 ? args[1] : null;
        final String appserverTarget    = args.length == 3 ? args[2] : null;

        new DeploymentTest("admin", "password", "localhost", 8686,
            archive, name, null, true, appserverTarget).run();
    }
}
