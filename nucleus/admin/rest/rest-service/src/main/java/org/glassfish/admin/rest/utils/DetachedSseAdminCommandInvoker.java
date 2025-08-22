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
import com.sun.enterprise.v3.common.PropsFileActionReporter;

import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;

import java.lang.System.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandState;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.Job;

import static java.lang.System.Logger.Level.TRACE;

/**
 * Starts the job, creates the {@link SseEventOutput} and writes the {@link ActionReport} containing
 * the job id as a message. Then ends.
 */
public class DetachedSseAdminCommandInvoker extends AsyncAdminCommandInvoker<Response> {
    private static final Logger LOG = System.getLogger(DetachedSseAdminCommandInvoker.class.getName());

    private final ResponseBuilder builder;

    public DetachedSseAdminCommandInvoker(CommandInvocation<AdminCommandJob> invocation, ResponseBuilder builder) {
        super(invocation);
        this.builder = builder;
    }

    /**
     * Starts the job, creates the {@link SseEventOutput} and writes the {@link ActionReport}
     * containing the job id as a message. Then ends.
     */
    @Override
    public Response start() {
        final AdminCommandJob job = getJob();
        final Response response = createResponse(job);
        LOG.log(TRACE, "Job parameters: {0}, this: {1}", job.getParameters(), this);
        startJob();
        LOG.log(TRACE, "Detached job started, leaving. {0}", this);
        return response;
    }

    private Response createResponse(AdminCommandJob job) {
        try (SseEventOutput eventOutput = new SseEventOutput(new DetachedAdminCommandState(job))) {
            LOG.log(TRACE, "Writing the job id. {0}", this);
            eventOutput.write();
            return builder.entity(eventOutput).build();
        }
    }

    private static final class DetachedAdminCommandState implements AdminCommandState {

        private final String id;
        private final String name;
        private final State state;

        DetachedAdminCommandState(Job job) {
            this.id = job.getId();
            this.name = job.getName();
            this.state = job.getState();
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public State getState() {
            return state;
        }

        @Override
        public ActionReport getActionReport() {
            ActionReport actionReport = new PropsFileActionReporter();
            actionReport.setActionDescription(name + " command");
            actionReport.setActionExitCode(ActionReport.ExitCode.SUCCESS);
            return actionReport;
        }

        @Override
        public boolean isOutboundPayloadEmpty() {
            return true;
        }
    }
}
