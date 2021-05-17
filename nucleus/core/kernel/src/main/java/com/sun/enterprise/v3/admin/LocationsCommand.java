/*
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
import com.sun.enterprise.universal.process.ProcessUtils;
import org.glassfish.api.ActionReport.MessagePart;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Inject;

import jakarta.inject.Singleton;
import org.glassfish.server.ServerEnvironmentImpl;
import com.sun.enterprise.glassfish.bootstrap.StartupContextUtil;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import org.glassfish.api.admin.*;
import org.glassfish.internal.config.UnprocessedConfigListener;

/**
 * Locations command to indicate where this server is installed.
 * @author Jerome Dochez
 */
@Service(name="__locations")
@Singleton
@CommandLock(CommandLock.LockType.NONE)
@I18n("locations.command")
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.GET,
        path="locations",
        description="Location",
        useForAuthorization=true)
})
public class LocationsCommand implements AdminCommand {

    @Inject
    ServerEnvironmentImpl env;

    @Inject
    private UnprocessedConfigListener ucl;

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        report.setMessage(env.getInstanceRoot().getAbsolutePath().replace('\\', '/'));
        MessagePart mp = report.getTopMessagePart();
        mp.addProperty("Base-Root", StartupContextUtil.getInstallRoot(env.getStartupContext()).getAbsolutePath());
        mp.addProperty("Domain-Root", env.getDomainRoot().getAbsolutePath());
        mp.addProperty("Instance-Root", env.getInstanceRoot().getAbsolutePath());
        mp.addProperty("Config-Dir", env.getConfigDirPath().getAbsolutePath());
        mp.addProperty("Uptime", ""+getUptime());
        mp.addProperty("Pid", ""+ProcessUtils.getPid());
        mp.addProperty("Restart-Required", ""+ucl.serverRequiresRestart());
    }

    private long getUptime() {
        RuntimeMXBean mxbean = ManagementFactory.getRuntimeMXBean();
        long totalTime_ms = -1;

        if (mxbean != null)
            totalTime_ms = mxbean.getUptime();

        if (totalTime_ms <= 0) {
            long start = env.getStartupContext().getCreationTime();
            totalTime_ms = System.currentTimeMillis() - start;
        }
        return totalTime_ms;
    }
}
