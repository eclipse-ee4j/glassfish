/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.deploy.shared.FileArchive;
import com.sun.enterprise.deployment.Application;
import com.sun.enterprise.deployment.BundleDescriptor;
import com.sun.enterprise.deployment.EarType;
import com.sun.enterprise.deployment.EjbBundleDescriptor;
import com.sun.enterprise.deployment.WebBundleDescriptor;
import com.sun.enterprise.deployment.annotation.introspection.EjbComponentAnnotationScanner;
import com.sun.enterprise.deployment.io.ApplicationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.ConfigurationDeploymentDescriptorFile;
import com.sun.enterprise.deployment.io.DeploymentDescriptorFile;
import com.sun.enterprise.deployment.util.AnnotationDetector;
import com.sun.enterprise.deployment.util.ApplicationValidator;
import com.sun.enterprise.deployment.util.DOLUtils;
import com.sun.enterprise.util.LocalStringManagerImpl;
import com.sun.enterprise.util.io.FileUtils;

import jakarta.inject.Inject;
import jakarta.inject.Provider;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;

import org.glassfish.api.deployment.archive.ArchiveType;
import org.glassfish.api.deployment.archive.EarArchiveType;
import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.api.deployment.archive.WritableArchive;
import org.glassfish.deployment.common.ModuleDescriptor;
import org.glassfish.deployment.common.RootDeploymentDescriptor;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;
import org.xml.sax.SAXException;

import static com.sun.enterprise.deployment.util.DOLUtils.ejbType;

/**
 * This class is responsible for handling application archive files
 *
 * @author  Jerome Dochez
 */
@Service
@PerLookup
@ArchivistFor(EarArchiveType.ARCHIVE_TYPE)
public class ApplicationArchivist extends Archivist<Application> {
    @Inject
    Provider<ArchivistFactory> archivistFactory;

    /** resources... */
    private static LocalStringManagerImpl localStrings =
        new LocalStringManagerImpl(ApplicationArchivist.class);

    /** Creates new ApplicationArchivist */
    public ApplicationArchivist() {
        handleRuntimeInfo = true;
    }

    /**
     * @return the  module type handled by this archivist
     * as defined in the application DTD
     *
     */
    @Override
    public ArchiveType getModuleType() {
        return DOLUtils.earType();
    }


    /**
     * writes the content of an archive to a JarFile
     *
     * @param in the descriptors to use for writing
     * @param out the output stream to write to
     */
    @Override
    protected void writeContents(ReadableArchive in, WritableArchive out) throws IOException {

        Set<String> filesToSkip = new HashSet<>();
        logger.log(Level.FINE, "writeContents(in={0}, out={1})", new Object[] {in, out});

        // any files already written to the output should never be rewritten
        for (Enumeration<String> alreadyWritten = out.entries(); alreadyWritten.hasMoreElements();) {
            String elementName = alreadyWritten.nextElement();
            filesToSkip.add(elementName);
        }

        // write this application .ear file contents...
        for (ModuleDescriptor<BundleDescriptor> aModule : descriptor.getModules()) {
            Archivist<BundleDescriptor> subArchivist = archivistFactory.get().getArchivist(aModule.getModuleType());
            subArchivist.initializeContext(this);
            subArchivist.setModuleDescriptor(aModule);
            String archiveUri = aModule.getArchiveUri();
            logger.log(Level.FINE, "Writing {0} with {1}", new Object[] {archiveUri, subArchivist});

            // we need to copy the old archive to a temp file so
            // the save method can copy its original contents from
            File tmpFile = null;
            try (InputStream is = in.getEntry(archiveUri);
                WritableArchive internalJar = out.createSubArchive(archiveUri)) {
                if (in instanceof WritableArchive) {
                    subArchivist.setArchiveUri(internalJar.getURI().getSchemeSpecificPart());
                } else {
                    tmpFile = getTempFile(path);
                    FileUtils.copy(is, tmpFile, in.getEntrySize(archiveUri));

                    // configure archivist
                    subArchivist.setArchiveUri(tmpFile.getAbsolutePath());
                }
                subArchivist.writeContents(internalJar);
            } finally {
                if (tmpFile != null) {
                    boolean ok = tmpFile.delete();
                    if (!ok) {
                        logger.log(Level.WARNING, localStrings.getLocalString("enterprise.deployment.cantDelete",
                            "Error deleting file {0}", tmpFile));
                    }
                }
            }

            // no need to copy the bundle from the original jar file
            filesToSkip.add(archiveUri);
        }

        // now write the old contents and new descriptors
        super.writeContents(in, out, filesToSkip);
    }

