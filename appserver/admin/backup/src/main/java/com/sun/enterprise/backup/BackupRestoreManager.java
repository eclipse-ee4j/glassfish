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

package com.sun.enterprise.backup;

import com.sun.enterprise.util.io.FileUtils;

import java.io.File;

/**
 * Baseclass for BackupManager and RestoreManager.  Common code between
 * the two goes in here.
 * @author  Byron Nevins
 */

abstract class BackupRestoreManager {

    public BackupRestoreManager(BackupRequest req) throws BackupException {
        if(req == null)
            throw new BackupException("backup-res.InternalError",
                getClass().getName() + ".ctor: null BackupRequest object");

        this.request = req;
        init();
        LoggerHelper.finest("Request DUMP **********\n" + req);
    }

    void init() throws BackupException {

        // only do once!
        if(wasInitialized)
            return;

        if(request == null)
            throw new BackupException("backup-res.InternalError",
                                      "null BackupRequest reference");

        // add a timestamp
        request.timestamp = System.currentTimeMillis();

        // validate domains dir
        if (request.domainsDir == null ||
            !FileUtils.safeIsDirectory(request.domainsDir))
            throw new BackupException("backup-res.NoDomainsDir",
                                      request.domainsDir);

        if (request.domainName != null)
            request.domainDir = new File(request.domainsDir, request.domainName);

        LoggerHelper.setLevel(request);
    }

    /**
     * If both the backupDir and backupConfig are not set then this method
     * behaves as it did in v2.  It returns a path to the
     * domainDir + BACKUP_DIR (backups).
     * If a backupConfig has been associated with the request and the
     * backupDir is not set then it returns a path to domainDir + backupConfig.
     * If a backupConfig has been associated with the request and the
     * backupDir is set then it returns a path to backupDir + domainName +
     * backupConfig.
     * If a backupConfig has not been associated with the request and the
     * backupDir is set then it returns a path to backupDir + domainName.
     */
    protected File getBackupDirectory(BackupRequest request) {

        // The v2 case.
        if (request.backupDir == null && request.backupConfig == null) {
            return (new File(request.domainDir, Constants.BACKUP_DIR));
        }

        if (request.backupDir == null && request.backupConfig != null) {
            return (new File(new File(request.domainDir, Constants.BACKUP_DIR),
                             request.backupConfig));
        }

        if (request.backupDir != null && request.backupConfig != null) {
            return (new File(new File(request.backupDir, request.domainName),
                             request.backupConfig));
        }

        // backupDir != null && backupConfig == null
        return (new File(request.backupDir, request.domainName));
    }


    BackupRequest   request;
    private boolean wasInitialized = false;
}
