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

import com.sun.enterprise.admin.cli.remote.RemoteCLICommand;
import com.sun.enterprise.admin.servermgmt.DomainConfig;
import com.sun.enterprise.admin.servermgmt.DomainsManager;
import com.sun.enterprise.admin.servermgmt.pe.PEDomainsManager;
import com.sun.enterprise.util.ColumnFormatter;
import com.sun.enterprise.util.HostAndPort;
import com.sun.enterprise.util.io.DomainDirs;

import java.io.File;
import java.util.List;
import java.util.logging.Level;

import org.glassfish.api.Param;
import org.glassfish.api.admin.CommandException;
import org.glassfish.api.admin.CommandValidationException;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jdke.i18n.LocalStringsImpl;
import org.jvnet.hk2.annotations.Service;

/**
 * This is a local command that lists the domains.
 */
@Service(name = "list-domains")
@PerLookup
public final class ListDomainsCommand extends LocalDomainCommand {
    private static final LocalStringsImpl strings = new LocalStringsImpl(ListDomainsCommand.class);

    @Param(name = "long", shortName = "l", optional = true)
    boolean longOpt;

    @Param(shortName = "h", optional = true, defaultValue = "true")
    boolean header;

    /*
     * Override the validate method because super.validate() calls initDomain,
     * and since we don't have a domain name yet, we aren't ready to call that.
     */
    @Override
    protected void validate() throws CommandException, CommandValidationException {
    }

    @Override
    protected int executeCommand() throws CommandException, CommandValidationException {
        try {
            File domainsDirFile = ok(domainDirParam) ? new File(domainDirParam) : DomainDirs.getDefaultDomainsDir();

            DomainConfig domainConfig = new DomainConfig(null, domainsDirFile.getAbsolutePath());
            DomainsManager manager = new PEDomainsManager();
            String[] domainsList = manager.listDomains(domainConfig);
            programOpts.setInteractive(false); // no prompting for passwords
            if (domainsList.length > 0) {
                if (longOpt) {
                    String headings[] = { "DOMAIN", "ADMIN_ENDPOINTS", "RUNNING", "RESTART_REQUIRED" };
                    ColumnFormatter cf = header ? new ColumnFormatter(headings) : new ColumnFormatter();
                    for (String dn : domainsList) {
                        DomainInfo di = getStatus(dn);
                        cf.addRow(new Object[] { dn, di.getAdminEndpoints(), di.status, di.restartRequired });
                    }
                    logger.info(cf.toString());
                } else {
                    for (String dn : domainsList) {
                        logger.info(getStatus(dn).statusMsg);
                    }
                }
            } else {
                logger.fine(strings.get("NoDomainsToList"));
            }
        } catch (Exception e) {
            throw new CommandException(e.getLocalizedMessage(), e);
        }
        return 0;
    }

    private DomainInfo getStatus(String domainName) throws CommandException {
        // We have to change these to get the right domain.xml
        setDomainName(domainName);
        initDomain();
        DomainInfo info = new DomainInfo();
        info.adminAddr = loadAdminAddresses(getDomainXml());
        HostAndPort reachableEndpoint = getReachableAdminAddress(() -> info.adminAddr);
        if (reachableEndpoint == null) {
            info.status = false;
            info.statusMsg = domainName + " not running";
        } else {
            programOpts.setHostAndPort(reachableEndpoint);
            info.status = isThisDAS(getDomainRootDir());
            info.statusMsg = domainName + " running";
            try {
                RemoteCLICommand cmd = new RemoteCLICommand("_get-restart-required", programOpts, env);
                String restartRequired = cmd.executeAndReturnOutput("_get-restart-required");
                info.restartRequired = Boolean.parseBoolean(restartRequired.trim());
                if (info.restartRequired) {
                    info.statusMsg = domainName + " running, restart required to apply configuration changes";
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to check domain status for " + domainName, e);
            }
        }
        return info;
    }


    private static class DomainInfo {
        List<HostAndPort> adminAddr;
        boolean status;
        String statusMsg;
        boolean restartRequired;

        private String getAdminEndpoints() {
            return toHttpList(adminAddr);
        }
    }
}
