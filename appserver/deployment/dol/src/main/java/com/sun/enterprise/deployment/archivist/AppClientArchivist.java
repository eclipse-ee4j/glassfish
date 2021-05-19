/*
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

package com.sun.enterprise.deployment.archivist;

import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.ApplicationClientDescriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import com.sun.enterprise.deployment.io.AppClientDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.runtime.AppClientRuntimeDDFile;
import com.sun.enterprise.deployment.io.runtime.GFAppClientRuntimeDDFile;
import com.sun.enterprise.deployment.util.AppClientValidator;
import com.sun.enterprise.deployment.util.DOLUtils;
import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.Map;
import java.util.Vector;
import java.util.List;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.logging.Level;

/**
 * This class is responsible for handling J2EE app client files.
 *
 * @author Sheetal Vartak
 */
@Service
@PerLookup
//TODO change to CarType.ARCHIVE_TYPE once moved to appclient module
@ArchivistFor("car")
public class AppClientArchivist extends Archivist<ApplicationClientDescriptor> {

    public static final Attributes.Name GLASSFISH_APPCLIENT =
            new Attributes.Name("GlassFish-AppClient");

    public static final Attributes.Name GLASSFISH_CLIENT_PU_SCAN_TARGETS_NAME =
            new Attributes.Name("GlassFish-Client-PersistenceUnit-Scan-Targets");

    public static final Attributes.Name GLASSFISH_GROUP_FACADE =
            new Attributes.Name("GlassFish-Group-Facade");

    public static final Attributes.Name GLASSFISH_ANCHOR_DIR =
            new Attributes.Name("GlassFish-Anchor");

    private String mainClassNameToRun = null;

    /**
     * Creates new ApplicationClientArchvisit
     */
    public AppClientArchivist() {
        handleRuntimeInfo = true;
    }

    /**
     * @return the  module type handled by this archivist
     *         as defined in the application DTD
     */
    @Override
    public ArchiveType getModuleType() {
        return DOLUtils.carType();
    }

    public ApplicationClientDescriptor open(final ReadableArchive archive,
            final String mainClassNameToRun) throws IOException, SAXException {
        this.mainClassNameToRun = mainClassNameToRun;
        return super.open(archive);
    }

    public void setDescriptor(Application application) {

        // this is acceptable if the application actually represents
        // a standalone module
        java.util.Set appClientBundles = application.getBundleDescriptors(ApplicationClientDescriptor.class);
        if (appClientBundles.size() > 0) {
            this.descriptor = (ApplicationClientDescriptor) appClientBundles.iterator().next();
            if (this.descriptor.getModuleDescriptor().isStandalone())
                return;
            else
                this.descriptor = null;
        }
        DOLUtils.getDefaultLogger().log(Level.SEVERE, "enterprise.deployment.backend.descriptorFailure", new Object[]{this});
        throw new RuntimeException("Error setting descriptor " + descriptor + " in " + this);
    }

    /**
     * @return the DeploymentDescriptorFile responsible for handling
     *         standard deployment descriptor
     */
    @Override
    public DeploymentDescriptorFile<ApplicationClientDescriptor> getStandardDDFile() {
         if (standardDD == null) {
             standardDD = new AppClientDeploymentDescriptorFile();
         }

         return standardDD;
    }

    /**
     * @return the list of the DeploymentDescriptorFile responsible for
     *         handling the configuration deployment descriptors
     */
    public List<ConfigurationDeploymentDescriptorFile> getConfigurationDDFiles() {
        if (confDDFiles == null) {
            confDDFiles = new ArrayList<ConfigurationDeploymentDescriptorFile>();
            confDDFiles.add(new GFAppClientRuntimeDDFile());
            confDDFiles.add(new AppClientRuntimeDDFile());
        }
        return confDDFiles;
    }

    /**
     * @return a default BundleDescriptor for this archivist
     */
    @Override
    public ApplicationClientDescriptor getDefaultBundleDescriptor() {
        ApplicationClientDescriptor appClientDesc =
                new ApplicationClientDescriptor();
        return appClientDesc;
    }

    /**
     * validates the DOL Objects associated with this archivist, usually
     * it requires that a class loader being set on this archivist or passed
     * as a parameter
     */
    @Override
    public void validate(ClassLoader aClassLoader) {
        ClassLoader cl = aClassLoader;
        if (cl == null) {
            cl = classLoader;
        }
        if (cl == null) {
            return;
        }
        descriptor.setClassLoader(cl);
        descriptor.visit(new AppClientValidator());
    }

    /**
     * perform any action after all standard DDs is read
     *
     * @param descriptor the deployment descriptor for the module
     * @param archive    the module archive
     * @param extensions map of extension archivists
     */
    @Override
    protected void postStandardDDsRead(ApplicationClientDescriptor descriptor, ReadableArchive archive,
                Map<ExtensionsArchivist, RootDeploymentDescriptor> extensions)
                throws IOException {
        super.postStandardDDsRead(descriptor, archive, extensions);
        // look for MAIN_CLASS
        if (mainClassNameToRun == null) {
            Manifest m = archive.getManifest();
            mainClassNameToRun = getMainClassName(m);
        }
        descriptor.setMainClassName(mainClassNameToRun);
    }

    /**
     * perform any post deployment descriptor reading action
     *
     * @param descriptor the deployment descriptor for the module
     * @param archive    the module archive
     */
    @Override
    protected void postOpen(ApplicationClientDescriptor descriptor, ReadableArchive archive)
            throws IOException {

        super.postOpen(descriptor, archive);

        postValidate(descriptor, archive);
    }

    /**
     * writes the content of an archive to a JarFile
     *
     * @param in            the input  archive
     * @param out           the archive output stream to write to
     * @param entriesToSkip the files to not write from the original archive
     */
    @Override
    protected void writeContents(ReadableArchive in, WritableArchive out, Vector entriesToSkip)
            throws IOException {

        // prepare the manifest file to add the main class entry
        if (manifest == null) {
            manifest = new Manifest();
        }
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, MANIFEST_VERSION_VALUE);
        manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS,
                ((ApplicationClientDescriptor) getDescriptor()).getMainClassName());

        super.writeContents(in, out, entriesToSkip);
    }

    /**
     * @return the manifest attribute Main-class
     */
    public String getMainClassName(Manifest m) {
        if (m != null) {
            return m.getMainAttributes().getValue(Attributes.Name.MAIN_CLASS);
        }
        return null;
    }

    @Override
    protected boolean postHandles(ReadableArchive archive)
            throws IOException {
        //check the main-class attribute
        if (getMainClassName(archive.getManifest()) != null) {
            return true;
        }

        return false;
    }

    protected String getArchiveExtension() {
        return APPCLIENT_EXTENSION;
    }
}
