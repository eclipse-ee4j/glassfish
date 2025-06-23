/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation
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

import com.sun.enterprise.admin.remote.AdminCommandStateImpl;
import com.sun.enterprise.v3.admin.JobManagerService;
import com.sun.enterprise.v3.admin.RunnableAdminCommandListener;

import java.io.IOException;
import java.util.logging.Level;

import org.glassfish.admin.rest.RestLogging;
import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.AdminCommandState;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.internal.api.Globals;
import org.glassfish.jersey.media.sse.EventOutput;
import org.glassfish.jersey.media.sse.OutboundEvent;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON_TYPE;
import static jakarta.ws.rs.core.MediaType.TEXT_PLAIN_TYPE;
import static org.glassfish.api.admin.AdminCommandState.EVENT_STATE_CHANGED;

/**
 * Provides bridge between CommandInvocation and ReST Response for Server-Sent-Events.
 *
 * @author martinmares
 */
public class SseCommandHelper extends RunnableAdminCommandListener {

    private final ActionReportProcessor processor;
    private final EventOutput eventOuptut;

    private SseCommandHelper(final CommandInvocation commandInvocation, final ActionReportProcessor processor) {
        super(commandInvocation);
        this.processor = processor;
        this.eventOuptut = new EventOutput();
    }

    @Override
    public void processCommandEvent(final String name, Object event) {
        if (eventOuptut.isClosed()) {
            return;
        }
        event = process(name, event);
        final OutboundEvent outEvent = new OutboundEvent.Builder().name(name)
            .mediaType(event instanceof String ? TEXT_PLAIN_TYPE : APPLICATION_JSON_TYPE)
            .data(event.getClass(), event).build();
        try {
            eventOuptut.write(outEvent);
        } catch (Exception ex) {
            RestLogging.restLogger.log(Level.FINE, null, ex);
        }
    }

    @Override
    protected void finalizeRun() {
        try {
            eventOuptut.close();
        } catch (IOException ex) {
            RestLogging.restLogger.log(Level.WARNING, RestLogging.IO_EXCEPTION, ex.getMessage());
        }
    }

    private Object process(final String name, Object event) {
        if (event instanceof Number || event instanceof CharSequence || event instanceof Boolean) {
            return event.toString();
        } else if (processor != null && EVENT_STATE_CHANGED.equals(name)) {
            AdminCommandState acs = (AdminCommandState) event;
            ActionReport report = processor.process(acs.getActionReport(), eventOuptut);
            return new AdminCommandStateImpl(acs.getState(), report, acs.isOutboundPayloadEmpty(), acs.getId());
        }
        return event;
    }

    public static EventOutput invokeAsync(CommandInvocation commandInvocation, ActionReportProcessor processor) {
        if (commandInvocation == null) {
            throw new IllegalArgumentException("commandInvocation");
        }
        SseCommandHelper helper = new SseCommandHelper(commandInvocation, processor);
        JobManagerService jobManagerService = Globals.getDefaultHabitat().getService(JobManagerService.class);
        jobManagerService.startAsyncListener(helper);
        return helper.eventOuptut;
    }

    /**
     * If implementation of this interface is registered then it's process() method is used to convert ActionReport before
     * it is transfered to the client.
     */
    public static interface ActionReportProcessor {

        /**
         * Framework calls this method to process report before it is send to the client. Implementation also can send custom
         * events using provided event channel.
         */
        public ActionReport process(ActionReport report, EventOutput ec);

    }
}
