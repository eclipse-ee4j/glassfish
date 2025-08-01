/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2012, 2018 Oracle and/or its affiliates. All rights reserved.
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

import com.sun.enterprise.util.LocalStringManagerImpl;

import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommand;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.ParameterMap;
import org.glassfish.kernel.KernelLoggerInfo;
import org.jvnet.hk2.annotations.Service;

/**
 *
 * Allows commands executing in the DAS or an instance to invoke other commands in the same server. This will be most
 * useful from commands that need to change configuration settings by delegating to a sequence of other commands.
 * <p>
 * This is similar to some logic in the AdminAdapter.
 *
 * @author Tim Quinn
 *
 */
@Service
@Singleton
public class InserverCommandRunnerHelper {

    private final static Logger logger = KernelLoggerInfo.getLogger();
    private final static LocalStringManagerImpl adminStrings = new LocalStringManagerImpl(InserverCommandRunnerHelper.class);

    @Inject
    private CommandRunnerImpl commandRunner;

    public ActionReport runCommand(final String command, final ParameterMap parameters, final ActionReport report, final Subject subject) {
        try {
            final AdminCommand adminCommand = commandRunner.getCommand(command, report);
            if (adminCommand == null) {
                // maybe commandRunner already reported the failure?
                if (report.getActionExitCode() == ActionReport.ExitCode.FAILURE) {
                    return report;
                }
                String message = adminStrings.getLocalString("adapter.command.notfound", "Command {0} not found", command);
                // cound't find command, not a big deal
                logger.log(Level.FINE, message);
                report.setMessage(message);
                report.setActionExitCode(ActionReport.ExitCode.FAILURE);
                return report;
            }
            CommandInvocation<AdminCommandJob> inv = commandRunner.getCommandInvocation(command, report, subject);
            inv.parameters(parameters).execute();
        } catch (Throwable t) {
            /*
             * Must put the error information into the report for the client to see it.
             */
            report.setActionExitCode(ActionReport.ExitCode.FAILURE);
            report.setFailureCause(t);
            report.setMessage(t.getLocalizedMessage());
            report.setActionDescription("Last-chance exception handler");
        }
        return report;
    }
}
