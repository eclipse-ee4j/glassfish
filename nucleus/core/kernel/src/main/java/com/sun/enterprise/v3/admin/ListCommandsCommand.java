/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * Simple admin command to list all existing commands.
 *
 * @author Jerome Dochez
 *
 */
@Service(name = "list-commands")
@Singleton // no per-execution state
@CommandLock(CommandLock.LockType.NONE)
@RestEndpoints({
        @RestEndpoint(configBean = Domain.class, opType = RestEndpoint.OpType.GET, path = "list-commands", description = "list-commands") })
@AccessRequired(resource = "domain", action = "read")
public class ListCommandsCommand implements AdminCommand {
    private static final String MODE = "mode";
    private static final String DEBUG = "debug";

    @Inject
    ServiceLocator habitat;

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the paramter names and the
     * values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {

        context.getActionReport().setActionExitCode(ActionReport.ExitCode.SUCCESS);
        ActionReport report = context.getActionReport();
        report.getTopMessagePart().setChildrenType("Command");
        for (String name : sortedAdminCommands()) {
            ActionReport.MessagePart part = report.getTopMessagePart().addChild();
            part.setMessage(name);
        }
    }

    protected String getScope() {
        return null;
    }

    private List<String> sortedAdminCommands() {
        String scope = getScope();
        List<String> names = new ArrayList<>();
        for (ServiceHandle<?> command : habitat.getAllServiceHandles(AdminCommand.class)) {
            String name = command.getActiveDescriptor().getName();
            // see 6161 -- I thought we should ensure that a command found in habitat should
            // return a valid Command Object, but it was decided that we don't need to instantiate
            // each object satisfying AdminCommand contract to get a list of all commands

            // limit list to commands for current scope
            if (name != null) {
                int ci = name.indexOf("/");
                if (ci != -1) {
                    String cmdScope = name.substring(0, ci + 1);
                    if (scope == null || !cmdScope.equals(scope)) {
                        continue;
                    }
                    name = name.substring(ci + 1);
                } else {
                    if (scope != null) {
                        continue;
                    }
                }

                if (debugCommand(command)) { // it's a debug command, add only if debug is set
                    if (debugSet()) {
                        names.add(name);
                    }
                } else { // always add non-debug commands \
                    names.add(name);
                }
            }
        }
        Collections.sort(names);
        return (names);

    }

    private static boolean debugCommand(ServiceHandle<?> command) {
        ActiveDescriptor<?> ad = command.getActiveDescriptor();
        Map<String, List<String>> metadata = ad.getMetadata();

        List<String> modes = metadata.get(MODE);
        if (modes == null) {
            return false;
        }

        for (String mode : modes) {
            if (DEBUG.equals(mode)) {
                return true;
            }
        }

        return false;
    }

    private static boolean debugSet() { // TODO take into a/c debug-enabled?
        String s = System.getenv("AS_DEBUG");
        return (Boolean.valueOf(s));
    }
}
