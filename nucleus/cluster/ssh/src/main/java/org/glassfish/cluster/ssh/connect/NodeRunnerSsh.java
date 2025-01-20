/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.cluster.ssh.connect;

import com.sun.enterprise.config.serverbeans.Node;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.ArrayList;
import java.util.List;

import org.glassfish.api.admin.SSHCommandExecutionException;
import org.glassfish.cluster.ssh.launcher.SSHException;
import org.glassfish.cluster.ssh.launcher.SSHLauncher;
import org.glassfish.cluster.ssh.launcher.SSHSession;
import org.glassfish.common.util.admin.AsadminInput;

import static java.lang.System.Logger.Level.DEBUG;

/**
 * This class is responsible for running asadmin commands on SSH nodes.
 */
public class NodeRunnerSsh {
    private static final Logger LOG = System.getLogger(NodeRunnerSsh.class.getName());

    private String lastCommandRun;
    private int commandStatus;

    /**
     * @param node
     * @return false if node is null or is not of SSH type.
     */
    public boolean isSshNode(Node node) {
        if (node == null) {
            throw new IllegalArgumentException("Node is null");
        }
        return "SSH".equals(node.getType());
    }

    /**
     * @return the last command we tried to execute. Useful for error logs.
     */
    String getLastCommandRun() {
        return lastCommandRun;
    }

    /**
     * Runs the command on the SSH node.
     *
     * @param node
     * @param output This will contain command output after execution.
     * @param args
     * @param stdinLines
     * @return exit code
     * @throws SSHCommandExecutionException communication or command failed.
     * @throws UnsupportedOperationException incompatible node.
     */
    public int runAdminCommandOnRemoteNode(Node node, StringBuilder output,
        List<String> args, List<String> stdinLines)
        throws SSHCommandExecutionException, UnsupportedOperationException {
        args.add(0, AsadminInput.CLI_INPUT_OPTION);
        args.add(1, AsadminInput.SYSTEM_IN_INDICATOR); // specified to read from System.in

        if (!isSshNode(node)) {
            throw new UnsupportedOperationException("Node is not of type SSH");
        }

        String installDir = node.getInstallDirUnixStyle() + "/" + SystemPropertyConstants.getComponentName();
        List<String> fullcommand = new ArrayList<>();
        final SSHLauncher sshL = new SSHLauncher(node);
        try {
            // We can just use "nadmin" even on Windows since the SSHD provider
            // will locate the command (.exe or .bat) for us
            fullcommand.add(installDir + "/lib/nadmin");
            fullcommand.addAll(args);
            lastCommandRun = commandListToString(fullcommand);
            LOG.log(DEBUG, () -> "Running command on " + node.getNodeHost() + ": " + lastCommandRun);
            final StringBuilder commandOutput = new StringBuilder();
            try (SSHSession session = sshL.openSession()) {
                commandStatus = session.exec(fullcommand, stdinLines, commandOutput);
            }
            output.append(commandOutput);
            return commandStatus;
        } catch (SSHException e) {
            LOG.log(Level.ERROR, "Command execution failed for " + lastCommandRun, e);
            SSHCommandExecutionException cee = new SSHCommandExecutionException(e.getMessage(), e);
            cee.setSSHSettings(sshL.toString());
            cee.setCommandRun(lastCommandRun);
            throw cee;
        }
    }


    private String commandListToString(List<String> command) {
        StringBuilder fullCommand = new StringBuilder();
        for (String s : command) {
            fullCommand.append(' ');
            fullCommand.append(s);
        }
        return fullCommand.toString();
    }
}
