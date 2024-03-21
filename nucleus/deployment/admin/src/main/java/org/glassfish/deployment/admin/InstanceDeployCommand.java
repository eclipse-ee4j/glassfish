/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.api.ActionReport;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.internal.data.ApplicationInfo;
import org.glassfish.internal.deployment.Deployment;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import org.glassfish.internal.deployment.SnifferManager;
import org.glassfish.deployment.common.DeploymentUtils;
import com.sun.enterprise.deployment.deploy.shared.Util;
import org.glassfish.deployment.versioning.VersioningUtils;
import org.jvnet.hk2.annotations.Service;
import jakarta.inject.Inject;
import jakarta.inject.Named;


import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.config.Transaction;

import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.config.serverbeans.Server;
import com.sun.enterprise.config.serverbeans.Applications;
import com.sun.enterprise.config.serverbeans.Application;
import com.sun.enterprise.config.serverbeans.Domain;
import java.util.Collection;
import org.glassfish.api.admin.AccessRequired.AccessCheck;
import org.glassfish.api.admin.AdminCommandSecurity;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;


/**
 * The deploy command that runs on instance
 * @author hzhang_jn
 * @author tjquinn
 */
@Service(name="_deploy")
@PerLookup
@ExecuteOn(value={RuntimeType.INSTANCE})
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.POST,
        path="_deploy",
        description="_deploy")
})
public class InstanceDeployCommand extends InstanceDeployCommandParameters
        implements AdminCommand, AdminCommandSecurity.AccessCheckProvider {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(InstanceDeployCommand.class);

    @Inject
    Deployment deployment;

    @Inject
    SnifferManager snifferManager;

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    Applications applications;

    @Inject
    ServerEnvironment env;

    @Inject @Named(ServerEnvironment.DEFAULT_INSTANCE_NAME)
    protected Server server;

    @Inject
    private Domain domain;

    @Override
    public Collection<? extends AccessCheck> getAccessChecks() {
        final List<AccessCheck> accessChecks = new ArrayList<>();
        accessChecks.add(new AccessCheck(DeploymentCommandUtils.getTargetResourceNameForNewAppRef(domain, target), "write"));
        return accessChecks;
    }


    @Override
    public void execute(AdminCommandContext ctxt) {

        long operationStartTime = Calendar.getInstance().getTimeInMillis();
        final ActionReport report = ctxt.getActionReport();
        final Logger logger = ctxt.getLogger();
        ReadableArchive archive = null;

        this.origin = Origin.deploy_instance;
        this.command = Command._deploy;

        this.previousContextRoot = preservedcontextroot;

        if (previousVirtualServers != null) {
            String vs = previousVirtualServers.getProperty(target);
            if (vs != null) {
                this.virtualservers = vs;
            }
        }

        if (previousEnabledAttributes != null) {
            String enabledAttr = previousEnabledAttributes.getProperty(target);
            if (enabledAttr != null) {
                String enabledAttrForApp = previousEnabledAttributes.getProperty(DeploymentUtils.DOMAIN_TARGET_NAME);
                this.enabled = Boolean.valueOf(enabledAttr) && Boolean.valueOf(enabledAttrForApp);
            }
        }

        try {
            if (!path.exists()) {
                report.setMessage(localStrings.getLocalString("fnf","File not found", path.getAbsolutePath()));
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return;
            }

            if (snifferManager.hasNoSniffers()) {
                String msg = localStrings.getLocalString("nocontainer", "No container services registered, done...");
                report.failure(logger,msg);
                return;
            }

            archive = archiveFactory.openArchive(path, this);
            ArchiveHandler archiveHandler = deployment.getArchiveHandler(archive, type);
            if (archiveHandler==null) {
                report.failure(logger,localStrings.getLocalString("deploy.unknownarchivetype","Archive type of {0} was not recognized",path.getName()));
                return;
            }

            // clean up any left over repository files
            if ( ! keepreposdir.booleanValue()) {
                FileUtils.whack(new File(env.getApplicationRepositoryPath(), VersioningUtils.getRepositoryName(name)));
            }

            ExtendedDeploymentContext deploymentContext = deployment.getBuilder(logger, this, report).source(archive).build();

            // clean up any remaining generated files
            deploymentContext.clean();

            deploymentContext.getAppProps().putAll(appprops);

            processGeneratedContent(generatedcontent, deploymentContext, logger);

            Transaction t = null;
            Application application = applications.getApplication(name);
            if (application != null) {
                // application element already been synchronized over
                t = new Transaction();
            } else {
                t = deployment.prepareAppConfigChanges(deploymentContext);
            }

            ApplicationInfo appInfo;
            appInfo = deployment.deploy(deploymentContext);

            if (report.getActionExitCode()==ActionReport.ExitCode.SUCCESS) {
                try {
                    moveAltDDFilesToPermanentLocation(deploymentContext, logger);
                    // register application information in domain.xml
                    if (application != null)  {
                        // application element already synchronized over
                        // just write application-ref
                        deployment.registerAppInDomainXML(appInfo, deploymentContext, t, true);
                    } else {
                        // write both application and application-ref
                        deployment.registerAppInDomainXML(appInfo, deploymentContext, t);
                    }
                } catch (Exception e) {
                    // roll back the deployment and re-throw the exception
                    deployment.undeploy(name, deploymentContext);
                    deploymentContext.clean();
                    throw e;
                }
            }
        } catch (Throwable e) {
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setMessage(e.getMessage());
            report.setFailureCause(e);
        } finally {
            try {
                if (archive != null)  {
                    archive.close();
                }
            } catch(IOException e) {
                logger.log(Level.INFO, localStrings.getLocalString(
                        "errClosingArtifact",
                        "Error while closing deployable artifact : ",
                        path.getAbsolutePath()), e);
            }
            if (report.getActionExitCode().equals(ActionReport.ExitCode.SUCCESS)) {
                logger.info(localStrings.getLocalString(
                        "deploy.done",
                        "Deployment of {0} done is {1} ms",
                        name,
                        (Calendar.getInstance().getTimeInMillis() - operationStartTime)));
            } else if (report.getActionExitCode().equals(ActionReport.ExitCode.FAILURE)) {
                String errorMessage = report.getMessage();
                Throwable cause = report.getFailureCause();
                if (cause != null) {
                    String causeMessage = cause.getMessage();
                    if (causeMessage != null &&
                        !causeMessage.equals(errorMessage)) {
                        errorMessage = errorMessage + " : " + cause.getMessage();
                    }
                    logger.log(Level.SEVERE, errorMessage, cause.getCause());
                }
                report.setMessage(localStrings.getLocalString(
                    "failToLoadOnInstance",
                    "Failed to load the application on instance {0} : {1}", server.getName(), errorMessage));
                // reset the failure cause so command framework will not try
                // to print the same message again
                report.setFailureCause(null);
            }

        }

    }

    private void processGeneratedContent(
            final File generatedContentParam,
            final ExtendedDeploymentContext deploymentContext,
            final Logger logger) throws IOException {
        if (generatedContentParam == null) {
            return;
        }

        final File baseDir = deploymentContext.getScratchDir("xml").getParentFile().getParentFile();
        if ( ! baseDir.exists() && ! baseDir.mkdirs()) {
            throw new IOException(localStrings.getLocalString("instancedeploy.command.errcredir",
                        "Error creating directory {0}.  No further information about the failure is available.", baseDir.getAbsolutePath()));
        }

        final URI baseURI = baseDir.toURI();
        final ZipFile zipFile = new ZipFile(generatedContentParam);
        try {
            for (Enumeration<? extends ZipEntry> entries = zipFile.entries(); entries.hasMoreElements();) {
                final ZipEntry zipEntry = entries.nextElement();
                final URI outputFileURI = Util.resolve(baseURI, zipEntry.getName());
                final File outputFile = new File(outputFileURI);
                if (zipEntry.isDirectory()) {
                    if (!outputFile.exists() && !outputFile.mkdirs()) {
                        throw new IOException(localStrings.getLocalString("instancedeploy.command.errcredir",
                            "Error creating directory {0}.  No further information about the failure is available.",
                            baseDir.getAbsolutePath()));
                    }
                } else {
                    try (InputStream inputStream = zipFile.getInputStream(zipEntry)) {
                        FileUtils.copy(inputStream, outputFile, zipEntry.getSize());
                    }
                }
            }
        } finally {
            zipFile.close();
        }
    }

    /**
     * Makes safe copies of the alternate dd, runtime alternate dd for
     * loading altdd and runtime altdd during enable application on instance
     * and loading application during instance start up.
     * <p>
     * We rename any uploaded files from the temp directory to the permanent
     * place, and we copy any archive files that were not uploaded.  This
     * prevents any confusion that could result if the developer modified the
     * archive file - changing its lastModified value - before redeploying it.
     *
     * @param deploymentContext
     * @param logger logger
     * @throws IOException
     */
    private void moveAltDDFilesToPermanentLocation(
            final ExtendedDeploymentContext deploymentContext,
            final Logger logger) throws IOException {
        final File finalAltDDDir = deploymentContext.getAppAltDDDir();
        if ( ! finalAltDDDir.mkdirs()) {
            logger.log(Level.FINE," Attempting to create directory {0} was reported as failed; attempting to continue",
                    new Object[] {finalAltDDDir.getAbsolutePath()});
        }
        DeploymentCommandUtils.renameUploadedFileOrCopyInPlaceFile( finalAltDDDir, altdd, logger, env);
        DeploymentCommandUtils.renameUploadedFileOrCopyInPlaceFile( finalAltDDDir, runtimealtdd, logger, env);
    }
}
