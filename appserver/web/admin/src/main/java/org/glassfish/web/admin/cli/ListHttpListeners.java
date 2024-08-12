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

package org.glassfish.web.admin.cli;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.HttpService;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.grizzly.config.dom.NetworkListener;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Target;
import org.jvnet.hk2.annotations.Service;

/**
 * List http listeners command
 */
@Service(name = "list-http-listeners")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.http.listeners")
@ExecuteOn(RuntimeType.DAS)
@TargetType({CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER, CommandTarget.CONFIG})
@RestEndpoints({
    @RestEndpoint(configBean=HttpService.class,
        opType=RestEndpoint.OpType.GET,
        path="list-http-listeners",
        description="list-http-listeners")
})
public class ListHttpListeners implements AdminCommand {
    @Param(primary = true, optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    String target;
    @Param(optional = true, defaultValue = "false", name = "long", shortName = "l")
    boolean verbose;
    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    Config config;
    @Inject
    Domain domain;
    @Inject
    ServiceLocator services;

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the parameter names and
     * the values the parameter values
     *
     * @param context information
     */
    public void execute(AdminCommandContext context) {
        Target targetUtil = services.getService(Target.class);
        Config newConfig = targetUtil.getConfig(target);
        if (newConfig != null) {
            config = newConfig;
        }
        final ActionReport report = context.getActionReport();
        List<NetworkListener> list = config.getNetworkConfig().getNetworkListeners().getNetworkListener();
        int size = 0;
        for (NetworkListener networkListener : list) {
            size = Math.max(size, networkListener.getName().length());
        }
        final String format = "%-" + (size + 2) + "s %-6s";
        if (verbose) {
            report.getTopMessagePart()
                .addChild().setMessage(String.format(format, "NAME", "PORT"));
        }
        for (NetworkListener listener : list) {
            if (listener.findHttpProtocol().getHttp() != null) {
                report.getTopMessagePart()
                    .addChild().setMessage(String.format(format, listener.getName(),
                    verbose ? listener.getPort() : ""));
            }
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);
    }
}
