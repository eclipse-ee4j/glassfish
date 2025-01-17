/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.SftpException;
import com.sun.enterprise.admin.remote.RemoteRestAdminCommand;
import com.sun.enterprise.admin.remote.ServerRemoteRestAdminCommand;
import com.sun.enterprise.admin.util.RemoteInstanceCommandHelper;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.v3.admin.StopServer;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.launcher.SSHSession;
import org.glassfish.cluster.ssh.sftp.SFTPClient;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.PostConstruct;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.ServerContext;
import org.jvnet.hk2.annotations.Service;

/**
 * AdminCommand to stop the instance
 * server.
 * Shutdown of an instance.
 * This command only runs on DAS.  It calls the instance and asks it to
 * kill itself

 * @author Byron Nevins
 */
@Service(name = "stop-instance")
@PerLookup
@CommandLock(CommandLock.LockType.NONE) // allow stop-instance always
@I18n("stop.instance.command")
@ExecuteOn(RuntimeType.DAS)
@RestEndpoints({
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.POST,
        path="stop-instance",
        description="Stop Instance",
        params={
            @RestParam(name="id", value="$parent")
        })
})
public class StopInstanceCommand extends StopServer implements AdminCommand, PostConstruct {

    @Inject
    private ServiceLocator habitat;
    @Inject
    private ServerContext serverContext;
    @Inject
    private Nodes nodes;
    @Inject
    private ServerEnvironment env;
    @Inject
    IterableProvider<Node> nodeList;
    @Param(optional = true, defaultValue = "true")
    private Boolean force = true;
    @Param(optional = true, defaultValue = "false")
    private Boolean kill = false;
    @Param(optional = false, primary = true)
    private String instanceName;
    private Logger logger;
    private RemoteInstanceCommandHelper helper;
    private ActionReport report;
    private String errorMessage = null;
    private String cmdName = "stop-instance";
    private Server instance;

    @Override
    public void execute(AdminCommandContext context) {
        report = context.getActionReport();
        logger = context.getLogger();

        if (env.isDas()) {
            if (kill) {
                errorMessage = killInstance(context);
            } else {
                errorMessage = callInstance();
            }
        }  else {
            errorMessage = Strings.get("stop.instance.notDas",
                    env.getRuntimeType().toString());
        }

        if(errorMessage == null && !kill) {
            errorMessage = pollForDeath();
        }

        if (errorMessage != null) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(errorMessage);
            return;
        }

        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        report.setMessage(Strings.get("stop.instance.success", instanceName));

        if (kill) {
            // If we killed then stop-local-instance already waited for death
            return;
        }

