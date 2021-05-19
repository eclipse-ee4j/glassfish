/*
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deploy.shared.ArchiveFactory;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;

import com.sun.enterprise.deployment.io.ApplicationDeploymentDescriptorFile;
import org.glassfish.deployment.common.ModuleDescriptor;
import com.sun.enterprise.deployment.util.ApplicationVisitor;
import com.sun.enterprise.deployment.util.ApplicationValidator;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.config.serverbeans.DasConfig;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.deployment.archive.ReadableArchive;
import jakarta.inject.Inject;

import org.jvnet.hk2.annotations.Service;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;

/**
 * Factory for application object
 *
 * @author Jerome Dochez
 */
@Service
public class ApplicationFactory {

    @Inject
    ArchiveFactory archiveFactory;

    @Inject
    ArchivistFactory archivistFactory;

    @Inject
    DasConfig dasConfig;

    protected static final Logger logger =
            DOLUtils.getDefaultLogger();

    // resources...
    private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(Archivist.class);

    /**
     * Open a jar file and return an application object for the modules contained
     * in the archive. If the archive is a standalone module, this API will
     * create an empty application and add the standalone module to it
     *
     * @param jarFile the archive file
     * @return the application object
     */
    public Application openArchive(URI jarFile, String archiveType)
            throws IOException, SAXException {

        return openArchive(jarFile, archiveType, false);
    }

    /**
     * Open a jar file and return an application object for the modules contained
     * in the archive/directory. If the archive/directory is a standalone module, this API will
     * create an empty application and add the standalone module to it
     *
     * @param archivist         to use to open the archive file
     * @param jarFile           the archive file
     * @param handleRuntimeInfo set to true to read configuration deployment descriptors
     * @return the application object
     */
    public Application openArchive(Archivist archivist, URI jarFile, boolean handleRuntimeInfo)
            throws IOException, SAXException {

        // never read the runtime deployment descriptor before the
        // module type is found and the application object created

        ReadableArchive archive = archiveFactory.openArchive(jarFile);
        Application application = openArchive(archivist, archive, handleRuntimeInfo);
        archive.close();
        return application;
    }

    /**
     * Open a jar file and return an application object for the modules contained
     * in the archive. If the archive is a standalone module, this API will
     * create an empty application and add the standalone module to it
     *
     * @param archivist         to use to open the archive file
     * @param in                the archive abstraction
     * @param handleRuntimeInfo true to read configuration deployment descriptors
     * @return the application object
     */

    public Application openArchive(Archivist archivist, ReadableArchive in, boolean handleRuntimeInfo)
            throws IOException, SAXException {

        return openArchive(in.getURI().getSchemeSpecificPart(), archivist, in, handleRuntimeInfo);
    }

    /**
     * Open a jar file and return an application object for the modules contained
     * in the archive. If the archive is a standalone module, this API will
     * create an empty application and add the standalone module to it
     *
     * @param appName           the application moduleID
     * @param archivist         to use to open the archive file
     * @param in                the input archive
     * @param handleRuntimeInfo set to true to read configuration deployment descriptors
     * @return the application object
     */

    public Application openArchive(String appName, Archivist archivist, ReadableArchive in, boolean handleRuntimeInfo)
            throws IOException, SAXException {
        // we are not reading the runtime deployment descriptor now...
        archivist.setHandleRuntimeInfo(false);

        BundleDescriptor descriptor = archivist.open(in);
        Application application;
        if (descriptor instanceof Application) {
            application = (Application) descriptor;
            application.setAppName(appName);
            application.setRegistrationName(appName);
        } else {
            if (descriptor == null) {
                logger.log(Level.SEVERE, localStrings.getLocalString(
                        "enterprise.deployment.cannotreadDDs",
                        "Cannot read the Deployment Descriptors for module {0}",
                        new Object[]{in.getURI()}));
                return null;
            }
            ModuleDescriptor newModule = archivist.createModuleDescriptor(descriptor);
            newModule.setArchiveUri(in.getURI().getSchemeSpecificPart());
            application = Application.createVirtualApplication(appName,newModule);
        }

        // now read the runtime deployment descriptor
        if (handleRuntimeInfo) {
            // now read the runtime deployment descriptors from the original jar file
            archivist.setHandleRuntimeInfo(true);
            archivist.readRuntimeDeploymentDescriptor(in, (BundleDescriptor)descriptor);
        }

        // validate
         application.setClassLoader(archivist.getClassLoader());
         application.visit(new ApplicationValidator());


        return application;

    }

