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
import com.sun.enterprise.v3.common.ActionReporter;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import java.lang.System.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.CommandInvocation;

import static java.lang.System.Logger.Level.TRACE;

/**
 * Starts the job, creates the {@link SseEventOutput} and writes the {@link ActionReport} containing
 * the job id as a message. Then ends.
 */
public class DetachedSseAdminCommandInvoker extends AsyncAdminCommandInvoker<Response> {
    private static final Logger LOG = System.getLogger(DetachedSseAdminCommandInvoker.class.getName());

    private final ActionReporter report;
    private final ResponseBuilder builder;

    public DetachedSseAdminCommandInvoker(ActionReporter idReport, CommandInvocation<AdminCommandJob> invocation, ResponseBuilder builder) {
        super(invocation);
        this.report = idReport;
        this.builder = builder;
    }

    /**
     * Starts the job, creates the {@link SseEventOutput} and writes the {@link ActionReport}
     * containing the job id as a message. Then ends.
     */
    @Override
    public Response start() {
        final AdminCommandJob job = getJob();
        report.setMessage(job.getId());
        report.setActionExitCode(ExitCode.SUCCESS);
        final Response response = createResponse(job);
        LOG.log(TRACE, "Job parameters: {0}, this: {1}", job.getParameters(), this);
        startJob();
        LOG.log(TRACE, "Detached job started, leaving. {0}", this);
        return response;
    }

    private Response createResponse(AdminCommandJob job) {
        try (SseEventOutput eventOutput = new SseEventOutput(job)) {
            LOG.log(TRACE, "Writing the job id. {0}", this);
            eventOutput.write();
            return builder.entity(eventOutput).build();
        }
    }
}
