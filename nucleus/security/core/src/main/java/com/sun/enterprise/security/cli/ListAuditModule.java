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

import com.sun.enterprise.config.serverbeans.AuditModule;
import com.sun.enterprise.config.serverbeans.Config;
import com.sun.enterprise.config.serverbeans.Domain;
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
 * List Audit Modules Command Usage: list-audit-modules [--terse=false] [--echo=false] [--interactive=true] [--host localhost]
 * [--port 4848|4849] [--secure | -s] [--user admin_user] [--passwordfile file_name] [target(Default server)]
 *
 * @author Nandini Ektare
 */

@Service(name = "list-audit-modules")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("list.audit.module")
@ExecuteOn({ RuntimeType.DAS })
@TargetType({ CommandTarget.DAS, CommandTarget.CLUSTERED_INSTANCE, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER,
    CommandTarget.CONFIG })
@RestEndpoints({
    @RestEndpoint(configBean = SecurityService.class, opType = RestEndpoint.OpType.GET, path = "list-audit-modules", description = "List Audit Modules") })
public class ListAuditModule implements AdminCommand, AdminCommandSecurity.Preauthorization {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(ListAuditModule.class);

    @Param(name = "target", primary = true, optional = true, defaultValue = SystemPropertyConstants.DAS_SERVER_NAME)
    private String target;

    @Inject
    @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    private Config config;

    @Inject
    private Domain domain;

    @AccessRequired.To(value = "read")
    private SecurityService securityService = null;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        securityService = chooseSecurityService(context.getActionReport());
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

        report.getTopMessagePart().setChildrenType("audit-module");
        for (AuditModule am : securityService.getAuditModule()) {
            ActionReport.MessagePart part = report.getTopMessagePart().addChild();
            part.setMessage(am.getName());
        }
    }

    private SecurityService chooseSecurityService(final ActionReport report) {
        config = CLIUtil.chooseConfig(domain, target, report);
        if (config == null) {
            return null;
        }
        return config.getSecurityService();
    }
}
