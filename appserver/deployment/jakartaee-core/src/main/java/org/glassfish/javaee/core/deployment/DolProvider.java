/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2009, 2022 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.javaee.core.deployment;

import com.sun.enterprise.config.serverbeans.DasConfig;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.config.serverbeans.Module;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.archivist.ApplicationArchivist;
import com.sun.enterprise.deployment.archivist.ApplicationFactory;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.archivist.DescriptorArchivist;
import com.sun.enterprise.deployment.deploy.shared.DeploymentPlanArchive;
import com.sun.enterprise.deployment.deploy.shared.InputJarArchive;
import com.sun.enterprise.deployment.deploy.shared.Util;
import com.sun.enterprise.deployment.util.ResourceValidator;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.v3.common.HTMLActionReporter;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.container.Sniffer;
import org.glassfish.api.deployment.ApplicationMetaDataProvider;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.deployment.DeploymentContext;
import org.glassfish.api.deployment.MetaData;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.deployment.common.DeploymentContextImpl;
import org.glassfish.deployment.common.DeploymentProperties;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.hk2.api.PreDestroy;
import org.glassfish.hk2.classmodel.reflect.Parser;
import org.glassfish.hk2.classmodel.reflect.Types;
import org.glassfish.internal.api.ClassLoaderHierarchy;
import org.glassfish.internal.deployment.ApplicationInfoProvider;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.DeploymentTracing;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.jvnet.hk2.annotations.Service;
import org.xml.sax.SAXException;

/**
 * ApplicationMetada
 */