    /**

    /**
     * @return a default BundleDescriptor for this archivist
     */
    @Override
    public Application getDefaultBundleDescriptor() {
        return Application.createApplication();
    }

    /**
     * open a new application archive file, read all the deployment descriptors
     *
     * @param appArchive the file path for the J2EE Application archive
     */
    @Override
    public Application open(ReadableArchive appArchive)
        throws IOException, SAXException {

        setManifest(appArchive.getManifest());

        // read the standard deployment descriptors
        Application appDesc = readStandardDeploymentDescriptor(appArchive);
        return openWith(appDesc, appArchive);
    }

    @Override
    public Application openWith(Application application, ReadableArchive archive)
        throws IOException, SAXException {
        setManifest(archive.getManifest());

        setDescriptor(application);

        Map<ExtensionsArchivist, RootDeploymentDescriptor> extensions = new HashMap<>();

        if (extensionsArchivists!=null) {
            for (ExtensionsArchivist extension : extensionsArchivists) {
                if (extension.supportsModuleType(getModuleType())) {
                    Object o = extension.open(this, archive, descriptor);
                    if (o instanceof RootDeploymentDescriptor) {
                        if (o != descriptor) {
                            extension.addExtension(descriptor, (RootDeploymentDescriptor) o);
                        }
                        extensions.put(extension, (RootDeploymentDescriptor) o);
                    }
                }
            }
        }

        // save the handleRuntimeInfo value first
        boolean origHandleRuntimeInfo = handleRuntimeInfo;

        // read the modules standard deployment descriptors
        handleRuntimeInfo = false;
        if (!readModulesDescriptors(application, archive)) {
            return null;
        }

        // now read the runtime deployment descriptors
        handleRuntimeInfo = origHandleRuntimeInfo;

        if (handleRuntimeInfo) {
            readRuntimeDeploymentDescriptor(archive, application);

            // read extensions runtime deployment descriptors if any
            for (Map.Entry<ExtensionsArchivist, RootDeploymentDescriptor> extension : extensions.entrySet()) {
                // after standard DD and annotations are processed, we should
                // an extension descriptor now
                if (extension.getValue() != null) {
                    extension.getKey().readRuntimeDeploymentDescriptor(this, archive, extension.getValue());
                }
            }
            if (classLoader != null) {
                validate(null);
            }
        }
        return application;
    }

    /**
     * This method creates a top level Application object for an ear.
     * @param archive the archive for the application
     * @param directory whether the application is packaged as a directory
     */
    public Application createApplication(ReadableArchive archive, boolean directory) throws IOException, SAXException {
        if (hasStandardDeploymentDescriptor(archive)) {
            return readStandardDeploymentDescriptor(archive);
        }
        return getApplicationFromIntrospection(archive, directory);
    }

