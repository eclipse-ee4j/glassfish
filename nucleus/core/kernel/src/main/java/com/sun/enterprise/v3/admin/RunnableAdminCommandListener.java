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

import com.sun.enterprise.admin.remote.AdminCommandStateImpl;
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

import static java.lang.System.Logger.Level.ERROR;
import static java.lang.System.Logger.Level.TRACE;
import static org.glassfish.api.admin.AdminCommandEventBroker.BrokerListenerRegEvent.EVENT_NAME_LISTENER_REG;
import static org.glassfish.api.admin.AdminCommandState.EVENT_STATE_CHANGED;

/**
 * Asynchronous admin command lister running in a separate thread.
 */
public abstract class RunnableAdminCommandListener implements Runnable, AdminCommandListener<Object> {

    private static final Logger LOG = System.getLogger(RunnableAdminCommandListener.class.getName());

    private final CommandInvocation commandInvocation;
    private AdminCommandEventBroker<Object> broker;

    protected RunnableAdminCommandListener(CommandInvocation commandInvocation) {
        this.commandInvocation = commandInvocation;
        commandInvocation.listener(".*", this);
    }


    /**
     * Process command events additional to events processed by this class
     * using {@link #onAdminCommandEvent(String, Object)}.
     *
     * @param name Name of the event
     * @param event Event object
     */
    protected abstract void processCommandEvent(final String name, Object event);

    /**
     * When the thread finishes, executes this method to finalize the run.
     * It is suitable for closing resources or performing any cleanup.
     * It should not throw any exceptions, as it is called in the separate thread.
     */
    protected abstract void finalizeRun();

    @Override
    public final void run() {
        try {
            commandInvocation.execute();
        } catch (Throwable e) {
            LOG.log(ERROR, "Command invocation failed - " + e.getMessage(), e);
            ActionReport report = createActionReport(ExitCode.FAILURE, "Command invocation failed.", e);
            AdminCommandState acs = new AdminCommandStateImpl(State.COMPLETED, report, true, "unknown");
            onAdminCommandEvent(EVENT_STATE_CHANGED, acs);
        } finally {
            LOG.log(TRACE, "Executing Command invocation finalization.");
            if (broker != null) {
                broker.unregisterListener(this);
            }
            finalizeRun();
            synchronized(this) {
                notifyAll();
            }
        }
    }

    @Override
    public final void onAdminCommandEvent(final String name, Object event) {
        if (name == null || event == null) {
            return;
        }
        if (EVENT_NAME_LISTENER_REG.equals(name)) {
            BrokerListenerRegEvent blre = (BrokerListenerRegEvent) event;
            broker = blre.getBroker();
            return;
        }
        if (name.startsWith(AdminCommandEventBroker.LOCAL_EVENT_PREFIX)) {
            // Prevent events from client to be send back to client
            return;
        }
        processCommandEvent(name, event);
    }

    /**
     * Blocks until {@link #run()} method has finishes executing.
     */
    protected synchronized void awaitFinish() {
        try {
            wait();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ActionReport createActionReport(ExitCode exitCode, String message, Throwable cause) {
        ActionReport report = new PropsFileActionReporter();
        report.setActionExitCode(exitCode);
        report.setFailureCause(cause);
        report.setMessage(message);
        return report;
    }
}
