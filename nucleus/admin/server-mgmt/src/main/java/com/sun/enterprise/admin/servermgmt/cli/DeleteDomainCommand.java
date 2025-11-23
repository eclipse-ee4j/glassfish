/*
 * Copyright (c) 2022, 2025 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.servermgmt.cli;

import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.pe.PEDomainsManager;

import java.io.File;
import java.io.IOException;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * This is a local command that deletes a domain.
 */
@Service(name = "delete-domain")
@PerLookup
public final class DeleteDomainCommand extends LocalDomainCommand {

    @Param(name = "domain_name", primary = true)
    private String domainName0;

    private static final LocalStringsImpl strings = new LocalStringsImpl(DeleteDomainCommand.class);

    @Override
    protected void validate() throws CommandException, CommandValidationException {
        setDomainName(domainName0);
        super.validate();
    }

    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {

        try {
            DomainConfig domainConfig = new DomainConfig(getDomainName(), getDomainsDir().getPath());
            checkRunning();
            checkRename();
            DomainsManager manager = new PEDomainsManager();
            manager.deleteDomain(domainConfig);
            // By default, do as what v2 does -- don't delete the entry -
            // might need a revisit (Kedar: 09/16/2009)
            //deleteLoginInfo();
        } catch (Exception e) {
            throw new CommandException(e.getLocalizedMessage(), e);
        }

        logger.fine(strings.get("DomainDeleted", getDomainName()));
        return 0;
    }

    private void checkRunning() throws CommandException {
        // don't prompt for password
        programOpts.setInteractive(false);
        if (isThisDAS(getDomainRootDir()) && getReachableAdminAddress() != null) {
            throw new CommandException(
                "Domain " + getDomainName() + " at " + getDomainRootDir() + " is running. Stop it first.");
        }
    }

    /**
     * Check that the domain directory can be renamed, to increase the likelyhood that it can be deleted.
     */
    private void checkRename() {
        boolean ok = true;
        try {
            File root = getDomainsDir();
            File domdir = new File(root, getDomainName());
            File tmpdir = File.createTempFile("del-", "", root);

            ok = tmpdir.delete() && domdir.renameTo(tmpdir) && tmpdir.renameTo(domdir);
        } catch (IOException ioe) {
            ok = false;
        }
        if (!ok) {
            String msg = strings.get("domain.fileinuse", getDomainName(), getDomainRootDir());
            throw new IllegalStateException(msg);
        }
    }

}
