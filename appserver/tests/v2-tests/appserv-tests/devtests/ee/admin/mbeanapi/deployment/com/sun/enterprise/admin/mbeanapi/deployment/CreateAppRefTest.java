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
import java.util.HashMap;

import com.sun.appserv.management.config.DeployedItemRefConfig;


/**
 */
public class CreateAppRefTest extends BaseTest
{
    private final Cmd target;

    public CreateAppRefTest(final String user,
        final String password, final String host, final int port,
        final String refName, final boolean enabled,
        final String virtualServers,final boolean lbEnabled,
        final int disableTimeoutInMinutes, final String appservTarget)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final CreateAppRefCmd createCmd =
                cmdFactory.createCreateAppRefCmd(refName, enabled,
                    virtualServers, lbEnabled, disableTimeoutInMinutes,
                    appservTarget);

        final PipeCmd p1 = new PipeCmd(connectCmd, createCmd);
        final PipeCmd p2 = new PipeCmd(p1, new VerifyCreateCmd());

        target = p2;
    }

    protected void runInternal() throws Exception
    {
        target.execute();
    }


    public static void main(String[] args) throws Exception
    {
        final String appRef = args[0];
        final String target = args[1];
        final String virtualServers = args.length == 3 ? args[2] : null;

        new CreateAppRefTest(
                "admin", "password", "localhost", 8686,
                appRef, false, virtualServers, false, 160, target).run();
    }

    private final class VerifyCreateCmd implements Cmd, SinkCmd
    {
        private DeployedItemRefConfig res;

        private VerifyCreateCmd()
        {
        }

        public void setPipedData(Object o)
        {
            res = (DeployedItemRefConfig)o;
        }

        public Object execute() throws Exception
        {
            System.out.println("Ref="+res.getName());
            System.out.println("Enabled="+res.getEnabled());
            System.out.println("VirtualServers="+res.getVirtualServers());
            //System.out.println("LBEnabled="+res.getLBEnabled());
            //System.out.println("DisableTimeoutInMinutes="+res.getDisableTimeoutInMinutes());
            return new Integer(0);
        }

    }
}
