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

import java.io.*;
import java.util.*;
import javax.management.*;
import com.sun.appserv.management.deploy.*;
import com.sun.appserv.management.config.*;

/**
 */
public class UndeployCmd extends DeployCmd
{
    public UndeployCmd(CmdEnv cmdEnv)
    {
        super(cmdEnv);
    }

    public Object execute() throws Exception
    {
        assert isConnected();

        undeploy();
        return null;
    }


    protected void undeploy() throws Exception
    {
        final String		appName		= getAppName();
        final DeploymentMgr	deployMgr	= getDeploymentMgr();

        if (!DEFAULT_DEPLOY_TARGET.equals(getTarget()))
        {
            final DeployedItemRefConfigCR	refMgr = 
                getDeployedItemRefConfigCR();

            stopApp();
            refMgr.removeDeployedItemRefConfig(appName);
        }
        final Map statusData = deployMgr.undeploy(appName, null);
        final DeploymentStatus status	= 
            DeploymentSupport.mapToDeploymentStatus( statusData );
        checkFailed(checkForException(status));
    }
}
