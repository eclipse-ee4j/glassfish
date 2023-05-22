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

import java.util.ArrayList;
import java.util.Collection;
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
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.SystemProperty;
import com.sun.enterprise.config.serverbeans.SystemPropertyBag;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;

/**
 * List System Properties Command
 *
 * Lists the system properties of the domain, configuration, cluster, or server instance
 *
 * Usage: lists-system-properties [--terse={true|false}][ --echo={true|false} ] [ --interactive={true|false} ] [ --host
 * host] [--port port] [--secure| -s ] [ --user admin_user] [--passwordfile filename] [--help] [target_name]
 *
 */
@Service(name = "list-system-properties")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@ExecuteOn({ RuntimeType.DAS })
@TargetType(value = { CommandTarget.CLUSTER, CommandTarget.CLUSTERED_INSTANCE, CommandTarget.CONFIG, CommandTarget.DAS,
        CommandTarget.DOMAIN, CommandTarget.STANDALONE_INSTANCE })
@I18n("list.system.properties")
@RestEndpoints({
    @RestEndpoint(
        configBean = Domain.class,
        opType = RestEndpoint.OpType.GET,
        path = "list-system-properties",
        description = "list-system-properties") })
public class ListSystemProperties implements AdminCommand, AdminCommandSecurity.Preauthorization, AdminCommandSecurity.AccessCheckProvider {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListSystemProperties.class);

    @Param(optional = true, primary = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target;

    @Inject
    Domain domain;

    private SystemPropertyBag spb;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        spb = CLIUtil.chooseTarget(domain, target);
        if (spb == null) {
            final ActionReport report = context.getActionReport();
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            String msg = localStrings.getLocalString("invalid.target.sys.props",
                    "Invalid target:{0}. Valid targets types are domain, config, cluster, default server, clustered instance, stand alone instance",
                    target);
            report.setMessage(msg);
            return false;
        }
        return true;
    }

    @Override
    public Collection<? extends AccessRequired.AccessCheck> getAccessChecks() {
        final Collection<AccessRequired.AccessCheck> result = new ArrayList<>();
        result.add(new AccessRequired.AccessCheck(AccessRequired.Util.resourceNameFromConfigBeanProxy(spb), "update"));
        return result;
    }

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the paramter names and the
     * values the parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        try {
            List<SystemProperty> sysProps = spb.getSystemProperty();
            int length = 0;
            if (sysProps.isEmpty()) {
                final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                part.setMessage(localStrings.getLocalString("NothingToList", "Nothing to List."));
            } else {
                for (SystemProperty prop : sysProps) {
                    final ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                    part.setMessage(prop.getName() + "=" + prop.getValue());
                    length++;
                }
                report.setMessage(
                        localStrings.getLocalString("list.ok", "The target {0} contains following {1} system properties", target, length));
            }
            report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
        } catch (Exception e) {
            report.setMessage(localStrings.getLocalString("list.system.properties.failed", "list-system-properties failed"));
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(e);
        }
    }
}
