/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2021 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.security.cli;

import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.MessageSecurityConfig;
import com.sun.enterprise.config.serverbeans.ProviderConfig;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

import jakarta.inject.Inject;
import jakarta.inject.Named;

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
import org.jvnet.hk2.annotations.Service;

/**
 * List Message Security Providers Command
 *
 * Usage: list-message-security-providers [--terse=false] [--echo=false] [--interactive=true] [--host localhost] [--port
 * 4848|4849] [--secure | -s] [--user admin_user] [--passwordfile file_name] [--layer message_layer] [target(Default server)]
 *
 * @author Nandini Ektare
 */

@Service(name = "list-message-security-providers")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.message.security.provider")
@ExecuteOn({ RuntimeType.DAS })
@TargetType({ CommandTarget.DAS, CommandTarget.CLUSTERED_INSTANCE, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER,
    CommandTarget.CONFIG })
@RestEndpoints({
    @RestEndpoint(configBean = SecurityService.class, opType = RestEndpoint.OpType.GET, path = "list-message-security-providers", description = "list-message-security-providers") })
public class ListMessageSecurityProvider implements AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListMessageSecurityProvider.class);

    @Param(name = "target", primary = true, optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

    @Inject
    private Domain domain;
    // auth-layer can only be SOAP | HttpServlet
    @Param(name = "layer", acceptableValues = "SOAP,HttpServlet", optional = true)
    String authLayer;

    @AccessRequired.To("read")
    private SecurityService secService;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        config = CLIUtil.chooseConfig(domain, target, context.getActionReport());
        if (config == null) {
            return false;
        }
        secService = config.getSecurityService();
        return true;
    }

    /**
     * Executes the command with the command parameters passed as Properties where the keys are the paramter names and the values the
     * parameter values
     *
     * @param context information
     */
    @Override
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        secService.getMessageSecurityConfig();

        report.getTopMessagePart().setMessage(
            localStrings.getLocalString("list.message.security.provider.success", "list-message-security-providers successful"));
        report.getTopMessagePart().setChildrenType("");

        for (MessageSecurityConfig msc : secService.getMessageSecurityConfig()) {
            if (authLayer == null) {
                for (ProviderConfig pc : msc.getProviderConfig()) {
                    ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                    part.setMessage(pc.getProviderId());
                }
            } else {
                if (msc.getAuthLayer().equals(authLayer)) {
                    for (ProviderConfig pc : msc.getProviderConfig()) {
                        ActionReport.MessagePart part = report.getTopMessagePart().addChild();
                        part.setMessage(pc.getProviderId());
                    }
                }
            }
        }
    }
}
