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

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.module.HK2Module;
import com.sun.enterprise.module.ModuleState;
import com.sun.enterprise.module.ModulesRegistry;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * List the modules available to this instance and their status
 */
@Service(name = "list-modules")
@Singleton // no per-execution state
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.modules.command")
@RestEndpoints({
        @RestEndpoint(configBean = Domain.class, opType = RestEndpoint.OpType.GET, path = "list-modules", description = "list-modules") })
@AccessRequired(resource = "domain", action = "dump")
public class ListModulesCommand implements AdminCommand {

    @Inject
    ModulesRegistry registry;

    @Override
    public void execute(AdminCommandContext context) {

        ActionReport report = context.getActionReport();
        report.setActionDescription("List of modules");
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

        ActionReport.MessagePart top = report.getTopMessagePart();
        top.setMessage("List Of Modules");
        top.setChildrenType("Module");

        StringBuilder sb = new StringBuilder("HK2Module Status Report Begins\n");
        // first started :

        for (HK2Module m : registry.getModules()) {
            if (m.getState() == ModuleState.READY) {
                sb.append(m).append("\n");
            }
        }
        sb.append("\n");
        // then resolved
        for (HK2Module m : registry.getModules()) {
            if (m.getState() == ModuleState.RESOLVED) {
                sb.append(m).append("\n");
            }
        }
        sb.append("\n");
        // finally installed
        for (HK2Module m : registry.getModules()) {
            if (m.getState() != ModuleState.READY && m.getState() != ModuleState.RESOLVED) {
                sb.append(m).append("\n");
            }
        }
        sb.append("HK2Module Status Report Ends");
        ActionReport.MessagePart childPart = top.addChild();
        childPart.setMessage(sb.toString());

    }
}
