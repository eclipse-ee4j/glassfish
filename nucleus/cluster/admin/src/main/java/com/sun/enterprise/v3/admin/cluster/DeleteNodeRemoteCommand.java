/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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
import com.sun.enterprise.config.serverbeans.SshAuth;
import com.sun.enterprise.config.serverbeans.SshConnector;
import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.IterableProvider;
import org.glassfish.hk2.api.ServiceLocator;

/**
 * Remote AdminCommand to delete a config node.  This command is run only on DAS.
 *
 * @author Carla Mott
 */
public abstract class DeleteNodeRemoteCommand implements AdminCommand {
    private static final int DEFAULT_TIMEOUT_MSEC = 300000; // 5 minutes
    @Inject
    protected ServiceLocator habitat;
    @Inject
    IterableProvider<Node> nodeList;
    @Inject
    Nodes nodes;
    @Inject
    private CommandRunner cr;
    @Param(name = "name", primary = true)
    String name;
    @Param(optional = true, defaultValue = "false")
    boolean uninstall;
    @Param(optional = true, defaultValue = "false")
    boolean force;
    protected String remotepassword = null;
    protected String sshkeypassphrase = null;
    private static final String NL = System.getProperty("line.separator");
    protected Logger logger = null;

    protected abstract List<String> getPasswords();

    protected abstract String getUninstallCommandName();
    protected abstract void setTypeSpecificOperands(List<String> command, ParameterMap map);

    protected final void executeInternal(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        logger = context.getLogger();
        Node node = nodes.getNode(name);

        if (node == null) {
            //no node to delete  nothing to do here
            String msg = Strings.get("noSuchNode", name);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }
        String type = node.getType();
        if ((type == null) || (type.equals("CONFIG"))) {
            //no node to delete  nothing to do here
            String msg = Strings.get("notRemoteNodeType", name);
            logger.warning(msg);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(msg);
            return;
        }

        ParameterMap info = new ParameterMap();

        if (uninstall) {
            //store needed info for uninstall
            SshConnector sshC = node.getSshConnector();
            SshAuth sshAuth = sshC.getSshAuth();

            if (sshAuth.getPassword() != null) {
                info.add(NodeUtils.PARAM_REMOTEPASSWORD, sshAuth.getPassword());
            }

            if (sshAuth.getKeyPassphrase() != null) {
                info.add(NodeUtils.PARAM_SSHKEYPASSPHRASE, sshAuth.getKeyPassphrase());
            }

            if (sshAuth.getKeyfile() != null) {
                info.add(NodeUtils.PARAM_SSHKEYFILE, sshAuth.getKeyfile());
            }

            info.add(NodeUtils.PARAM_INSTALLDIR, node.getInstallDir());
            info.add(NodeUtils.PARAM_REMOTEPORT, sshC.getSshPort());
            info.add(NodeUtils.PARAM_REMOTEUSER, sshAuth.getUserName());
            info.add(NodeUtils.PARAM_NODEHOST, node.getNodeHost());
            info.add(NodeUtils.PARAM_WINDOWS_DOMAIN, node.getWindowsDomain());
        }

        CommandInvocation ci = cr.getCommandInvocation("_delete-node", report, context.getSubject());
        ParameterMap map = new ParameterMap();
        map.add("DEFAULT", name);
        ci.parameters(map);
        ci.execute();

        //uninstall GlassFish after deleting the node
        if (uninstall) {
            boolean s = uninstallNode(context, info, node);
            if (!s && !force) {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }
    }

    /**
     * Prepares for invoking uninstall-node on DAS
     * @param ctx command context
     * @return true if uninstall-node succeeds, false otherwise
     */
    private boolean uninstallNode(AdminCommandContext ctx, ParameterMap map, Node node) {
        boolean res = false;

        remotepassword = map.getOne(NodeUtils.PARAM_REMOTEPASSWORD);
        sshkeypassphrase = map.getOne(NodeUtils.PARAM_SSHKEYPASSPHRASE);

        ArrayList<String> command = new ArrayList<String>();

        command.add(getUninstallCommandName());
        command.add("--installdir");
        command.add(map.getOne(NodeUtils.PARAM_INSTALLDIR));

        if (force) {
            command.add("--force");
        }

        setTypeSpecificOperands(command, map);
        String host = map.getOne(NodeUtils.PARAM_NODEHOST);
        command.add(host);

        String firstErrorMessage = Strings.get("delete.node.ssh.uninstall.failed", node.getName(), host);
        StringBuilder out = new StringBuilder();
        int exitCode = execCommand(command, out);

        //capture the output in server.log
        logger.info(out.toString().trim());

        ActionReport report = ctx.getActionReport();
        if (exitCode == 0) {
            // If it was successful say so and display the command output
            String msg = Strings.get("delete.node.ssh.uninstall.success", host);
            report.setMessage(msg);
            res = true;
        }
        else {
            report.setMessage(firstErrorMessage);
        }
        return res;
    }

    /**
     * Invokes install-node using ProcessManager and returns the exit message/status.
     * @param cmdLine list of args
     * @param output contains output message
     * @return exit status of uninstall-node
     */
    private int execCommand(List<String> cmdLine, StringBuilder output) {
        int exit = -1;

        List<String> fullcommand = new ArrayList<String>();
        String installDir = nodes.getDefaultLocalNode().getInstallDirUnixStyle() + "/glassfish";
        if (!StringUtils.ok(installDir)) {
            throw new IllegalArgumentException(Strings.get("create.node.ssh.no.installdir"));
        }

        File asadmin = new File(SystemPropertyConstants.getAsAdminScriptLocation(installDir));
        fullcommand.add(asadmin.getAbsolutePath());

        //if password auth is used for deleting the node, use the same auth mechanism for
        //uinstall-node as well. The passwords are passed directly through input stream
        List<String> pass = new ArrayList<String>();
        if (remotepassword != null) {
            fullcommand.add("--passwordfile");
            fullcommand.add("-");
            pass = getPasswords();
        }

        fullcommand.add("--interactive=false");
        fullcommand.addAll(cmdLine);

        ProcessManager pm = new ProcessManager(fullcommand);
        if (!pass.isEmpty()) {
            pm.setStdinLines(pass);
        }

        if (logger.isLoggable(Level.INFO)) {
            logger.info("Running command on DAS: " + commandListToString(fullcommand));
        }
        pm.setTimeout(DEFAULT_TIMEOUT_MSEC);

        if (logger.isLoggable(Level.FINER)) {
            pm.setEcho(true);
        } else {
            pm.setEcho(false);
        }

        try {
            exit = pm.execute();
        }
        catch (ProcessManagerException ex) {
            if (logger.isLoggable(Level.FINE)) {
                logger.fine("Error while executing command: " + ex.getMessage());
            }
            exit = 1;
        }

        String stdout = pm.getStdout();
        String stderr = pm.getStderr();

        if (output != null) {
            if (StringUtils.ok(stdout)) {
                output.append(stdout);
            }

            if (StringUtils.ok(stderr)) {
                if (output.length() > 0) {
                    output.append(NL);
                }
                output.append(stderr);
            }
        }
        return exit;
    }

    private String commandListToString(List<String> command) {
        StringBuilder fullCommand = new StringBuilder();

        for (String s : command) {
            fullCommand.append(" ");
            fullCommand.append(s);
        }

        return fullCommand.toString();
    }
}
