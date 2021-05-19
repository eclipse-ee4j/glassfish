/*
 * Copyright (c) 2010, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.admin.cli.remote.RemoteCLICommand;
import java.io.File;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import com.sun.enterprise.admin.cli.remote.RemoteCommand;
import com.sun.enterprise.backup.BackupException;
import com.sun.enterprise.backup.BackupManager;
import com.sun.enterprise.backup.BackupWarningException;
import com.sun.enterprise.universal.i18n.LocalStringsImpl;
import com.sun.enterprise.util.ObjectAnalyzer;

/**
 * This is a local command for backing-up domains.
 * The Options:
 *  <ul>
 *  <li>domaindir
 *  </ul>
 * The Operand:
 *  <ul>
 *  <li>domain
 *  </ul>
 */
@Service(name = "backup-domain")
@PerLookup
public final class BackupDomainCommand extends BackupCommands {

    @Param(name = "description", optional = true)
    String description;

    @Param(name="_force",optional=true)
    String force;

    @Param(name="_recyclelimit",optional=true)
    String recycleLimit;

    private static final LocalStringsImpl strings =
            new LocalStringsImpl(BackupDomainCommand.class);

    @Override
    protected void validate()
            throws CommandException {
        // only if domain name is not specified, it should try to find one
        if (domainName == null)
            super.validate();

        checkOptions();

        setDomainName(domainName);
        initDomain();

        File domainFile = new File(new File(domainDirParam), domainName);

        if (!isWritableDirectory(domainFile)) {
            throw new CommandException(
                strings.get("InvalidDirectory", domainFile.getPath()));
        }

        if (force == null ) {
            if (isRunning()) {
                boolean suspendAvailable = canSuspend();

                if (suspendAvailable && !isSuspended()) {
                    throw new CommandException(
                        strings.get("DomainIsNotSuspended", domainName));
                } else if (!suspendAvailable) {
                    throw new CommandException(strings.get("DomainIsNotStopped",
                        domainName));
                }
            }
        }

        int limit = 0;
        if (recycleLimit != null ) {
            try {
                limit = Integer.parseInt(recycleLimit.trim());
            } catch (NumberFormatException ex) {
                limit = -1;
            }
            if (limit < 0) {
                throw new CommandException(
                        strings.get("InvalidBackupRecycleLimit", recycleLimit));
            }
        }

        setDescription(description);
        setBackupDir(backupdir);
        setRecycleLimit(limit);
        prepareRequest();
        initializeLogger();     // in case program options changed
    }

    /**
     */
    @Override
    protected int executeCommand()
            throws CommandException {

        try {
            BackupManager mgr = new BackupManager(request);
            logger.info(mgr.backup());
        } catch (BackupWarningException bwe) {
            logger.info(bwe.getMessage());
        } catch (BackupException be) {
            throw new CommandException(be);
        }
        return 0;
    }

    @Override
    public String toString() {
        return super.toString() + "\n" + ObjectAnalyzer.toString(this);
    }

    /**
     * This method determines if the DAS has the ability to suspend itself.
     */
    private boolean canSuspend() {

        try {
            RemoteCLICommand cmd = new RemoteCLICommand("list-commands",
                                                  programOpts, env);
            String response = cmd.executeAndReturnOutput("list-commands");

            if (response.indexOf("suspend-domain") >= 0)
                return true;
        } catch (Exception e) {
            logger.info("Exception while probing DAS (list-commands): " +
                e.getMessage());
        }

        return false;
    }

    /**
     * This method determines if the DAS is currently suspended.
     */
    private boolean isSuspended() {

        try {
            RemoteCLICommand cmd = new RemoteCLICommand("suspend-domain",
                                                  programOpts, env);
            String response = cmd.executeAndReturnOutput("suspend-domain",
                                                         "--_test=true");

            if (response.indexOf("SUSPENDED=TRUE") >= 0)
                return true;

        } catch (Exception e) {
            logger.info("Exception while probing DAS (suspend-domain): " +
                e.getMessage());
        }

        return false;
    }
}
