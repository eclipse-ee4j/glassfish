/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

package org.glassfish.admin.rest.utils;

import com.sun.enterprise.v3.admin.AdminCommandJob;
import com.sun.enterprise.v3.admin.AsyncAdminCommandInvoker;

import java.lang.System.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.CommandInvocation;

import static java.lang.System.Logger.Level.TRACE;

/**
 * Starts the job, creates the {@link SseEventOutput} and writes the {@link ActionReport} containing
 * the job id as a message. Then ends.
 */
public class DetachedSseAdminCommandInvoker extends AsyncAdminCommandInvoker<SseEventOutput> {
    private static final Logger LOG = System.getLogger(DetachedSseAdminCommandInvoker.class.getName());

    public DetachedSseAdminCommandInvoker(CommandInvocation<AdminCommandJob> commandInvocation) {
        super(commandInvocation);
    }


    /**
     * Starts the job, creates the {@link SseEventOutput} and writes the {@link ActionReport}
     * containing
     * the job id as a message. Then ends.
     */
    @Override
    public SseEventOutput start() {
        final AdminCommandJob job = getJob();
        LOG.log(TRACE, "Job parameters: {0}, this: {1}", job.getParameters(), this);
        final SseEventOutput eventOutput = new SseEventOutput(job);
        LOG.log(TRACE, "Writing the job id. {0}", this);
        final ActionReport report = job.getActionReport();
        report.setMessage(job.getId());
        report.setActionExitCode(ExitCode.SUCCESS);
        // No other writes to the report or event output until we close.
        report.lock();
        eventOutput.write();
        startJob();
        LOG.log(TRACE, "Detached job started, leaving. {0}", this);
        return eventOutput;
    }
}
