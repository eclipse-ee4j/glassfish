/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.admin.servermgmt.pe.PEDomainsManager;
import com.sun.enterprise.universal.process.ProcessUtils;
import com.sun.enterprise.util.HostAndPort;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * The change-master-password command for the DAS.
 * This is a hidden command which is called from change-master-password command.
 *
 * @author Bhakti Mehta
 */
@Service(name = "_change-master-password-das")
@PerLookup
public class ChangeMasterPasswordCommandDAS extends LocalDomainCommand {

    @Param(name = "domain", primary = true, optional = true)
    protected String domainName0;

    @Param(name = "savemasterpassword", optional = true, defaultValue = "false")
    protected boolean savemp;

    private static final LocalStringsImpl strings = new LocalStringsImpl(ChangeMasterPasswordCommandDAS.class);

    @Override
    protected void validate() throws CommandException {
        String dName;
        if (domainName0 != null) {
            dName = domainName0;
        } else {
            dName = getDomainName();
        }
        setDomainName(dName);
        super.validate();
    }

    @Override
    public int execute(String... argv) throws CommandException {
        // This will parse the args and then call executeCommand
        return super.execute(argv);
    }

    @Override
    protected int executeCommand() throws CommandException {

        try {
            HostAndPort adminAddress = getAdminAddress();
            if (ProcessUtils.isListening(adminAddress)) {
                throw new CommandException(strings.get("domain.is.running", getDomainName(), getDomainRootDir()));
            }
            DomainConfig domainConfig = new DomainConfig(getDomainName(), getDomainsDir().getAbsolutePath());
            PEDomainsManager manager = new PEDomainsManager();
            String mp = super.readFromMasterPasswordFile();
            if (mp == null) {
                mp = passwords.get("AS_ADMIN_MASTERPASSWORD");
                if (mp == null) {
                    char[] mpCharArr = super.readPassword(strings.get("current.mp"));
                    mp = mpCharArr != null ? new String(mpCharArr) : null;
                }
            }
            if (mp == null) {
                throw new CommandException(strings.get("no.console"));
            }
            if (!super.verifyMasterPassword(mp)) {
                throw new CommandException(strings.get("incorrect.mp"));
            }
            char[] nmpCharArr = getPassword("newmasterpassword", strings.get("new.mp"), strings.get("new.mp.again"), true);
            String nmp = nmpCharArr != null ? new String(nmpCharArr) : null;
            if (nmp == null) {
                throw new CommandException(strings.get("no.console"));
            }
            if (nmp.trim().length() < 6) {
                throw new CommandException(strings.get("incorrect.password.length"));
            }
            domainConfig.put(DomainConfig.K_MASTER_PASSWORD, mp);
            domainConfig.put(DomainConfig.K_NEW_MASTER_PASSWORD, nmp);
            domainConfig.put(DomainConfig.K_SAVE_MASTER_PASSWORD, savemp);
            manager.changeMasterPassword(domainConfig);

            return 0;
        } catch (Exception e) {
            throw new CommandException(e.getMessage(), e);
        }
    }
}
