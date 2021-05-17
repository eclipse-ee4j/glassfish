/*
 * Copyright (c) 2011, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.File;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.backup.BackupException;
import com.sun.enterprise.backup.BackupRequest;
import com.sun.enterprise.backup.BackupWarningException;
import com.sun.enterprise.backup.RestoreManager;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.ObjectAnalyzer;

/**
 * This is a local command for restoring domains.
 * The Options:
 *  <ul>
 *  <li>domaindir
 *  </ul>
 * The Operand:
 *  <ul>
 *  <li>domain_name
 *  </ul>
 */
@Service(name = "restore-domain")
@PerLookup
public final class RestoreDomainCommand extends BackupCommands {

    @Param(name = "filename", optional = true)
    private String backupFilename;

    @Param(name= "force", optional = true, defaultValue = "false")
    private boolean force;

    @Param(name= "description", optional = true, obsolete = true)
    private String description;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(BackupDomainCommand.class);

    /**
     */
    @Override
    protected void validate()
            throws CommandException, CommandValidationException {

        boolean domainExists = true;

        if (backupFilename == null && domainName == null) {
            if (!force) {
                throw new CommandException(strings.get("UseForceOption"));
            }
            // this will properly initialize the domain dir
            // see LocalDomainCommand.initDomain())
            super.validate();
        }

        checkOptions();

        try {
            setDomainName(domainName);
            initDomain();
        } catch (CommandException e) {
            if (e.getCause() != null &&
                (e.getCause() instanceof java.io.IOException)) {
                // The domain does not exist which is allowed if the
                // force option is used (checked later).
                domainExists = false;
            } else {
                throw e;
            }
        }

        if (domainExists && isRunning()) {
            throw new CommandException(strings.get("DomainIsNotStopped",
                                       domainName));
        }

        if (backupFilename != null) {
            File f = new File(backupFilename);

            if (!f.exists()) {
                throw new CommandValidationException(
                    strings.get("FileDoesNotExist", backupFilename));
            }

            if (!f.canRead()) {
                throw new CommandValidationException(
                    strings.get("FileCanNotRead", backupFilename));
            }

            if (f.isDirectory()) {
                throw new CommandValidationException(
                    strings.get("FileIsDirectory", backupFilename));
            }
        }

        setBackupDir(backupdir);
        initRequest();

        initializeLogger();     // in case program options changed
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException {
        try {
            RestoreManager mgr = new RestoreManager(request);
            logger.info(mgr.restore());
        } catch (BackupWarningException bwe) {
            logger.info(bwe.getMessage());
        } catch (BackupException be) {
            throw new CommandException(be);
        }
        return 0;
    }

    private void initRequest() throws CommandValidationException {

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
                                    backupConfig, backupFilename,configonlybackup);
        request.setTerse(programOpts.isTerse());
        request.setVerbose(verbose);
        request.setForce(force);
    }

    @Override
    public String toString() {
        return super.toString() + "\n" + ObjectAnalyzer.toString(this);
    }

}
