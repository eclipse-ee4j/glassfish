/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation
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

package com.sun.enterprise.admin.cli;

import java.io.PrintStream;

/**
 * The communication channel between asadmin and its user.
 * <p>
 * Everything printed here is plain text, independent of the logging configuration:
 * no timestamps, levels or other decorations, regardless of the log formatter set
 * by the <code>AS_ADMIN_LOG_FORMATTER</code> environment variable or of the debug and trace
 * settings. The logging system is reserved for diagnostics.
 * <ul>
 * <li>{@link #result(String)} - the output of the command, printed even with <code>--terse</code>.
 * Scripts parse it.
 * <li>{@link #message(boolean, String)} - informational messages for humans, suppressed with
 * <code>--terse</code>.
 * <li>{@link #error(String)} - error messages, printed to the standard error output.
 * </ul>
 * The streams are resolved on every call, so redirections by {@link System#setOut(PrintStream)}
 * and {@link System#setErr(PrintStream)} are respected.
 */
public final class AsadminOutput {

    private AsadminOutput() {
        // utility class
    }

    /**
     * Prints the result of the command to the standard output.
     * The result is printed even when the <code>--terse</code> option is used.
     *
     * @param text the result, ignored if null or empty.
     */
    public static void result(String text) {
        print(System.out, text);
    }

    /**
     * Prints an informational message to the standard output unless the <code>--terse</code> option is used.
     *
     * @param terse value of the <code>--terse</code> option
     * @param text the message, ignored if null or empty.
     */
    public static void message(boolean terse, String text) {
        if (!terse) {
            print(System.out, text);
        }
    }

    /**
     * Prints an error message to the standard error output.
     *
     * @param text the message, ignored if null or empty.
     */
    public static void error(String text) {
        print(System.err, text);
    }

    private static void print(PrintStream stream, String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        if (text.endsWith("\n")) {
            stream.print(text);
        } else {
            stream.println(text);
        }
        stream.flush();
    }
}
