/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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
import com.sun.enterprise.config.serverbeans.Cluster;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.deployment.OpsParams.Command;
import org.glassfish.api.deployment.OpsParams.Origin;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.config.support.TargetType;
import org.glassfish.deployment.versioning.VersioningException;
import org.glassfish.deployment.versioning.VersioningService;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.jvnet.hk2.annotations.Service;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Delete application ref command
 */
@Service(name="delete-application-ref")
@I18n("delete.application.ref.command")
@ExecuteOn(value={RuntimeType.DAS, RuntimeType.INSTANCE})
@PerLookup
@TargetType(value={CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,opType=RestEndpoint.OpType.DELETE, path="delete-application-ref"),
    @RestEndpoint(configBean=Server.class,opType=RestEndpoint.OpType.DELETE, path="delete-application-ref")
})
public class DeleteApplicationRefCommand implements AdminCommand, AdminCommandSecurity.Preauthorization, AdminCommandSecurity.AccessCheckProvider {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DeleteApplicationRefCommand.class);

    @Param(primary=true)
    public String name = null;

    @Param(optional=true)
    String target = "server";

    @Param(optional=true, defaultValue="false")
    public Boolean cascade;

    @Inject
    Deployment deployment;

    @Inject
    Domain domain;

    @Inject
    Applications applications;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    VersioningService versioningService;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    protected Server server;

    @Inject
    ServerEnvironment env;

    private List<String> matchedVersions;

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();
        // retrieve matched version(s) if exist
        try {
            matchedVersions = versioningService.getMatchedVersions(name, target);
        } catch (VersioningException e) {
            report.failure(logger, e.getMessage());
            return false;
        }

        // if matched list is empty and no VersioningException thrown,
        // this is an unversioned behavior and the given application is not registered
        if(matchedVersions.isEmpty()){
            if (env.isDas()) {
                // let's only do this check for DAS to be more
                // tolerable of the partial deployment case
                report.setMessage(localStrings.getLocalString("ref.not.referenced.target","Application {0} is not referenced by target {1}", name, target));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            }
            return false;
        }
        return true;
    }

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        return DeploymentCommandUtils.getAccessChecksForExistingApp(
                domain, applications, target, matchedVersions, "update", "delete");
    }

    /**
     * Entry point from the framework into the command execution
     * @param context context for the command.
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        UndeployCommandParameters commandParams =
            new UndeployCommandParameters();

        if (server.isDas()) {
            commandParams.origin = Origin.unload;
        } else {
            // delete application ref on instance
            // is essentially an undeploy
            commandParams.origin = Origin.undeploy;
        }
        commandParams.command = Command.delete_application_ref;

        // for each matched version
        Iterator it = matchedVersions.iterator();
        while (it.hasNext()) {
            String appName = (String)it.next();

            Application application = applications.getApplication(appName);
            if (application == null) {
                if (env.isDas()) {
                    // let's only do this check for DAS to be more
                    // tolerable of the partial deployment case
                    report.setMessage(localStrings.getLocalString("application.notreg","Application {0} not registered", appName));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                }
                return;
            }

            ApplicationRef applicationRef = domain.getApplicationRefInTarget(appName, target);
            if (applicationRef == null) {
                if (env.isDas()) {
                    // let's only do this check for DAS to be more
                    // tolerable of the partial deployment case
                    report.setMessage(localStrings.getLocalString("appref.not.exists","Target {1} does not have a reference to application {0}.", appName, target));
                    report.setActionExitCode(ActionReport.ExitCode.WARNING);
                }
                return;
            }

            if (application.isLifecycleModule()) {
                try  {
                    deployment.unregisterAppFromDomainXML(appName, target, true);
                } catch(Exception e) {
                    report.failure(logger, e.getMessage());
                }
                return;
            }

            try {
                ReadableArchive source = null;
                ApplicationInfo appInfo = deployment.get(appName);
                if (appInfo != null) {
                    source = appInfo.getSource();
                } else {
                    File location = new File(new URI(application.getLocation()));
                    source = archiveFactory.openArchive(location);
                }

                commandParams.name = appName;
                commandParams.cascade = cascade;

                final ExtendedDeploymentContext deploymentContext =
                        deployment.getBuilder(logger, commandParams, report).source(source).build();
                deploymentContext.getAppProps().putAll(
                    application.getDeployProperties());
                deploymentContext.setModulePropsMap(
                    application.getModulePropertiesMap());

                if (domain.isCurrentInstanceMatchingTarget(target, appName, server.getName(), null)&& appInfo != null) {
                    // stop and unload application if it's the target and the
                    // the application is in enabled state
                    deployment.unload(appInfo, deploymentContext);
                }

                if (report.getActionExitCode().equals(
                    ActionReport.ExitCode.SUCCESS)) {
                    try {
                        if (server.isInstance()) {
                            // if it's on instance, we should clean up
                            // the bits
                            deployment.undeploy(appName, deploymentContext);
                            deploymentContext.clean();
                            if (!Boolean.valueOf(application.getDirectoryDeployed()) && source.exists()) {
                                FileUtils.whack(new File(source.getURI()));
                            }
                            deployment.unregisterAppFromDomainXML(appName, target);
                        } else {
                            deployment.unregisterAppFromDomainXML(appName, target, true);
                        }
                    } catch(TransactionFailure e) {
                        logger.warning("failed to delete application ref for " + appName);
                    }
                }
            } catch(Exception e) {
                logger.log(Level.SEVERE, "Error during deleteing application ref ", e);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                report.setMessage(e.getMessage());
            }
        }
    }
}