    /**
     * This method creates an Application object from reading the
     * standard deployment descriptor.
     * @param archive the archive for the application
     */
    public Application createApplicationFromStandardDD(
        ReadableArchive archive, String archiveType) throws IOException, SAXException {
        Archivist archivist = archivistFactory.getArchivist(archiveType, null);
        String xmlValidationLevel = dasConfig.getDeployXmlValidation();
        archivist.setXMLValidationLevel(xmlValidationLevel);
        if (xmlValidationLevel.equals("none")) {
            archivist.setXMLValidation(false);
        }
        BundleDescriptor desc = archivist.readStandardDeploymentDescriptor(archive);
        Application application = null;
        if (desc instanceof Application) {
            application = (Application)desc;
        } else {
            ModuleDescriptor newModule = archivist.createModuleDescriptor(desc);
            newModule.setArchiveUri(archive.getURI().getSchemeSpecificPart());
            String moduleName = newModule.getModuleName();
            application = Application.createVirtualApplication(moduleName, newModule);
        }
        return application;
    }


    /**
     * This method populates the rest of the Application object from the
     * previous standard deployment descriptor reading
     * @param archive the archive for the application
     */
    public Application openWith(Application application,
        ReadableArchive archive, Archivist archivist)
        throws IOException, SAXException {
        archivist.openWith(application, archive);
        // validate
        if (application.isVirtual()) {
            application.setClassLoader(archivist.getClassLoader());
            application.visit((ApplicationVisitor) new ApplicationValidator());
        }
        return application;
    }


    /**
     * Open a jar file with the default Archivists and return an application
     * object for the modules contained in the archive.
     * If the archive is a standalone module, this API will
     * create an empty application and add the standalone module to it
     *
     * @param jarFile the  archive file
     * @param handleRuntimeInfo set to true to read configuration deployment descriptors
     * @return the application object
     */
    public Application openArchive(URI jarFile, String archiveType, boolean handleRuntimeInfo)
            throws IOException, SAXException {
        Archivist archivist = archivistFactory.getArchivist(archiveType);
        return openArchive(archivist, jarFile, handleRuntimeInfo);
    }

    /**
     * @param jarFile the .ear file
     * @return the application name from an application .ear file
     */
    public String getApplicationName(File jarFile) throws IOException {

        if (!jarFile.exists()) {
            throw new IOException(localStrings.getLocalString(
                    "enterprise.deployment.exceptionjarfiledoesn'texist",
                    "{0} does not exist", new Object[]{jarFile}));
        }

        /*
        *Add finally clause containing explicit close of jar file.
        */
        JarFile jar = null;
        try {
            jar = new JarFile(jarFile);
            ApplicationDeploymentDescriptorFile node = new ApplicationDeploymentDescriptorFile();
            node.setXMLValidation(false);
            ZipEntry deploymentEntry = jar.getEntry(node.getDeploymentDescriptorPath());
            if (deploymentEntry != null) {
                try {
                    Application application = (Application) node.read(jar.getInputStream(deploymentEntry));
                    return application.getDisplayName();
                } catch (Exception pe) {
                    logger.log(Level.WARNING, "Error occurred", pe);
                }
            }
        } finally {
            if (jar != null) {
                jar.close();
            }
        }
        return null;
     }
}
