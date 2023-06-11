/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.universal.process.ProcessManager;
import com.sun.enterprise.universal.process.ProcessManagerException;
import com.sun.enterprise.util.StringUtils;
import com.sun.enterprise.util.SystemPropertyConstants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.SSHCommandExecutionException;
import org.glassfish.common.util.admin.AsadminInput;
import org.glassfish.common.util.admin.AuthTokenManager;
import org.glassfish.hk2.api.ServiceLocator;

public class NodeRunner {
    private static final String NL = System.lineSeparator();
    private static final String AUTH_TOKEN_STDIN_LINE_PREFIX = "option." + AuthTokenManager.AUTH_TOKEN_OPTION_NAME + "=";
    private final ServiceLocator habitat;
    private final Logger logger;
    private String lastCommandRun = null;
    private final AuthTokenManager authTokenManager;

    public NodeRunner(ServiceLocator habitat, Logger logger) {
        this.logger = logger;
        this.habitat = habitat;
        authTokenManager = habitat.getService(AuthTokenManager.class);
    }

    public String getLastCommandRun() {
        return lastCommandRun;
    }

    public boolean isSshNode(Node node) {

        if (node == null) {
            throw new IllegalArgumentException("Node is null");
        }
        if (node.getType() == null) {
            return false;
        }
        return node.getType().equals("SSH");
    }

    /**
     * Run an asadmin command on a Node. The node may be local or remote. If
     * it is remote then SSH is used to execute the command on the node.
     * The args list is all parameters passed to "asadmin", but not
     * "asadmin" itself. So an example args is:
     *
     * "--host", "mydashost.com", "start-local-instance", "--node", "n1", "i1"
     *
     * @param node  The node to run the asadmin command on
     * @param output    A StringBuilder to hold the command's output in. Both
     *                  stdout and stderr are placed in output. null if you
     *                  don't want the output.
     * @param args  The arguments to the asadmin command. This includes
     *              parameters for asadmin (like --host) as well as the
     *              command (like start-local-instance) as well as an
     *              parameters for the command. It does not include the
     *              string "asadmin" itself.
     * @return      The status of the asadmin command. Typically 0 if the
     *              command was successful else 1.
     *
     * @throws SSHCommandExecutionException There was an error executing the
     *                                      command via SSH.
     * @throws ProcessManagerException      There was an error executing the
     *                                      command locally.
     * @throws UnsupportedOperationException The command needs to be run on
     *                                       a remote node, but the node is not
     *                                       of type SSH.
     * @throws IllegalArgumentException     The passed node is malformed.
     */
    public int runAdminCommandOnNode(Node node, StringBuilder output,
            List<String> args,
            AdminCommandContext context) throws
            SSHCommandExecutionException,
            ProcessManagerException,
            UnsupportedOperationException,
            IllegalArgumentException {


        if (node == null) {
            throw new IllegalArgumentException("Node is null");
        }

        final List<String> stdinLines = new ArrayList<>();
        stdinLines.add(AsadminInput.versionSpecifier());
        stdinLines.add(AUTH_TOKEN_STDIN_LINE_PREFIX + authTokenManager.createToken(context.getSubject()));
        args.add(0, "--interactive=false");            // No prompting!

        if (node.isLocal()) {
            return runAdminCommandOnLocalNode(node, output, args, stdinLines);
        } else {
            return runAdminCommandOnRemoteNode(node, output, args, stdinLines);
        }
    }

    private int runAdminCommandOnLocalNode(Node node, StringBuilder output,
            List<String> args,
            List<String> stdinLines) throws
            ProcessManagerException {
        args.add(0, AsadminInput.CLI_INPUT_OPTION);
        args.add(1, AsadminInput.SYSTEM_IN_INDICATOR); // specified to read from System.in
        List<String> fullcommand = new ArrayList<>();
        String installDir = node.getInstallDirUnixStyle() + "/"
                + SystemPropertyConstants.getComponentName();
        if (!StringUtils.ok(installDir)) {
            throw new IllegalArgumentException("Node does not have an installDir");
        }

        File asadmin = new File(SystemPropertyConstants.getAsAdminScriptLocation(installDir));
        fullcommand.add(asadmin.getAbsolutePath());
        fullcommand.addAll(args);

        if (!asadmin.canExecute()) {
            throw new ProcessManagerException(asadmin.getAbsolutePath() + " is not executable.");
        }

        lastCommandRun = commandListToString(fullcommand);

        trace("Running command locally: " + lastCommandRun);
        ProcessManager pm = new ProcessManager(fullcommand);
        pm.setStdinLines(stdinLines);

        int exitValue = pm.execute();  // blocks until command is complete

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
        return exitValue;
    }

    private int runAdminCommandOnRemoteNode(Node node, StringBuilder output,
            List<String> args,
            List<String> stdinLines) throws
            SSHCommandExecutionException, IllegalArgumentException,
            UnsupportedOperationException {

        // don't want to call a config object proxy more than absolutely necessary!
        String type = node.getType();

        if ("SSH".equals(type)) {
            NodeRunnerSsh nrs = new NodeRunnerSsh(habitat, logger);
            int result = nrs.runAdminCommandOnRemoteNode(node, output, args, stdinLines);
            lastCommandRun = nrs.getLastCommandRun();
            return result;
        }

        throw new UnsupportedOperationException("Node is not of type SSH");
    }

    private void trace(String s) {
        logger.fine(String.format("%s: %s", this.getClass().getSimpleName(), s));
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
