/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.cli.optional;

import com.sun.enterprise.admin.servermgmt.cli.LocalDomainCommand;
import com.sun.enterprise.backup.BackupRequest;
import com.sun.enterprise.util.ObjectAnalyzer;

import java.io.File;
import java.io.IOException;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;

import static com.sun.enterprise.util.Utility.isEmpty;
import static com.sun.enterprise.util.io.DomainDirs.getDefaultDomainsDir;

/**
 * This is a local command for backing-up domains.
 *
 * <p>
 * The Options:
 * <ul>
 *   <li>domaindir
 * </ul>
 *
 * The Operand:
 * <ul>
 *   <li>domain_name
 * </ul>
 */
public abstract class BackupCommands extends LocalDomainCommand {

    private static final LocalStringsImpl strings = new LocalStringsImpl(BackupCommands.class);

    @Param(name = "long", shortName = "l", alias = "verbose", optional = true)
    boolean verbose;

    @Param(name = "domain_name", primary = true, optional = true)
    String domainName;

    @Param(name = "_configonly", optional = true)
    String configonly;

    @Param(optional = true)
    String backupConfig;

    @Param(optional = true)
    String backupdir;

    BackupRequest request;
    private String description;
    private int recycleLimit = 0;

    /**
     * A method that checks the options and operand that the user supplied. These tests are slightly different for different
     * CLI commands
     */
    protected void checkOptions() throws CommandException {
        if (verbose && programOpts.isTerse()) {
            throw new CommandValidationException(strings.get("NoVerboseAndTerseAtTheSameTime"));
        }

        if (isEmpty(domainDirParam)) {
            try {
                domainDirParam = getDefaultDomainsDir().getPath();
            } catch (IOException ioe) {
                throw new CommandException(ioe.getMessage());
            }
        }

        File domainsDirFile = new File(domainDirParam);

        // Make sure domainsDir exists and is a directory
        if (!domainsDirFile.isDirectory()) {
            throw new CommandValidationException(strings.get("InvalidDomainPath", domainDirParam));
        }

        // If user hasn't specified domain_name, get the default one
        if (domainName == null) {
            domainName = getDomainName();
        }

    }

    protected void setDescription(String description) {
        this.description = description;
    }

    protected void setBackupDir(String backupdir) {
        this.backupdir = backupdir;
    }

    protected void setRecycleLimit(int recycleLimit) {
        this.recycleLimit = recycleLimit;
    }

    protected void prepareRequest() throws CommandValidationException {
        File backupdirFile = null;
        if (backupdir != null) {
            backupdirFile = new File(backupdir);
            if (!backupdirFile.isAbsolute()) {
                throw new CommandValidationException(strings.get("InvalidBackupDirPath", backupdir));
            }
        }

        boolean configonlybackup = false;
        if (Boolean.valueOf(configonly)) {
            configonlybackup = true;
        }

        request = new BackupRequest(domainDirParam, domainName, backupdirFile, backupConfig, description, recycleLimit, configonlybackup);
        request.setTerse(programOpts.isTerse());
        request.setVerbose(verbose);
    }

    /*
     * Method to check if the file is writable directory
     */
    protected boolean isWritableDirectory(File domainFile) {
        if (domainFile.isDirectory() || domainFile.canWrite()) {
            return true;
        }

        return false;
    }

    @Override
    public String toString() {
        return super.toString() + "\n" + ObjectAnalyzer.toString(this);
    }

}
