/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation.
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

package com.sun.enterprise.glassfish.bootstrap.cfg;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Path;

public class ServerFiles {

    private final Path installRoot;
    private final Path instanceRoot;

    public ServerFiles(Path installRoot, Path instanceRoot) {
        this.installRoot = installRoot;
        this.instanceRoot = instanceRoot;
    }


    public Path getInstallRoot() {
        return this.installRoot;
    }


    public Path getInstanceRoot() {
        return this.instanceRoot;
    }


    public File getFileUnderInstallRoot(Path relativePath) {
        return this.installRoot.resolve(relativePath).toFile();
    }


    public File getFileUnderInstanceRoot(Path relativePath) {
        return this.instanceRoot.resolve(relativePath).toFile();
    }


    /**
     * @param envKey Key to use with {@link System#getenv(String)}
     * @param sysPropsKey Key to use with {@link System#getProperty(String)}
     * @param defaultSubdir Relative path to {@link #getInstanceRoot()}
     * @return first configured directory.
     * @throws IllegalArgumentException if the directory does not exist.
     */
    public File getOsgiHome(String envKey, String sysPropsKey, Path defaultSubdir) {
        final String envProperty = System.getenv(envKey);
        if (envProperty != null) {
            return toExistingFile(envProperty);
        }
        // try system property, which comes from asenv.conf
        final String sysProperty = System.getProperty(sysPropsKey);
        if (sysProperty != null) {
            return toExistingFile(sysProperty);
        }
        return getFileUnderInstallRoot(defaultSubdir);
    }


    private static File toExistingFile(String path) throws IllegalArgumentException {
        final File file = new File(path);
        try {
            File existingFile = file.getCanonicalFile();
            if (existingFile.isDirectory() && existingFile.canRead()) {
                return existingFile;
            }
            throw new IllegalArgumentException(
                "Invalid path: " + path + " - must be an existing and readable directory.");
        } catch (IOException e) {
            throw new IllegalArgumentException("Invalid path: " + path, e);
        }
    }

    /**
     * @return autodetected glassfish directory based on where usually is this class.
     */
    public static File detectInstallRoot() {
        // glassfish/lib/bootstrap/glassfish.jar
        File bootstrapFile = findBootstrapFile();
        // glassfish/
        // Defensive code for embedded.
        File parent = bootstrapFile.getParentFile();
        for (int i = 0; i < 2; i++) {
            if (parent == null) {
                return null;
            }
            parent = parent.getParentFile();
        }
        return parent;
    }


    /**
     * @return uses this class to locate its jar file.
     */
    private static File findBootstrapFile() {
        URL jarUrl = ServerFiles.class.getProtectionDomain().getCodeSource().getLocation();
        return new File(URI.create(jarUrl.toExternalForm().replaceFirst("jar:", "file:")));
    }
}
