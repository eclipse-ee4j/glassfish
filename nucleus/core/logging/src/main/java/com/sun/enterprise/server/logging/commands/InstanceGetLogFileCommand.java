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

package com.sun.enterprise.server.logging.commands;

import com.sun.enterprise.config.serverbeans.Domain;
import com.sun.enterprise.server.logging.GFFileHandler;
import com.sun.enterprise.util.LocalStringManagerImpl;
import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.*;
import jakarta.inject.Inject;

import org.jvnet.hk2.annotations.Service;
import org.glassfish.hk2.api.PerLookup;

import java.io.File;
import java.net.URI;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: naman
 * Date: 19 Jul, 2010
 * Time: 5:02:25 PM
 * To change this template use File | Settings | File Templates.
 */

@ExecuteOn({RuntimeType.INSTANCE})
@Service(name = "_get-log-file")
@PerLookup
@CommandLock(CommandLock.LockType.NONE)
@I18n("get.log.file")
@RestEndpoints({
    @RestEndpoint(configBean=Domain.class,
        opType=RestEndpoint.OpType.GET,
        path="_get-log-file",
        description="_get-log-file")
})
public class InstanceGetLogFileCommand implements AdminCommand {

    final private static LocalStringManagerImpl localStrings = new LocalStringManagerImpl(InstanceGetLogFileCommand.class);

    @Inject
    private ServerEnvironment env;

    @Inject
    GFFileHandler gf;


    @Override
    public void execute(AdminCommandContext context) {
        try {

            File logFile = gf.getCurrentLogFile();

            Payload.Outbound outboundPayload = context.getOutboundPayload();
            Properties props = new Properties();
            props.setProperty("file-xfer-root", ".");

            outboundPayload.attachFile(
                    "application/octet-stream",
                    URI.create(logFile.getName()),
                    "files",
                    props,
                    logFile);
        }
        catch (Exception e) {
            final String errorMsg = localStrings.getLocalString(
                    "download.errDownloading", "Error while downloading generated files");
            ActionReport report = context.getActionReport();
            boolean reportErrorsInTopReport = false;
            if (!reportErrorsInTopReport) {
                report = report.addSubActionsReport();
                report.setActionExitCode(ActionReport.ExitCode.WARNING);
            } else {
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            }
            report.setMessage(errorMsg);
            report.setFailureCause(e);
        }
    }
}
