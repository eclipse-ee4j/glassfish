/*
 * Copyright (c) 2024, 2025 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.config.serverbeans.Nodes;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Servers;
import com.sun.enterprise.universal.glassfish.ASenvPropertyReader;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.InstanceDirs;
import com.sun.enterprise.v3.admin.cluster.SecureAdminBootstrapHelper.BootstrapException;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
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
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.embeddable.GlassFishVariable.NODES_ROOT;

/**
 * Remote AdminCommand to create an instance.  This command is run only on DAS.
 *  1. Register the instance on DAS
 *  2. Create the file system on the instance node via ssh, node agent, or other
 *  3. Bootstrap a minimal set of config files on the instance for secure admin.
 *
 * @author Jennifer Chou
 */
@Service(name = "create-instance")
@I18n("create.instance")
@PerLookup
@ExecuteOn({RuntimeType.DAS})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="create-instance",
        description="Create Instance")
})
public class CreateInstanceCommand implements AdminCommand {
    private static final String NL = System.getProperty("line.separator");
    @Inject
    private CommandRunner cr;
    @Inject
    ServiceLocator habitat;
    @Inject
    IterableProvider<Node> nodeList;
    @Inject
    private Nodes nodes;
    @Inject
    private Servers servers;
    @Inject
    private ServerEnvironment env;
    @Param(name = "node", alias = "nodeagent")
    String node;
    @Param(name = "config", optional = true)
    @I18n("generic.config")
    String configRef;
    @Param(name = "cluster", optional = true)
    String clusterName;
    @Param(name = "lbenabled", optional = true)
    private Boolean lbEnabled;
    @Param(name = "checkports", optional = true, defaultValue = "true")
    private boolean checkPorts;
    @Param(optional = true, defaultValue = "false")
    private boolean terse;
    @Param(name = "portbase", optional = true)
    private String portBase;
    @Param(name = "systemproperties", optional = true, separator = ':')
    private String systemProperties;
    @Param(name = "instance_name", primary = true)
    private String instance;
    private Logger logger;
    private AdminCommandContext ctx;
    private Node theNode;
    private String nodeHost;
    private String nodeDir;
    private String installDir;
    private String registerInstanceMessage;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        ctx = context;
        logger = context.getLogger();

