/*
 * Copyright (c) 2009, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.tools.verifier;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.ArchiveHandler;
import org.glassfish.api.deployment.archive.Archive;
import org.glassfish.api.deployment.DeployCommandParameters;
import org.glassfish.api.admin.ServerEnvironment;
import org.glassfish.internal.deployment.Deployment;
import com.sun.enterprise.deployment.archivist.Archivist;
import com.sun.enterprise.deployment.archivist.ArchivistFactory;
import com.sun.enterprise.deployment.archivist.ApplicationFactory;
import com.sun.enterprise.config.serverbeans.DasConfig;
import com.sun.enterprise.deployment.Application;
import org.glassfish.deployment.common.Descriptor;
import com.sun.enterprise.deployment.util.ApplicationVisitor;
import com.sun.enterprise.deployment.util.ApplicationValidator;
import com.sun.enterprise.util.io.FileUtils;
import org.glassfish.deployment.common.DeploymentContextImpl;
import org.glassfish.deployment.common.DeploymentUtils;
import org.glassfish.internal.deployment.ExtendedDeploymentContext;
import com.sun.enterprise.v3.common.HTMLActionReporter;
import org.glassfish.api.ActionReport;

import org.jvnet.hk2.annotations.Service;
import javax.inject.Inject;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;

/**
 * @author Hong.Zhang@Sun.COM
 * @author Sanjeeb.Sahoo@Sun.COM
 */
@Service
public class DescriptorFactory
{
    public static class ResultHolder {
        Application application;
        Archive archive;
    }

    @Inject 
    Deployment deployment;

    @Inject
    protected ArchiveFactory archiveFactory;

    @Inject
    ArchivistFactory archivistFactory;

    @Inject
    protected ApplicationFactory applicationFactory;

    @Inject
    DasConfig dasConfig;

    @Inject
    ServerEnvironment env;

    /**
     * Returns the parsed DOL object from archive
     *
     * @param archiveFile original archive file
     * @param destRootDir root destination directory where the application
     *        should be expanded under in case of archive deployment
     * @param parentCl parent classloader
     *
     * @return the parsed DOL object
     */
    public ResultHolder createApplicationDescriptor(File archiveFile, File destRootDir, ClassLoader parentCl) throws IOException {
        ReadableArchive archive = null;
        Application application = null;
        try {
            Descriptor.setBoundsChecking(false);
            archive = archiveFactory.openArchive(archiveFile);
            ArchiveHandler archiveHandler = deployment.getArchiveHandler(archive);
            ActionReport dummyReport = new HTMLActionReporter();

            String appName = DeploymentUtils.getDefaultEEName(archiveFile.getName());

            DeployCommandParameters params = new DeployCommandParameters();
            params.name = appName;

            ExtendedDeploymentContext context = new DeploymentContextImpl(dummyReport, archive, params, env);
            context.setArchiveHandler(archiveHandler);

            if (!archiveFile.isDirectory()) {
                // expand archive
                File destDir = new File(destRootDir, appName);
                if (destDir.exists()) {
                    FileUtils.whack(destDir);
                }
                destDir.mkdirs();
                archiveHandler.expand(archive, archiveFactory.createArchive(destDir), context);
                archive.close();
                archive = archiveFactory.openArchive(destDir);
                context.setSource(archive);
            }

            context.addTransientAppMetaData(ExtendedDeploymentContext.IS_TEMP_CLASSLOADER, Boolean.TRUE); // issue 14564
            String archiveType = context.getArchiveHandler().getArchiveType();
            ClassLoader cl = archiveHandler.getClassLoader(parentCl, context);
            Archivist archivist = archivistFactory.getArchivist(archiveType, cl);
            if (archivist == null) {
                throw new IOException("Cannot determine the Java EE module type for " + archive.getURI());
            }
            archivist.setAnnotationProcessingRequested(true);
            String xmlValidationLevel = dasConfig.getDeployXmlValidation();
            archivist.setXMLValidationLevel(xmlValidationLevel);
            if (xmlValidationLevel.equals("none")) {
                archivist.setXMLValidation(false);
            }
            archivist.setRuntimeXMLValidation(false);
            try {
                application = applicationFactory.openArchive(
                        appName, archivist, archive, true);
            } catch(SAXException e) {
                throw new IOException(e);
            }
            if (application != null) {
                application.setClassLoader(cl);
                application.visit((ApplicationVisitor) new ApplicationValidator());
            }
        } finally {
            if (archive != null) {
                archive.close();
            }
            // We need to reset it after descriptor building
            Descriptor.setBoundsChecking(true);
        }

        ResultHolder result = new ResultHolder();
        result.application = application;
        result.archive = archive;
        return result;
    }

}
