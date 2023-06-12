/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;


/**
 * AdminCommand to start the instance server.
 * <p>
 * If this is DAS -- we call the instance
 * If this is an instance we start it
 *
 * @author Carla Mott
 */
@Service(name = "start-instance")
@CommandLock(CommandLock.LockType.NONE) // don't prevent _synchronize-files
@PerLookup
@I18n("start.instance.command")
@RestEndpoints({
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.POST,
        path="start-instance",
        description="Start Instance",
        params={
            @RestParam(name="id", value="$parent")
        })
})
public class StartInstanceCommand implements AdminCommand {
    @Inject
    ServiceLocator habitat;

    @Inject
    private Nodes nodes;

    @Inject
    private ServerEnvironment env;

    @Inject
    private Servers servers;

    @Param(name = "instance_name", primary = true)
    private String instanceName;

    @Param(optional = true, defaultValue = "normal", acceptableValues="none, normal, full")
    private String sync="normal";

    @Param(optional = true, defaultValue = "false")
    private boolean debug;

    @Param(optional = true, defaultValue = "false")
    private boolean terse;

    @Param(optional = true, obsolete = true)
    private String setenv;

    private Logger logger;

    private Node   node;
    private String noderef;
    private String nodedir;
    private String nodeHost;
    private Server instance;

    private static final String NL = System.lineSeparator();

    /**
     * restart-instance needs to try to start the instance from scratch if it is not
     * running.  We need to do some housekeeping first.
     * There is no clean way to do this through CommandRunner -- it is twisted together
     * with Grizzly parameters and so on.  So we short-circuit this way!
     * do NOT make this public!!
     * @author Byron Nevins
     */
    StartInstanceCommand(ServiceLocator habitat_, String iname_, boolean debug_, ServerEnvironment env_) {
        instanceName = iname_;
        debug = debug_;
        habitat = habitat_;
        nodes = habitat.getService(Nodes.class);

        // env:  neither getByType or getByContract works.  Not worth the effort
        //to find the correct magic incantation for HK2!
        env = env_;
        servers = habitat.getService(Servers.class);
    }

    /**
     * we have to declare this since HK2 needs it and we have another ctor
     * defined.
     */
    public StartInstanceCommand() {
    }

    @Override
    public void execute(AdminCommandContext ctx) {
        logger = ctx.getLogger();
        ActionReport report = ctx.getActionReport();
        String msg = "";
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);

        if(!StringUtils.ok(instanceName)) {
            msg = Strings.get("start.instance.noInstanceName");
            logger.severe(msg);
            report.setMessage(msg);
            return;
        }
        instance = servers.getServer(instanceName);
        if(instance == null) {
            msg = Strings.get("start.instance.noSuchInstance", instanceName);
            logger.severe(msg);
            report.setMessage(msg);
            return;
        }

        if (instance.isRunning()) {
            msg = Strings.get("start.instance.already.running", instanceName);
            logger.info(msg);
            report.setMessage(msg);
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            return;
        }

        noderef = instance.getNodeRef();
        if(!StringUtils.ok(noderef)) {
            msg = Strings.get("missingNodeRef", instanceName);
            logger.severe(msg);
            report.setMessage(msg);
            return;
        }

        node = nodes.getNode(noderef);
        if (node != null) {
            nodedir = node.getNodeDirAbsolute();
            nodeHost = node.getNodeHost();
        } else {
            msg = Strings.get("missingNode", noderef);
            logger.severe(msg);
            report.setMessage(msg);
            return;
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        if(env.isDas()) {
            startInstance(ctx);
        } else {
            msg = Strings.get("start.instance.notAnInstanceOrDas",
                    env.getRuntimeType().toString());
            logger.severe(msg);
            report.setMessage(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }

        if (report.getActionExitCode() == ActionReport.ExitCode.SUCCESS) {
            // Make sure instance is really up
            String s = pollForLife(instance);
            if (s != null) {
                report.setMessage(s);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            }
        }
    }

    private void startInstance(AdminCommandContext ctx) {
        NodeUtils nodeUtils = new NodeUtils(habitat, logger);
        ArrayList<String> command = new ArrayList<>();
        String humanCommand = null;

        command.add("start-local-instance");

        command.add("--node");
        command.add(noderef);

        if (nodedir != null) {
            command.add("--nodedir");
            command.add(nodedir); //XXX escape space?
        }

        command.add("--sync");
        command.add(sync);

        if (debug) {
            command.add("--debug");
        }

        command.add(instanceName);

        // Convert the command into a string representing the command
        // a human should run.
        humanCommand = makeCommandHuman(command);

        // First error message displayed if we fail
        String firstErrorMessage = Strings.get("start.instance.failed",
                        instanceName, noderef, nodeHost );

        StringBuilder output = new StringBuilder();

        // Run the command on the node and handle errors.
        nodeUtils.runAdminCommandOnNode(node, command, ctx, firstErrorMessage, humanCommand, output);

        ActionReport report = ctx.getActionReport();
        if (report.getActionExitCode() == ActionReport.ExitCode.SUCCESS) {
            // If it was successful say so and display the command output
            String msg = Strings.get("start.instance.success",
                    instanceName, nodeHost);
            if (!terse) {
                msg = StringUtils.cat(NL, output.toString().trim(), msg);
            }
            report.setMessage(msg);
        }

    }

    // return null means A-OK
    private String pollForLife(Server instance) {
        int counter = 0;  // 120 seconds

        while (++counter < 240) {
            if (instance.isRunning()) {
                return null;
            }

            try {
                Thread.sleep(500);
            }
            catch (Exception e) {
                // ignore
            }
        }
        return Strings.get("start.instance.timeout", instanceName);
    }

    private String makeCommandHuman(List<String> command) {
        StringBuilder fullCommand = new StringBuilder();

        // don't use file.separator since this is a local command
        // that may run on a different computer.  We don't necessarily know
        // what it is.

        fullCommand.append("lib/nadmin ");

        for (String s : command) {
            fullCommand.append(" ");
            fullCommand.append(s);
        }

        return fullCommand.toString();
    }
}
