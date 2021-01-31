/*
 * Copyright (c) 2008, 2018 Oracle and/or its affiliates. All rights reserved.
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

import java.io.BufferedReader;
import java.util.logging.Logger;

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Contract;

/**
 * CommandRunner is a service that allows you to run administrative commands.
 *
 * @author Jerome Dochez
 */
@Contract
public interface CommandRunner {

    /**
     * Returns an initialized ActionReport instance for the passed type or null if it cannot be found.
     *
     * @param name actiopn report type name
     * @return uninitialized action report or null
     */
    ActionReport getActionReport(String name);

    /**
     * Returns the command model for a command name for the null scope
     *
     * @param name command name
     * @param logger logger to log any error messages
     * @return model for this command (list of parameters,etc...), null if command is not found
     */
    CommandModel getModel(String name, Logger logger);

    /**
     * Returns the command model for a command name
     *
     * @param scope the scope (or namespace) for the command
     * @param name command name
     * @param logger logger to log any error messages
     * @return model for this command (list of parameters,etc...), null if command is not found
     */
    CommandModel getModel(String scope, String name, Logger logger);

    /**
     * Returns manpage for the command.
     *
     * @param model of command
     * @return Formated manpage
     */
    BufferedReader getHelp(CommandModel model);

    /**
     * Checks if given command model eTag is equal to current command model eTag
     *
     * @param command Command to be checked
     * @param eTag ETag to validate
     */
    boolean validateCommandModelETag(AdminCommand command, String eTag);

    /**
     * Checks if given command model eTag is equal to current command model eTag
     *
     * @param model of command to be checked
     * @param eTag ETag to validate
     */
    boolean validateCommandModelETag(CommandModel model, String eTag);

    /**
     * Obtain and return the command implementation defined by the passed commandName for the null scope
     *
     * @param commandName command name as typed by users
     * @param report report used to communicate command status back to the user
     * @param logger logger to log
     * @return command registered under commandName or null if not found.
     */
    AdminCommand getCommand(String commandName, ActionReport report, Logger logger);

    /**
     * Obtain and return the command implementation defined by the passed commandName
     *
     * @param scope the scope (or namespace) for the command
     * @param commandName command name as typed by users
     * @param report report used to communicate command status back to the user
     * @param logger logger to log
     * @return command registered under commandName or null if not found.
     */
    AdminCommand getCommand(String scope, String commandName, ActionReport report, Logger logger);

    /**
     * Obtain a new command invocation object for the null scope. Command invocations can be configured and used to trigger
     * a command execution.
     *
     * @param name name of the requested command to invoke
     * @param report where to place the status of the command execution
     * @param subject the Subject under which to execute the command
     * @return a new command invocation for that command name.
     */
    CommandInvocation getCommandInvocation(String name, ActionReport report, Subject subject);

    /**
     * Obtain a new command invocation object. Command invocations can be configured and used to trigger a command
     * execution.
     *
     * @param scope the scope (or namespace) for the command
     * @param name name of the requested command to invoke
     * @param report where to place the status of the command execution
     * @param subject the Subject under which to execute the command
     * @return a new command invocation for that command name.
     */
    CommandInvocation getCommandInvocation(String scope, String name, ActionReport report, Subject subject);

    /**
     * Obtain a new command invocation object for the null scope. Command invocations can be configured and used to trigger
     * a command execution.
     *
     * @param name name of the requested command to invoke
     * @param report where to place the status of the command execution
     * @param subject the Subject under which to execute the command
     * @param isNotify should notification be enabled
     * @return a new command invocation for that command name.
     */
    CommandInvocation getCommandInvocation(String name, ActionReport report, Subject subject, boolean isNotify);

    /**
     * Obtain a new command invocation object. Command invocations can be configured and used to trigger a command
     * execution.
     *
     * @param scope the scope (or namespace) for the command
     * @param name name of the requested command to invoke
     * @param report where to place the status of the command execution
     * @param subject the Subject under which to execute the command
     * @param isNotify should notification be enabled
     * @return a new command invocation for that command name.
     */
    CommandInvocation getCommandInvocation(String scope, String name, ActionReport report, Subject subject, boolean isNotify);

    /**
     * CommandInvocation defines a command excecution context like the requested name of the command to execute, the
     * parameters of the command, etc...
     *
     */
    public interface CommandInvocation {

        /**
         * Sets the command parameters as a typed inteface
         *
         * @param params the parameters
         * @return itself
         */
        CommandInvocation parameters(CommandParameters params);

        /**
         * Sets the command parameters as a ParameterMap.
         *
         * @param params the parameters
         * @return itself
         */
        CommandInvocation parameters(ParameterMap params);

        /**
         * Sets the data carried with the request (could be an attachment)
         *
         * @param inbound inbound data
         * @return itself
         */
        CommandInvocation inbound(Payload.Inbound inbound);

        /**
         * Sets the data carried with the response
         *
         * @param outbound outbound data
         * @return itself
         */
        CommandInvocation outbound(Payload.Outbound outbound);

        /**
         * Register new event listener.
         *
         * @param nameRegexp
         * @param listener
         * @return itself
         */
        CommandInvocation listener(String nameRegexp, AdminCommandEventBroker.AdminCommandListener listener);

        /**
         * Register child of ProgressStatus. Usable for command from command execution.
         *
         * @param ps
         * @return
         */
        CommandInvocation progressStatusChild(ProgressStatus ps);

        /**
         * Set the AdminCommand to be a managed job
         */
        CommandInvocation managedJob();

        /**
         * Current report. After command execution report can be changed by command
         */
        ActionReport report();

        /**
         * Executes the command and populate the report with the command execution result. Parameters must have been set before
         * invoking this method.
         */
        void execute();

        /**
         * Executes the passed command with this context and populates the report with the execution result. Parameters must be
         * set before invoking this command.
         *
         * @param command command implementation to execute
         */
        void execute(AdminCommand command);

    }
}