    /**
     * This method introspect an ear file and populate the Application object.
     * We follow the Jakarta EE platform specification, Section EE.8.4.2
     * to determine the type of the modules included in this application.
     *
     * @param archive   the archive representing the application root
     * @param directory whether this is a directory deployment
     */
    private Application getApplicationFromIntrospection(ReadableArchive archive, boolean directory) {
        String appRoot = archive.getURI().getSchemeSpecificPart(); // archive is a directory
        if (appRoot.endsWith(File.separator)) {
            appRoot = appRoot.substring(0, appRoot.length() - 1);
        }

        Application app = Application.createApplication();
        app.setLoadedFromApplicationXml(false);
        app.setVirtual(false);

        //name of the file without its extension
        String appName = appRoot.substring(appRoot.lastIndexOf(File.separatorChar) + 1);
        app.setName(appName);

        List<ReadableArchive> unknowns = new ArrayList<>();
        File[] files = getEligibleEntries(new File(appRoot), directory);
        for (File subModule : files) {
            ReadableArchive subArchive = null;
            try {
                try {
                    subArchive = archiveFactory.openArchive(subModule);
                } catch (IOException ex) {
                    logger.log(Level.WARNING, ex.getMessage());
                }

                //for archive deployment, we check the sub archives by its
                //file extension; for directory deployment, we check the sub
                //directories by its name. We are now supporting directory
                //names with both "_suffix" and ".suffix".

                //Section EE.8.4.2.1.a
                String name = subModule.getName();
                String uri = deriveArchiveUri(appRoot, subModule, directory);
                if ((!directory && name.endsWith(".war"))
                    || (directory && (name.endsWith("_war") || name.endsWith(".war")))) {
                    ModuleDescriptor<BundleDescriptor> md = new ModuleDescriptor<>();
                    md.setArchiveUri(uri);
                    md.setModuleType(DOLUtils.warType());
                    // the context root will be set later after
                    // we process the sub modules
                    app.addModule(md);
                }
                // Section EE.8.4.2.1.b
                else if ((!directory && name.endsWith(".rar"))
                    || (directory && (name.endsWith("_rar") || name.endsWith(".rar")))) {
                    ModuleDescriptor<BundleDescriptor> md = new ModuleDescriptor<>();
                    md.setArchiveUri(uri);
                    md.setModuleType(DOLUtils.rarType());
                    app.addModule(md);
                } else if ((!directory && name.endsWith(".jar"))
                        || (directory &&
                        (name.endsWith("_jar") ||
                                name.endsWith(".jar")))) {
                    try {
                        //Section EE.8.4.2.1.d.i
                        AppClientArchivist acArchivist = new AppClientArchivist();
                        if (acArchivist.hasStandardDeploymentDescriptor(subArchive)
                                || acArchivist.hasRuntimeDeploymentDescriptor(subArchive)
                                || acArchivist.getMainClassName(subArchive.getManifest()) != null) {

                            ModuleDescriptor<BundleDescriptor> md = new ModuleDescriptor<>();
                            md.setArchiveUri(uri);
                            md.setModuleType(DOLUtils.carType());
                            md.setManifest(subArchive.getManifest());
                            app.addModule(md);
                            continue;
                        }

                        //Section EE.8.4.2.1.d.ii
                        Archivist<EjbBundleDescriptor> ejbArchivist = archivistFactory.get().getArchivist(ejbType());
                        if (ejbArchivist.hasStandardDeploymentDescriptor(subArchive)
                            || ejbArchivist.hasRuntimeDeploymentDescriptor(subArchive)) {

                            ModuleDescriptor<BundleDescriptor> md = new ModuleDescriptor<>();
                            md.setArchiveUri(uri);
                            md.setModuleType(ejbType());
                            app.addModule(md);
                            continue;
                        }
                    } catch (IOException ex) {
                        logger.log(Level.WARNING, ex.getMessage());
                    }

                    //Still could not decide between an ejb and a library
                    unknowns.add(subArchive);

                    // Prevent this unknown archive from being closed in the
                    // finally block, because the same object will be used in
                    // the block below where unknowns are checked one more time.
                    subArchive = null;
                } else {
                    //ignored
                }
            } finally {
                if (subArchive != null) {
                    try {
                        subArchive.close();
                    } catch (IOException ioe) {
                        logger.log(Level.WARNING, localStrings.getLocalString("enterprise.deployment.errorClosingSubArch", "Error closing subarchive {0}", new Object[]{subModule.getAbsolutePath()}), ioe);
                    }
                }
            }
        }

        if (unknowns.size() > 0) {
            AnnotationDetector detector = new AnnotationDetector(new EjbComponentAnnotationScanner());
            for (ReadableArchive unknown : unknowns) {
                File jarFile = new File(unknown.getURI().getSchemeSpecificPart());
                try {
                    if (detector.hasAnnotationInArchive(unknown)) {
                        String uri = deriveArchiveUri(appRoot, jarFile, directory);
                        //Section EE.8.4.2.1.d.ii, alas EJB
                        ModuleDescriptor<BundleDescriptor> md = new ModuleDescriptor<>();
                        md.setArchiveUri(uri);
                        md.setModuleType(ejbType());
                        app.addModule(md);
                    }
                    /*
                     * The subarchive was opened by the anno detector.  Close it.
                     */
                    unknown.close();
                } catch (IOException ex) {
                    logger.log(Level.WARNING, ex.getMessage());
                }
            }
        }

        return app;
    }

