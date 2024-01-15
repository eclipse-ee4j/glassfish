/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin.cluster;

import com.sun.enterprise.config.serverbeans.Cluster;
import java.util.logging.Logger;
import jakarta.inject.Inject;


import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.component.*;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import com.sun.enterprise.config.serverbeans.Domain;
import org.glassfish.api.admin.*;
import org.glassfish.hk2.api.PerLookup;

@I18n("stop.cluster.command")
@Service(name="stop-cluster")
@PerLookup
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.POST,
        path="stop-cluster",
        description="Stop Cluster",
        params={
            @RestParam(name="id", value="$parent")
        })
})
@Progress
public class StopClusterCommand implements AdminCommand {

    @Param(optional=false, primary=true)
    private String clusterName;

    @Param(optional=true, defaultValue="false")
    private boolean kill = false;

    @Inject
    private ServerEnvironment env;

    @Inject
    private Domain domain;

    @Inject
    private CommandRunner runner;

    @Param(optional = true, defaultValue = "false")
    private boolean verbose;

    @Override
    public void execute(AdminCommandContext context) {

        ActionReport report = context.getActionReport();
        Logger logger = context.getLogger();

        logger.info(Strings.get("stop.cluster", clusterName));

        // Require that we be a DAS
        if(!env.isDas()) {
            String msg = Strings.get("cluster.command.notDas");
            logger.warning(msg);
            report.setActionExitCode(ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        ClusterCommandHelper clusterHelper = new ClusterCommandHelper(domain,
                runner);

        ParameterMap map = null;
        if (kill) {
            map = new ParameterMap();
            map.add("kill", "true");
        }
        try {
            // Run start-instance against each instance in the cluster
            String commandName = "stop-instance";
            clusterHelper.runCommand(commandName, map, clusterName, context, false,
                    verbose);
        } catch (CommandException e) {
            String msg = e.getLocalizedMessage();
            logger.warning(msg);
            report.setActionExitCode(ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }
    }
}
