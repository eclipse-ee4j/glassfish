/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.admin.util.ClusterOperationUtil;
import com.sun.enterprise.config.serverbeans.*;
import java.util.Collection;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.Param;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.event.Events;
import org.glassfish.api.event.EventListener.Event;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.OpsParams.Origin;
import org.glassfish.api.deployment.OpsParams.Command;
import org.glassfish.config.support.TargetType;
import org.glassfish.config.support.CommandTarget;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.deployment.common.DeploymentContextImpl;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.jvnet.hk2.annotations.Service;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.config.TransactionFailure;
import org.jvnet.hk2.config.Transaction;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Map;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.FailurePolicy;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.deployment.common.ApplicationConfigInfo;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.deployment.versioning.VersioningException;
import org.glassfish.deployment.versioning.VersioningSyntaxException;
import org.glassfish.deployment.versioning.VersioningUtils;
import org.glassfish.deployment.versioning.VersioningWildcardException;
import org.glassfish.deployment.versioning.VersioningService;

/**
 * Create application ref command
 */
@Service(name="create-application-ref")
@I18n("create.application.ref.command")
@ExecuteOn(value={RuntimeType.DAS})
@PerLookup
@TargetType(value={CommandTarget.DAS, CommandTarget.STANDALONE_INSTANCE, CommandTarget.CLUSTER})
@RestEndpoints({
    @RestEndpoint(configBean=Cluster.class,opType=RestEndpoint.OpType.POST, path="create-application-ref"),
    @RestEndpoint(configBean=Server.class,opType=RestEndpoint.OpType.POST, path="create-application-ref")
})
public class CreateApplicationRefCommand implements AdminCommand, AdminCommandSecurity.AccessCheckProvider {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(CreateApplicationRefCommand.class);

    @Param(primary=true)
    public String name = null;

    @Param(optional=true)
    String target = "server";

    @Param(optional=true)
    public String virtualservers = null;

    @Param(optional=true, defaultValue="true")
    public Boolean enabled = true;

    @Param(optional=true, acceptableValues="true,false")
    public String lbenabled;

    @Inject
    Deployment deployment;

    @Inject
    Domain domain;

    @Inject
    ServerEnvironment env;

    @Inject
    Applications applications;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    VersioningService versioningService;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    protected Server server;

    @Inject
    private ServiceLocator habitat;