    private static String deriveArchiveUri(
            String appRoot, File subModule, boolean deploydir) {

        //if deploydir, revert the name of the directory to
        //the format of foo/bar/voodoo.ext (where ext is war/rar/jar)
        if (deploydir) {
            return FileUtils.revertFriendlyFilename(subModule.getName());
        }

        // convert appRoot to canonical path so it would work on windows platform
        String aRoot = null;
        try {
            aRoot = (new File(appRoot)).getCanonicalPath();
        } catch (IOException ex) {
            aRoot = appRoot;
        }

        //if archive deploy, need to make sure all of the directory
        //structure is correctly included
        String uri = null;
        try {
            uri = subModule.getCanonicalPath().substring(aRoot.length() + 1);
        } catch (IOException ex) {
            uri = subModule.getAbsolutePath().substring(aRoot.length() + 1);
        }
        return uri.replace(File.separatorChar, '/');
    }

    private static File[] getEligibleEntries(File appRoot, boolean deploydir) {

        //For deploydir, all modules are exploded at the top of application root
        if (deploydir) {
            return appRoot.listFiles(new DirectoryIntrospectionFilter());
        }

        //For archive deploy, recursively search the entire package
        Vector<File> files = new Vector<>();
        getListOfFiles(appRoot, files,
                new ArchiveIntrospectionFilter(appRoot.getAbsolutePath()));
        return files.toArray(new File[files.size()]);
    }

    private static void getListOfFiles(
            File directory, Vector<File> files, FilenameFilter filter) {

        File[] list = directory.listFiles(filter);
        if (list == null) {
            return;
        }
        for (File element : list) {
            if (!element.isDirectory()) {
                files.add(element);
            } else {
                getListOfFiles(element, files, filter);
            }
        }
    }

    private static class ArchiveIntrospectionFilter implements FilenameFilter {
        private final String libDir;
        private final FileArchive.StaleFileManager sfm;

        ArchiveIntrospectionFilter(String root) {
            try {
                sfm = FileArchive.StaleFileManager.Util.getInstance(
                    new File(root));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            libDir = root + File.separator + "lib" + File.separator;
        }

        @Override
        public boolean accept(File dir, String name) {

            File currentFile = new File(dir, name);
            if (currentFile.isDirectory()) {
                return sfm.isEntryValid(currentFile, true);
            }

            //For ".war" and ".rar", check all files in the archive
            if (name.endsWith(".war") || name.endsWith(".rar")) {
                return sfm.isEntryValid(currentFile, true);
            }

            String path = currentFile.getAbsolutePath();
            if (!path.startsWith(libDir) && path.endsWith(".jar")) {
                return sfm.isEntryValid(currentFile, true);
            }

            return false;
        }
    }

    private static class DirectoryIntrospectionFilter implements FilenameFilter {

        DirectoryIntrospectionFilter() {
        }

        @Override
        public boolean accept(File dir, String name) {

            File currentFile = new File(dir, name);
            if (!currentFile.isDirectory()) {
                return false;
            }

            // now we are supporting directory names with
            // ".suffix" and "_suffix"
            if (resemblesTopLevelSubmodule(name)) {
                return true;
            }

            return false;
        }
    }


