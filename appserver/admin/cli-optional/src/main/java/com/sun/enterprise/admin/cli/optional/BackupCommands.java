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

package com.sun.enterprise.admin.cli.optional;

import java.io.*;

import org.glassfish.api.admin.*;
import org.glassfish.api.Param;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.cli.*;
import com.sun.enterprise.admin.servermgmt.cli.LocalDomainCommand;
import com.sun.enterprise.backup.BackupRequest;
import com.sun.enterprise.util.ObjectAnalyzer;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;

import com.sun.enterprise.util.io.DomainDirs;

/**
 * This is a local command for backing-up domains.
 * The Options:
 *  <ul>
 *  <li>domaindir
 *  </ul>
 * The Operand:
 *  <ul>
 *  <li>domain_name
 *  </ul>
 */

public abstract class BackupCommands extends LocalDomainCommand {

    BackupRequest   request;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(BackupCommands.class);

    @Param(name = "long", shortName="l", alias = "verbose", optional = true)
    boolean verbose;

    @Param(name = "domain_name", primary = true, optional = true)
    String domainName;

    @Param(name= "_configonly", optional = true)
    String configonly;

    @Param(optional = true)
    String backupConfig;

    @Param(optional = true)
    String backupdir;


    private String desc = null;

    private int recycleLimit = 0;

     /**
     * A method that checks the options and operand that the user supplied.
     * These tests are slightly different for different CLI commands
     */
    protected void checkOptions() throws CommandException {
        if (verbose && programOpts.isTerse())
            throw new CommandValidationException(
                strings.get("NoVerboseAndTerseAtTheSameTime"));

        if (domainDirParam == null || domainDirParam.length() <= 0) {

            try {

                domainDirParam = DomainDirs.getDefaultDomainsDir().getPath();
            } catch (IOException ioe) {
                throw new CommandException(ioe.getMessage());
            }
        }

        File domainsDirFile = new File(domainDirParam);

        // make sure domainsDir exists and is a directory
        if (!domainsDirFile.isDirectory()) {
            throw new CommandValidationException(
                strings.get("InvalidDomainPath", domainDirParam));
        }

        // if user hasn't specified domain_name, get the default one

        if (domainName == null)
            domainName = getDomainName();

    }

    protected void setDescription(String d) {
        desc = d;
    }

    protected void setBackupDir(String dir) {
        backupdir = dir;
    }

    protected void setRecycleLimit(int limit) {
        recycleLimit = limit;
    }

    protected void prepareRequest() throws CommandValidationException {

        File backupdir_f = null;
        if (backupdir != null) {
            backupdir_f = new File(backupdir);
            if (!backupdir_f.isAbsolute()) {
                throw new CommandValidationException(
                    strings.get("InvalidBackupDirPath", backupdir));
            }
        }
        boolean configonlybackup = false;
        if ((configonly != null) && ( Boolean.valueOf(configonly))) {
            configonlybackup = true;
        }
        request = new BackupRequest(domainDirParam, domainName, backupdir_f,
                                    backupConfig, desc, recycleLimit,configonlybackup);

        request.setTerse(programOpts.isTerse());
        request.setVerbose(verbose);
    }

    /*
     * Method to check if the file is writable directory
     */
    protected boolean isWritableDirectory(File domainFile) {
        boolean result = false;
        if (domainFile.isDirectory() || domainFile.canWrite()) {
            result = true;
        }
        return result;
    }

    @Override
    public String toString() {
        return super.toString() + "\n" + ObjectAnalyzer.toString(this);
    }

}
