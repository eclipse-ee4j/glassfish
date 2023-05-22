/*
 * Copyright (c) 2023 Contributors to the Eclipse Foundation
 * Copyright (c) 2006, 2018 Oracle and/or its affiliates. All rights reserved.
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

package com.sun.enterprise.v3.admin;

import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

import com.sun.appserv.server.util.Version;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;

/**
 * Return the version and build number
 *
 * @author Jerome Dochez
 */
@Service(name = "version")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("version.command")
@RestEndpoints({
        @RestEndpoint(configBean = Domain.class, opType = RestEndpoint.OpType.GET, path = "version", description = "version", useForAuthorization = true) })
public class VersionCommand implements AdminCommand {

    private static final LocalStringManagerImpl I18N = new LocalStringManagerImpl(VersionCommand.class);

    @Param(optional = true, defaultValue = "false", shortName = "v")
    Boolean verbose;

    @Override
    public void execute(AdminCommandContext context) {
        String msg;
        if (verbose) {
            msg = I18N.getLocalString("version.verbose", "{0}, JRE version {1}", Version.getProductIdInfo(),
                    System.getProperty("java.version"));
        } else {
            msg = I18N.getLocalString("version", "{0}", Version.getProductIdInfo());
        }
        ActionReport report = context.getActionReport();
        Properties ep = new Properties();
        ep.setProperty("version", Version.getProductId());
        ep.setProperty("full-version", Version.getProductIdInfo());
        ep.setProperty("version-number", Version.getVersionNumber());
        report.setExtraProperties(ep);
        report.setActionExitCode(ExitCode.SUCCESS);
        report.setMessage(msg);
    }
}
