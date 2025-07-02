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

import jakarta.ws.rs.core.MediaType;

import java.io.IOException;
import java.lang.System.Logger;

import org.glassfish.api.admin.Job;
import org.glassfish.api.admin.ProgressEvent;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.TRACE;
import static org.glassfish.api.admin.AdminCommandState.EVENT_STATE_CHANGED;

/**
 * A special {@link EventOutput} for sending Server Side Events related to the {@link Job}.
 */
public class SseEventOutput extends EventOutput {
    private static final Logger LOG = System.getLogger(SseEventOutput.class.getName());

    private final Job job;

    SseEventOutput(Job job) {
        this.job = job;
    }

    void write(String eventName, ProgressEvent event) {
        if (!isClosed()) {
            writeJson(eventName, event);
        }
    }

    void write() {
        if (!isClosed()) {
            writeJson(EVENT_STATE_CHANGED, job);
        }
    }

    private void writeJson(String eventName, Object event) {
        LOG.log(DEBUG, "write(eventName={0}, event={1}", eventName, event);
        final OutboundEvent outEvent = new OutboundEvent.Builder()
            .name(eventName)
            .mediaType(MediaType.APPLICATION_JSON_TYPE)
            .data(event.getClass(), event)
            .build();
        try {
            write(outEvent);
        } catch (IOException e) {
            // This is probably OK, client can close connection.
            LOG.log(DEBUG, "Failed to write event.", e);
        }
    }

    @Override
    public void close() {
        LOG.log(TRACE, "Closing the event output.");
        try {
            super.close();
        } catch (Exception ex) {
            // Client can close the connection at any time,
            // and usually does when he receives the COMPLETED state.
            // The close() call does flush and may fail then.
            LOG.log(TRACE, "Failed to close the event output.", ex);
        } finally {
            // When we close, we abandon the lock so other threads can continue
            job.getActionReport().unlock();
        }
    }
}
