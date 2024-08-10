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

package com.sun.enterprise.v3.admin.commands;

import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.config.serverbeans.Server;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RestParam;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/** Implements the front end for generating the JVM report. Sends back a String
 * to the asadmin console based on server's locale.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @since GlassFish V3
 */
@Service(name="generate-jvm-report")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("generate.jvm.report")
@TargetType({CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTERED_INSTANCE})
@ExecuteOn(value = {RuntimeType.INSTANCE}, ifNeverStarted=FailurePolicy.Error)
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,
        opType=RestEndpoint.OpType.GET,
        path="generate-jvm-report",
        description="Generate Report",
        params={
            @RestParam(name="target", value="$parent")
        }),
    @RestEndpoint(configBean=Server.class,
        opType=RestEndpoint.OpType.GET,
        path="generate-jvm-report",
        description="Generate Report",
        params={
            @RestParam(name="target", value="$parent")
        }),
    @RestEndpoint(configBean=JavaConfig.class,
        opType=RestEndpoint.OpType.GET,
        path="generate-jvm-report",
        description="Generate Report",
        params={
            @RestParam(name="target", value="$grandparent")
        })
})
@AccessRequired(resource="domain/jvm", action="read")
public class GenerateJvmReportCommand implements AdminCommand {

    @Param(name="target", optional=true)
    String target;

    @Param(name="type", optional=true, defaultValue="summary",
           acceptableValues = "summary, thread, class, memory, log")
    String type;

    private MBeanServer mbs = null;  //needs to be injected, I guess

    public void execute(AdminCommandContext ctx) {
        prepare();
        String result = getResult();
        ActionReport report = ctx.getActionReport();
        report.setMessage(result);
        report.setActionExitCode(ExitCode.SUCCESS);
    }

    private synchronized void prepare() {
        mbs = ManagementFactory.getPlatformMBeanServer();
    }
    private String getResult() {
        if (type.equals("summary"))
            return new SummaryReporter(mbs).getSummaryReport();
        else if (type.equals("thread"))
            return new ThreadMonitor(mbs).getThreadDump();
        else if (type.equals("class"))
            return new ClassReporter(mbs).getClassReport();
        else if (type.equals("memory"))
            return new MemoryReporter(mbs).getMemoryReport();
        else if (type.equals("log"))
            return new LogReporter().getLoggingReport();
        else
            throw new IllegalArgumentException("Unsupported Option: " + type);   //this should not happen
    }
}