    /**
     * read the modules deployment descriptor from this application object using
     * the passed archive
     * @param app application containing the list of modules.
     * @param appArchive containing the sub modules files.
     * @return true if everything went fine
     */
    public <T extends BundleDescriptor> boolean readModulesDescriptors(Application app, ReadableArchive appArchive)
        throws IOException, SAXException {
        List<ModuleDescriptor<BundleDescriptor>> nonexistentModules = new ArrayList<>();
        List<ModuleDescriptor<BundleDescriptor>> sortedModules = sortModules(app);
        for (ModuleDescriptor<BundleDescriptor> aModule : sortedModules) {
            if (aModule.getArchiveUri().contains(" ")) {
                throw new IllegalArgumentException(localStrings.getLocalString("enterprise.deployment.unsupporturi",
                    "Unsupported module URI {0}, it contains space(s)", new Object[] {aModule.getArchiveUri()}));
            }
            DOLUtils.getDefaultLogger().log(Level.FINE, "Opening sub-module {0}", aModule);
            Archivist<T> newArchivist = archivistFactory.get().getArchivist(aModule.getModuleType());
            newArchivist.initializeContext(this);
            newArchivist.setRuntimeXMLValidation(this.getRuntimeXMLValidation());
            newArchivist.setRuntimeXMLValidationLevel(this.getRuntimeXMLValidationLevel());
            newArchivist.setAnnotationProcessingRequested(annotationProcessingRequested);

            T bundleDescriptor = null;
            try (ReadableArchive embeddedArchive = appArchive.getSubArchive(aModule.getArchiveUri())) {
                if (embeddedArchive == null) {
                    throw new IllegalArgumentException(localStrings.getLocalString("enterprise.deployment.nosuchmodule",
                        "Could not find sub module [{0}] as defined in application.xml",
                        new Object[] {aModule.getArchiveUri()}));
                }
                embeddedArchive.setParentArchive(appArchive);
                DOLUtils.setExtensionArchivistForSubArchivist(habitat, embeddedArchive, aModule, app, newArchivist);
                if (aModule.getAlternateDescriptor() == null) {
                    // open the subarchive to get the deployment descriptor...
                    bundleDescriptor = newArchivist.open(embeddedArchive, app);
                } else {
                    // the module use alternate deployement descriptor, ignore the
                    // DDs in the archive.
                    try (InputStream is = appArchive.getEntry(aModule.getAlternateDescriptor())) {
                        DeploymentDescriptorFile<?> ddFile = newArchivist.getStandardDDFile();
                        ddFile.setXMLValidation(newArchivist.getXMLValidation());
                        ddFile.setXMLValidationLevel(newArchivist.getXMLValidationLevel());
                        if (appArchive.getURI() != null) {
                            ddFile.setErrorReportingString(appArchive.getURI().getSchemeSpecificPart());
                        }
                        bundleDescriptor = (T) ddFile.read(is);
                        bundleDescriptor.setApplication(app);
                    }

                    // TODO : JD need to be revisited for EAR files with Alternative descriptors,
                    // what does it mean for sub components.
                    Map<ExtensionsArchivist<?>, RootDeploymentDescriptor> extensions = new HashMap<>();
                    List<ExtensionsArchivist<?>> extensionsArchivists = newArchivist.getExtensionArchivists();
                    if (extensionsArchivists != null) {
                        for (ExtensionsArchivist<?> extension : extensionsArchivists) {
                            RootDeploymentDescriptor rdd = extension.open(newArchivist, embeddedArchive, bundleDescriptor);
                            extensions.put(extension, rdd);
                        }
                    }
                    newArchivist.postStandardDDsRead(bundleDescriptor, embeddedArchive, extensions);
                    newArchivist.readAnnotations(embeddedArchive, bundleDescriptor, extensions);
                    newArchivist.postAnnotationProcess(bundleDescriptor, embeddedArchive);
                    newArchivist.postOpen(bundleDescriptor, embeddedArchive);
                    // now reads the runtime deployment descriptor...
                    if (isHandlingRuntimeInfo()) {
                        DOLUtils.readAlternativeRuntimeDescriptor(appArchive, embeddedArchive, newArchivist, bundleDescriptor,
                            aModule.getAlternateDescriptor());
                        // read extensions runtime deployment descriptors if any
                        for (Map.Entry<ExtensionsArchivist<?>, RootDeploymentDescriptor> extension : extensions.entrySet()) {
                            // after standard DD and annotations are processed
                            // we should have an extension descriptor now
                            if (extension.getValue() != null) {
                                extension.getKey().readRuntimeDeploymentDescriptor(newArchivist, embeddedArchive,
                                    extension.getValue());
                            }
                        }
                    }
                } // else
            }
            if (bundleDescriptor == null) {
                // display a message only if we had a handle on the sub archive
                return false;
            }
            bundleDescriptor.getModuleDescriptor().setArchiveUri(aModule.getArchiveUri());
            aModule.setModuleName(bundleDescriptor.getModuleDescriptor().getModuleName());
            aModule.setDescriptor(bundleDescriptor);
            bundleDescriptor.setApplication(app);
            aModule.setManifest(newArchivist.getManifest());
            // for optional application.xml case, set the
            // context root as module name for web modules
            if (!appArchive.exists("META-INF/application.xml")) {
                if (aModule.getModuleType().equals(DOLUtils.warType())) {
                    WebBundleDescriptor wbd = (WebBundleDescriptor) bundleDescriptor;
                    if (wbd.getContextRoot() != null && !wbd.getContextRoot().isEmpty()) {
                        aModule.setContextRoot(wbd.getContextRoot());
                    } else {
                        aModule.setContextRoot(aModule.getModuleName());
                    }
                }
            }
        }
        // now remove all the non-existent modules from app so these modules
        // don't get processed further
        for (ModuleDescriptor<BundleDescriptor> nonexistentModule : nonexistentModules) {
            app.removeModule(nonexistentModule);
        }
        return true;
    }