        // we think the instance is down but it might not be completely down so do further checking
        // get the node name and then the node
        // if localhost check if files exists
        // else if SSH check if file exists  on remote system
        // else can't check anything else.
        String nodeName = instance.getNodeRef();
        Node node = nodes.getNode(nodeName);
        InstanceDirUtils insDU = new InstanceDirUtils(node, serverContext);
        // this should be replaced with method from Node config bean.
        final Path pidFilePath;
        try {
            pidFilePath = insDU.getLocalInstanceDir(instance.getName()).toPath().resolve(Path.of("config", "pid"));
        } catch (IOException e) {
            // could not get the file name so can't see if it still exists. Need to exit
            return;
        }
        if (node.isLocal()) {
            final File pidFile = pidFilePath.toFile();
            if (pidFile.exists()) {
                // server still not down completely, do we poll?
                errorMessage = pollForRealDeath(pidFile::exists);
            }
        } else if (node.getType().equals("SSH")) {
            //use SFTPClient to see if file exists.
            SSHLauncher launcher = new SSHLauncher(node);
            try (SSHSession session = launcher.openSession(); SFTPClient ftpClient = session.createSFTPClient()) {
                if (ftpClient.exists(pidFilePath)){
                    // server still not down, do we poll?
                    Supplier<Boolean> check = () -> {
                        try {
                            return ftpClient.exists(pidFilePath);
                        } catch (SftpException e) {
                            return false;
                        }
                    };
                    errorMessage = pollForRealDeath(check);
                }
            } catch (JSchException ex) {
                //could not get to other host
            } catch (SftpException ex) {
                //could not get to other host
            }
        }
        if (errorMessage != null) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(errorMessage);
        }
    }

    @Override
    public void postConstruct() {
        helper = new RemoteInstanceCommandHelper(habitat);
    }

    private String initializeInstance() {
        if (!StringUtils.ok(instanceName)) {
            return Strings.get("stop.instance.noInstanceName", cmdName);
        }

        instance = helper.getServer(instanceName);
        if (instance == null) {
            return Strings.get("stop.instance.noSuchInstance", instanceName);
        }

        return null;
    }

    /**
     * return null if all went OK...
     *
     */
    private String callInstance() {

        String msg = initializeInstance();
        if (msg != null) {
            return msg;
        }

        String host = instance.getAdminHost();

        if (host == null) {
            return Strings.get("stop.instance.noHost", instanceName);
        }

        int port = helper.getAdminPort(instance);

        if (port < 0) {
            return Strings.get("stop.instance.noPort", instanceName);
        }

        if(!instance.isRunning()) {
            return null;
        }

        try {
            logger.info(Strings.get("stop.instance.init", instanceName));
            RemoteRestAdminCommand rac = new ServerRemoteRestAdminCommand(habitat, "_stop-instance",
                    host, port, false, "admin", null, logger);

            // notice how we do NOT send in the instance's name as an operand!!
            ParameterMap map = new ParameterMap();
            map.add("force", Boolean.toString(force));
            rac.executeCommand(map);
       } catch (Exception e) {
            // The instance server may have died so fast we didn't have time to
            // get the (always successful!!) return data.  This is NOT AN ERROR!
            // see: http://java.net/jira/browse/GLASSFISH-19672
            // also see StopDomainCommand which does the same thing.
        }

        return null;
    }

    private String killInstance(AdminCommandContext context) {
        String msg = initializeInstance();
        if (msg != null) {
            return msg;
        }

        String nodeName = instance.getNodeRef();
        Node node = nodes.getNode(nodeName);
        NodeUtils nodeUtils = new NodeUtils(habitat, logger);

        // asadmin command to run on instances node
        ArrayList<String> command = new ArrayList<String>();
        command.add("stop-local-instance");
        command.add("--kill");
        command.add(instanceName);
        String humanCommand = makeCommandHuman(command);
        String firstErrorMessage = Strings.get("stop.local.instance.kill",
                instanceName, nodeName, humanCommand);

        if (logger.isLoggable(Level.FINE)) {
            logger.fine("stop-instance: running " + humanCommand +
                        " on " + nodeName);
        }

        nodeUtils.runAdminCommandOnNode(node, command, context,
                                        firstErrorMessage, humanCommand, null);

        ActionReport killreport = context.getActionReport();
        if (killreport.getActionExitCode() != ActionReport.ExitCode.SUCCESS) {
            return killreport.getMessage();
        }
        return null;
    }


    // return null means A-OK
    private String pollForDeath() {
        int counter = 0;  // 120 seconds

        while (++counter < 240) {
            if (!instance.isRunning()) {
                return null;
            }

            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return Strings.get("stop.instance.timeout", instanceName);
    }


    private String pollForRealDeath(Supplier<Boolean> pidFileExists) {
        int counter = 0; // 30 seconds

        // 24 * 5 = 120 seconds
        while (++counter < 24) {
            try {
                if (!pidFileExists.get()) {
                    return null;
                }
                // Fairly long interval between tries because checking over
                // SSH is expensive.
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        return Strings.get("stop.instance.timeout.completely", instanceName);

    }

    private String makeCommandHuman(List<String> command) {
        StringBuilder fullCommand = new StringBuilder();

        for (String s : command) {
            fullCommand.append(" ");
            fullCommand.append(s);
        }
        return fullCommand.toString().trim();
    }
}
