/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.annotation.impl.ModuleScanner;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.util.DOLUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.api.deployment.archive.WritableArchiveEntry;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.jvnet.hk2.annotations.Contract;
import org.xml.sax.SAXException;

/**
 * An extension archivist is processing extensions deployment descriptors like
 * web services, persistence or even EJB information within a war file.
 *
 * They do not represent a top level archivist, as it is not capable of loading
 * BundleDescriptors directly but require a top level archivist to do so before
 * they can process their own metadata
 *
 * @author Jerome Dochez
 */
@Contract
public abstract class ExtensionsArchivist<T extends RootDeploymentDescriptor>  {

    private static final Logger LOG = DOLUtils.deplLogger;

    /** standard DD file associated with this archivist */
    // FIXME: Use T? Fix issues with compatibility
    protected DeploymentDescriptorFile<RootDeploymentDescriptor> standardDD;

    /** configuration DD files associated with this archivist */
    protected List<ConfigurationDeploymentDescriptorFile> confDDFiles;

    /** the sorted configuration DD files with precedence from high to low */
    private List<ConfigurationDeploymentDescriptorFile> sortedConfDDFiles;

    /** configuration DD file that will be used */
    private ConfigurationDeploymentDescriptorFile confDD;

    /**
     * @return the DeploymentDescriptorFile responsible for handling
     *         standard deployment descriptor
     */
    public abstract DeploymentDescriptorFile getStandardDDFile(RootDeploymentDescriptor descriptor);

    /**
     * @return the list of the DeploymentDescriptorFile responsible for
     *         handling the configuration deployment descriptors
     */
    public abstract List<ConfigurationDeploymentDescriptorFile> getConfigurationDDFiles(
        RootDeploymentDescriptor descriptor);


    /**
     * @return if exists the DeploymentDescriptorFile responsible for
     *         handling the configuration deployment descriptors
     */
    public ConfigurationDeploymentDescriptorFile getConfigurationDDFile(Archivist main,
        RootDeploymentDescriptor descriptor, ReadableArchive archive) throws IOException {
        if (confDD == null) {
            getSortedConfigurationDDFiles(descriptor, archive, main.getModuleType());
            if (sortedConfDDFiles != null && !sortedConfDDFiles.isEmpty()) {
                confDD = sortedConfDDFiles.get(0);
            }
        }
        return confDD;
    }


    /**
     * @param moduleType
     * @return whether this extension archivist supports this module type
     */
    public abstract boolean supportsModuleType(ArchiveType moduleType);

    /**
     * @return a default Descriptor for this archivist
     */
    public abstract <T extends RootDeploymentDescriptor> T getDefaultDescriptor();


    /**
     * @return null
     */
    public <T extends RootDeploymentDescriptor> ModuleScanner<T> getScanner() {
        return null;
    }


    /**
     * Add the extension descriptor to the main descriptor
     *
     * @param root the main descriptor
     * @param extension the extension descriptor
     */
    public void addExtension(RootDeploymentDescriptor root, RootDeploymentDescriptor extension) {
        root.addExtensionDescriptor(extension.getClass(), extension, null);
        extension.setModuleDescriptor(root.getModuleDescriptor());
    }


    /**
     * Read the standard deployment descriptor of the extension
     *
     * @param main the primary archivist for this archive
     * @param archive the archive
     * @param descriptor the main deployment descriptor
     * @return the extension descriptor object
     */
    public RootDeploymentDescriptor open(Archivist<?> main, ReadableArchive archive, RootDeploymentDescriptor descriptor)
        throws IOException, SAXException {
        getStandardDDFile(descriptor).setArchiveType(main.getModuleType());
        if (archive.getURI() != null) {
            standardDD.setErrorReportingString(archive.getURI().getSchemeSpecificPart());
        }
        try (InputStream is = archive.getEntry(standardDD.getDeploymentDescriptorPath())) {
            if (is == null) {
                if (LOG.isLoggable(Level.CONFIG)) {
                    LOG.log(Level.CONFIG, "Deployment descriptor: " + standardDD.getDeploymentDescriptorPath()
                        + " does not exist in archive: " + archive.getURI());
                }
                return null;
            }
            standardDD.setXMLValidation(main.getXMLValidation());
            standardDD.setXMLValidationLevel(main.getXMLValidationLevel());
            return standardDD.read(descriptor, is);
        }
    }


