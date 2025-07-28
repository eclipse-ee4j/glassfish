/*
 * Copyright (c) 2025 Contributors to the Eclipse Foundation.
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

import javax.security.auth.Subject;

import org.glassfish.api.ActionReport;
import org.jvnet.hk2.annotations.Contract;

/**
 * CommandRunner is a service that allows you to run administrative commands.
 *
 * @author Jerome Dochez
 */
@Contract
public interface CommandRunner<T extends Job> {

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
     * @return model for this command (list of parameters,etc...), null if command is not found
     */
    CommandModel getModel(String name);

    /**
     * Returns the command model for a command name
     *
     * @param scope the scope (or namespace) for the command
     * @param name command name
     * @return model for this command (list of parameters,etc...), null if command is not found
     */
    CommandModel getModel(String scope, String name);

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
     * @return command registered under commandName or null if not found.
     */
    AdminCommand getCommand(String commandName, ActionReport report);

    /**
     * Obtain and return the command implementation defined by the passed commandName
     *
     * @param scope the scope (or namespace) for the command
     * @param commandName command name as typed by users
     * @param report report used to communicate command status back to the user
     * @return command registered under commandName or null if not found.
     */
    AdminCommand getCommand(String scope, String commandName, ActionReport report);


    /**
     * Obtain a new command invocation object.
     * Calls {@link #getCommandInvocation(String, String, ActionReport, Subject, boolean, boolean)}
     * with null scope, false notify and false detach.
     *
     * @param commandName name of the requested command to invoke
     * @param report where to place the status of the command execution
     * @param subject the Subject under which to execute the command
     * @return a new command invocation for that command name.
     */
    default CommandInvocation<T> getCommandInvocation(String commandName, ActionReport report, Subject subject) {
        return getCommandInvocation(null, commandName, report, subject, false, false);
    }


    /**
     * Obtain a new command invocation object.
     *
     * @param scope the scope (or namespace) for the command
     * @param commandName name of the requested command to invoke
     * @param report where to place the status of the command execution
     * @param subject the Subject under which to execute the command
     * @param notify should notification be enabled
     * @param detach true if the command was executed as detached
     * @return a new command invocation for that command name.
     */
    CommandInvocation<T> getCommandInvocation(String scope, String commandName, ActionReport report, Subject subject,
        boolean notify, boolean detach);

    void doCommand(CommandInvocation<T> ctx, AdminCommand command, final Subject subject, final T job);
}
