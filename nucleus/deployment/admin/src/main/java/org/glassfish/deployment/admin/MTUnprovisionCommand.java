/*
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
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.jvnet.hk2.annotations.Service;

@Service(name="_mt-unprovision")
@org.glassfish.api.admin.ExecuteOn(value={RuntimeType.DAS})
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
public class MTUnprovisionCommand implements AdminCommand, AdminCommandSecurity.AccessCheckProvider {

    @Param(primary=true)
    public String appname = null;

    @Param
    public String tenant = null;

    @Inject
    Applications applications;

    @Inject
    Deployment deployment;

    @Inject
    ArchiveFactory archiveFactory;

    private Application app;
    private AppTenant appTenant = null;

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        final List<AccessCheck> accessChecks = new ArrayList<AccessCheck>();
        app = applications.getApplication(appname);
        if (app != null) {
            accessChecks.add(new AccessCheck(AccessRequired.Util.resourceNameFromConfigBeanProxy(app), "read"));
            if (app.getAppTenants() != null) {
                appTenant = app.getAppTenants().getAppTenant(tenant);
                if (appTenant != null) {
                    accessChecks.add(new AccessCheck(AccessRequired.Util.resourceNameFromConfigBeanProxy(appTenant), "unprovision"));
                }
            }
        }
        return accessChecks;
    }

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(MTUnprovisionCommand.class);

    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        final Logger logger = context.getLogger();

        if (app == null) {
            report.setMessage("Application " + appname + " is not deployed");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        if (appTenant == null) {
            report.setMessage("Application " + appname + " is not provisioned to tenant " + tenant);
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        String internalAppName = DeploymentUtils.getInternalNameForTenant(appname, tenant);
        ApplicationInfo appInfo = deployment.get(internalAppName);

        ReadableArchive archive = null;

        try {
            if (appInfo != null) {
                archive = appInfo.getSource();
            } else {
                URI uri = new URI(app.getLocation());
                File location = new File(uri);
                if (location.exists()) {
                    archive = archiveFactory.openArchive(location);
                } else {
                    logger.log(Level.WARNING, localStrings.getLocalString("fnf", "File not found", location.getAbsolutePath()));
                    deployment.unregisterTenantWithAppInDomainXML(appname, tenant);
                    return;
                }
            }

            UndeployCommandParameters commandParams =
                new UndeployCommandParameters();

            commandParams.target = DeploymentUtils.DAS_TARGET_NAME;
            commandParams.name = internalAppName;
            commandParams.origin = DeployCommandParameters.Origin.mt_unprovision;
            ExtendedDeploymentContext deploymentContext = deployment.getBuilder(logger, commandParams, report).source(archive).build();

            deploymentContext.getAppProps().putAll(
                app.getDeployProperties());
            deploymentContext.getAppProps().putAll(
                appTenant.getDeployProperties());
            deploymentContext.setModulePropsMap(
                app.getModulePropertiesMap());

            deploymentContext.setTenant(tenant, app.getName());

            deployment.undeploy(internalAppName, deploymentContext);

            deployment.unregisterTenantWithAppInDomainXML(appname, tenant);

            // remove context from generated and tenant dir
            deploymentContext.clean();

        } catch(Throwable e) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(e.getMessage());
            report.setFailureCause(e);
        } finally {
            try {
                if (archive != null) {
                    archive.close();
                }
            } catch(IOException e) {
                // ignore
            }
        }

    }
}