    private List<ModuleDescriptor<BundleDescriptor>> sortModules(Application app) {
        List<ModuleDescriptor<BundleDescriptor>> sortedModules = new ArrayList<>();
        sortedModules.addAll(app.getModuleDescriptorsByType(DOLUtils.rarType()));
        sortedModules.addAll(app.getModuleDescriptorsByType(ejbType()));
        sortedModules.addAll(app.getModuleDescriptorsByType(DOLUtils.warType()));
        sortedModules.addAll(app.getModuleDescriptorsByType(DOLUtils.carType()));
        return sortedModules;
    }


    /**
     * Read the runtime deployment descriptors (can contained in one or
     * many file) set the corresponding information in the passed descriptor.
     * By default, the runtime deployment descriptors are all contained in
     * the xml file characterized with the path returned by
     *
     * @param archive the input archive
     * @param descriptor the initialized deployment descriptor
     */
    @Override
    public void readRuntimeDeploymentDescriptor(ReadableArchive archive, Application descriptor)
        throws IOException, SAXException {

        if (descriptor != null) {
            // each modules first...
            for (ModuleDescriptor<BundleDescriptor> md : descriptor.getModules()) {
                Archivist<BundleDescriptor> archivist = archivistFactory.get().getArchivist(md.getModuleType());
                archivist.initializeContext(this);
                archivist.setRuntimeXMLValidation(this.getRuntimeXMLValidation());
                archivist.setRuntimeXMLValidationLevel(this.getRuntimeXMLValidationLevel());
                try (ReadableArchive subArchive = archive.getSubArchive(md.getArchiveUri())) {
                    if (md.getAlternateDescriptor() == null) {
                        archivist.readRuntimeDeploymentDescriptor(subArchive, md.getDescriptor());
                    } else {
                        DOLUtils.readAlternativeRuntimeDescriptor(archive, subArchive, archivist,
                            md.getDescriptor(), md.getAlternateDescriptor());
                    }
                }
            }
        }
        // for the application
        super.readRuntimeDeploymentDescriptor(archive,  descriptor);
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
        descriptor.visit(new ApplicationValidator());

    }

    /**
     * @return the DeploymentDescriptorFile responsible for handling
     * standard deployment descriptor
     */
    @Override
    public DeploymentDescriptorFile getStandardDDFile() {
        if (standardDD == null) {
            standardDD = new ApplicationDeploymentDescriptorFile();
        }
        return standardDD;
    }


    /**
     * @return the list of the DeploymentDescriptorFile responsible for
     *         handling the configuration deployment descriptors
     */
    @Override
    public List<ConfigurationDeploymentDescriptorFile> getConfigurationDDFiles() {
        if (confDDFiles == null) {
            confDDFiles = DOLUtils.getConfigurationDeploymentDescriptorFiles(habitat, EarArchiveType.ARCHIVE_TYPE);
        }
        return confDDFiles;
    }

    /**
     * Perform Optional packages dependencies checking on an archive
     */
    @Override
    public boolean performOptionalPkgDependenciesCheck(ReadableArchive archive) throws IOException {

        if (!super.performOptionalPkgDependenciesCheck(archive)) {
            return false;
        }

        // now check sub modules
        if (descriptor==null) {
            throw new IOException("Application object not set on archivist");
        }

        boolean returnValue = true;
        for (ModuleDescriptor<BundleDescriptor> md : descriptor.getModules()) {
            try (ReadableArchive sub = archive.getSubArchive(md.getArchiveUri())) {
                if (sub != null) {
                    Archivist<?> subArchivist = archivistFactory.get().getArchivist(md.getModuleType());
                    if (!subArchivist.performOptionalPkgDependenciesCheck(sub)) {
                        returnValue = false;
                    }
                }
            }
        }
        return returnValue;
    }

