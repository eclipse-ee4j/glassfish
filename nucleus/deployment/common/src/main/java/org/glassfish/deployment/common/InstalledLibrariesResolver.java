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

package org.glassfish.deployment.common;

import static java.util.Collections.emptyList;
import static java.util.logging.Level.FINE;
import static java.util.logging.Level.FINEST;
import static java.util.logging.Level.INFO;
import static java.util.logging.Level.WARNING;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.glassfish.api.deployment.archive.ReadableArchive;
import org.glassfish.logging.annotation.LogMessageInfo;

/**
 * This class resolves the dependencies between optional packages (installed libraries) and also between
 * apps/stand-alone modules that depend on optional packages (installed libraries)
 *
 * @author Sheetal Vartak
 */

public class InstalledLibrariesResolver {

    /**
     * Installed libraries list (accounts only for "domainRoot/lib/applibs" and not any of "java.ext.dirs" entries)
     */
    private static Map<Extension, String> appLibsDirLibsStore = new HashMap<Extension, String>();

    public static final Logger deplLogger = org.glassfish.deployment.common.DeploymentContextImpl.deplLogger;

    @LogMessageInfo(message = "Optional package {0} does not exist or its Specification-Version does not match. Unable to satisfy dependency for {1}", level = "WARNING")
    private static final String PACKAGE_NOT_FOUND = "NCLS-DEPLOYMENT-00011";

    @LogMessageInfo(message = "Optional package dependency satisfied for {0}", level = "INFO")
    private static final String PACKAGE_SATISFIED = "NCLS-DEPLOYMENT-00012";

    @LogMessageInfo(message = "Error in opening optional package file {0} due to exception: {1}.", level = "WARNING")
    private static final String INVALID_ZIP = "NCLS-DEPLOYMENT-00013";

    @LogMessageInfo(message = "Exception occurred : {0}.", level = "WARNING")
    private static final String EXCEPTION_OCCURRED = "NCLS-DEPLOYMENT-00014";

    @LogMessageInfo(message = "Specification-Version for the optional package [ {0} ] in the jarfile [ {1} ] is not specified. Please provide a valid specification version for this optional package", level = "WARNING")
    private static final String NULL_SPEC_VERS = "NCLS-DEPLOYMENT-00015";

    @LogMessageInfo(message = "Skipping extension processing for {0} due to error: {1}", level = "INFO")
    private static final String SKIPPING_PROCESSING_INFO = "NCLS-DEPLOYMENT-00016";

    /**
     * resolves installed library dependencies
     *
     * @param manifest Manifest File
     * @param archiveUri archive
     * @return status indicating whether all dependencies (transitive) is resolved or not
     */
    public static boolean resolveDependencies(Manifest manifest, String archiveUri) {
        // let us try app-libs directory
        try {
            getInstalledLibraries(archiveUri, manifest, true, appLibsDirLibsStore);
        } catch (MissingResourceException e1) {
            deplLogger.log(WARNING, PACKAGE_NOT_FOUND, new Object[] { e1.getClass(), archiveUri });
            return false;
        }

        deplLogger.log(INFO, PACKAGE_SATISFIED, new Object[] { archiveUri });

        return true;
    }

    /**
     * Check whether the optional packages have all their internal dependencies resolved
     *
     * @param libDir libraryDirectory
     */
    public static void initializeInstalledLibRegistry(String libDir) {
        initializeInstalledLibRegistryForApplibs(libDir);
    }

    public static Set<String> getInstalledLibraries(ReadableArchive archive) throws IOException {
        Set<String> libraries = new HashSet<String>();
        if (archive == null) {
            return libraries;
        }

        Manifest manifest = archive.getManifest();

        // We are looking for libraries only in "applibs" directory, hence strict=false
        Set<String> installedLibraries = getInstalledLibraries(archive.getURI().toString(), manifest, false, appLibsDirLibsStore);
        libraries.addAll(installedLibraries);

        // Now check any embedded libraries.
        for (String libUri : getEmbeddedLibraries(archive)) {
            try (InputStream embeddedLibInputStream = archive.getEntry(libUri)) {
                if (embeddedLibInputStream == null) {
                    // embeddedLibInputStream can be null if reading an exploded archive where directories are also exploded. See
                    // FileArchive.getEntry()
                    continue;
                }

                manifest = new JarInputStream(embeddedLibInputStream).getManifest();
                if (manifest != null) {
                    // We are looking for libraries only in "applibs" directory, hence strict=false
                    libraries.addAll(getInstalledLibraries(archive.getURI().toString(), manifest, false, appLibsDirLibsStore));
                }
            }
        }

        return libraries;
    }

