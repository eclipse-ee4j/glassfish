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
import com.sun.enterprise.config.serverbeans.Configs;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.JaccProvider;
import com.sun.enterprise.config.serverbeans.SecurityService;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.SystemPropertyConstants;

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
import org.jvnet.hk2.annotations.Service;

/**
 * Usage: list-jacc-providers [--help] [--user admin_user] [--passwordfile file_name] [target(Default server)]
 *
 */
@Service(name = "list-jacc-providers")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.jacc.provider")
@ExecuteOn({ RuntimeType.DAS })
@TargetType({ CommandTarget.DAS, CommandTarget.CLUSTERED_INSTANCE, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER,
    CommandTarget.CONFIG })
@RestEndpoints({
    @RestEndpoint(configBean = SecurityService.class, opType = RestEndpoint.OpType.GET, path = "list-jacc-providers", description = "list-jacc-providers") })
public class ListJaccProviders implements AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteJaccProvider.class);

    @Param(name = "target", primary = true, optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

    @Inject
    private Configs configs;

    @Inject
    private Domain domain;

    @AccessRequired.To("read")
    private SecurityService securityService;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        config = CLIUtil.chooseConfig(domain, target, report);
        if (config == null) {
            return false;
        }
        securityService = config.getSecurityService();
        return true;
    }

    @Override
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();

        List<JaccProvider> jaccProviders = securityService.getJaccProvider();
        JaccProvider jprov = null;
        for (JaccProvider jaccProv : jaccProviders) {
            ActionReport.MessagePart part = report.getTopMessagePart().addChild();
            part.setMessage(jaccProv.getName());
        }
        report.setActionExitCode(ActionReport.ExitCode.SUCCESS);

    }

}
