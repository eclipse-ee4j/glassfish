/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.cli.cluster;

import com.sun.enterprise.universal.io.SmartFile;
import com.sun.enterprise.util.SystemPropertyConstants;
import com.sun.enterprise.util.io.FileListerRelative;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.zip.ZipFileException;
import com.sun.enterprise.util.zip.ZipWriter;

import jakarta.inject.Inject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.cluster.ssh.sftp.SFTPPath;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;

import static java.util.logging.Level.FINER;

/**
 * @author Rajiv Mordani
 * @author Byron Nevins
 */
@Service
@PerLookup
abstract class InstallNodeBaseCommand extends NativeRemoteCommandsBase {
    @Param(name = "archive", optional = true)
    private String archive;
    @Param(name = "installdir", optional = true, defaultValue = "${com.sun.aas.productRoot}")
    private String installDir;
    @Param(optional = true, defaultValue = "false")
    private boolean create;
    @Param(optional = true, defaultValue = "false")
    private boolean save;
    @Param(name = "force", optional = true, defaultValue = "false")
    private boolean force;
    @Inject
    private ServiceLocator habitat;
    private String archiveName;
    private boolean delete = true;

    abstract void copyToHosts(File zipFile, List<SFTPPath> binDirFiles) throws CommandException;
    abstract void precopy() throws CommandException;

    @Override
    protected void validate() throws CommandException {
        super.validate();
        Globals.setDefaultHabitat(habitat);

        installDir = resolver.resolve(installDir);

        if(ok(archive)) {
            archive = SmartFile.sanitize(archive);
        }
    }

    @Override
    protected int executeCommand() throws CommandException {
        File zipFile = null;
        try {
            ArrayList<SFTPPath> binDirFiles = new ArrayList<>();
            precopy();
            zipFile = createZipFileIfNeeded(binDirFiles);
            copyToHosts(zipFile, binDirFiles);
        } catch (CommandException e) {
            throw e;
        } catch (Exception e) {
            throw new CommandException(e);
        } finally {
            if (!save && delete) {
                if (zipFile != null) {
                    if (!zipFile.delete()) {
                        zipFile.deleteOnExit();
                    }
                }
            }
        }

        return SUCCESS;
    }

    final String getInstallDir() {
        return installDir;
    }

    final String getArchiveName() {
        return archiveName;
    }

    final boolean getForce() {
        return force;
    }


    private File createZipFileIfNeeded(List<SFTPPath> binDirFiles) throws IOException, ZipFileException {
        final String baseRootValue = getSystemProperty(SystemPropertyConstants.PRODUCT_ROOT_PROPERTY);
        final File installRoot = new File(baseRootValue);
        final File zipFileDir;
        final File zipFile;
        if (archive == null) {
            zipFile = File.createTempFile("glassfish", ".zip");
        } else {
            zipFile = new File(archive);
            zipFileDir = new File(archive.substring(0, archive.lastIndexOf("/")));
            if (zipFile.exists() && !create) {
                logger.log(FINER, "Found {0}", archive);
                delete = false;
                return zipFile;
            } else if (!zipFileDir.canWrite()) {
                throw new IOException("Cannot write to " + archive);
            }
        }
        archiveName = zipFile.getName();

        FileListerRelative lister = new FileListerRelative(installRoot);
        lister.keepEmptyDirectories();
        String[] files = lister.getFiles();
        ArrayList<String> resultFiles = new ArrayList<>(Arrays.asList(files));
        logger.finer(() -> "Number of files to be zipped = " + resultFiles.size());

        Iterator<String> iter = resultFiles.iterator();
        while (iter.hasNext()) {
            final File file = new File(iter.next());
            final SFTPPath path = SFTPPath.of(file);
            if (archiveName.equals(file.getName())) {
                logger.log(FINER, "Removing file = {0}", path);
                iter.remove();
                continue;
            }
            if (path.contains("domains") || path.contains("nodes")) {
                iter.remove();
            } else if (isFileWithinBinDirectory(path) || "nadmin".equals(file.getName())) {
                binDirFiles.add(path);
            }
        }

        logger.finer(() -> "Final number of files to be zipped = " + resultFiles.size());

        String[] filesToZip = new String[resultFiles.size()];
        filesToZip = resultFiles.toArray(filesToZip);

        ZipWriter writer = new ZipWriter(FileUtils.safeGetCanonicalPath(zipFile), installRoot.toString(), filesToZip);
        writer.safeWrite();
        logger.info("Created installation zip " + FileUtils.safeGetCanonicalPath(zipFile));

        return zipFile;
    }

    /**
     * Determines if a file is under "bin" directory
     * @param file path to the file
     * @return true if file is under "bin" dir, false otherwise
     */
    private static boolean isFileWithinBinDirectory(Path path) {
        Path parent = path.getParent();
        if (parent == null || parent.getFileName() == null) {
            return false;
        }
        return parent.getFileName().toString().equals("bin");
    }

    public static String toString(InputStream ins) throws IOException {
        StringWriter sw = new StringWriter();
        InputStreamReader reader = new InputStreamReader(ins);

        char[] buffer = new char[4096];
        int n;
        while ((n = reader.read(buffer)) >= 0) {
            sw.write(buffer, 0, n);
        }

        return sw.toString();
    }
}