    private static Set<String> getInstalledLibraries(String archiveURI, Manifest manifest, boolean strict, Map<Extension, String> libraryStore) {
        Set<String> libraries = new HashSet<String>();
        String extensionList = null;

        try {
            extensionList = manifest.getMainAttributes().getValue(Attributes.Name.EXTENSION_LIST);
            if (deplLogger.isLoggable(FINE)) {
                deplLogger.fine("Extension-List for archive [" + archiveURI + "] : " + extensionList);
            }
        } catch (Exception e) {
            // Ignore this exception
            deplLogger.log(FINE, () -> "InstalledLibrariesResolver : exception occurred : " + e.toString());
        }

        if (extensionList != null) {
            StringTokenizer extensionListTokenizer = new StringTokenizer(extensionList, " ");
            while (extensionListTokenizer.hasMoreTokens()) {

                String token = extensionListTokenizer.nextToken().trim();
                String extensionName = manifest.getMainAttributes().getValue(token + "-" + Attributes.Name.EXTENSION_NAME);
                if (extensionName != null) {
                    extensionName = extensionName.trim();
                }

                String specVersion = manifest.getMainAttributes().getValue(token + "-" + Attributes.Name.SPECIFICATION_VERSION);

                Extension extension = new Extension(extensionName);
                if (specVersion != null) {
                    extension.setSpecVersion(specVersion);
                }

                // TODO possible NPE when extension is unspecified
                boolean isLibraryInstalled = libraryStore.containsKey(extension);
                if (isLibraryInstalled) {
                    libraries.add(libraryStore.get(extension));
                } else if (strict) {
                    throw new MissingResourceException(extensionName + " not found", extensionName, null);
                }

                if (deplLogger.isLoggable(FINEST)) {
                    deplLogger.log(FINEST,
                            " is library installed [" + extensionName + "] " + "for archive [" + archiveURI + "]: " + isLibraryInstalled);
                }
            }
        }

        return libraries;
    }

    /**
     * @return a list of libraries included in the archive
     */
    private static List<String> getEmbeddedLibraries(ReadableArchive archive) {
        Enumeration<String> entries = archive.entries();
        if (entries == null) {
            emptyList();
        }

        List<String> libs = new ArrayList<>();
        while (entries.hasMoreElements()) {
            String entryName = entries.nextElement();
            if (entryName.indexOf('/') != -1) {
                continue; // not on the top level
            }

            if (entryName.endsWith(".jar")) {
                libs.add(entryName);
            }
        }

        return libs;
    }

