/*
 * Copyright (c) 2022, 2024 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.config.serverbeans.Domain;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.MessagePart;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.internal.api.ServerContext;
import org.glassfish.internal.config.UnprocessedConfigListener;
import org.jvnet.hk2.annotations.Service;

import static org.glassfish.api.ActionReport.ExitCode.SUCCESS;

/**
 * Locations command to indicate where this server is installed.
 *
 * @author Jerome Dochez
 */
@Service(name = "__locations")
@Singleton
@CommandLock(CommandLock.LockType.NONE)
@I18n("locations.command")
@RestEndpoints({
    @RestEndpoint(
        configBean = Domain.class,
        opType = RestEndpoint.OpType.GET,
        path = "locations",
        description = "Location", useForAuthorization = true) })
public class LocationsCommand implements AdminCommand {

    @Inject
    private ServerEnvironment serverEnvironment;

    @Inject
    private ServerContext server;

    @Inject
    private UnprocessedConfigListener ucl;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        report.setActionExitCode(SUCCESS);
        report.setMessage(serverEnvironment.getInstanceRoot().getAbsolutePath().replace('\\', '/'));

        MessagePart messagePart = report.getTopMessagePart();
        messagePart.addProperty("Base-Root", server.getInstallRoot().getAbsolutePath());
        messagePart.addProperty("Domain-Root", serverEnvironment.getInstanceRoot().getAbsolutePath());
        messagePart.addProperty("Instance-Root", serverEnvironment.getInstanceRoot().getAbsolutePath());
        messagePart.addProperty("Config-Dir", serverEnvironment.getConfigDirPath().getAbsolutePath());
        messagePart.addProperty("Uptime", Long.toString(getUptime()));
        messagePart.addProperty("Pid", Long.toString(ProcessHandle.current().pid()));
        messagePart.addProperty("Restart-Required", Boolean.toString(ucl.serverRequiresRestart()));
    }

    private long getUptime() {
        RuntimeMXBean mxbean = ManagementFactory.getRuntimeMXBean();
        long totalTime_ms = -1;

        if (mxbean != null) {
            totalTime_ms = mxbean.getUptime();
        }

        if (totalTime_ms <= 0) {
            long start = serverEnvironment.getStartupContext().getCreationTime();
            totalTime_ms = System.currentTimeMillis() - start;
        }

        return totalTime_ms;
    }
}
