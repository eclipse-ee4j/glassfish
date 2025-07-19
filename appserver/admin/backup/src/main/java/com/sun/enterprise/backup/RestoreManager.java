/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import com.sun.enterprise.backup.util.BackupUtils;
import com.sun.enterprise.util.io.FileUtils;
import com.sun.enterprise.util.zip.ZipFile;

import java.io.File;
import java.io.IOException;

import static com.sun.enterprise.util.SystemPropertyConstants.MASTER_PASSWORD_FILENAME;

/**
 *
 * @author  Byron Nevins
 */

public class RestoreManager extends BackupRestoreManager {

    public RestoreManager(BackupRequest req) throws BackupException {
        super(req);
    }

    //////////////////////////////////////////////////////////////////////////

    public String restore() throws BackupException {
        try {
            boolean isConfigBackup = isAConfigBackup();

            checkDomainName();
            ZipFile zf = new ZipFile(request.backupFile, tempRestoreDir);

            zf.explode();
            sanityCheckExplodedFiles();

            // If we are restoring the whole domain then we need to preserve
            // the backups directory.
            if (!isConfigBackup) {
                copyBackups();
            }
            atomicSwap(request.domainDir, request.domainName, isConfigBackup);
            setPermissions();
            String mesg = readAndDeletePropsFile(isConfigBackup);
            return mesg;
        }
        catch(BackupException be) {
            throw be;
        }
        catch(Exception e) {
            throw new BackupException("Restore Error" + e.toString(), e);
        }
    }

    //////////////////////////////////////////////////////////////////////////

    @Override
    void init() throws BackupException {
        super.init();

        if(request.backupFile == null) {
            initWithNoSpecifiedBackupFile();
        } else {
            initWithSpecifiedBackupFile();
        }

        tempRestoreDir = new File(request.domainsDir, request.domainName +
            "_" + System.currentTimeMillis());
    }

    //////////////////////////////////////////////////////////////////////////

    private void initWithSpecifiedBackupFile() throws BackupException {
        if(request.backupFile.length() <= 0) {
            throw new BackupException("backup-res.CorruptBackupFile",
                                      request.backupFile);
        }

        if (request.domainName == null) {

            if (!request.force) {
                throw new BackupException("backup-res.UseForceOption");
            }

            Status status = new Status();
            status.read(request.backupFile);
            request.domainName = status.getDomainName();

            request.domainDir = new File(request.domainsDir, request.domainName);
        }

        if(!FileUtils.safeIsDirectory(request.domainDir)) {
            if (!request.domainDir.mkdirs()) {
                throw new BackupException("backup-res.CantCreateDomainDir",
                                          request.domainDir);
            }
        }

        backupDir = new File(request.domainDir, Constants.BACKUP_DIR);

        // It's NOT an error to not exist.  The domain may not exist
        // currently and, besides, they are specifying the backup-file
        // from anywhere potentially...
        if(!FileUtils.safeIsDirectory(backupDir)) {
            backupDir = null;
        }

        //throw new BackupException("NOT YET IMPLEMENTED");

    }

    //////////////////////////////////////////////////////////////////////////

    private void initWithNoSpecifiedBackupFile() throws BackupException {
        // if they did NOT specify a backupFile, then we *must* have a
        // pre-existing backups directory in a pre-existing domain directory.

        if(!FileUtils.safeIsDirectory(request.domainDir)) {
            throw new BackupException("backup-res.NoDomainDir",
                                      request.domainDir);
        }

        backupDir = getBackupDirectory(request);

        // It's an error to not exist...
        if(!FileUtils.safeIsDirectory(backupDir)) {
            throw new BackupException("backup-res.NoBackupDir", backupDir);
        }

        BackupFilenameManager bfmgr = new BackupFilenameManager(backupDir,
            request.domainName);
        request.backupFile = bfmgr.latest();

        //request.backupFile = getNewestZip(backupDir);
    }