    /**
     * Read the runtime deployment descriptors of the extension
     *
     * @param main the primary archivist for this archive
     * @param archive the archive
     * @param descriptor the extension deployment descriptor
     * @return the extension descriptor object with additional runtime information
     */
    public RootDeploymentDescriptor readRuntimeDeploymentDescriptor(Archivist main, ReadableArchive archive,
        RootDeploymentDescriptor descriptor) throws IOException, SAXException {
        ConfigurationDeploymentDescriptorFile<?> ddFile = getConfigurationDDFile(main, descriptor, archive);

        // if this extension archivist has no runtime DD, just return the
        // original descriptor
        if (ddFile == null) {
            return descriptor;
        }

        DOLUtils.readRuntimeDeploymentDescriptor(
            getSortedConfigurationDDFiles(descriptor, archive, main.getModuleType()), archive, descriptor, main, true);

        return descriptor;
    }


    /**
     * writes the deployment descriptors (standard and runtime)
     * to a JarFile using the right deployment descriptor path
     *
     * @param in the input archive
     * @param out the abstract archive file to write to
     */
    public void writeDeploymentDescriptors(Archivist main, BundleDescriptor descriptor, ReadableArchive in,
        WritableArchive out) throws IOException {
        // Standard DDs
        writeStandardDeploymentDescriptors(main, descriptor, out);

        // Runtime DDs
        writeRuntimeDeploymentDescriptors(main, descriptor, in, out);
    }

    /**
     * writes the standard deployment descriptors to an abstract archive
     *
     * @param out archive to write to
     */
    public void writeStandardDeploymentDescriptors(Archivist main, BundleDescriptor descriptor, WritableArchive out) throws IOException {

        getStandardDDFile(descriptor).setArchiveType(main.getModuleType());
        try (WritableArchiveEntry os = out.putNextEntry(standardDD.getDeploymentDescriptorPath())) {
            standardDD.write(descriptor, os);
        }
    }

    /**
     * writes the runtime deployment descriptors to an abstract archive
     *
     * @param in the input archive
     * @param out output archive
     */
    public void writeRuntimeDeploymentDescriptors(Archivist main, BundleDescriptor descriptor, ReadableArchive in, WritableArchive out) throws IOException {

        // when source archive contains runtime deployment descriptor
        // files, write those out
        // otherwise write all possible runtime deployment descriptor
        // files out
        List<ConfigurationDeploymentDescriptorFile> confDDFilesToWrite = getSortedConfigurationDDFiles(descriptor, in, main.getModuleType());
        if (confDDFilesToWrite.isEmpty()) {
            confDDFilesToWrite = getConfigurationDDFiles(descriptor);
        }
        for (ConfigurationDeploymentDescriptorFile<BundleDescriptor> ddFile : confDDFilesToWrite) {
            ddFile.setArchiveType(main.getModuleType());
            try (WritableArchiveEntry os = out.putNextEntry(ddFile.getDeploymentDescriptorPath())) {
                ddFile.write(descriptor, os);
            }
        }
    }

    private List<ConfigurationDeploymentDescriptorFile> getSortedConfigurationDDFiles(RootDeploymentDescriptor descriptor, ReadableArchive archive, ArchiveType archiveType) throws IOException {
        if (sortedConfDDFiles == null) {
            sortedConfDDFiles = DOLUtils.processConfigurationDDFiles(getConfigurationDDFiles(descriptor), archive, archiveType);
        }
        return sortedConfDDFiles;
    }
}
