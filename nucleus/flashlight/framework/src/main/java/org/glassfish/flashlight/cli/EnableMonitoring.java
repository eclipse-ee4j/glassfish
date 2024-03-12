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

package org.glassfish.flashlight.cli;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.*;
import org.glassfish.api.admin.*;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.config.support.*;
import org.glassfish.flashlight.impl.client.AgentAttacher;
import org.glassfish.internal.api.*;
import org.jvnet.hk2.annotations.Service;

import org.glassfish.hk2.api.PerLookup;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.MonitoringService;

import jakarta.inject.Inject;

/**
 * @author Sreenivas Munnangi (3.0)
 * @author Byron Nevins (3.1+)
 */
@Service(name = "enable-monitoring")
@PerLookup
@I18n("enable.monitoring")
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="enable-monitoring",
        description="enable-monitoring")
})
public class EnableMonitoring implements AdminCommand, AdminCommandSecurity.Preauthorization {
    // do NOT inject this.
    private MonitoringService ms;
    @Inject
    private Target targetService;
    @Param(name = "target", optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target;
    @Param(optional = true)
    private String pid;
    @Param(optional = true)
    private String options;
    @Param(optional = true)
    private String modules;
    @Param(optional = true)
    private Boolean mbean;
    @Param(optional = true)
    private Boolean dtrace;
    final private LocalStringManagerImpl localStrings =
            new LocalStringManagerImpl(EnableMonitoring.class);

    @AccessRequired.To("update")
    private Config targetConfig;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        try {
            targetConfig = targetService.getConfig(target);
            if (targetConfig != null) {
                ms = targetConfig.getMonitoringService();
                return true;
            }
            fail(report, "unknown.target", "Could not find target {0}", target);
            return false;
        }
        catch (Exception e) {
            fail(report, "target.service.exception",
                    "Encountered exception trying to locate the MonitoringService element "
                    + "in the target ({0}) configuration: {1}", target, e.getMessage());
            return false;
        }
    }

    private void fail(final ActionReport report, final String messageKey,
            final String fallbackMessageText, final Object... args) {
        report.setMessage(localStrings.getLocalString(messageKey, fallbackMessageText, args));
        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
    }

    @Override
    public void execute(AdminCommandContext context) {
        ActionReport report = context.getActionReport();

        int pidInt = -1;

        try {
            if (pid != null)
                pidInt = Integer.parseInt(pid);
        }
        catch (Exception e) {
            pidInt = -1;
        }

        if (options == null)
            options = "";

         if (!AgentAttacher.attachAgent(pidInt, options)) {
            ActionReport.MessagePart part = report.getTopMessagePart().addChild();
            part.setMessage(localStrings.getLocalString("attach.agent.exception",
                    "Can't attach the agent to the JVM."));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }


        // following ordering is deliberate to facilitate config change
        // event handling by monitoring infrastructure
        // for ex. by the time monitoring-enabled is true all its constituent
        // elements should have been set to avoid multiple processing

        // module monitoring levels
        if ((modules != null) && (modules.length() > 0)) {
            String[] strArr = modules.split(":");
            String[] nvArr = null;
            for (String nv : strArr) {
                if (nv.length() > 0) {
                    nvArr = nv.split("=");
                    if (nvArr.length > 1) {
                        if (isValidString(nvArr[1])) {
                            setModuleMonitoringLevel(nvArr[0], nvArr[1], report);
                        }
                    }
                    else {
                        if (isValidString(nvArr[0])) {
                            setModuleMonitoringLevel(nvArr[0], "HIGH", report);
                        }
                    }
                }
            }
        }

        // mbean-enabled
        if (mbean != null) {
            MonitoringConfig.setMBeanEnabled(ms, mbean.toString(), report);
        }

        // dtrace-enabled
        if (dtrace != null) {
            MonitoringConfig.setDTraceEnabled(ms, dtrace.toString(), report);
        }

        // check and set monitoring-enabled to true
        MonitoringConfig.setMonitoringEnabled(ms, "true", report);
    }

    private void setModuleMonitoringLevel(String moduleName, String level, ActionReport report) {
        ActionReport.MessagePart part = report.getTopMessagePart().addChild();

        if (!isValidString(moduleName)) {
            part.setMessage(localStrings.getLocalString("enable.monitoring.invalid",
                    "Invalid module name {0}", moduleName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }

        if ((!isValidString(level)) || (!isValidLevel(level))) {
            part.setMessage(localStrings.getLocalString("enable.monitoring.invalid",
                    "Invalid level {0} for module name {1}", level, moduleName));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }

        MonitoringConfig.setMonitoringLevel(ms, moduleName, level, report);
    }

    private boolean isValidString(String str) {
        return (str != null && str.length() > 0);
    }

    private boolean isValidLevel(String level) {
        return ((level.equals("OFF")) || (level.equals("HIGH")) || (level.equals("LOW")));
    }

    static final String FLASHLIGHT_AGENT_PATH = "lib/monitor/flashlight-agent.jar";
}
