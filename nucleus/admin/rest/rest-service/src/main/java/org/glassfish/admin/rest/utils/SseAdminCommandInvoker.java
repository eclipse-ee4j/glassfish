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

import com.sun.enterprise.v3.admin.AsyncAdminCommandInvoker;
import com.sun.enterprise.v3.admin.JobManagerService;

import java.io.IOException;
import java.lang.System.Logger;

import org.glassfish.api.admin.AdminCommandState;
import org.glassfish.api.admin.AdminCommandState.State;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.api.admin.ProgressEvent;
import org.glassfish.internal.api.Globals;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;

/**
 * Provides bridge between CommandInvocation and ReST Response for Server-Sent-Events.
 *
 * @author martinmares
 */
public final class SseAdminCommandInvoker extends AsyncAdminCommandInvoker {
    private static final Logger LOG = System.getLogger(SseAdminCommandInvoker.class.getName());

    private final SseCommandHelperEventOutput eventOuptut;

    /**
     * @param commandInvocation must not be null
     */
    public SseAdminCommandInvoker(final CommandInvocation commandInvocation) {
        super(commandInvocation);
        this.eventOuptut = new SseCommandHelperEventOutput();
    }

    @Override
    protected void onStateChangeEvent(final String eventName, final AdminCommandState state) {
        if (eventOuptut.isClosed()) {
            return;
        }
        eventOuptut.write(eventName, state);
        if (state.getState() == State.COMPLETED || state.getState() == State.REVERTED || eventOuptut.isClosed()) {
            close();
        }
    }

    @Override
    protected void onStateChangeEvent(String eventName, ProgressEvent state) {
        if (eventOuptut.isClosed()) {
            return;
        }
        eventOuptut.write(eventName, state);
        if (eventOuptut.isClosed()) {
            close();
        }
    }


    /**
     * Closes the event output and unregisters the listener.
     * The client can close at any point, so when we have no chance to send him anything,
     * we go away too.
     * Note that first we have to try to write to learn that.
     */
    private void close() {
        getBroker().unregisterListener(this);
        eventOuptut.close();
    }

    /**
     * Starts the job and the listener.
     * The method is synchronized to ensure that the job doesn't write to {@link EventOutput}
     * before the job id is written first.
     *
     * @return {@link EventOutput} to be used for the communication with the client.
     */
    public synchronized EventOutput start() {
        Globals.getDefaultHabitat().getService(JobManagerService.class).start(this);
        return eventOuptut;
    }


    private static final class SseCommandHelperEventOutput extends EventOutput {

        void write(final String eventName, final Object event) {
            LOG.log(DEBUG, "write(eventName={0}, event={1})", eventName, event);
            if (isClosed()) {
                // The client can close the connection at any time
                LOG.log(TRACE, "EventOutput is closed, not writing event.");
                return;
            }
            Object evaluated = evaluate(event);
            final OutboundEvent outEvent = new OutboundEvent.Builder()
                .name(eventName)
                .mediaType(evaluated instanceof String ? TEXT_PLAIN_TYPE : APPLICATION_JSON_TYPE)
                .data(evaluated.getClass(), event)
                .build();
            try {
                super.write(outEvent);
            } catch (IOException e) {
                LOG.log(DEBUG, "Failed to write event, perhaps client already closed the connection?", e);
            }
        }

        @Override
        public void write(OutboundEvent event) throws IOException {
            throw new UnsupportedOperationException("Use write(String eventName, Object event) instead.");
        }

        @Override
        public void close() {
            LOG.log(TRACE, "Closing the event output.");
            try {
                super.close();
            } catch (Exception ex) {
                // Client can close the connection at any time,
                // and usually does when he receives the COMPLETED state.
                LOG.log(TRACE, "Failed to close the event output.", ex);
            }
        }

        private Object evaluate(Object event) {
            if (event instanceof Number || event instanceof CharSequence || event instanceof Boolean) {
                return event.toString();
            }
            return event;
        }
    }
}
