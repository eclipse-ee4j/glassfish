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

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationRef;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.ServerTags;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
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
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.jvnet.hk2.annotations.Service;

@Service(name="_mt-provision")
@org.glassfish.api.admin.ExecuteOn(value={RuntimeType.DAS})
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
public class MTProvisionCommand implements AdminCommand, AdminCommandSecurity.AccessCheckProvider {

    @Param(optional=true)
    public File customizations = null;

    @Param
    public String tenant = null;

    @Param
    public String contextroot = null;

    @Param(primary=true)
    public String appname;

    @Inject
    Domain domain;

    @Inject
    Applications applications;

    @Inject
    Deployment deployment;

    @Inject
    ArchiveFactory archiveFactory;

    private Application app;
    private ApplicationRef appRef;

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        final List<AccessCheck> accessChecks = new ArrayList<AccessCheck>();
        app = applications.getApplication(appname);
        if (app != null) {
            accessChecks.add(new AccessCheck(AccessRequired.Util.resourceNameFromConfigBeanProxy(app), "provision"));
            appRef = domain.getApplicationRefInTarget(appname, DeploymentUtils.DAS_TARGET_NAME);
            if (appRef != null) {
                accessChecks.add(new AccessCheck(AccessRequired.Util.resourceNameFromConfigBeanProxy(appRef), "provision"));
            }
        }
        return accessChecks;
    }

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(MTProvisionCommand.class);

    public void execute(AdminCommandContext context) {

        final ActionReport report = context.getActionReport();

        final Logger logger = context.getLogger();

        if (app == null) {
            report.setMessage("Application " + appname + " needs to be deployed first before provisioned to tenant");
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            return;
        }

        ReadableArchive archive = null;

        DeployCommandParameters commandParams = app.getDeployParameters(appRef);

        commandParams.contextroot = contextroot;
        commandParams.target = DeploymentUtils.DAS_TARGET_NAME;
        commandParams.name = DeploymentUtils.getInternalNameForTenant(appname, tenant);
        commandParams.enabled = Boolean.TRUE;
        commandParams.origin = DeployCommandParameters.Origin.mt_provision;

        try {
            URI uri = new URI(app.getLocation());
            File file = new File(uri);

            if (!file.exists()) {
                throw new Exception(localStrings.getLocalString("fnf", "File not found", file.getAbsolutePath()));
            }

            archive = archiveFactory.openArchive(file);

            ExtendedDeploymentContext deploymentContext =
                deployment.getBuilder(logger, commandParams, report).
                    source(archive).build();

            Properties appProps = deploymentContext.getAppProps();
            appProps.putAll(app.getDeployProperties());

            // some container code is accessing context root through
            // app props so we also need to override that
            if (contextroot!=null) {
                appProps.setProperty(ServerTags.CONTEXT_ROOT, contextroot);
            }

            deploymentContext.setModulePropsMap(app.getModulePropertiesMap());

            deploymentContext.setTenant(tenant, appname);

            expandCustomizationJar(deploymentContext.getTenantDir());
            deployment.deploy(deploymentContext);

            deployment.registerTenantWithAppInDomainXML(appname, deploymentContext);
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

    private void expandCustomizationJar(File tenantDir) throws IOException {
        if (!tenantDir.exists() && !tenantDir.mkdirs()) {
             // TODO Handle this situation properly -- issue reported by findbugs
        }

        if (customizations == null) {
            return;
        }

        ReadableArchive cusArchive = null;
        WritableArchive expandedArchive = null;

        try {
            expandedArchive = archiveFactory.createArchive(tenantDir);
            cusArchive = archiveFactory.openArchive(customizations);
            DeploymentUtils.expand(cusArchive, expandedArchive);
        } finally {
            try {
                if (cusArchive != null) {
                    cusArchive.close();
                }
                if (expandedArchive != null) {
                    expandedArchive.close();
                }
            } catch(IOException e) {
                // ignore
            }
        }
    }
}
