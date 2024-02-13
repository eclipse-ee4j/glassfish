/*
 * Copyright (c) 2021, 2024 Contributors to the Eclipse Foundation.
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.config.serverbeans.*;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.ApplicationRegistry;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.internal.deployment.DeploymentTargetResolver;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.admin.util.ClusterOperationUtil;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.EventListener.Event;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.config.support.TargetType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.common.util.admin.ParameterMapExtractor;
import jakarta.inject.Inject;
import org.jvnet.hk2.annotations.Service;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.TransactionFailure;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Collections;
import org.glassfish.api.admin.AccessRequired;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.CommandRunner;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.deployment.common.Artifacts;

import org.glassfish.deployment.versioning.VersioningService;
import org.glassfish.deployment.versioning.VersioningException;

/**
 * Undeploys applications.
 *
 * @author dochez
 */
@Service(name="undeploy")
@I18n("undeploy.command")
@PerLookup
@ExecuteOn(value={RuntimeType.DAS, RuntimeType.INSTANCE})
@TargetType(value={CommandTarget.DOMAIN, CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
@RestEndpoints({
        @RestEndpoint(configBean = Applications.class, opType = RestEndpoint.OpType.DELETE, path = "undeploy", description = "Undeploy an application")
})
public class UndeployCommand extends UndeployCommandParameters implements AdminCommand, DeploymentTargetResolver,
    AdminCommandSecurity.Preauthorization, AdminCommandSecurity.AccessCheckProvider {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(UndeployCommand.class);

    @Inject
    Deployment deployment;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    Applications apps;

    @Inject
    ApplicationRegistry appRegistry;

    @Inject
    VersioningService versioningService;

    @Inject
    ServerEnvironment env;

    @Inject
    Domain domain;

    @Inject
    CommandRunner commandRunner;

    @Inject
    ServiceLocator habitat;

    @Inject
    Events events;

    private ActionReport report;
    private Logger logger;
    private List<String> matchedVersions;

    public UndeployCommand() {
        origin = Origin.undeploy;
        command = Command.undeploy;
    }

    @Override
    public boolean preAuthorization(AdminCommandContext context) {
        report = context.getActionReport();
        logger = context.getLogger();

        deployment.validateSpecifiedTarget(target);

        // we need to set the default target for non-paas case first
        // so the versioned code could execute as expected
        if (target == null) {
            target = deployment.getDefaultTarget(_classicstyle);
        }

        /**
         * A little bit of dancing around has to be done, in case the
         * user passed the path to the original directory.
         */
        name = (new File(name)).getName();

/*
        // I should really look if the associated cluster is virtual
        Cluster cluster = domain.getClusterNamed(name);
        if (cluster!=null) {
            target = name;
        }
*/

        // retrieve matched version(s) if exist
        matchedVersions = null;
        try {
            matchedVersions = versioningService.getMatchedVersions(name,
                target);
        } catch (VersioningException e) {
            if (env.isDas()) {
                report.failure(logger, e.getMessage());
            } else {
                // we should let undeployment go through
                // on instance side for partial deployment case
                logger.fine(e.getMessage());
            }
            return false;
        }

        // if matched list is empty and no VersioningException thrown,
        // this is an unversioned behavior and the given application is not registered
        if(matchedVersions.isEmpty()){
            if (env.isDas()) {
                report.setMessage(localStrings.getLocalString("ref.not.referenced.target","Application {0} is not referenced by target {1}", name, target));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            } else {
                // we should let undeployment go through
                // on instance side for partial deployment case
                logger.fine(localStrings.getLocalString("ref.not.referenced.target","Application {0} is not referenced by target {1}", name, target));
            }
            return false;
        }

        for (String appName : matchedVersions) {
            if (target == null) {
                target = deployment.getDefaultTarget(appName, origin, _classicstyle);
            }

            Application application = apps.getModule(Application.class, appName);

            if (application==null) {
                report.setMessage(localStrings.getLocalString("application.notreg","Application {0} not registered", appName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return false;
            }
        }
        return true;
    }


    @Override
    public Collection<? extends AccessRequired.AccessCheck> getAccessChecks() {
        return DeploymentCommandUtils.getAccessChecksForExistingApp(
                domain, apps, target, matchedVersions, "delete", "delete");
    }

    public void execute(AdminCommandContext context) {



        // for each matched version
        Iterator it = matchedVersions.iterator();
        while (it.hasNext()) {
            String appName = (String)it.next();

            if (target == null) {
                target = deployment.getDefaultTarget(appName, origin, _classicstyle);
            }

            ApplicationInfo info = deployment.get(appName);

            Application application = apps.getModule(Application.class, appName);

            if (application==null) {
                report.setMessage(localStrings.getLocalString("application.notreg","Application {0} not registered", appName));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;

            }

            deployment.validateUndeploymentTarget(target, appName);

            if (!DeploymentUtils.isDomainTarget(target)) {
                ApplicationRef ref = domain.getApplicationRefInTarget(appName, target);
                if (ref == null) {
                    report.setMessage(localStrings.getLocalString("ref.not.referenced.target","Application {0} is not referenced by target {1}", appName, target));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            }

            ReadableArchive source = null;
            if (info==null) {
                // disabled application or application failed to be
                // loaded for some reason
                URI uri = null;
                try {
                    uri = new URI(application.getLocation());
                } catch (URISyntaxException e) {
                    logger.severe("Cannot determine original location for application : " + e.getMessage());
                }
                if (uri != null) {
                    File location = new File(uri);
                    if (location.exists()) {
                        try {
                            source = archiveFactory.openArchive(location);
                        } catch (IOException e) {
                            logger.log(Level.INFO, e.getMessage(),e );
                        }
                    } else {
                        logger.warning("Originally deployed application at "+ location + " not found");
                    }
                }
            } else {
                source = info.getSource();
            }

            if (source == null) {
                logger.fine("Cannot get source archive for undeployment");
                // remove the application from the domain.xml so at least
                // server is in a consistent state after restart
                try {
                    deployment.unregisterAppFromDomainXML(appName, target);
                } catch(TransactionFailure e) {
                    logger.warning("Module " + appName + " not found in configuration");
                }
                // also remove application from runtime registry
                if (info != null) {
                    appRegistry.remove(appName);
                }
                return;
            }

            File sourceFile = new File(source.getURI());
            if (!source.exists()) {
                logger.log(Level.WARNING, "Cannot find application bits at " +
                    sourceFile.getPath() + ". Please restart server to ensure server is in a consistent state before redeploy the application.");
                // remove the application from the domain.xml so at least
                // server is in a consistent state after restart
                try {
                    deployment.unregisterAppFromDomainXML(appName, target);
                } catch(TransactionFailure e) {
                    logger.warning("Module " + appName + " not found in configuration");
                }
                // also remove application from runtime registry
                if (info != null) {
                    appRegistry.remove(appName);
                }
                return;
            }

            // now start the normal undeploying
            this.name = appName;
            this._type = application.archiveType();

            ExtendedDeploymentContext deploymentContext = null;
            try {
                deploymentContext = deployment.getBuilder(logger, this, report).source(source).build();
                // Remove old metadata to avoid object leak
                deploymentContext.getSource().removeArchiveMetaData(DeploymentProperties.COMMAND_PARAMS);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Cannot create context for undeployment ", e);
                report.setMessage(localStrings.getLocalString("undeploy.contextcreation.failed","Cannot create context for undeployment : {0} ", e.getMessage()));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            final InterceptorNotifier notifier = new InterceptorNotifier(habitat, deploymentContext);
            final DeployCommandSupplementalInfo suppInfo = new DeployCommandSupplementalInfo();
            suppInfo.setDeploymentContext(deploymentContext);
            report.setResultType(DeployCommandSupplementalInfo.class, suppInfo);

            final Properties appProps = deploymentContext.getAppProps();
            appProps.putAll(application.getDeployProperties());

            if (properties!=null) {
                appProps.putAll(properties);
            }

            deploymentContext.setModulePropsMap(
                application.getModulePropertiesMap());

            events.send(new Event<>(Deployment.UNDEPLOYMENT_VALIDATION, deploymentContext), false);

            if (report.getActionExitCode()==ActionReport.ExitCode.FAILURE) {
                // if one of the validation listeners sets the action report
                // status as failure, return
                return;
            }

            // disable the application first for non-DAS target
            if (env.isDas() && !DeploymentUtils.isDASTarget(target)) {
                ActionReport subReport = report.addSubActionsReport();
                CommandRunner.CommandInvocation inv = commandRunner.getCommandInvocation("disable", subReport, context.getSubject());

                try {
                    final ParameterMapExtractor extractor = new ParameterMapExtractor(this);
                    final ParameterMap parameters = extractor.extract(Collections.EMPTY_LIST);
                    parameters.set("DEFAULT", appName);
                    parameters.add(DeploymentProperties.IS_UNDEPLOY, Boolean.TRUE.toString());
                    inv.parameters(parameters).execute();

                    if (subReport.getActionExitCode().equals(
                    ActionReport.ExitCode.FAILURE)) {
                    // if disable application failed
                    // we should just return
                        report.setMessage(localStrings.getLocalString("disable.command.failed","{0} disabled failed", appName));
                    return;
                    }

                    if (DeploymentUtils.isDomainTarget(target)) {
                        List<String> targets = domain.getAllReferencedTargetsForApplication(appName);
                        // replicate command to all referenced targets
                        parameters.remove("isUndeploy");
                        notifier.ensureBeforeReported(ExtendedDeploymentContext.Phase.REPLICATION);
                        ClusterOperationUtil.replicateCommand("undeploy", FailurePolicy.Error, FailurePolicy.Warn,
                                FailurePolicy.Ignore, targets, context, parameters, habitat);
                    }
                } catch (Exception e) {
                    report.failure(logger, e.getMessage());
                    return;
                }
            }

            /*
             * Extract the generated artifacts from the application's properties
             * and record them in the DC.  This will be useful, for example,
             * during Deployer.clean.
             */
            final Artifacts generatedArtifacts = DeploymentUtils.generatedArtifacts(application);
            generatedArtifacts.record(deploymentContext);

            if (info!=null) {
                deployment.undeploy(appName, deploymentContext);
            }

            // check if it's directory deployment
            boolean isDirectoryDeployed =
                Boolean.valueOf(application.getDirectoryDeployed());

            // we should try to unregister the application for both success
            // and warning case
            if (!report.getActionExitCode().equals(ActionReport.ExitCode.FAILURE)) {
                // so far I am doing this after the unload, maybe this should be moved before...
                try {
                    // remove the "application" element
                    deployment.unregisterAppFromDomainXML(appName, target);
                } catch(TransactionFailure e) {
                    logger.warning("Module " + appName + " not found in configuration");
                }

                //remove context from generated
                deploymentContext.clean();

                //if directory deployment then do not remove the directory
                if ( (! keepreposdir) && !isDirectoryDeployed && source.exists()) {
                    /*
                     * Delete the repository directory as an archive so
                     * any special handling (such as stale file handling)
                     * known to the archive can run.
                     */
                    source.delete();
                }

            } // else a message should have been provided.
        }
    }

    public String getTarget(ParameterMap parameters) {
        return DeploymentCommandUtils.getTarget(parameters, origin, deployment);
    }
}
