/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
 * Copyright (c) 2013, 2018 Oracle and/or its affiliates. All rights reserved.
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

import org.glassfish.api.admin.AdminCommandEventBroker;
import org.glassfish.api.admin.AdminCommandEventBroker.AdminCommandListener;
import org.glassfish.api.admin.CommandInvocation;
import org.glassfish.api.admin.CommandProgress;
import org.glassfish.api.admin.ProgressEvent;
import org.glassfish.jersey.media.sse.EventOutput;

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;
import static org.glassfish.api.admin.AdminCommandState.EVENT_STATE_CHANGED;

/**
 * Provides bridge between CommandInvocation and ReST Response for Server-Sent-Events.
 */
public final class SseAdminCommandInvoker extends AsyncAdminCommandInvoker<SseEventOutput> {
    private static final Logger LOG = System.getLogger(SseAdminCommandInvoker.class.getName());
    private SseEventOutput eventOutput;

    /**
     * @param commandInvocation must not be null
     */
    public SseAdminCommandInvoker(final CommandInvocation<AdminCommandJob> commandInvocation) {
        super(commandInvocation);
    }

    /**
     * Starts the job. If it is the attach command, it then blocks and waits for
     * the detached command to complete.
     *
     * @return {@link EventOutput} to be used for the communication with the client.
     */
    @Override
    public SseEventOutput start() {
        final AdminCommandJob job = getJob();
        LOG.log(TRACE, "Job parameters: {0}, this: {1}", job.getParameters(), this);
        this.eventOutput = new SseEventOutput(job);

        AdminCommandEventBroker broker = job.getEventBroker();
        final ProgressEventListener progressEventListener = new ProgressEventListener();
        broker.registerListener(CommandProgress.EVENT_PROGRESSSTATUS_CHANGE, progressEventListener);
        broker.registerListener(CommandProgress.EVENT_PROGRESSSTATUS_STATE, progressEventListener);
        broker.registerListener(EVENT_STATE_CHANGED, new AdminCommandJobStateListener());
        startJob();

        LOG.log(TRACE, "Writing the current report and leaving. {0}", this);
        this.eventOutput.write();
        return this.eventOutput;
    }

    private class ProgressEventListener implements AdminCommandListener<ProgressEvent>  {

        @Override
        public void onAdminCommandEvent(String eventName, ProgressEvent event) {
            try {
                eventOutput.write(eventName, event);
            } catch (Exception e) {
                // Log the exception but do not stop processing the state change event.
                LOG.log(ERROR, () -> "Failed to process progress change event for: " + event, e);
            }
        }
    }

    private class AdminCommandJobStateListener implements AdminCommandListener<AdminCommandJob>  {

        @Override
        public void onAdminCommandEvent(String eventName, AdminCommandJob job) {
            if (job != getJob()) {
                LOG.log(TRACE, "Ignoring job state change: {0}", job);
                return;
            }
            try {
                eventOutput.write();
                if (!getJob().isJobStillActive()) {
                    eventOutput.close();
                }
            } catch (Exception e) {
                // Log the exception but do not stop processing the state change event.
                LOG.log(ERROR, () -> "Failed to process progress job event for: " + job, e);
            }
        }
    }
}