    //////////////////////////////////////////////////////////////////////////

    /*
    private File getNewestZip(File dir) throws BackupException
    {
        File newestFile = null;
        long newestTime = 0;

        File[] zips = dir.listFiles(new ZipFilenameFilter());

        for(int i = 0; i < zips.length; i++)
        {
            //long time = zips[i].lastModified();
            Status status = new Status();
            long time = status.getInternalTimestamp(zips[i]);

            String msg = "filename: " + zips[i] + ", ts= " + time;
            if(time > newestTime)
            {
                newestFile = zips[i];
                newestTime = time;
                msg += " --- newest file so far";
            }
            else
                msg += " -- NOT newest";

            System.out.println(msg);
        }

        if(newestFile == null)
            throw new BackupException("backup-res.NoBackupFiles", dir);

        return newestFile;
    }
    */
    //////////////////////////////////////////////////////////////////////////

    private void copyBackups() throws IOException    {

        File domainBackupDir =
            new File(request.domainDir, Constants.BACKUP_DIR);

        /**
         * If an existing backup directory does not exist then there
         * is nothing to copy.
         */
        if(!FileUtils.safeIsDirectory(domainBackupDir)) {
            return;
        }

        File tempRestoreDirBackups = new File(tempRestoreDir,
                                                  Constants.BACKUP_DIR);
        FileUtils.copyTree(domainBackupDir, tempRestoreDirBackups);
    }

    //////////////////////////////////////////////////////////////////////////

    private void atomicSwap(File domainDir, String domainName,
                            boolean configOnly) throws BackupException {

        File oldDir;
        File configDir = new File (request.domainsDir, domainName + "/" +
        Constants.CONFIG_DIR);

        // 1 -- rename original dir
        // 2 -- rename restored dir to domain/config dir
        // 3 -- delete original dir

        // The current domain will be copied to oldDir.
        if (configOnly) {
            oldDir = new File(request.domainsDir, domainName + "/" +
            Constants.CONFIG_DIR + OLD_DOMAIN_SUFFIX +
                System.currentTimeMillis());
        } else {
            oldDir = new File(request.domainsDir,
                domainName + OLD_DOMAIN_SUFFIX + System.currentTimeMillis());
        }

        // Move the current domain to the side.
        // On Error -- just fail and delete the new files
        if (configOnly) {
            if (!configDir.renameTo(oldDir)) {
                FileUtils.whack(tempRestoreDir);
                throw new BackupException("backup-res.CantRenameOriginalDomain",
                                          configDir);
            }
        } else if (!domainDir.renameTo(oldDir)) {
            FileUtils.whack(tempRestoreDir);
            throw new BackupException("backup-res.CantRenameOriginalDomain",
                                      domainDir);
        }

        // Move the restored domain from the temp location to the domain dir.
        // On Error -- Delete the new files and undo the rename that was done
        // successfully above
        if (configOnly) {
            if(!tempRestoreDir.renameTo(configDir)) {
                FileUtils.whack(tempRestoreDir);
                if (!oldDir.renameTo(configDir)) {
                    throw new BackupException("backup-res.CantRevertOldDomain",
                                               configDir);
                }
                throw new BackupException("backup-res.CantRenameRestoredDomain");
            }
        } else if(!tempRestoreDir.renameTo(domainDir)) {
            FileUtils.whack(tempRestoreDir);
            if (!oldDir.renameTo(domainDir)) {
                throw new BackupException("backup-res.CantRevertOldDomain",
                                           domainDir);
            }
            throw new BackupException("backup-res.CantRenameRestoredDomain");
        }

        FileUtils.whack(oldDir);
    }

    //////////////////////////////////////////////////////////////////////////

