/*
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

package org.glassfish.api.admin;

import java.io.Serializable;
import java.util.UUID;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.glassfish.api.admin.progress.ProgressStatusImpl;

/**
 * Useful services for administrative commands implementation
 *
 * @author Jerome Dochez
 */
public class AdminCommandContextImpl implements AdminCommandContext, Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private ActionReport report;
    transient private final Logger logger;
    transient private Payload.Inbound inboundPayload;
    transient private Payload.Outbound outboundPayload;
    transient private Subject subject; // Subject is Serializable but we want up to date Subject when deserializing
    private ProgressStatus progressStatus; // new ErrorProgressStatus();
    transient private final AdminCommandEventBroker eventBroker;
    private final String jobId;

    public AdminCommandContextImpl(Logger logger, ActionReport report) {
        this(logger, report, null, null, null, null);
    }

    public AdminCommandContextImpl(Logger logger, ActionReport report, final Payload.Inbound inboundPayload, final Payload.Outbound outboundPayload,
            final AdminCommandEventBroker eventBroker, final String jobId) {
        this.logger = logger;
        this.report = report;
        this.inboundPayload = inboundPayload;
        this.outboundPayload = outboundPayload;
        this.eventBroker = eventBroker;
        this.jobId = jobId;
    }

    @Override
    public ActionReport getActionReport() {
        return report;
    }

    @Override
    public void setActionReport(ActionReport newReport) {
        report = newReport;
    }

    @Override
    public Logger getLogger() {
        return logger;
    }

    @Override
    public Payload.Inbound getInboundPayload() {
        return inboundPayload;
    }

    @Override
    public void setInboundPayload(Payload.Inbound newInboundPayload) {
        inboundPayload = newInboundPayload;
    }

    @Override
    public Payload.Outbound getOutboundPayload() {
        return outboundPayload;
    }

    @Override
    public void setOutboundPayload(Payload.Outbound newOutboundPayload) {
        outboundPayload = newOutboundPayload;
    }

    @Override
    public Subject getSubject() {
        return subject;
    }

    @Override
    public void setSubject(Subject subject) {
        this.subject = subject;
    }

    @Override
    public ProgressStatus getProgressStatus() {
        if (progressStatus == null) {
            progressStatus = new ProgressStatusImpl();
        }
        return progressStatus;
    }

    @Override
    public AdminCommandEventBroker getEventBroker() {
        return this.eventBroker;
    }

    @Override
    public String getJobId() {
        return this.jobId;
    }

    static class ErrorProgressStatus implements ProgressStatus, Serializable {

        /**
         *
         */
        private static final long serialVersionUID = 1L;
        private static final String EXC_MESSAGE = "@Progress annotation is not present.";
        private String id = null;

        @Override
        public void setTotalStepCount(int totalStepCount) {
            throw new IllegalStateException(EXC_MESSAGE);
        }

        @Override
        public int getTotalStepCount() {
            return 0;
        }

        @Override
        public int getRemainingStepCount() {
            return 0;
        }

        @Override
        public void progress(int steps, String message) {
            throw new IllegalStateException(EXC_MESSAGE);
        }

        @Override
        public void progress(int steps) {
            throw new IllegalStateException(EXC_MESSAGE);
        }

        @Override
        public void progress(String message) {
            throw new IllegalStateException(EXC_MESSAGE);
        }

        @Override
        public void progress(int steps, String message, boolean spinner) {
            throw new IllegalStateException(EXC_MESSAGE);
        }

        @Override
        public void setCurrentStepCount(int stepCount) {
            throw new IllegalStateException(EXC_MESSAGE);
        }

        @Override
        public void complete(String message) {
        }

        @Override
        public void complete() {
        }

        @Override
        public boolean isComplete() {
            return true;
        }

        @Override
        public ProgressStatus createChild(String name, int allocatedSteps) {
            throw new IllegalStateException(EXC_MESSAGE);
        }

        @Override
        public ProgressStatus createChild(int allocatedSteps) {
            throw new IllegalStateException(EXC_MESSAGE);
        }

        @Override
        public synchronized String getId() {
            if (id == null) {
                id = UUID.randomUUID().toString();
            }
            return id;
        }

    }

}
