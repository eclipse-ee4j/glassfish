/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.deployment.admin;

import com.sun.enterprise.config.serverbeans.AppTenant;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Applications;

import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

@Service(name="_mt-undeploy")
@org.glassfish.api.admin.ExecuteOn(value={RuntimeType.DAS})
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
public class MTUndeployCommand implements AdminCommand, AdminCommandSecurity.AccessCheckProvider {

    @Param(primary = true)
    public String name;

    @Inject
    CommandRunner<?> commandRunner;

    @Inject
    Applications applications;

    private Application app;
    private List<AppTenant> appTenants = null;

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        final List<AccessCheck> accessChecks = new ArrayList<AccessCheck>();
        app = applications.getApplication(name);
        if (app != null) {
            accessChecks.add(new AccessCheck(AccessRequired.Util.resourceNameFromConfigBeanProxy(app), "read"));
            if (app.getAppTenants() != null) {
                appTenants = app.getAppTenants().getAppTenant();
                for (AppTenant appTenant : appTenants) {
                    accessChecks.add(new AccessCheck(AccessRequired.Util.resourceNameFromConfigBeanProxy(appTenant), "delete"));
                }
            }
        }
        return accessChecks;
    }

    @Override
    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        // now unprovision the application from tenants if any was
        // provisioned
        unprovisionAppFromTenants(name, report, context.getSubject());

        // invoke the undeploy command with domain target to undeploy the
        // application from domain

        CommandInvocation<?> inv = commandRunner.getCommandInvocation("undeploy", report, context.getSubject());

        final ParameterMap parameters = new ParameterMap();

        parameters.set("DEFAULT", name);

        parameters.set(DeploymentProperties.TARGET, DeploymentUtils.DOMAIN_TARGET_NAME);
        inv.parameters(parameters).execute();
    }

    private void unprovisionAppFromTenants(String appName, ActionReport report, final Subject subject) {

        if (app == null || appTenants== null) {
            return;
        }

        for (AppTenant tenant : appTenants) {
            ActionReport subReport = report.addSubActionsReport();
            CommandInvocation<?> inv = commandRunner.getCommandInvocation("_mt-unprovision", subReport, subject);
            ParameterMap parameters = new ParameterMap();
            parameters.add("DEFAULT", appName);
            parameters.add("tenant", tenant.getTenant());
            inv.parameters(parameters).execute();
        }
    }
}
