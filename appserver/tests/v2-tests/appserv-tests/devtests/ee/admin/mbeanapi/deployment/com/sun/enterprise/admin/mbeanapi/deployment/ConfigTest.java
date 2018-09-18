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

import com.sun.appserv.management.util.misc.ExceptionUtil;

/**
 */
public class ConfigTest extends BaseTest
{
    private final Cmd target;

    public ConfigTest(final String user, final String password,
            final String host, final int port, final String configName)
    {
        final CmdFactory cmdFactory = getCmdFactory();

        final ConnectCmd connectCmd = cmdFactory.createConnectCmd(
                user, password, host, port);

        final CmdEnv cmdEnv = new CmdEnv();
        cmdEnv.put(DeleteConfigCmd.kConfigName, configName);

        final DeleteConfigCmd cmd = new DeleteConfigCmd(cmdEnv);

        target = new PipeCmd(connectCmd, cmd);
    }

    protected void runInternal() throws Exception
    {
        try
        {
            target.execute();
        }
        catch (Exception e)
        {
            System.out.println("Error: " + ExceptionUtil.getRootCause(e).
                getMessage());
        }
    }

    public static void main(String[] args) throws Exception
    {
        new ConfigTest("admin", "password", "localhost", 8686, args[0]).run();
    }
}
