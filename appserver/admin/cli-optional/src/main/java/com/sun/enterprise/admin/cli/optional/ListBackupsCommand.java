/*
 * Copyright (c) 2021 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.backup.BackupException;
import com.sun.enterprise.backup.BackupWarningException;
import com.sun.enterprise.backup.ListManager;
import com.sun.enterprise.util.ObjectAnalyzer;

import java.io.File;

import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * This is a local command for listing backed up domains.
 *
 */
@Service(name = "list-backups")
@PerLookup
public final class ListBackupsCommand extends BackupCommands {

    private static final LocalStringsImpl strings = new LocalStringsImpl(ListBackupsCommand.class);

    @Override
    protected void validate() throws CommandException {
        // Only if domain name is not specified, it should try to find one
        if (domainName == null) {
            super.validate();
        }

        checkOptions();

        File domainFile = new File(new File(domainDirParam), domainName);

        if (!isWritableDirectory(domainFile)) {
            throw new CommandException(strings.get("InvalidDirectory", domainFile.getPath()));
        }

        setBackupDir(backupdir);
        prepareRequest();
        initializeLogger(); // in case program options changed
    }

    @Override
    protected int executeCommand() throws CommandException {
        try {
            ListManager listManager = new ListManager(request);
            logger.info(listManager.list());
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

}
