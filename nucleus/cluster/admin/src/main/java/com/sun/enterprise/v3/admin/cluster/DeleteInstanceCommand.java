/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.util.StringUtils;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * Remote AdminCommand to delete an instance.  This command is run only on DAS.
 *
 * @author Jennifer Chou
 */
@Service(name = "delete-instance")
@I18n("delete.instance")
@PerLookup
@ExecuteOn({RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.DELETE,
        path="delete-instance",
        description="Delete Instance",
        params={
            @RestParam(name="id", value="$parent")
        })
})
public class DeleteInstanceCommand implements AdminCommand {

    private static final String NL = System.getProperty("line.separator");

    @Inject
    private CommandRunner cr;

    @Inject
    ServiceLocator habitat;

    @Inject
    private Servers servers;

    @Inject
    private Nodes nodes;

    @Param(name = "instance_name", primary = true)
    private String instanceName;

    @Param(defaultValue = "false", optional=true)
    private boolean terse;

    private Server instance;
    private String noderef;
    private String nodedir;
    private Logger logger;
    private String instanceHost;
    private Node theNode = null;

    @Override
    public void execute(AdminCommandContext ctx) {
        ActionReport report = ctx.getActionReport();
        logger = ctx.getLogger();
        String msg = "";
        boolean  fsfailure = false;
        boolean  configfailure = false;

        // We are going to delete a server instance. Get the instance
        instance = servers.getServer(instanceName);

        if (instance == null) {
            msg = Strings.get("start.instance.noSuchInstance", instanceName);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }
        instanceHost = instance.getAdminHost();

        // make sure instance is not running.
        if (instance.isRunning()){
            msg = Strings.get("instance.shutdown", instanceName);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        // We attempt to delete the instance filesystem first by running
        // _delete-instance-filesystem. We then remove the instance
        // from the config no matter if we could delete the files or not.

        // Get the name of the node from the instance's node-ref field
        noderef = instance.getNodeRef();
        if(!StringUtils.ok(noderef)) {
            msg = Strings.get("missingNodeRef", instanceName);
            fsfailure = true;
        } else {
            theNode = nodes.getNode(noderef);
            if (theNode == null) {
                msg = Strings.get("noSuchNode", noderef);
                fsfailure = true;
            }
        }

        if (!fsfailure) {
            nodedir = theNode.getNodeDirAbsolute();
            deleteInstanceFilesystem(ctx);
            report = ctx.getActionReport();
            if (report.getActionExitCode() != ActionReport.ExitCode.SUCCESS) {
                fsfailure = true;
            }
            msg = report.getMessage();
        }

        // Now remove the instance from domain.xml.
        CommandInvocation ci = cr.getCommandInvocation("_unregister-instance", report, ctx.getSubject());
        ParameterMap map = new ParameterMap();
        map.add("DEFAULT", instanceName);
        ci.parameters(map);
        ci.execute();

        if (report.getActionExitCode() != ActionReport.ExitCode.SUCCESS &&
            report.getActionExitCode() != ActionReport.ExitCode.WARNING) {
            // Failed to delete from domain.xml
            configfailure = true;
            if (fsfailure) {
                // Failed to delete instance from fs too
                msg = msg + NL + report.getMessage();
            } else {
                msg = report.getMessage();
            }
        }

        // OK, try to give a helpful message depending on the failure
        if (configfailure && fsfailure) {
            msg = msg + NL + NL + Strings.get("delete.instance.failed",
                    instanceName, instanceHost);
        } else if (configfailure && !fsfailure) {
            msg = msg + NL + NL + Strings.get("delete.instance.config.failed",
                    instanceName, instanceHost);
        } else if (!configfailure && fsfailure) {
            report.setActionExitCode(ActionReport.ExitCode.WARNING);
            // leave msg as is
        }

        if (configfailure) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
        }
    }

    private void deleteInstanceFilesystem(AdminCommandContext ctx) {

        NodeUtils nodeUtils = new NodeUtils(habitat, logger);
        ArrayList<String> command = new ArrayList<String>();
        String humanCommand = null;

        command.add("_delete-instance-filesystem");

        if (nodedir != null) {
            command.add("--nodedir");
            command.add(nodedir); //XXX escape spaces?
        }

        command.add("--node");
        command.add(noderef);

        command.add(instanceName);

        humanCommand = makeCommandHuman(command);

        // First error message displayed if we fail
        String firstErrorMessage = Strings.get("delete.instance.filesystem.failed",
                        instanceName, noderef, theNode.getNodeHost() );

        StringBuilder output = new StringBuilder();

        // Run the command on the node and handle errors.
        nodeUtils.runAdminCommandOnNode(theNode, command, ctx, firstErrorMessage,
                humanCommand, output);

        ActionReport report = ctx.getActionReport();

        if (report.getActionExitCode() != ActionReport.ExitCode.SUCCESS) {
            return;
        }

        // If it was successful say so and display the command output
        String msg = Strings.get("delete.instance.success",
                    instanceName, theNode.getNodeHost());
        if (!terse) {
            msg = StringUtils.cat(NL,
                    output.toString().trim(), msg);
        }
        report.setMessage(msg);
    }

    private String makeCommandHuman(List<String> command) {
        StringBuilder fullCommand = new StringBuilder();

        fullCommand.append("lib");
        fullCommand.append(System.getProperty("file.separator"));
        fullCommand.append("nadmin ");

        for (String s : command) {
            if (s.equals("_delete-instance-filesystem")) {
                // We tell the user to run delete-local-instance, not the
                // hidden command
                fullCommand.append(" ");
                fullCommand.append("delete-local-instance");
            } else {
                fullCommand.append(" ");
                fullCommand.append(s);
            }
        }

        return fullCommand.toString();
    }

}
