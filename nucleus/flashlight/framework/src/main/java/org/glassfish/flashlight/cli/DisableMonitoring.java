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
import com.sun.enterprise.config.serverbeans.MonitoringService;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.monitoring.ContainerMonitoring;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Service;

/**
 * @author Sreenivas Munnangi (3.0)
 * @author Byron Nevins (3.1+)
 */
@Service(name="disable-monitoring")
@PerLookup
@I18n("disable.monitoring")
@ExecuteOn({RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType({CommandTarget.DAS,CommandTarget.STANDALONE_INSTANCE,CommandTarget.CLUSTER,CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="disable-monitoring",
        description="disable-monitoring")
})
public class DisableMonitoring implements AdminCommand, AdminCommandSecurity.Preauthorization {

    // do NOT inject this.  We may need it for a different config tha ours.
    private MonitoringService ms;

    @Inject
    private Target targetService;

    @Param(name="target", optional=true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target;

    // list of modules separated by comma
    @Param(optional=true)
    private String modules;

    final private LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(DisableMonitoring.class);

    @AccessRequired.To("update")
    private Config targetConfig = null;

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

        try {
            if ((modules == null) || (modules.length() < 1)) {
                // check if it is already false
                boolean enabled = Boolean.parseBoolean(ms.getMonitoringEnabled());
                // set overall monitoring-enabled to false
                if (enabled) {
                    MonitoringConfig.setMonitoringEnabled(ms, "false", report);
                } else {
                    report.setMessage(
                        localStrings.getLocalString("disable.monitoring.alreadyfalse",
                        "monitoring-enabled is already set to false"));
                }
            } else {
                // for each module set monitoring level to OFF
                String[] strArr = modules.split(":");
                for (String moduleName: strArr) {
                    if (moduleName.length() > 0) {
                        MonitoringConfig.setMonitoringLevel(ms, moduleName, ContainerMonitoring.LEVEL_OFF, report);
                    }
                }
            }
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("disable.monitoring.exception",
                "Encountered exception during disabling monitoring {0}", e.getMessage()));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
        }
    }

}
