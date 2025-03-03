/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 * Copyright (c) 1997, 2018 Oracle and/or its affiliates. All rights reserved.
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

package org.glassfish.embeddable.client;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.text.MessageFormat;

/**
 * Represents any user error, such as an invalid combination of command options,
 * or specifying a non-existent JAR file.
 * <p>
 * Such errors should be user-correctable, provided the message is clear.
 * So no stack traces should be displayed when UserErrors are thrown.
 *
 * @author tjquinn
 */
public class UserError extends Throwable {

    private static final long serialVersionUID = 4171944609382558583L;

    /** Allows user to turn on stack traces for user errors - normally off */
    private static final String SHOW_STACK_TRACES_PROPERTY_NAME = UserError.class.getPackage().getName()
        + ".showUserErrorStackTraces";

    /**
     * Creates a new UserError instance having formatted the message with the
     * arguments provided.
     *
     * @param message the message string, presumably containing argument placeholders
     * @param args 0 or more arguments for substitution for the placeholders in the message string
     * @return new UserError with message formatted as requested
     */
    public static UserError formatUserError(String message, String... args) {
        String formattedMessage = MessageFormat.format(message, (Object[]) args);
        UserError ue = new UserError(formattedMessage);
        return ue;
    }

    /** xmlMessage implementation showed the usage message after the error */
    private String usage;

    public UserError(String message) {
        super(message);
    }

    public UserError(String message, Throwable cause) {
        super(message, cause);
    }

    public UserError(Throwable cause) {
        super(cause);
    }


    /**
     * Sets whether or not the usage message should be displayed after the
     * error message is displayed to the user.
     *
     * @param usage the new setting
     */
    public void setUsage(String usage) {
        this.usage = usage;
    }


    /**
     * Displays the user error message, and any messages along the exception
     * chain, if any, and then exits. If showUsage has been set to true, then
     * the usage message is displayed before exiting.
     * <p>
     * Only the messages, and not the stack traces, are shown because these are
     * user errors that should be user-correctable. Stack traces are too
     * alarming and of minimal use to the user as he or she tries to understand
     * and fix the error.
     */
    public void displayAndExit() {
        display(System.err);
        System.exit(1);
    }

    private void display(final PrintStream ps) {
        for (Throwable t = this; t != null; t = t.getCause()) {
            ps.println(t.toString());
        }
        if (usage != null) {
            ps.println(usage);
        }
        if (Boolean.getBoolean(SHOW_STACK_TRACES_PROPERTY_NAME)) {
            printStackTrace(ps);
        }
    }

    public String messageForGUIDisplay() {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(os);
        display(ps);
        return os.toString();
    }

}
