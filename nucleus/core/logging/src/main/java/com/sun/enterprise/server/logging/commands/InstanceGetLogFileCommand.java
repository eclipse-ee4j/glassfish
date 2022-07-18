/*
 * Copyright (c) 2022 Contributors to the Eclipse Foundation
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
import com.sun.enterprise.util.LocalStringManagerImpl;

import java.io.File;
import java.net.URI;
import java.util.Properties;

import org.glassfish.api.ActionReport;
import org.glassfish.api.I18n;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.AdminCommandContext;
import org.glassfish.api.admin.CommandLock;
import org.glassfish.api.admin.ExecuteOn;
import org.glassfish.api.admin.Payload;
import org.glassfish.api.admin.RestEndpoint;
import org.glassfish.api.admin.RestEndpoints;
import org.glassfish.api.admin.RuntimeType;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.main.jul.JULHelperFactory;
import org.glassfish.main.jul.JULHelperFactory.JULHelper;
import org.glassfish.main.jul.handler.GlassFishLogHandler;
import org.jvnet.hk2.annotations.Service;

/**
 * @author David Matejcek
 * @author naman
 * Date: 19 Jul, 2010
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

    private static final LocalStringManagerImpl MESSAGES = new LocalStringManagerImpl(InstanceGetLogFileCommand.class);
    private final JULHelper julHelper = JULHelperFactory.getHelper();

    @Override
    public void execute(AdminCommandContext context) {
        try {

            GlassFishLogHandler handler = julHelper.findGlassFishLogHandler();
            if (handler == null) {
                throw new IllegalStateException("GlassFishLogHandler not found, check your logging configuration.");
            }
            File logFile = handler.getConfiguration().getLogFile();

            Payload.Outbound outboundPayload = context.getOutboundPayload();
            Properties props = new Properties();
            props.setProperty("file-xfer-root", ".");

            outboundPayload.attachFile(
                    "application/octet-stream",
                    URI.create(logFile.getName()),
                    "files",
                    props,
                    logFile);
        } catch (Exception e) {
            final String errorMsg = MESSAGES.getLocalString("download.errDownloading",
                "Error while downloading generated files");
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