    /**
     * Initialize the "applibs" part of installed libraries ie., any library within "applibs" which represents an extension
     * (EXTENSION_NAME in MANIFEST.MF) that can be used by applications (as EXTENSION_LIST in their MANIFEST.MF)
     *
     * @param domainLibDir library directory (of a domain)
     */
    private static void initializeInstalledLibRegistryForApplibs(String domainLibDir) {
        String applibsDirString = domainLibDir + File.separator + "applibs";
        deplLogger.fine("applib-Dir-String..." + applibsDirString);

        List<File> validApplibsDirLibFiles = new ArrayList<>();
        Map<Extension, String> installedLibraries = getInstalledLibraries(applibsDirString, null, validApplibsDirLibFiles);
        appLibsDirLibsStore.putAll(installedLibraries);

        for (File file : validApplibsDirLibFiles) {
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(file);
                Manifest manifest = jarFile.getManifest();
                if (manifest != null) {
                    try {
                        getInstalledLibraries(file.getAbsolutePath(), manifest, true, appLibsDirLibsStore);
                    } catch (MissingResourceException mre) {
                        deplLogger.log(WARNING, PACKAGE_NOT_FOUND, new Object[] { mre.getClass(), file.getAbsolutePath() });
                    }
                }
            } catch (IOException ioe) {
                deplLogger.log(WARNING, INVALID_ZIP, new Object[] { file.getAbsolutePath(), ioe.getMessage() });
            } finally {
                if (jarFile != null) {
                    try {
                        jarFile.close();
                    } catch (IOException e) {
                        deplLogger.log(WARNING, EXCEPTION_OCCURRED, new Object[] { e.getMessage() });
                    }
                }
            }
        }
    }

    private static Map<Extension, String> getInstalledLibraries(String libraryDirectoryName, Set<String> processedLibraryNames, List<File> processedLibraries) {
        Map<Extension, String> installedLibraries = new HashMap<Extension, String>();

        File dir = new File(libraryDirectoryName);

        if (deplLogger.isLoggable(FINE)) {
            deplLogger.log(FINE, "installed library directory : " + dir);
        }

        File[] libraries = dir.listFiles();
        if (libraries != null) {
            try {
                for (int i = 0; i < libraries.length; i++) {

                    if (deplLogger.isLoggable(FINE)) {
                        deplLogger.log(FINE, "installed library : " + libraries[i]);
                    }

                    /*
                     * Skip any candidate that does not end with .jar or is a directory.
                     */
                    if (libraries[i].isDirectory()) {
                        deplLogger.log(FINE, "Skipping installed library processing on " + libraries[i].getAbsolutePath() + "; it is a directory");
                        continue;
                    }

                    if (!libraries[i].getName().toLowerCase(Locale.getDefault()).endsWith(".jar")) {
                        deplLogger.log(FINE, "Skipping installed library processing on " + libraries[i].getAbsolutePath()
                                + "; it does not appear to be a JAR file based on its file type");
                        continue;
                    }

                    JarFile jarFile = null;
                    try {
                        jarFile = new JarFile(libraries[i]);

                        Manifest manifest = jarFile.getManifest();
                        if (processedLibraryNames != null) {
                            processedLibraryNames.add(libraries[i].toString());
                        }
                        if (processedLibraries != null) {
                            processedLibraries.add(libraries[i]);
                        }

                        // Extension-Name of optional package
                        if (manifest != null) {
                            String extName = manifest.getMainAttributes().getValue(Attributes.Name.EXTENSION_NAME);
                            String specVersion = manifest.getMainAttributes().getValue(Attributes.Name.SPECIFICATION_VERSION);
                            deplLogger.fine("Extension " + libraries[i].getAbsolutePath() + ", extNameOfOPtionalPkg..." + extName
                                    + ", specVersion..." + specVersion);
                            if (extName != null) {
                                if (specVersion == null) {
                                    deplLogger.log(WARNING, NULL_SPEC_VERS, new Object[] { extName, jarFile.getName() });
                                    specVersion = "";
                                }

                                Extension extension = new Extension(extName);
                                extension.setSpecVersion(specVersion);

                                installedLibraries.put(extension, libraries[i].getName());
                            }
                        }
                    } catch (Throwable thr) {
                        String msg = deplLogger.getResourceBundle().getString("enterprise.deployment.backend.optionalpkg.dependency.error");
                        if (deplLogger.isLoggable(FINE)) {
                            deplLogger.log(FINE, MessageFormat.format(msg, libraries[i].getAbsolutePath(), thr.getMessage()), thr);
                        } else {
                            LogRecord lr = new LogRecord(INFO, SKIPPING_PROCESSING_INFO);
                            lr.setParameters(new Object[] { libraries[i].getAbsolutePath(), thr.getMessage() });
                            deplLogger.log(lr);
                        }
                    } finally {
                        if (jarFile != null) {
                            jarFile.close();
                        }
                    }
                }
            } catch (IOException e) {
                deplLogger.log(WARNING, "enterprise.deployment.backend.optionalpkg.dependency.exception",
                        new Object[] { e.getMessage() });
            }
        }

        return installedLibraries;
    }

    static class Extension {
        private String extensionName;
        private String specVersion = "";
        private String specVendor = "";
        private String implVersion = "";
        private String implVendor = "";

        public Extension(String name) {
            this.extensionName = name;
        }

        public String getExtensionName() {
            return extensionName;
        }

        public void setExtensionName(String extensionName) {
            this.extensionName = extensionName;
        }

        public String getSpecVersion() {
            return specVersion;
        }

        public void setSpecVersion(String specVersion) {
            this.specVersion = specVersion;
        }

        public String getSpecVendor() {
            return specVendor;
        }

        public void setSpecVendor(String specVendor) {
            this.specVendor = specVendor;
        }

        public String getImplVersion() {
            return implVersion;
        }

        public void setImplVersion(String implVersion) {
            this.implVersion = implVersion;
        }

        public String getImplVendor() {
            return implVendor;
        }

        public void setImplVendor(String implVendor) {
            this.implVendor = implVendor;
        }

        @Override
        public boolean equals(Object o) {
            if (o != null && o instanceof Extension) {
                Extension e = (Extension) o;
                if ((o == this) || (e.getExtensionName().equals(extensionName)
                        && (e.getImplVendor().equals(implVendor) || (e.getImplVendor().equals("")))
                        && (e.getImplVersion().equals(implVersion) || (e.getImplVersion().equals("")))
                        && (e.getSpecVendor().equals(specVendor) || (e.getSpecVendor().equals("")))
                        && (e.getSpecVersion().equals(specVersion) || (e.getSpecVersion().equals(""))))) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            int result = 17;
            result = 37 * result + extensionName.hashCode();
            return result;
        }
    }
}