        if (!env.isDas()) {
            String msg = Strings.get("notAllowed");
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        // Make sure Node is valid
        theNode = nodes.getNode(node);
        if (theNode == null) {
            String msg = Strings.get("noSuchNode", node);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        if (lbEnabled != null && clusterName == null) {
            String msg = Strings.get("lbenabledNotForStandaloneInstance");
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        nodeHost = theNode.getNodeHost();
        nodeDir = theNode.getNodeDirAbsolute();
        installDir = theNode.getInstallDir();

        if (theNode.isLocal()){
            validateInstanceDirUnique(report, context);
            if (report.getActionExitCode() != ActionReport.ExitCode.SUCCESS
                    && report.getActionExitCode() != ActionReport.ExitCode.WARNING) {
                // If we couldn't update domain.xml then stop!
                return;
            }
        }

        // First, update domain.xml by calling _register-instance
        CommandInvocation ci = cr.getCommandInvocation("_register-instance", report, context.getSubject());
        ParameterMap map = new ParameterMap();
        map.add("node", node);
        map.add("config", configRef);
        map.add("cluster", clusterName);
        if (lbEnabled != null) {
            map.add("lbenabled", lbEnabled.toString());
        }
        if (!checkPorts) {
            map.add("checkports", "false");
        }
        if (StringUtils.ok(portBase)) {
            map.add("portbase", portBase);
        }
        map.add("systemproperties", systemProperties);
        map.add("DEFAULT", instance);
        ci.parameters(map);
        ci.execute();


        if (report.getActionExitCode() != ActionReport.ExitCode.SUCCESS
                && report.getActionExitCode() != ActionReport.ExitCode.WARNING) {
            // If we couldn't update domain.xml then stop!
            return;
        }

        registerInstanceMessage = report.getMessage();

        // if nodehost is localhost and installdir is null and config node, update config node
        // so installdir is product root. see register-instance above
        if (theNode.isLocal() && installDir == null) {
            ci = cr.getCommandInvocation("_update-node", report, context.getSubject());
            map = new ParameterMap();
            map.add("installdir", "${com.sun.aas.productRoot}");
            map.add("type", "CONFIG");
            map.add("DEFAULT", theNode.getName());
            ci.parameters(map);
            ci.execute();


            if (report.getActionExitCode() != ActionReport.ExitCode.SUCCESS
                    && report.getActionExitCode() != ActionReport.ExitCode.WARNING) {
                // If we couldn't update domain.xml then stop!
                return;
            }
        }

        if (!validateDasOptions(context)) {
            report.setActionExitCode(ActionReport.ExitCode.WARNING);
            return;
        }

        // Then go create the instance filesystem on the node
        createInstanceFilesystem(context);
    }

    private void validateInstanceDirUnique(ActionReport report, AdminCommandContext context) {
        CommandInvocation listInstances = cr.getCommandInvocation("list-instances", report, context.getSubject());
        ParameterMap map = new ParameterMap();
        map.add("whichTarget", theNode.getName());
        listInstances.parameters(map);
        listInstances.execute();
        Properties pro = listInstances.report().getExtraProperties();
        if (pro != null){
            List<HashMap> instanceList = (List<HashMap>) pro.get("instanceList");
            if (instanceList == null) {
                return;
            }
            for (HashMap instanceMap : instanceList) {
                final File nodeDirFile = nodeDir == null ? defaultLocalNodeDirFile() : new File(nodeDir);
                File instanceDir = new File(new File(nodeDirFile.toString(), theNode.getName()), instance);
                String instanceName = (String)instanceMap.get("name");
                File instanceListDir = new File(new File(nodeDirFile.toString(), theNode.getName()), instance);
                if (instance.equalsIgnoreCase(instanceName) && instanceDir.equals(instanceListDir)) {
                    String msg = Strings.get("Instance.duplicateInstanceDir", instance, instanceName);
                    logger.warning(msg);
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    report.setMessage(msg);
                    return;
                }
            }
        }
    }

    private File defaultLocalNodeDirFile() {
        final Map<String,String> systemProps =
            Collections.unmodifiableMap(new ASenvPropertyReader().getProps());
        // The default "nodes" directory we want to use has been set in asenv.conf named as
        // AS_DEF_NODES_PATH
        String nodeDirDefault = systemProps.get(NODES_ROOT.getPropertyName());
        return new File(nodeDirDefault);

    }

    private File getDomainInstanceDir() {
        return env.getInstanceRoot();
    }

    private void createInstanceFilesystem(AdminCommandContext context) {
        ActionReport report = ctx.getActionReport();
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        try {
            Server dasServer = servers.getServer(SystemPropertyConstants.DAS_SERVER_NAME);
            final SSHLauncher sshL = theNode.isLocal() ? null : new SSHLauncher(theNode);
            List<String> command = generateCommand(dasServer, sshL);
            String humanCommand = makeCommandHuman(command);
            if (userManagedNodeType()) {
                String msg = Strings.get("create.instance.config", instance, humanCommand);
                msg = StringUtils.cat(NL, registerInstanceMessage, msg);
                report.setMessage(msg);
                return;
            }

            // First error message displayed if we fail
            String firstErrorMessage = Strings.get("create.instance.filesystem.failed", instance, node, nodeHost);

            // Run the command on the node and handle errors.
            NodeUtils nodeUtils = new NodeUtils(habitat);
            StringBuilder output = new StringBuilder();
            nodeUtils.runAdminCommandOnNode(theNode, command, ctx, firstErrorMessage, humanCommand, output);

            if (report.getActionExitCode() != ActionReport.ExitCode.SUCCESS) {
                // something went wrong with the nonlocal command don't continue but set status to
                // warning because config was updated correctly or we would not be here.
                report.setActionExitCode(ActionReport.ExitCode.WARNING);
                return;
            }

            // If it was successful say so and display the command output
            String msg = Strings.get("create.instance.success", instance, nodeHost);
            if (!terse) {
                msg = StringUtils.cat(NL, output.toString().trim(), registerInstanceMessage, msg);
            }
            report.setMessage(msg);

            try (SecureAdminBootstrapHelper bootstrapHelper = createBootstrapHelper(sshL)) {
                bootstrapHelper.bootstrapInstance();
            }
        } catch (IOException | BootstrapException e) {
            String message = Strings.get("create.instance.boot.failed", instance, node, e.getMessage());
            logger.log(Level.SEVERE, message, e);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(message);
        }
        if (report.getActionExitCode() != ActionReport.ExitCode.SUCCESS) {
            // something went wrong with the nonlocal command don't continue but set status to
            // warning because config was updated correctly or we would not be here.
            report.setActionExitCode(ActionReport.ExitCode.WARNING);
        }
    }


    private List<String> generateCommand(Server dasServer, SSHLauncher sshL) throws BootstrapException {
        List<String> command = new ArrayList<>();
        if (!theNode.isLocal()) {
            command.add("--host");
            command.add(resolveAdminHost(sshL));
        }
        command.add("--port");
        command.add(Integer.toString(dasServer.getAdminPort()));

        command.add("_create-instance-filesystem");
        if (nodeDir != null) {
            command.add("--nodedir");
            command.add(StringUtils.quotePathIfNecessary(nodeDir));
        }

        command.add("--node");
        command.add(node);
        command.add(instance);
        return command;
    }


    private SecureAdminBootstrapHelper createBootstrapHelper(SSHLauncher sshL) throws IOException, BootstrapException {
        if (theNode.isLocal()) {
            /*
             * Pass the node directory parent and the node directory name explicitly
             * or else InstanceDirs will not work as we want if there are multiple
             * nodes registered on this node.
             *
             * If the configuration recorded an explicit directory for the node,
             * then use it.  Otherwise, use the default node directory of
             * ${installDir}/glassfish/nodes/${nodeName}.
             */
            final File nodeDirFile = nodeDir == null ? defaultLocalNodeDirFile() : new File(nodeDir);
            final File localInstanceDir = new InstanceDirs(nodeDirFile.toString(), theNode.getName(), instance)
                .getInstanceDir();
            return SecureAdminBootstrapHelper.getLocalHelper(env.getInstanceRoot(), localInstanceDir);
        }
        // nodedir is the root of where all the node dirs will be created.
        // add the name of the node as that is where the instance files should be created
        String thisNodeDir = nodeDir == null ? null : (nodeDir + "/" + node);
        return SecureAdminBootstrapHelper.getRemoteHelper(sshL, getDomainInstanceDir(), thisNodeDir, instance, theNode);
    }

    /**
     * This ensures we don't step on another domain's node files on a remote
     * instance. See bug GLASSFISH-14985.
     */
    private boolean validateDasOptions(AdminCommandContext context) {
        boolean isDasOptionsValid = true;
        if (theNode.isLocal() || (!theNode.isLocal() && theNode.getType().equals("SSH"))) {
            ActionReport report = ctx.getActionReport();
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

            NodeUtils nodeUtils = new NodeUtils(habitat);
            Server dasServer = servers.getServer(SystemPropertyConstants.DAS_SERVER_NAME);
            String dasHost = dasServer.getAdminHost();
            String dasPort = Integer.toString(dasServer.getAdminPort());

            ArrayList<String> command = new ArrayList<>();

            if (!theNode.isLocal()) {
                // Only specify the DAS host if the node is remote. See issue 13993
                command.add("--host");
                command.add(dasHost);
            }

            command.add("--port");
            command.add(dasPort);

            command.add("_validate-das-options");

            if (nodeDir != null) {
                command.add("--nodedir");
                command.add(nodeDir); //XXX escape spaces?
            }

            command.add("--node");
            command.add(node);

            command.add(instance);

            // Run the command on the node
            nodeUtils.runAdminCommandOnNode(theNode, command, ctx, "", null, null);

            if (report.getActionExitCode() != ActionReport.ExitCode.SUCCESS) {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                isDasOptionsValid = false;
            }
        }
        return isDasOptionsValid;
    }

    private String makeCommandHuman(List<String> command) {
        StringBuilder fullCommand = new StringBuilder();

        fullCommand.append("lib");
        fullCommand.append(System.getProperty("file.separator"));
        fullCommand.append("nadmin ");

        for (String s : command) {
            if (s.equals("_create-instance-filesystem")) {
                // We tell the user to run create-local-instance, not the
                // hidden command
                fullCommand.append(" ");
                fullCommand.append("create-local-instance");
            }
            else {
                fullCommand.append(" ");
                fullCommand.append(s);
            }
        }

        return fullCommand.toString();
    }

    // verbose but very readable...
    private boolean userManagedNodeType() {
        if (theNode.isLocal()) {
            return false;
        }

        if (theNode.getType().equals("SSH")) {
            return false;
        }

        return true;
    }


    private String resolveAdminHost(SSHLauncher sshLauncher) throws BootstrapException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(InetAddress.getByName(sshLauncher.getHost()), sshLauncher.getPort()));
            return socket.getLocalAddress().getHostName();
        } catch (IOException e) {
            throw new BootstrapException("Failed to resolve the admin host visible from the remote node " + node, e);
        }
    }
}
