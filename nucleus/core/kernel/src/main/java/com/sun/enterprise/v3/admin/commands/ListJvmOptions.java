/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.JavaConfig;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.i18n.StringManager;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Service;

/**
 * Lists the JVM options configured in server's configuration.
 * @author &#2325;&#2375;&#2342;&#2366;&#2352 (km@dev.java.net)
 * @author Kin-man Chung
 * @since GlassFish V3
 */
@Service(name="list-jvm-options")   //implements the cli command by this "name"
@PerLookup            //should be provided "per lookup of this class", not singleton
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.jvm.options")
@ExecuteOn({RuntimeType.DAS})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.GET,
        path="list-jvm-options",
        description="list-jvm-options")
})
public final class ListJvmOptions implements AdminCommand, AdminCommandSecurity.Preauthorization {

    @Param(name="target", optional=true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target;

    @Param(name="profiler", optional=true)
    Boolean profiler=false;

    @Inject
    Target targetService;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;

    private static final StringManager lsm = StringManager.getManager(ListJvmOptions.class);

    @AccessRequired.To("read")
    private JavaConfig jc;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        config = CLIUtil.updateConfigIfNeeded(config, targetService, target);
        jc = config.getJavaConfig();
        return true;
    }

    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        List<String> opts;
        if (profiler) {
                if (jc.getProfiler() == null) {
                    report.setMessage(lsm.getString("create.profiler.first"));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            opts = jc.getProfiler().getJvmOptions();
        } else
            opts = jc.getJvmOptions();
        //Collections.sort(opts); //sorting is garbled by Reporter anyway, so let's move sorting to the client side
        try {
            for (String option : opts) {
                ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(option);
            }
        } catch (Exception e) {
            report.setMessage(lsm.getStringWithDefault("list.jvm.options.failed",
                    "Command: list-jvm-options failed"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
            return;
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