    @Inject
    Events events;

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        final List<AccessCheck> accessChecks = new ArrayList<AccessCheck>();
        accessChecks.add(new AccessCheck(DeploymentCommandUtils.getTargetResourceNameForNewAppRef(domain, target), "create"));
        return accessChecks;
    }

    /**
     * Entry point from the framework into the command execution
     * @param context context for the command.
     */
    public void execute(AdminCommandContext context) {
        final ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();
        // retrieve matched version(s) if exist
        List<String> matchedVersions = null;

        if ( enabled ) {
            try {
                // warn users that they can use version expressions
                VersioningUtils.checkIdentifier(name);
                matchedVersions = new ArrayList<String>(1);
                matchedVersions.add(name);
            } catch (VersioningWildcardException ex) {
                // a version expression is supplied with enabled == true
                report.setMessage(localStrings.getLocalString("wildcard.not.allowed",
                        "WARNING : version expression are available only with --enabled=false"));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            } catch (VersioningSyntaxException ex) {
                report.setMessage(ex.getLocalizedMessage());
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            if (!deployment.isRegistered(name)) {
                report.setMessage(localStrings.getLocalString("application.notreg", "Application {0} not registered", name));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        } else {
            // retrieve matched version(s) if exist
            try {
                matchedVersions = versioningService.getMatchedVersions(name, null);
            } catch (VersioningException e) {
                report.failure(logger, e.getMessage());
                return;
            }

            // if matched list is empty and no VersioningException thrown,
            // this is an unversioned behavior and the given application is not registered
            if(matchedVersions.isEmpty()){
                report.setMessage(localStrings.getLocalString("ref.not.referenced.target","Application {0} is not referenced by target {1}", name, target));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }
        }

        ActionReport.MessagePart part = report.getTopMessagePart();
        boolean isVersionExpression = VersioningUtils.isVersionExpression(name);

        // for each matched version
        Iterator it = matchedVersions.iterator();
        while (it.hasNext()) {
            String appName = (String) it.next();
            Application app = applications.getApplication(appName);

            ApplicationRef applicationRef = domain.getApplicationRefInTarget(appName, target);
            if ( applicationRef != null ) {
                // we provides warning messages
                // if a versioned name has been provided to the command
                if( isVersionExpression ){
                    ActionReport.MessagePart childPart = part.addChild();
                    childPart.setMessage(localStrings.getLocalString("appref.already.exists",
                            "Application reference {0} already exists in target {1}.", appName, target));
                } else {
                    // returns failure if an untagged name has been provided to the command
                    report.setMessage(localStrings.getLocalString("appref.already.exists",
                            "Application reference {0} already exists in target {1}.", name, target));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }
            } else {

                Transaction t = new Transaction();
                if (app.isLifecycleModule()) {
                    handleLifecycleModule(context, t);
                    return;
                }

                ReadableArchive archive;
                File file = null;
                DeployCommandParameters commandParams=null;
                Properties contextProps;
                Map<String, Properties> modulePropsMap = null;
                ApplicationConfigInfo savedAppConfig = null;
                try {
                    commandParams = app.getDeployParameters(null);
                    commandParams.origin = Origin.create_application_ref;
                    commandParams.command = Command.create_application_ref;
                    commandParams.target = target;
                    commandParams.virtualservers = virtualservers;
                    commandParams.enabled = enabled;
                    if(lbenabled != null){
                        commandParams.lbenabled = lbenabled;
                    }
                    commandParams.type = app.archiveType();

                    contextProps = app.getDeployProperties();
                    modulePropsMap = app.getModulePropertiesMap();
                    savedAppConfig = new ApplicationConfigInfo(app);

                    URI uri = new URI(app.getLocation());
                    file = new File(uri);

                    if (!file.exists()) {
                        report.setMessage(localStrings.getLocalString("fnf",
                            "File not found", file.getAbsolutePath()));
                        report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                        return;
                    }

                    archive = archiveFactory.openArchive(file);
                } catch (Exception e) {
                    logger.log(Level.SEVERE, "Error opening deployable artifact : " + file.getAbsolutePath(), e);
                    report.setMessage(localStrings.getLocalString("unknownarchiveformat", "Archive format not recognized"));
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                    return;
                }

                try {
                    final ExtendedDeploymentContext deploymentContext =
                            deployment.getBuilder(logger, commandParams, report).source(archive).build();

                    Properties appProps = deploymentContext.getAppProps();
                    appProps.putAll(contextProps);

                    // relativize the location so it could be set properly in
                    // domain.xml
                    String location = DeploymentUtils.relativizeWithinDomainIfPossible(new URI(app.getLocation()));
                    appProps.setProperty(ServerTags.LOCATION, location);

                    // relativize the URI properties so they could store in the
                    // domain.xml properly on the instances
                    String appLocation = appProps.getProperty(Application.APP_LOCATION_PROP_NAME);
                    appProps.setProperty(Application.APP_LOCATION_PROP_NAME, DeploymentUtils.relativizeWithinDomainIfPossible(new URI(appLocation)));
                    String planLocation = appProps.getProperty(Application.DEPLOYMENT_PLAN_LOCATION_PROP_NAME);
                    if (planLocation != null) {
                        appProps.setProperty(Application.DEPLOYMENT_PLAN_LOCATION_PROP_NAME, DeploymentUtils.relativizeWithinDomainIfPossible(new URI(planLocation)));
                    }
                    String altDDLocation = appProps.getProperty(Application.ALT_DD_LOCATION_PROP_NAME);
                    if (altDDLocation != null) {
                        appProps.setProperty(Application.ALT_DD_LOCATION_PROP_NAME, DeploymentUtils.relativizeWithinDomainIfPossible(new URI(altDDLocation)));
                    }
                    String runtimeAltDDLocation = appProps.getProperty(Application.RUNTIME_ALT_DD_LOCATION_PROP_NAME);
                    if (runtimeAltDDLocation != null) {
                        appProps.setProperty(Application.RUNTIME_ALT_DD_LOCATION_PROP_NAME, DeploymentUtils.relativizeWithinDomainIfPossible(new URI(runtimeAltDDLocation)));
                    }
                    savedAppConfig.store(appProps);

                    if (modulePropsMap != null) {
                        deploymentContext.setModulePropsMap(modulePropsMap);
                    }

                    if(enabled){
                        versioningService.handleDisable(appName, target, deploymentContext.getActionReport(), context.getSubject());
                    }

                    if (domain.isCurrentInstanceMatchingTarget(target, appName, server.getName(), null)) {
                        deployment.deploy(deployment.getSniffersFromApp(app), deploymentContext);
                    } else {
                        // send the APPLICATION_PREPARED event for DAS
                        events.send(new Event<>(Deployment.APPLICATION_PREPARED, deploymentContext), false);
                    }

                    if (report.getActionExitCode().equals(
                        ActionReport.ExitCode.SUCCESS)) {
                        try {
                            deployment.registerAppInDomainXML(null, deploymentContext, t, true);
                        } catch(TransactionFailure e) {
                            logger.warning("failed to create application ref for " + appName);
                        }
                    }

                    // if the target is DAS, we do not need to do anything more
                    if (!isVersionExpression && DeploymentUtils.isDASTarget(target)) {
                        return;
                    }

                    final ParameterMap paramMap =
                            deployment.prepareInstanceDeployParamMap(deploymentContext);
                    final List<String> targets =
                            new ArrayList<String>(Arrays.asList(commandParams.target.split(",")));

                    ClusterOperationUtil.replicateCommand(
                        "_deploy",
                        FailurePolicy.Error,
                        FailurePolicy.Warn,
                        FailurePolicy.Ignore,
                        targets,
                        context,
                        paramMap,
                        habitat);

                } catch(Exception e) {
                    logger.log(Level.SEVERE, "Error during creating application ref ", e);
                    report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                } finally {
                    try {
                        archive.close();
                    } catch(IOException e) {
                        logger.log(Level.INFO, "Error while closing deployable artifact : " + file.getAbsolutePath(), e);
                    }
                }
            }
        }
    }

    private void handleLifecycleModule(AdminCommandContext context, Transaction t) {
        final ActionReport report = context.getActionReport();
        final Logger logger = context.getLogger();

        Application app = applications.getApplication(name);

        // create a dummy context to hold params and props
        DeployCommandParameters commandParams = new DeployCommandParameters();
        commandParams.name = name;
        commandParams.target = target;
        commandParams.virtualservers = virtualservers;
        commandParams.enabled = enabled;

        ExtendedDeploymentContext lifecycleContext = new DeploymentContextImpl(report, null, commandParams, null);
        try  {
            deployment.registerAppInDomainXML(null, lifecycleContext, t, true);
        } catch(Exception e) {
            report.failure(logger, e.getMessage());
        }

        if (!DeploymentUtils.isDASTarget(target)) {
            final ParameterMap paramMap = new ParameterMap();
            paramMap.add("DEFAULT", name);
            paramMap.add(DeploymentProperties.TARGET, target);
            paramMap.add(DeploymentProperties.ENABLED, enabled.toString());
            if (virtualservers != null) {
                paramMap.add(DeploymentProperties.VIRTUAL_SERVERS,
                    virtualservers);
            }
            // pass the app props so we have the information to persist in the
            // domain.xml
            Properties appProps = app.getDeployProperties();
            paramMap.set(DeploymentProperties.APP_PROPS, DeploymentUtils.propertiesValue(appProps, ':'));

            final List<String> targets = new ArrayList<String>();
            targets.add(target);
            ClusterOperationUtil.replicateCommand("_lifecycle", FailurePolicy.Error, FailurePolicy.Warn,
                    FailurePolicy.Ignore, targets, context, paramMap, habitat);
        }
    }
}
