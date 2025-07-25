/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.admin.util;

import com.sun.enterprise.admin.remote.ServerRemoteAdminCommand;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.StringUtils;

import java.util.Map;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.InstanceCommand;
import org.glassfish.api.admin.InstanceCommandResult;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.hk2.api.ServiceLocator;

/**
 *
 * Causes execution of an administrative command on one or more remote instances to be triggered from code running
 * inside the DAS.
 *
 * @author Vijay Ramachandran
 */
public class InstanceCommandExecutor extends ServerRemoteAdminCommand implements Runnable, InstanceCommand {

    private Server server;
    private ParameterMap params;
    private ActionReport aReport;
    private String commandName;
    private FailurePolicy offlinePolicy;
    private FailurePolicy failPolicy;
    private InstanceCommandResult result;

    private static final LocalStringManagerImpl strings = new LocalStringManagerImpl(InstanceCommandExecutor.class);

    public InstanceCommandExecutor(ServiceLocator habitat, String name, FailurePolicy fail, FailurePolicy offline, Server server,
            String host, int port, Logger logger, ParameterMap p, ActionReport r, InstanceCommandResult res) throws CommandException {
        super(habitat, name, host, port, false, "admin", "", logger);
        this.server = server;
        this.params = p;
        this.aReport = r;
        this.commandName = name;
        this.offlinePolicy = offline;
        this.failPolicy = fail;
        this.result = res;
    }

    @Override
    public String getCommandOutput() {
        return this.output;
    }

    public Server getServer() {
        return server;
    }

    public ActionReport getReport() {
        return this.aReport;
    }

    @Override
    public void run() {
        try {
            executeCommand(params);
            aReport.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            if (StringUtils.ok(getCommandOutput())) {
                aReport.setMessage(strings.getLocalString("ice.successmessage", "{0}:\n{1}\n", getServer().getName(), getCommandOutput()));
            }
            Map<String, String> attributes = this.getAttributes();
            for (Map.Entry<String, String> ae : attributes.entrySet()) {
                String key = ae.getKey();
                if (key.endsWith("_value")) {
                    continue;
                }
                if (!key.endsWith("_name")) {
                    if (ae.getValue() != null) {
                        aReport.getTopMessagePart().addProperty(key, ae.getValue());
                    }
                    continue;
                }
                String keyWithoutSuffix = key.substring(0, key.lastIndexOf("_name"));
                aReport.getTopMessagePart().addProperty(keyWithoutSuffix, attributes.get(keyWithoutSuffix + "_value"));
            }
        } catch (CommandException cmdEx) {
            ActionReport.ExitCode finalResult;
            if (cmdEx.getCause() instanceof java.net.ConnectException) {
                finalResult = FailurePolicy.applyFailurePolicy(offlinePolicy, ActionReport.ExitCode.FAILURE);
                if (!finalResult.equals(ActionReport.ExitCode.FAILURE)) {
                    aReport.setMessage(strings.getLocalString("clusterutil.warnoffline",
                            "WARNING: Instance {0} seems to be offline; command {1} was not replicated to that instance",
                            getServer().getName(), commandName));
                } else {
                    aReport.setMessage(strings.getLocalString("clusterutil.failoffline",
                            "FAILURE: Instance {0} seems to be offline; command {1} was not replicated to that instance",
                            getServer().getName(), commandName));
                }
            } else {
                finalResult = FailurePolicy.applyFailurePolicy(failPolicy, ActionReport.ExitCode.FAILURE);
                if (finalResult.equals(ActionReport.ExitCode.FAILURE)) {
                    aReport.setMessage(
                            strings.getLocalString("clusterutil.commandFailed", "FAILURE: Command {0} failed on server instance {1}: {2}",
                                    commandName, getServer().getName(), cmdEx.getMessage()));
                } else {
                    aReport.setMessage(strings.getLocalString("clusterutil.commandWarning",
                            "WARNING: Command {0} did not complete successfully on server instance {1}: {2}", commandName,
                            getServer().getName(), cmdEx.getMessage()));
                }
            }
            aReport.setActionExitCode(finalResult);
        }
        result.setInstanceCommand(this);
    }
}
