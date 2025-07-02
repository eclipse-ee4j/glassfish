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

package com.sun.enterprise.v3.admin;

import com.sun.enterprise.v3.common.PropsFileActionReporter;

import java.lang.System.Logger;

import org.glassfish.api.ActionReport;
import org.glassfish.api.ActionReport.ExitCode;
import org.glassfish.api.admin.AdminCommandEventBroker;
import org.glassfish.api.admin.AdminCommandEventBroker.AdminCommandListener;
import org.glassfish.api.admin.AdminCommandEventBroker.BrokerListenerRegEvent;
import org.glassfish.api.admin.AdminCommandState;
import org.glassfish.api.admin.AdminCommandState.State;
import org.glassfish.api.admin.CommandRunner.CommandInvocation;
import org.glassfish.api.admin.ProgressEvent;

import static java.lang.System.Logger.Level.DEBUG;
import static java.lang.System.Logger.Level.ERROR;
import static org.glassfish.api.admin.AdminCommandEventBroker.EVENT_NAME_LISTENER_REG;
import static org.glassfish.api.admin.AdminCommandState.EVENT_STATE_CHANGED;
import static org.glassfish.api.admin.CommandProgress.EVENT_PROGRESSSTATUS_CHANGE;
import static org.glassfish.api.admin.CommandProgress.EVENT_PROGRESSSTATUS_STATE;

/**
 * Asynchronous admin command lister running in a separate thread.
 */
public abstract class AsyncAdminCommandInvoker implements Runnable, AdminCommandListener<Object> {

    private static final Logger LOG = System.getLogger(AsyncAdminCommandInvoker.class.getName());

    private final CommandInvocation commandInvocation;
    private AdminCommandEventBroker<Object> broker;

    /**
     * @param commandInvocation must not be null
     */
    protected AsyncAdminCommandInvoker(CommandInvocation commandInvocation) {
        this.commandInvocation = commandInvocation.listener(".*", this);
    }


    /**
     * @return the broker notifying listeners about changes. Can be null.
     */
    protected final AdminCommandEventBroker<Object> getBroker() {
        return broker;
    }

    @Override
    public final void run() {
        try {
            commandInvocation.execute();
        } catch (Throwable e) {
            LOG.log(ERROR, "Command invocation failed.", e);
            onAdminCommandEvent(EVENT_STATE_CHANGED, new FailedAsyncCommandState(e));
        }
    }

    @Override
    public final synchronized void onAdminCommandEvent(final String name, final Object event) {
        LOG.log(DEBUG, "onAdminCommandEvent(name={0}, event={1})", name, event);
        if (name == null || event == null) {
            return;
        }
        if (EVENT_STATE_CHANGED.equals(name)) {
            final AdminCommandState job = (AdminCommandState) event;
            try {
                onStateChangeEvent(name, job);
            } catch (Exception e) {
                // Log the exception but do not stop processing the state change event.
                LOG.log(ERROR, () -> "Failed to process state change event for: " + job, e);
            }
            if (job.getState().equals(State.COMPLETED) || job.getState().equals(State.REVERTED)) {
                synchronized (job) {
                    LOG.log(DEBUG, "Notifying all attached to {0}", job);
                    job.notifyAll();
                }
            }
            return;
        }
        if (EVENT_PROGRESSSTATUS_STATE.equals(name) || EVENT_PROGRESSSTATUS_CHANGE.equals(name)) {
            try {
                onStateChangeEvent(name, (ProgressEvent) event);
            } catch (Exception e) {
                // Log the exception but do not stop processing the state change event.
                LOG.log(ERROR, () -> "Failed to process progress change event for: " + event, e);
            }
            return;
        }
        if (EVENT_NAME_LISTENER_REG.equals(name)) {
            BrokerListenerRegEvent brokerRegisteredEvent = (BrokerListenerRegEvent) event;
            broker = brokerRegisteredEvent.getBroker();
            return;
        }
    }

    /**
     * Reaction for command invocation state changes.
     *
     * @param eventName
     * @param state the new state
     */
    protected abstract void onStateChangeEvent(final String eventName, AdminCommandState state);

    /**
     * Reaction for command invocation progress changes.
     *
     * @param eventName
     * @param state the new state
     */
    protected abstract void onStateChangeEvent(final String eventName, ProgressEvent state);

    private static final class FailedAsyncCommandState implements AdminCommandState {
        private final ActionReport actionReport;

        FailedAsyncCommandState(Throwable cause) {
            this.actionReport = createActionReport(ExitCode.FAILURE, "Command invocation failed.", cause);
        }

        @Override
        public State getState() {
            return State.COMPLETED;
        }

        @Override
        public ActionReport getActionReport() {
            return actionReport;
        }

        @Override
        public boolean isOutboundPayloadEmpty() {
            return false;
        }

        @Override
        public String getId() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public String toString() {
            return getClass().getSimpleName() + "[state=" + getState() + ", actionReport=" + actionReport + ']';
        }

        private static ActionReport createActionReport(ExitCode exitCode, String message, Throwable cause) {
            ActionReport report = new PropsFileActionReporter();
            report.setActionExitCode(exitCode);
            report.setFailureCause(cause);
            report.setMessage(message);
            return report;
        }
    }
}
