/*
 * Copyright (c) 2022 Eclipse Foundation and/or its affiliates. All rights reserved.
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

package org.glassfish.main.tests.tck.ant.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.jboss.shrinkwrap.resolver.api.maven.MavenFormatStage;
import org.jboss.shrinkwrap.resolver.api.maven.MavenResolverSystem;

/**
 * Maven ZIP resolver.
 * <p>
 * It uses Shrinkwrap to resolve maven dependencies and provide them a a ZIP file or even unpacked
 * ZIP file content in the target directory.
 *
 * @author David Matejcek
 */
public class ZipResolver {
    private static final Logger LOG = System.getLogger(ZipResolver.class.getName());

    private final MavenResolverSystem resolver;
    private final File targetDirectory;

    /**
     * Initializes the resolver.
     *
     * @param targetDirectory - this directory will be used for the output.
     * @param settingsXml - Maven configuration
     */
    public ZipResolver(final File targetDirectory, final File settingsXml) {
        LOG.log(Level.DEBUG, "ZipResolver(targetDirectory={0}, settingsXml={1})", targetDirectory, settingsXml);
        this.resolver = Maven.configureResolver().withMavenCentralRepo(false).workOffline().fromFile(settingsXml);
        this.targetDirectory = targetDirectory;
    }


    /**
     * @return the artifact ZIP file in user's default local Maven repository.
     */
    public File getZipFile(String groupId, String artifactId, String version) {
        LOG.log(Level.INFO, "getZipFile(groupId={0}, artifactId={1}, version={2})", groupId, artifactId, version);
        return resolve(groupId, artifactId, version).asSingleFile();
    }


    /**
     * Resolves the ZIP dependency and unzips it to the target directory given in constructor.
     */
    public void unzipDependency(String groupId, String artifactId, String version) {
        LOG.log(Level.INFO, "unzipDependency(groupId={0}, artifactId={1}, version={2})", groupId, artifactId, version);
        MavenFormatStage tckZip = resolve(groupId, artifactId, version);
        try (ZipInputStream zis = new ZipInputStream(tckZip.asSingleInputStream())) {
            try {
                byte[] buffer = new byte[1024];
                while (true) {
                    ZipEntry zipEntry = zis.getNextEntry();
                    if (zipEntry == null) {
                        break;
                    }
                    File newFile = newFile(targetDirectory, zipEntry);
                    if (zipEntry.isDirectory()) {
                        if (!newFile.isDirectory() && !newFile.mkdirs()) {
                            throw new IOException("Failed to create directory " + newFile);
                        }
                    } else {
                        // fix for Windows-created archives
                        File parent = newFile.getParentFile();
                        if (!parent.isDirectory() && !parent.mkdirs()) {
                            throw new IOException("Failed to create directory " + parent);
                        }
                        try (FileOutputStream fos = new FileOutputStream(newFile)) {
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                    }
                }
            } finally {
                zis.closeEntry();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not open the file.", e);
        }
    }


    private MavenFormatStage resolve(String groupId, String artifactId, String version) {
        return resolver.resolve(groupId + ":" + artifactId + ":zip:" + version).withoutTransitivity();
    }


    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }
}
