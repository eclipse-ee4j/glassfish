/*
 * Copyright (c) 2022, 2022 Contributors to the Eclipse Foundation.
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

package org.glassfish.internal.deployment;

import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.ApplicationRef;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.OpsParams;
import org.glassfish.api.deployment.UndeployCommandParameters;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.event.EventTypes;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.data.EngineInfo;
import org.glassfish.internal.data.ModuleInfo;
import org.glassfish.internal.data.ProgressTracker;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.config.RetryableException;
import org.jvnet.hk2.config.Transaction;
import org.jvnet.hk2.config.TransactionFailure;

/**
 * Deployment facility
 *
 * @author Jerome Dochez
 */
@Contract
public interface Deployment {
    /**
     * This synchronous event is sent right after initial deployment context is created
     */
    EventTypes<DeploymentContext> INITIAL_CONTEXT_CREATED = EventTypes.create("Initial_Context_Created",
            DeploymentContext.class);
    /**
     * This synchronous event is sent when a new deployment or loading of an already deployed application start. It is
     * invoked once before any sniffer is invoked.
     */
    EventTypes<DeploymentContext> DEPLOYMENT_START = EventTypes.create("Deployment_Start", DeploymentContext.class);

    /**
     * The name of the Deployment Failure event
     */
    String DEPLOYMENT_FAILURE_NAME = "Deployment_Failed";
    /**
     * This asynchronous event is sent when a deployment activity (first time deploy or loading of an already deployed
     * application) failed.
     */
    EventTypes<DeploymentContext> DEPLOYMENT_FAILURE = EventTypes.create(DEPLOYMENT_FAILURE_NAME, DeploymentContext.class);
    /**
     * This synchronous event is sent after creation of deployment classloader.
     */
    EventTypes<DeploymentContext> AFTER_DEPLOYMENT_CLASSLOADER_CREATION = EventTypes
            .create("After_Deployment_ClassLoader_Creation", DeploymentContext.class);
    /**
     * This synchronous event is sent before prepare phase of deployment.
     */
    EventTypes<DeploymentContext> DEPLOYMENT_BEFORE_CLASSLOADER_CREATION = EventTypes.create("Deployment_ClassLoader_Creation",
            DeploymentContext.class);
    /**
     * This synchronous event is sent after creation of application classloader.
     */
    EventTypes<DeploymentContext> AFTER_APPLICATION_CLASSLOADER_CREATION = EventTypes
            .create("After_Application_ClassLoader_Creation", DeploymentContext.class);

    /**
     * This asynchronous event is sent when a deployment activity (first time deploy or loading of an already deployed
     * application) succeeded.
     */
    EventTypes<ApplicationInfo> DEPLOYMENT_SUCCESS = EventTypes.create("Deployment_Success", ApplicationInfo.class);

    /**
     * This asynchronous event is sent when a new deployment or loading of an already deployed application start. It is
     * invoked once before any sniffer is invoked.
     */
    EventTypes<ApplicationInfo> UNDEPLOYMENT_START = EventTypes.create("Undeployment_Start", ApplicationInfo.class);
    /**
     * This asynchronous event is sent when a deployment activity (first time deploy or loading of an already deployed
     * application) failed.
     */
    EventTypes<DeploymentContext> UNDEPLOYMENT_FAILURE = EventTypes.create("Undeployment_Failed", DeploymentContext.class);

    /**
     * This asynchronous event is sent when a deployment activity (first time deploy or loading of an already deployed
     * application) succeeded.
     */
    EventTypes<DeploymentContext> UNDEPLOYMENT_SUCCESS = EventTypes.create("Undeployment_Success", DeploymentContext.class);

    /**
     * The following synchronous events are sent after each change in a module state.
     */
    EventTypes<DeploymentContext> MODULE_PREPARED = EventTypes.create("Module_Prepared", DeploymentContext.class);
    EventTypes<ModuleInfo> MODULE_LOADED = EventTypes.create("Module_Loaded", ModuleInfo.class);
    EventTypes<ModuleInfo> MODULE_STARTED = EventTypes.create("Module_Running", ModuleInfo.class);
    EventTypes<ModuleInfo> MODULE_STOPPED = EventTypes.create("Module_Stopped", ModuleInfo.class);
    EventTypes<ModuleInfo> MODULE_UNLOADED = EventTypes.create("Module_Unloaded", ModuleInfo.class);
    EventTypes<DeploymentContext> MODULE_CLEANED = EventTypes.create("Module_Cleaned", DeploymentContext.class);

    /**
     * The following synchronous events are sent after each change in an application stated (An application contains 1 to
     * many modules)
     */
    EventTypes<DeploymentContext> APPLICATION_PREPARED = EventTypes.create("Application_Prepared", DeploymentContext.class);
    EventTypes<ApplicationInfo> APPLICATION_LOADED = EventTypes.create("Application_Loaded", ApplicationInfo.class);
    EventTypes<ApplicationInfo> APPLICATION_STARTED = EventTypes.create("Application_Running", ApplicationInfo.class);
    EventTypes<ApplicationInfo> APPLICATION_STOPPED = EventTypes.create("Application_Stopped", ApplicationInfo.class);
    EventTypes<ApplicationInfo> APPLICATION_UNLOADED = EventTypes.create("Application_Unloaded", ApplicationInfo.class);
    EventTypes<DeploymentContext> APPLICATION_CLEANED = EventTypes.create("Application_Cleaned", DeploymentContext.class);
    EventTypes<ApplicationInfo> APPLICATION_DISABLED = EventTypes.create("Application_Disabled", ApplicationInfo.class);

