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
 * BackupRequest.java
 *
 * Created on February 22, 2004, 1:40 AM
 */

package com.sun.enterprise.backup;

import com.sun.enterprise.util.ObjectAnalyzer;
import com.sun.enterprise.util.io.FileUtils;

import java.io.File;

/**
 * This class holds all of the values that the caller needs.
 * An instance of this class can be used to create a request object.
 * @author  bnevins
 */

public class BackupRequest {
    /**
     * Create an instance (generic)
     **/
    public BackupRequest(String domainsDirName, String domain,
                         File backupDir, String backupConfig,boolean configonly) {
        setDomainsDir(domainsDirName);
        setBackupDir(backupDir);
        setBackupConfig(backupConfig);
        domainName = domain;
        configOnly = configonly;
    }

    /**
     * Create an instance (used by backup-domain and list-backups)
     **/
    public BackupRequest(String domainsDirName, String domain,
                         File backupDir, String backupConfig,
                         String desc, int limit,boolean configonly) {
        this(domainsDirName, domain, backupDir, backupConfig,configonly);
        setDescription(desc);
        setRecycleLimit(limit);
    }

    /**
     * Create an instance (used by restore-domain)
     **/
    public BackupRequest(String domainsDirName, String domain,
                         File backupDir, String backupConfig,
                         String backupFileName,boolean configonly) {
        this(domainsDirName, domain, backupDir, backupConfig,configonly);
        if (backupFileName != null)
            setBackupFile(backupFileName);
    }

    ///////////////////////////////////////////////////////////////////////////

    public void setTerse(boolean b) {
        terse = b;
    }

    public void setVerbose(boolean b) {
        verbose = b;
    }

    public String toString() {
        return ObjectAnalyzer.toString(this);
    }

    public void setForce(boolean f) {
        force = f;
    }

    ///////////////////////////////////////////////////////////////////////////

    private void setDomainsDir(String name) {
        domainsDir = FileUtils.safeGetCanonicalFile(new File(name));
    }

    private void setBackupDir(File dir) {
        backupDir = dir;
    }

    private void setRecycleLimit(int limit) {
        recycleLimit = limit;
    }

    private void setDescription(String desc) {
        description = desc;
    }

    private void setBackupFile(String name) {
        backupFile = FileUtils.safeGetCanonicalFile(new File(name));
    }

    private void setBackupConfig(String name) {
        backupConfig = name;
    }

    ///////////////////////////////////////////////////////////////////////////
    ////////////     Variables     ////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////

    final static String[] excludeDirs = {Constants.BACKUP_DIR + "/",
                                         Constants.OSGI_CACHE + "/"};

    File    domainsDir;
    String  domainName;
    String  description;
    int     recycleLimit = 0;
    File    backupDir = null;
    String  backupConfig = null;
    boolean configOnly = false;

    // VARIABLES POSSIBLY SET AT RUNTIME
    File    backupFile;

    // VARIABLES SET AT RUNTIME
    File    domainDir;
    long    timestamp;

    boolean terse = false;
    boolean verbose = false;
    boolean force = false;
}
