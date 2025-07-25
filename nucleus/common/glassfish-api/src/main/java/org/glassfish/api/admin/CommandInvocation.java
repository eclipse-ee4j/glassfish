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

package org.glassfish.api.admin;

import org.glassfish.api.ActionReport;

/**
 * CommandInvocation defines a command excecution context like the requested name of the command to
 * execute, the parameters of the command, etc...
 *
 * @param <T> Concrete {@link Job} type managed by this invocation.
 */
public interface CommandInvocation<T extends Job> {

    /** @return the command name. For the life of this invocation it never changes. */
    String getCommandName();

    /** @return {@link ParameterMap}, never null, but may be empty */
    ParameterMap parameters();

    /**
     * @return {@link CommandParameters}, usually null, it can be used as an alternative for
     *         {@link #parameters()}
     */
    CommandParameters typedParams();

    /**
     * @return true to run the command asynchronously. User then can use the <code>attach</code>
     *         command to access results.
     */
    boolean isDetached();

    /**
     * Sets the command parameters as a typed inteface
     *
     * @param params the parameters
     * @return itself
     */
    CommandInvocation<T> parameters(CommandParameters params);

    /**
     * Sets the command parameters as a ParameterMap.
     *
     * @param params the parameters
     * @return itself
     */
    CommandInvocation<T> parameters(ParameterMap params);

    /**
     * Sets the data carried with the request (could be an attachment)
     *
     * @param inbound inbound data
     * @return itself
     */
    CommandInvocation<T> inbound(Payload.Inbound inbound);

    /**
     * Sets the data carried with the response
     *
     * @param outbound outbound data
     * @return itself
     */
    CommandInvocation<T> outbound(Payload.Outbound outbound);

    /**
     * Register {@link ProgressStatus} listener. Usable for command from command execution.
     *
     * @param ps
     * @return this
     */
    CommandInvocation<T> progressStatus(ProgressStatus ps);

    Payload.Inbound inboundPayload();

    Payload.Outbound outboundPayload();

    ProgressStatus progressStatus();

    /**
     * @return Current report. After command execution report can be changed by command
     */
    ActionReport report();

    /**
     * @return {@link AdminCommand} evaluated using its name etc.
     */
    AdminCommand evaluateCommand();

    /**
     * Creates a job to be executed/
     *
     * @param command
     * @return new job to be executed.
     */
    T createJob(AdminCommand command);

    /**
     * Schedules the invocation as an asynchronous job.
     */
    void start(AdminCommand command, T job);

    /**
     * Executes the command and populate the report with the command execution result.
     * Parameters must have been set before invoking this method.
     * <p>
     * Command is evaluated using HK2 and command name.
     *
     * @return {@link Job} or null for non existing command.
     */
    T execute();

    /**
     * Executes the passed command with this context and populates the report with the execution
     * result. Parameters must be set before invoking this command.
     *
     * @param command command implementation to execute. Cannot be null.
     * @return {@link Job} or null for non existing command.
     */
    T execute(AdminCommand command);


    /**
     * Executes the passed command with this context and populates the report with the execution
     * result. Parameters must be set before invoking this command.
     *
     * @param command command implementation to execute. Cannot be null.
     * @param job {@link Job}, cannot be null.
     */
    void execute(AdminCommand command, T job);
}