    /**
     * The following synchronous events are sent during CDI initialization during application deployment
     */
    // before extensions are started
    EventTypes<ApplicationInfo> CDI_BEFORE_EXTENSIONS_STARTED = EventTypes.create("CDI_Extensions_Prepared", ApplicationInfo.class);
    EventTypes<ServerModuleCdiRegistry> CDI_REGISTER_SERVER_MODULES = EventTypes.create("CDI_Server_Modules_Registration", ServerModuleCdiRegistry.class);

    /**
     * The following synchronous event is sent before the application is undeployed so various listeners could validate the
     * undeploy operation and decide whether to abort undeployment
     */
    EventTypes<DeploymentContext> UNDEPLOYMENT_VALIDATION = EventTypes.create("Undeployment_Validation",
            DeploymentContext.class);

    public interface DeploymentContextBuilder {

        DeploymentContextBuilder source(File source);

        DeploymentContextBuilder source(ReadableArchive archive);

        File sourceAsFile();

        ReadableArchive sourceAsArchive();

        ArchiveHandler archiveHandler();

        DeploymentContextBuilder archiveHandler(ArchiveHandler handler);

        Logger logger();

        ActionReport report();

        OpsParams params();

        ExtendedDeploymentContext build() throws IOException;

        ExtendedDeploymentContext build(ExtendedDeploymentContext initialContext) throws IOException;

    }

    /**
     * The following asynchronous event is sent after all applications are started in server start up.
     */
    EventTypes<DeploymentContext> ALL_APPLICATIONS_PROCESSED = EventTypes.create("All_Applications_Processed",
            DeploymentContext.class);

    DeploymentContextBuilder getBuilder(Logger loggger, OpsParams params, ActionReport report);

    ArchiveHandler getArchiveHandler(ReadableArchive archive) throws IOException;

    ArchiveHandler getArchiveHandler(ReadableArchive archive, String type) throws IOException;

    ModuleInfo prepareModule(List<EngineInfo<?, ?>> sortedEngineInfos, String moduleName, DeploymentContext context, ProgressTracker tracker) throws Exception;

    ApplicationInfo deploy(final ExtendedDeploymentContext context);

    ApplicationInfo deploy(final Collection<? extends Sniffer> sniffers, final ExtendedDeploymentContext context);

    void undeploy(String appName, ExtendedDeploymentContext context);

    Transaction prepareAppConfigChanges(final DeploymentContext context) throws TransactionFailure;

    void registerAppInDomainXML(final ApplicationInfo applicationInfo, final DeploymentContext context, Transaction t) throws TransactionFailure;

    void unregisterAppFromDomainXML(final String appName, final String target) throws TransactionFailure;

    void registerAppInDomainXML(final ApplicationInfo applicationInfo, final DeploymentContext context, Transaction t,
            boolean appRefOnly) throws TransactionFailure;

    void unregisterAppFromDomainXML(final String appName, final String target, boolean appRefOnly) throws TransactionFailure;

    void registerTenantWithAppInDomainXML(final String appName, final ExtendedDeploymentContext context) throws TransactionFailure;

    void unregisterTenantWithAppInDomainXML(final String appName, final String tenantName)
            throws TransactionFailure, RetryableException;

    void updateAppEnabledAttributeInDomainXML(final String appName, final String target, final boolean enabled)
            throws TransactionFailure;

    List<EngineInfo<?, ?>> setupContainerInfos(DeploymentContext context) throws Exception;

    List<EngineInfo<?, ?>> setupContainerInfos(final ArchiveHandler handler, Collection<? extends Sniffer> sniffers,
            DeploymentContext context) throws Exception;

    boolean isRegistered(String appName);

    ApplicationInfo get(String appName);

    ParameterMap prepareInstanceDeployParamMap(DeploymentContext dc) throws Exception;

    void validateDeploymentTarget(String target, String name, boolean isRedeploy);

    void validateUndeploymentTarget(String target, String name);

    void validateSpecifiedTarget(String target);

    boolean isAppEnabled(Application app);

    ApplicationInfo unload(ApplicationInfo appInfo, ExtendedDeploymentContext context);

    DeploymentContext disable(UndeployCommandParameters commandParams, Application app, ApplicationInfo appInfo, ActionReport report,
            Logger logger) throws Exception;

    DeploymentContext enable(String target, Application app, ApplicationRef appRef, ActionReport report, Logger logger)
            throws Exception;

    /**
     * Scans the source of the deployment operation for all types and store the result in the deployment context. Subsequent
     * calls will return the cached copy from the context
     *
     * @param context deployment context
     * @return the types information from the deployment artifacts
     * @throws IOException if the scanning fails due to an I/O exception
     */
    Types getDeployableTypes(DeploymentContext context) throws IOException;

    List<Sniffer> getSniffersFromApp(Application app);

    Collection<? extends Sniffer> getSniffers(ArchiveHandler archiveHandler, Collection<? extends Sniffer> sniffers, DeploymentContext context);

    // sets the default target when the target is not specified
    String getDefaultTarget(String appName, OpsParams.Origin origin, Boolean isClassicStyle);

    // gets the default target when no target is specified for non-paas case
    String getDefaultTarget(Boolean isClassicStyle);

}
