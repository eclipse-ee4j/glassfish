/*
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

import static org.glassfish.api.ActionReport.ExitCode.SUCCESS;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.server.ServerEnvironmentImpl;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.universal.Duration;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

/**
 * Uptime command Reports on how long the server has been running.
 *
 */
@Service(name = "uptime")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("uptime")
@RestEndpoints({
    @RestEndpoint(
        configBean = Domain.class,
        opType = RestEndpoint.OpType.GET,
        path = "uptime",
        description = "Uptime",
        useForAuthorization = true) })
public class UptimeCommand implements AdminCommand {

    private static final LocalStringManagerImpl localStrings = new LocalStringManagerImpl(UptimeCommand.class);

    @Inject
    ServerEnvironmentImpl serverEnvironment;

    @Param(name = "milliseconds", optional = true, defaultValue = "false")
    Boolean milliseconds;

    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        long totalTime_ms = getUptime();
        String totalTime_mss = "" + totalTime_ms;
        Duration duration = new Duration(totalTime_ms);
        duration.setTerse();

        report.setActionExitCode(SUCCESS);
        String message;

        if (milliseconds) {
            message = totalTime_mss;
        } else {
            message = localStrings.getLocalString("uptime.output.terse", "Uptime: {0}", duration);
        }

        report.setMessage(message);
        report.getTopMessagePart().addProperty("milliseconds", totalTime_mss);
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