@Service
public class DolProvider implements ApplicationMetaDataProvider<Application>,
        ApplicationInfoProvider {

    @Inject
    ArchivistFactory archivistFactory;

    @Inject
    protected ApplicationFactory applicationFactory;

    @Inject
    protected ArchiveFactory archiveFactory;

    @Inject
    protected DescriptorArchivist descriptorArchivist;

    @Inject
    protected ApplicationArchivist applicationArchivist;

    @Inject
    Domain domain;

    @Inject
    DasConfig dasConfig;

    @Inject
    Deployment deployment;

    @Inject
    ServerEnvironment env;

    @Inject
    Provider<ClassLoaderHierarchy> clhProvider;

    @Inject
    ResourceValidator resourceValidator;

    private static final String WRITEOUT_XML = System.getProperty("writeout.xml");

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(DolProvider.class);


    @Override
    public MetaData getMetaData() {
        return new MetaData(false, new Class[] { Application.class }, null);
    }

    private Application processDOL(DeploymentContext dc) throws IOException {
        ReadableArchive sourceArchive = dc.getSource();

        sourceArchive.setExtraData(Types.class, dc.getTransientAppMetaData(Types.class.getName(), Types.class));
        sourceArchive.setExtraData(Parser.class, dc.getTransientAppMetaData(Parser.class.getName(), Parser.class));

        ClassLoader cl = dc.getClassLoader();
        DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);

        sourceArchive.addArchiveMetaData(DeploymentProperties.APP_PROPS, dc.getAppProps());
        sourceArchive.addArchiveMetaData(DeploymentProperties.COMMAND_PARAMS, params);

        String name = params.name();
        String archiveType = dc.getArchiveHandler().getArchiveType();
        Archivist<? extends BundleDescriptor> archivist = archivistFactory.getArchivist(archiveType, cl);
        if (archivist == null) {
            // if no JavaEE medata was found in the archive, we return
            // an empty Application object
            return Application.createApplication();
        }
        archivist.setAnnotationProcessingRequested(true);
        String xmlValidationLevel = dasConfig.getDeployXmlValidation();
        archivist.setXMLValidationLevel(xmlValidationLevel);
        if (xmlValidationLevel.equals("none")) {
            archivist.setXMLValidation(false);
        }
        archivist.setRuntimeXMLValidationLevel(xmlValidationLevel);
        if (xmlValidationLevel.equals("none")) {
            archivist.setRuntimeXMLValidation(false);
        }
        Collection<Sniffer> sniffers = dc.getTransientAppMetaData(DeploymentProperties.SNIFFERS, Collection.class);
        archivist.setExtensionArchivists(archivistFactory.getExtensionsArchivists(sniffers, archivist.getModuleType()));

        ApplicationHolder holder = dc.getModuleMetaData(ApplicationHolder.class);
        File deploymentPlan = params.deploymentplan;
        handleDeploymentPlan(deploymentPlan, archivist, sourceArchive, holder);

        long start = System.currentTimeMillis();
        Application application = null;
        if (holder != null) {
            application = holder.app;

            application.setAppName(name);
            application.setClassLoader(cl);

            if (application.isVirtual()) {
                ModuleDescriptor<RootDeploymentDescriptor> md = application.getStandaloneBundleDescriptor()
                    .getModuleDescriptor();
                md.setModuleName(name);
            }

            try {
                applicationFactory.openWith(application, sourceArchive, archivist);
            } catch (SAXException e) {
                throw new IOException(e);
            }
        } else {
            // for case where user specified --name
            // and it's a standalone module
            try {
                application = applicationFactory.openArchive(name, archivist, sourceArchive, true);
                application.setAppName(name);
                ModuleDescriptor<RootDeploymentDescriptor> md = application.getStandaloneBundleDescriptor()
                    .getModuleDescriptor();
                md.setModuleName(name);
            } catch (SAXException e) {
                throw new IOException(e);
            }
        }

        application.setRegistrationName(name);

        sourceArchive.removeExtraData(Types.class);
        sourceArchive.removeExtraData(Parser.class);

        Logger.getAnonymousLogger().log(Level.FINE, "DOL Loading time: {0} ms", System.currentTimeMillis() - start);

        return application;
    }

    @Override
    public Application load(DeploymentContext dc) throws IOException {
        DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);
        Application application = processDOL(dc);

        // write out xml files if needed
        if (Boolean.parseBoolean(WRITEOUT_XML)) {
            saveAppDescriptor(application, dc);
        }

        if (application.isVirtual()) {
            dc.addModuleMetaData(application.getStandaloneBundleDescriptor());
            for (RootDeploymentDescriptor extension : application.getStandaloneBundleDescriptor().getExtensionsDescriptors()) {
                dc.addModuleMetaData(extension);
            }
        }

        addModuleConfig(dc, application);

        validateKeepStateOption(dc, params, application);

        return application;

    }

    /**
     * return the name for the given application
     */
    @Override
    public String getNameFor(ReadableArchive archive, DeploymentContext context) {
        if (context == null) {
            return null;
        }
        DeployCommandParameters params = context.getCommandParameters(DeployCommandParameters.class);
        Application application = null;
        try {
            // for these cases, the standard DD could contain the application
            // name for ear and module name for standalone module
            if (params.altdd != null ||
                archive.exists("META-INF/application.xml") ||
                archive.exists("WEB-INF/web.xml") ||
                archive.exists("META-INF/ejb-jar.xml") ||
                archive.exists("META-INF/application-client.xml") ||
                archive.exists("META-INF/ra.xml")) {
                String archiveType = context.getArchiveHandler().getArchiveType() ;
                application = applicationFactory.createApplicationFromStandardDD(archive, archiveType);
                DeploymentTracing tracing = null;
                tracing = context.getModuleMetaData(DeploymentTracing.class);
                if (tracing != null) {
                    tracing.addMark(DeploymentTracing.Mark.DOL_LOADED);
                }
                ApplicationHolder holder = new ApplicationHolder(application);
                context.addModuleMetaData(holder);

                return application.getAppName();
            }
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.WARNING, "Error occurred", e);
        }
        return null;
    }

    /**
     * This method populates the Application object from a ReadableArchive
     * @param archive the archive for the application
     */
    public Application processDeploymentMetaData(ReadableArchive archive) throws Exception {
        final ArchiveHandler archiveHandler = deployment.getArchiveHandler(archive);
        if (archiveHandler == null) {
            throw new IllegalArgumentException(localStrings.getLocalString("deploy.unknownarchivetype",
                "Archive type of {0} was not recognized", archive.getURI()));
        }

        final DeployCommandParameters parameters = new DeployCommandParameters(new File(archive.getURI()));
        final ActionReport report = new HTMLActionReporter();
        final ExtendedDeploymentContext context = new DeploymentContextImpl(report, archive, parameters, env);
        try {
            context.setArchiveHandler(archiveHandler);
            parameters.name = archiveHandler.getDefaultApplicationName(archive, context);
            return processDeployment(archive, archiveHandler, context);
        } finally {
            context.postDeployClean(true);
        }
    }


    private Application processDeployment(ReadableArchive archive, final ArchiveHandler archiveHandler,
        final ExtendedDeploymentContext context) throws IOException, URISyntaxException, MalformedURLException {
        final File tmpDirectory = prepareTmpDir(archive);
        try {
            if (tmpDirectory != null) {
                final WritableArchive expandedArchive = archiveFactory.createArchive(tmpDirectory);
                archiveHandler.expand(archive, expandedArchive, context);
                context.setSource((ReadableArchive) expandedArchive);
            }
            context.setPhase(DeploymentContextImpl.Phase.PREPARE);
            context.createDeploymentClassLoader(clhProvider.get(), archiveHandler);
            return processDeployment(archiveHandler, context);
        } finally {
            if (context.getSource() != null) {
                context.getSource().close();
            }
            if (tmpDirectory != null) {
                try {
                    FileUtils.whack(tmpDirectory);
                } catch (Exception e) {
                    Logger.getAnonymousLogger().log(Level.WARNING,
                        "Could not delete the temporary directory " + tmpDirectory, e);
                }
            }
        }
    }


    private File prepareTmpDir(final ReadableArchive archive) throws IOException {
        if (!(archive instanceof InputJarArchive)) {
            return null;
        }
        // we need to expand the archive first in this case
        final String archiveName = Util.getURIName(archive.getURI());
        final File tmpFile = File.createTempFile(archiveName, "");
        final String path = tmpFile.getAbsolutePath();
        if (!tmpFile.delete()) {
            Logger.getAnonymousLogger().log(Level.WARNING, "cannot.delete.temp.file", path);
        }
        final File tmpDir = new File(path);
        tmpDir.deleteOnExit();

        if (!tmpDir.exists() && !tmpDir.mkdirs()) {
            throw new IOException("Unable to create directory " + tmpDir.getAbsolutePath());
        }
        return tmpDir;
    }


    private Application processDeployment(final ArchiveHandler archiveHandler, final ExtendedDeploymentContext context)
        throws IOException {
        final ClassLoader cl = context.getClassLoader();
        try {
            deployment.getDeployableTypes(context);
            deployment.getSniffers(archiveHandler, null, context);
            return processDOL(context);
        } finally {
            if (cl instanceof PreDestroy) {
                try {
                    PreDestroy.class.cast(cl).preDestroy();
                } catch (Exception e) {
                    Logger.getAnonymousLogger().log(Level.WARNING,
                        "ClassLoader preDestroy failed for " + cl, e);
                }
            }
        }
    }


    protected void handleDeploymentPlan(File deploymentPlan, Archivist archivist, ReadableArchive sourceArchive,
        ApplicationHolder holder) throws IOException {
        if (deploymentPlan == null) {
            return;
        }
        try (DeploymentPlanArchive dpa = new DeploymentPlanArchive()) {
            dpa.setParentArchive(sourceArchive);
            dpa.open(deploymentPlan.toURI());
            // need to revisit for ear case
            WritableArchive targetArchive = archiveFactory.createArchive(sourceArchive.getURI());
            if (archivist instanceof ApplicationArchivist) {
                ((ApplicationArchivist) archivist).copyInto(holder.app, dpa, targetArchive, false);
            } else {
                archivist.copyInto(dpa, targetArchive, false);
            }
        }
    }


    protected void saveAppDescriptor(Application application, DeploymentContext context) throws IOException {
        if (application != null) {
            ReadableArchive archive = archiveFactory.openArchive(context.getSourceDir());
            boolean isMkdirs = context.getScratchDir("xml").mkdirs();
            if (isMkdirs) {
                WritableArchive archive2 = archiveFactory.createArchive(context.getScratchDir("xml"));
                descriptorArchivist.write(application, archive, archive2);

                // copy the additional webservice elements etc
                applicationArchivist.copyExtraElements(archive, archive2);
            } else {
                context.getLogger().log(Level.WARNING,
                    "Error in creating directory " + context.getScratchDir("xml").getAbsolutePath());
            }
        }
    }

    private void addModuleConfig(DeploymentContext dc,
        Application application) {
        DeployCommandParameters params = dc.getCommandParameters(DeployCommandParameters.class);
        if (!params.origin.isDeploy()) {
            return;
        }

        try {
            com.sun.enterprise.config.serverbeans.Application app_w = dc.getTransientAppMetaData(
                com.sun.enterprise.config.serverbeans.ServerTags.APPLICATION,
                com.sun.enterprise.config.serverbeans.Application.class);
            if (app_w != null) {
                if (application.isVirtual()) {
                    Module modConfig = app_w.createChild(Module.class);
                    app_w.getModule().add(modConfig);
                    modConfig.setName(application.getRegistrationName());
                } else {
                    for (ModuleDescriptor moduleDesc : application.getModules()) {
                        Module modConfig = app_w.createChild(Module.class);
                        app_w.getModule().add(modConfig);
                        modConfig.setName(moduleDesc.getArchiveUri());
                    }
                }
            }
        } catch (Exception e) {
            Logger.getAnonymousLogger().log(Level.WARNING, "failed to add the module config", e);
        }
    }

    private void validateKeepStateOption(DeploymentContext context, DeployCommandParameters params, Application app) {
        if ((params.keepstate != null && params.keepstate) ||
            app.getKeepState()) {
            if (!isDASTarget(context, params)) {
                // for non-DAS target, and keepstate is set to true either
                // through deployment option or deployment descriptor
                // explicitly set the deployment option to false
                params.keepstate = false;
                String warningMsg = localStrings.getLocalString("not.support.keepstate.in.cluster",
                    "Ignoring the keepstate setting: the keepstate option is only supported in developer profile and not cluster profile.");
                ActionReport subReport = context.getActionReport().addSubActionsReport();
                subReport.setActionExitCode(ActionReport.ExitCode.WARNING);
                subReport.setMessage(warningMsg);
                context.getLogger().log(Level.WARNING, warningMsg);
            }
        }
    }

    private boolean isDASTarget(DeploymentContext context, DeployCommandParameters params) {
        if (DeploymentUtils.isDASTarget(params.target)) {
            return true;
        } else if (DeploymentUtils.isDomainTarget(params.target)) {
            List<String> targets = context.getTransientAppMetaData(DeploymentProperties.PREVIOUS_TARGETS, List.class);
            if (targets == null) {
                targets = domain.getAllReferencedTargetsForApplication(params.name);
            }
            if (targets.size() == 1 && DeploymentUtils.isDASTarget(targets.get(0))) {
                return true;
            }
        }
        return false;
    }
}
