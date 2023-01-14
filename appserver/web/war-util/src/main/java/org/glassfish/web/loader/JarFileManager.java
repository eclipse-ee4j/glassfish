/*
 * Copyright (c) 2023 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.web.loader;

import com.sun.enterprise.util.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.System.Logger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static java.lang.System.Logger.Level.WARNING;
import static org.glassfish.web.loader.LogFacade.getString;

/**
 * @author David Matejcek
 */
class JarFileManager {
    private static final Logger LOG = System.getLogger(JarFileManager.class.getName());

    /**
     * The list of JARs, in the order they should be searched
     * for locally loaded classes or resources.
     */
    private JarFile[] jarFiles = new JarFile[0];
    /**
     * The list of JARs, in the order they should be searched
     * for locally loaded classes or resources.
     */
    private File[] jarRealFiles = new File[0];

    private volatile boolean resourcesExtracted;

    /** Last time a JAR was accessed. */
    private long lastJarAccessed;

    synchronized void addJarFile(File file) throws IOException {
        jarFiles = new JarFile[jarFiles.length + 1];
        final File[] result4 = Arrays.copyOf(jarRealFiles, jarRealFiles.length + 1);
        result4[jarRealFiles.length] = file;
        jarRealFiles = result4;
    }


    synchronized JarFile[] getJarFiles() {
        // TODO: Don't provide private field, but rather a copy
        return openJARs() ? jarFiles : null;
    }


    synchronized File[] getJarRealFiles() {
        // TODO: Don't provide private field, but rather a copy
        return jarRealFiles;
    }


    synchronized void closeJarFiles() {
        int length = jarFiles.length;
        for (int i = 0; i < length; i++) {
            final JarFile jarFile = jarFiles[i];
            if (jarFile == null) {
                continue;
            }
            jarFiles[i] = null;
            try {
                jarFile.close();
            } catch (IOException e) {
                LOG.log(WARNING, "Could not close the jarFile " + jarFile, e);
            }
        }
    }


    synchronized void closeJarFilesIfNotUsed() {
        if (jarFiles.length == 0) {
            return;
        }
        if (System.currentTimeMillis() - lastJarAccessed > 90000) {
            for (int i = 0; i < jarFiles.length; i++) {
                try {
                    if (jarFiles[i] != null) {
                        jarFiles[i].close();
                        jarFiles[i] = null;
                    }
                } catch (IOException e) {
                    LOG.log(DEBUG, "Failed to close JAR", e);
                }
            }
        }
    }


    synchronized void extractResources(File loaderDir, String canonicalLoaderDir) {
        if (resourcesExtracted) {
            return;
        }
        for (int i = jarFiles.length - 1; i >= 0; i--) {
            extractResource(jarFiles[i], loaderDir, canonicalLoaderDir);
        }
        resourcesExtracted = true;
    }



    /**
     * Attempts to load the requested resource from this classloader's JAR files.
     *
     * @return The requested resource, or null if not found
     */
    synchronized ResourceEntry findResource(String name, String path, File loaderDir, boolean antiJARLocking) {
        LOG.log(TRACE, "findResource(name={0}, path={1}, loaderDir={2}, antiJARLocking={3})",
            name, path, loaderDir, antiJARLocking);
        if (!openJARs()) {
            return null;
        }
        final int jarFilesLength = jarFiles.length;
        InputStream binaryStream = null;
        int contentLength = -1;
        for (int i = 0; i < jarFilesLength; i++) {
            final JarFile jarFile = jarFiles[i];
            final JarEntry jarEntry = jarFile.getJarEntry(path);
            if (jarEntry == null) {
                continue;
            }
            final File file = jarRealFiles[i];
            final ResourceEntry entry = new ResourceEntry();
            entry.codeBase = toURL(file);
            if (entry.codeBase == null) {
                return null;
            }
            try {
                entry.source = new URL("jar:" + entry.codeBase + "!/" + path);
            } catch (MalformedURLException e) {
                LOG.log(DEBUG, "Failed to create URL from " + entry.codeBase + " and path " + path, e);
                return null;
            }
            entry.lastModified = file.lastModified();
            contentLength = (int) jarEntry.getSize();
            try {
                entry.manifest = jarFile.getManifest();
                binaryStream = jarFile.getInputStream(jarEntry);
            } catch (IOException e) {
                LOG.log(DEBUG, "Failed to get manifest or input stream for " + jarFile.getName(), e);
                return null;
            }

            // Extract resources contained in JAR to the workdir
            if (antiJARLocking && !path.endsWith(".class")) {
                File resourceFile = new File(loaderDir, jarEntry.getName());
                if (!resourcesExtracted && !resourceFile.exists()) {
                    extractResources(loaderDir, path);
                }
            }
            if (binaryStream != null) {
                entry.readEntryData(name, binaryStream, contentLength, jarEntry);
            }
            return entry;
        }
        return null;
    }


    /**
     * @return true if open
     */
    private boolean openJARs() {
        LOG.log(DEBUG, "openJARs()");
        lastJarAccessed = System.currentTimeMillis();
        if (jarFiles.length == 0) {
            return true;
        }
        for (int i = 0; i < jarFiles.length; i++) {
            if (jarFiles[i] != null) {
                continue;
            }
            try {
                jarFiles[i] = new JarFile(jarRealFiles[i]);
            } catch (IOException e) {
                LOG.log(DEBUG, "Failed to open JAR", e);
                closeJarFiles();
                return false;
            }
        }
        return true;
    }


    private static void extractResource(JarFile jarFile, File loaderDir, String canonicalLoaderDir) {
        LOG.log(DEBUG, "extractResource(jarFile={0}, loaderDir={1}, canonicalLoaderDir={2})", jarFile, loaderDir, canonicalLoaderDir);
        Enumeration<JarEntry> jarEntries = jarFile.entries();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            if (!jarEntry.isDirectory() && !jarEntry.getName().endsWith(".class")) {
                File resourceFile = new File(loaderDir, jarEntry.getName());
                try {
                    if (!resourceFile.getCanonicalPath().startsWith(canonicalLoaderDir)) {
                        throw new IllegalArgumentException(getString(LogFacade.ILLEGAL_JAR_PATH, jarEntry.getName()));
                    }
                } catch (IOException ioe) {
                    throw new IllegalArgumentException(
                        getString(LogFacade.VALIDATION_ERROR_JAR_PATH, jarEntry.getName()), ioe);
                }
                if (!FileUtils.mkdirsMaybe(resourceFile.getParentFile())) {
                    LOG.log(WARNING, LogFacade.UNABLE_TO_CREATE, resourceFile.getParentFile());
                }

                try (InputStream is = jarFile.getInputStream(jarEntry);
                    FileOutputStream os = new FileOutputStream(resourceFile)) {
                    FileUtils.copy(is, os, Long.MAX_VALUE);
                } catch (IOException e) {
                    LOG.log(DEBUG, "Failed to copy entry " + jarEntry, e);
                }
            }
        }
    }


    private static URL toURL(File file) {
        try {
            return file.getCanonicalFile().toURI().toURL();
        } catch (IOException e) {
            LOG.log(WARNING, "Could not convert file to URL: " + file, e);
            return null;
        }
    }
}