    /**
     * Copy this archivist to a new abstract archive
     * @param source the archive to copy from
     * @param target the new archive to use to copy our contents into
     */
    @Override
    public void copyInto(ReadableArchive source, WritableArchive target) throws IOException {
        try {
            Application a = readStandardDeploymentDescriptor(source);
            copyInto(a, source, target);
        } catch(SAXException spe) {
            DOLUtils.getDefaultLogger().log(Level.SEVERE, "enterprise.deployment.backend.fileCopyFailure", spe);
        }
    }

    /**
     * Copy this archivist to a new abstract archive
     * @param a the deployment descriptor for an application
     * @param source the source archive
     * @param target the target archive
     */
    public void copyInto(Application a, ReadableArchive source, WritableArchive target) throws IOException {
        copyInto(a, source, target, true);
    }

    /**
     * Copy this archivist to a new abstract archive
     * @param a the deployment descriptor for an application
     * @param source the source archive
     * @param target the target archive
     * @param overwriteManifest if true, the manifest in source archive overwrites the one in target
     */
    public void copyInto(Application a, ReadableArchive source, WritableArchive target, boolean overwriteManifest)
        throws IOException {
        Set<String> entriesToSkip = new HashSet<>();
        for (ModuleDescriptor<?> aModule : a.getModules()) {
            entriesToSkip.add(aModule.getArchiveUri());
            try (ReadableArchive subSource = source.getSubArchive(aModule.getArchiveUri());
                WritableArchive subTarget = target.createSubArchive(aModule.getArchiveUri())) {
                Archivist<?> newArchivist = archivistFactory.get().getArchivist(aModule.getModuleType());
                try (ReadableArchive subArchive = archiveFactory.openArchive(subTarget.getURI())) {
                    subSource.setParentArchive(subArchive);
                    newArchivist.copyInto(subSource, subTarget, overwriteManifest);
                    String subModulePath = subSource.getURI().getSchemeSpecificPart();
                    String parentPath = source.getURI().getSchemeSpecificPart();
                    if (subModulePath.startsWith(parentPath)) {
                        subModulePath = subModulePath.substring(parentPath.length() + File.separator.length());
                        for (Enumeration<String> subEntries = subSource.entries(); subEntries.hasMoreElements();) {
                            String anEntry = subEntries.nextElement();
                            entriesToSkip.add(subModulePath + "/" + anEntry);
                        }
                    }
                }
            }
        }
        super.copyInto(source, target, entriesToSkip, overwriteManifest);
    }


    /**
     * This method will be invoked if and only if the following is true:
     * 1. directory deployment with neither standard nor runtime DD
     * 2. JSR88 DeploymentManager.distribute using InputStream with neither
     *    standard nor runtime DD
     * <p>
     * Note that we will only venture a guess for case 1.  JSR88 deployment
     * of an application (ear) using InputStream without any deployment
     * descriptor will NOT be supported at this time.
     */
    @Override
    protected boolean postHandles(ReadableArchive abstractArchive)
            throws IOException {
        // if we come here and archive is not a directory, it could not be ear
        if (!(abstractArchive instanceof FileArchive)) {
            return false;
        }

        // Only try to make a guess if the archive is a directory

        // We will try to conclude if a directory represents an application
        // by looking at if it contains any Jakarta EE modules.
        // We are supporting directory names with both "_suffix" and ".suffix".
        File file = new File(abstractArchive.getURI());
        if (file.isDirectory()) {
            for (String dirName : abstractArchive.getDirectories()) {
                if (resemblesTopLevelSubmodule(dirName)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    protected String getArchiveExtension() {
        return APPLICATION_EXTENSION;
    }

    /**
     * Returns whether the entry name appears to be that of a submodule at
     * the top level of an enclosing application.
     * <p>
     * Judge an entry to be a top-level submodule if it ends with _war, _jar,
     * _rar, or .war, .jar, or .rar (MyEclipse uses latter pattern.)
     *
     * @param entryName entryName
     * @return true | false
     */
    private static boolean resemblesTopLevelSubmodule(final String entryName) {
        return (entryName.endsWith("_war")
                || entryName.endsWith("_jar")
                || entryName.endsWith("_rar")
                || entryName.endsWith(".war")
                || entryName.endsWith(".jar")
                || entryName.endsWith(".rar"));
    }
}
