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

/*
 * ZipStorage.java
 *
 * Created on January 30, 2004, 7:15 PM
 */

package com.sun.enterprise.backup;

import com.sun.enterprise.util.io.FileListerRelative;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.zip.ZipFileException;
import com.sun.enterprise.util.zip.ZipWriter;

import java.io.File;

/**
 * This class implements storing backups as zip files.
 * @author Byron Nevins
 */
class ZipStorage {

    /**
     * @param req
     * @throws BackupException
     */
    ZipStorage(BackupRequest req) throws BackupException {

        if(req == null)
            throw new BackupException("backup-res.NoBackupRequest",
                getClass().getName() + ".ctor");

        request = req;
    }

    /**
     * Backups the files to a zip file.
     * @throws BackupException if there were any errors writing the file.
     */
    void store() throws BackupException {

        File backupFileDir = null;
        if (request.configOnly) {
            backupFileDir = new File(request.domainDir, Constants.CONFIG_DIR) ;
        } else {
            backupFileDir = request.domainDir;
        }

        String zipName = FileUtils.safeGetCanonicalPath(request.backupFile);
        String domainDirName = FileUtils.safeGetCanonicalPath(backupFileDir);

        FileListerRelative lister = new FileListerRelative(backupFileDir);
        lister.keepEmptyDirectories(); // we want to restore any empty directories too!
        String[] files = lister.getFiles();

        LoggerHelper.fine("Writing " + zipName);

        try {

            ZipWriter writer = new ZipWriter(zipName, domainDirName, files);

            if(request.excludeDirs != null && request.excludeDirs.length > 0)
                writer.excludeDirs(request.excludeDirs);

            writer.safeWrite();
        }
        catch(ZipFileException zfe)  {
            throw new BackupException("backup-res.ZipBackupError", zfe, zipName);
        }
    }

    void write() throws BackupException  {

    }

    private    BackupRequest request;
}