    private String readAndDeletePropsFile(boolean isConfigBackup) {
        // The "backup.properties" file from the restored zip should be
        // in the domain dir now.
        File propsFile;
        String mesg = "";

        // If this is a config only restore the prop file will be in
        // the config directory.
        if (isConfigBackup) {
            propsFile = new File (request.domainDir, Constants.CONFIG_DIR +
                                  "/" + Constants.PROPS_FILENAME);
        } else {
            propsFile = new File(request.domainDir, Constants.PROPS_FILENAME);
        }

        if (request.verbose == true || request.terse != true) {
            if (isConfigBackup) {
                mesg = StringHelper.get("backup-res.SuccessfulConfigRestore",
                                        request.domainName, request.domainDir);
            } else {
                mesg = StringHelper.get("backup-res.SuccessfulFullRestore",
                                        request.domainName, request.domainDir);
            }
        }

        if (request.verbose == true) {
            Status status = new Status();
            mesg += "\n" + status.read(propsFile, false);
        }

        if (!propsFile.delete()) {
            propsFile.deleteOnExit();
        }

        return mesg;
    }

    /** zip is platform dependent -- so non-default permissions are gone!
     */

    private void setPermissions() {
        File backups        = new File(request.domainDir, "backups");
        File bin            = new File(request.domainDir, "bin");
        File config         = new File(request.domainDir, "config");
        File webtmp         = new File(request.domainDir, "generated/tmp");
        File masterPassword = new File(request.domainDir, MASTER_PASSWORD_FILENAME);

        // note that makeExecutable(File f) will make all the files under f
        // executable if f happens to be a directory.
        BackupUtils.makeExecutable(bin);

        BackupUtils.protect(backups);
        BackupUtils.protect(config);
        BackupUtils.protect(masterPassword);
        BackupUtils.protect(webtmp);

        // Jan 19, 2005 -- rolled back the fix for 6206176.  It has been decided
        // that this is not a bug but rather a security feature.
        //FileUtils.whack(webtmp);

        // fix: 6206176 -- instead of setting permissions for the tmp dir,
        // we just delete it.   This will allow, say, user 'A' to do the restore,
        // and then allow user 'B' (including root) to start the domain without
        // getting a web-container error.
        // see: bug 6194504 for the tmp dir details
        //old:
        //new:
    }

    //////////////////////////////////////////////////////////////////////////

    private void sanityCheckExplodedFiles() throws BackupException {
        // Is the "magic" properties file where it is supposed to be?

        File statusFile = new File(tempRestoreDir, Constants.PROPS_FILENAME);

        if(!statusFile.exists()) {
            // cleanup -- we are officially failing the restore!
            FileUtils.whack(tempRestoreDir);
            throw new BackupException(
                "backup-res.RestoreError.CorruptBackupFile.NoStatusFile",
                request.domainName);
        }
    }

    //////////////////////////////////////////////////////////////////////////

    private void checkDomainName() throws BackupException {
        Status status = new Status();
        status.read(request.backupFile);
        String buDomainName = status.getDomainName();

        if (buDomainName == null) {
            //this means the backup zip is bad...
            throw new BackupException(StringHelper.get(
                "backup-res.CorruptBackupFile", request.backupFile));
        } else if(!request.domainName.equals(buDomainName)) {
            if (!request.force) {
                throw new BackupException(
                    StringHelper.get("backup-res.DomainNameDifferentWarning",
                                     buDomainName, request.domainName));
            }
        }
    }

    // Return true if the backup contains only configuration (vs a full backup)
    private boolean isAConfigBackup() {
        Status status = new Status();
        status.read(request.backupFile);
        String backupType = status.getBackupType();

        if (backupType == null || backupType.equals("")) {
            // Backup from releases prior to 3.1 did not contain a type.
            // We assume the lack of the type means it is a Full backup.
            // the only type supported at the time.
            return false;
        }

        return backupType.equals(Constants.FULL) ? false : true;
    }

    //////////////////////////////////////////////////////////////////////////

    private static final    String OLD_DOMAIN_SUFFIX = "_beforeRestore_";
    private File tempRestoreDir;
    private File backupDir;
}
