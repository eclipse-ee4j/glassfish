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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.internal.api.Globals;
import org.jvnet.hk2.annotations.Service;

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

    abstract void copyToHosts(File zipFile, ArrayList<String> binDirFiles) throws CommandException;
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
            ArrayList<String> binDirFiles = new ArrayList<String>();
            precopy();
            zipFile = createZipFileIfNeeded(binDirFiles);
            copyToHosts(zipFile, binDirFiles);
        }
        catch (CommandException e) {
            throw e;
        }
        catch (Exception e) {
            throw new CommandException(e);
        }
        finally {
            if (!save && delete) {
                if (zipFile != null) {
                    if (!zipFile.delete())
                        zipFile.deleteOnExit();
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


    private File createZipFileIfNeeded(ArrayList<String> binDirFiles) throws IOException, ZipFileException {
        String baseRootValue = getSystemProperty(SystemPropertyConstants.PRODUCT_ROOT_PROPERTY);
        File installRoot = new File(baseRootValue);

        File zipFileLocation = null;
        File glassFishZipFile = null;

        if (archive != null) {
            archive = archive.replace('\\', '/');
            archiveName = archive.substring(archive.lastIndexOf("/") + 1, archive.length());
            zipFileLocation = new File(archive.substring(0, archive.lastIndexOf("/")));
            glassFishZipFile = new File(archive);
            if (glassFishZipFile.exists() && !create) {
                if (logger.isLoggable(Level.FINER))
                    logger.finer("Found " + archive);
                delete = false;
                return glassFishZipFile;
            }
            else if (!zipFileLocation.canWrite()) {
                throw new IOException("Cannot write to " + archive);
            }
        }
        else {
            zipFileLocation = new File(".");
            if (!zipFileLocation.canWrite()) {
                zipFileLocation = new File(System.getProperty("java.io.tmpdir"));
            }
            glassFishZipFile = File.createTempFile("glassfish", ".zip", zipFileLocation);
            String filePath = glassFishZipFile.getCanonicalPath();
            filePath = filePath.replaceAll("\\\\", "/");
            archiveName = filePath.substring(filePath.lastIndexOf("/") + 1, filePath.length());
        }

        FileListerRelative lister = new FileListerRelative(installRoot);
        lister.keepEmptyDirectories();
        String[] files = lister.getFiles();

        List<String> resultFiles1 = Arrays.asList(files);
        ArrayList<String> resultFiles = new ArrayList<String>(resultFiles1);

        if (logger.isLoggable(Level.FINER))
            logger.finer("Number of files to be zipped = " +
                                                            resultFiles.size());

        Iterator<String> iter = resultFiles.iterator();
        while (iter.hasNext()) {
            String fileName = iter.next();
            String fPath = fileName.substring(fileName.lastIndexOf("/") + 1);
            if (fPath.equals(glassFishZipFile.getName())) {
                if (logger.isLoggable(Level.FINER))
                    logger.finer("Removing file = " + fileName);
                iter.remove();
                continue;
            }
            if (fileName.contains("domains") || fileName.contains("nodes")) {
                iter.remove();
            }
            else if (isFileWithinBinDirectory(fileName)) {
                binDirFiles.add(fileName);
            }
        }

        if (logger.isLoggable(Level.FINER))
            logger.finer("Final number of files to be zipped = " +
                                                            resultFiles.size());

        String[] filesToZip = new String[resultFiles.size()];
        filesToZip = resultFiles.toArray(filesToZip);

        ZipWriter writer = new ZipWriter(FileUtils.safeGetCanonicalPath(glassFishZipFile), installRoot.toString(), filesToZip);
        writer.safeWrite();
        logger.info("Created installation zip " + FileUtils.safeGetCanonicalPath(glassFishZipFile));

        return glassFishZipFile;
    }

    /**
     * Determines if a file is under "bin" directory
     * @param file path to the file
     * @return true if file is under "bin" dir, false otherwise
     */
    private static boolean isFileWithinBinDirectory(String file) {
        String parent = null;
        //for top-level files, parent would be null
        String pFile = new File(file).getParent();
        if (pFile != null) {
            parent = new File(pFile).getName();
        }
        return parent != null && parent.equals("bin");
    }

    public static String toString(InputStream ins) throws IOException {
        StringWriter sw = new StringWriter();
        InputStreamReader reader = new InputStreamReader(ins);

        char[] buffer = new char[4096];
        int n;
        while ((n = reader.read(buffer)) >= 0)
            sw.write(buffer, 0, n);

        return sw.toString();
    }
}
