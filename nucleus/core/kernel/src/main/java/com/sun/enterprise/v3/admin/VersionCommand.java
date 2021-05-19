/*
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

import com.sun.appserv.server.util.Version;
import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.util.LocalStringManagerImpl;
import java.util.Properties;
import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.I18n;
import org.glassfish.api.Param;
import org.glassfish.api.admin.*;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Return the version and build number
 *
 * @author Jerome Dochez
 */
@Service(name="version")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("version.command")
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.GET,
        path="version",
        description="version",
        useForAuthorization=true)
})
public class VersionCommand implements AdminCommand {

    @Param(optional=true, defaultValue="false", shortName = "v")
    Boolean verbose;

    final private static LocalStringManagerImpl strings = new LocalStringManagerImpl(VersionCommand.class);

    @Override
    public void execute(AdminCommandContext context) {
        String vers;
        if (verbose) {
            vers = strings.getLocalString("version.verbose",
                "{0}, JRE version {1}",
                Version.getFullVersion(), System.getProperty("java.version"));
        } else {
            vers = strings.getLocalString("version",
                "{0}", Version.getFullVersion());
        }
        ActionReport report = context.getActionReport();
        Properties ep = new Properties();
        ep.setProperty("version", Version.getVersion());
        ep.setProperty("full-version", Version.getFullVersion());
        ep.setProperty("version-number", Version.getVersionNumber());
        report.setExtraProperties(ep);
        report.setActionExitCode(ExitCode.SUCCESS);
        report.setMessage(vers);
    }
}
